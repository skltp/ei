/**
 * Copyright (c) 2013 Sveriges Kommuner och Landsting (SKL). <http://www.skl.se/>
 *
 * This file is part of SKLTP.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package se.skltp.ei.intsvc.update.collect;

import java.util.List;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads a number of messages (determined by a
 * <code>MessageCollectionStrategy</code>) from a JMS queue into a
 * <code>MessageCollectionStrategy</code> and then transmits the collected
 * messages to an JMS output queue.
 * 
 * @author hakan
 */
public class JmsMessageCollectionController implements Runnable {
	private static final Logger log = LoggerFactory
			.getLogger(JmsMessageCollectionController.class);

	// TODO: log statistics: for every output message:
	// - number of processed input-messages
	// - number of processed input-records
	// - number of duplicates found
	// - number of resulting messages
	// - number of resulting output-records
	// - corrId/sequence number if multiple output records

	private String jmsInputQueue = "skltp.ei.collect";
	private String jmsOutputQueue = "skltp.ei.process";
	private String jmsErrorQueue = "DLQ.skltp.ei.collect";
	private long jmsReceiveTimeoutMillis = 30000;
	private MessageCollectionStrategy messageCollectionStrategy;
	private QueueConnectionFactory qcf;
	private QueueConnection conn;

	public void setJmsInputQueue(String jmsInputQueue) {
		this.jmsInputQueue = jmsInputQueue;
	}

	public void setJmsOutputQueue(String jmsOutputQueue) {
		this.jmsOutputQueue = jmsOutputQueue;
	}

	public void setJmsErrorQueue(String jmsErrorQueue) {
		this.jmsErrorQueue = jmsErrorQueue;
	}

	public void setJmsReceiveTimeoutMillis(long jmsReceiveTimeoutMillis) {
		this.jmsReceiveTimeoutMillis = jmsReceiveTimeoutMillis;
	}

	public void setMessageCollectionStrategy(
			MessageCollectionStrategy messageCollectionStrategy) {
		this.messageCollectionStrategy = messageCollectionStrategy;
	}

	public void setQueueConnectionFactory(QueueConnectionFactory qcf) {
		this.qcf = qcf;
	}

	public void init() {
		startMessageCollectionThread();
	}

	private void startMessageCollectionThread() {
		try {
			conn = qcf.createQueueConnection();
			log.debug("got a JMS connection");
			conn.start();
			Thread t = new Thread(this, getClass().getName());
			t.setDaemon(true);
			t.start();
			log.info(
					"started JMS message listener on queue: {}, receiveTimeout: {}, output queue: {}, error queue: {}",
					new Object[] { jmsInputQueue, jmsReceiveTimeoutMillis,
							jmsOutputQueue, jmsErrorQueue });
		} catch (JMSException e) {
			String errMsg = "failed to get connection to JMS broker";
			log.error(errMsg, e);
			// signal error, app should not start without a QCF connection
			throw new RuntimeException(errMsg, e);
		}
	}

	@Override
	public void run() {
		runCollectMessagesLoop();
	}

	private void runCollectMessagesLoop() {
		while (true) {
			long minimumLoopTimeForFastErrorLoopProtection = jmsReceiveTimeoutMillis;
			long startLoopTime = System.currentTimeMillis();
			Session session = null;
			try {
				// start JMS transaction
				boolean isTransacted = true;
				session = conn.createSession(isTransacted,
						Session.SESSION_TRANSACTED);
				MessageConsumer msgConsumer = session.createConsumer(session
						.createQueue(jmsInputQueue));

				// begin reading JMS messages
				boolean commitTransactionDueToErrors = false;
				while (!commitTransactionDueToErrors
						&& !messageCollectionStrategy
								.isCollectedMessagesReadyToBeTransmitted()) {
					// Note: listen with a timeout since we need to periodically
					// ask the collection strategy if we should transmit the
					// buffer and start a new collection
					Message msg = msgConsumer.receive(jmsReceiveTimeoutMillis);

					if (msg == null) {
						log.debug("JMS recieve timed out after [ms]: {}",
								jmsReceiveTimeoutMillis);
					} else if (msg instanceof TextMessage) {
						log.debug("JMS receive returned a TextMessage");
						String text = ((TextMessage) msg).getText();
						try {
							// catch all exceptions since we are likely to have
							// a poison message if an exception is thrown
							messageCollectionStrategy.collectMessage(text);
						} catch (Exception e) {
							// commit current transaction directly/prematurely
							// to avoid complexity
							// with adding thresholds (count/time) for error
							// messages being held in the transaction
							commitTransactionDueToErrors = true;
							log.error(
									"could not collect message, will send message to error queue",
									e);
							sendMessageToErrorQueue(session, msg);
						}
					} else {
						log.error(
								"JMS receive returned unexpected message type: {}, will send message to error queue",
								msg.getClass().getName());
						commitTransactionDueToErrors = true;
						sendMessageToErrorQueue(session, msg);
					}
				}

				sendMessagesToOutQueue(session,
						messageCollectionStrategy
								.getCollectedMessagesAndClearBuffer());

				session.commit();
				log.debug("commited JMS transaction");

			} catch (JMSException e) {
				log.error("error occured in message receive loop", e);
				if (session != null) {
					try {
						session.rollback();
					} catch (JMSException e1) {
						log.warn("failed to rollback JMS transaction", e);
					}
				}
			} finally {
				if (session != null) {
					try {
						session.close();
					} catch (JMSException e) {

					}
				}
			}
			// protect against fast spinning in case of errors
			waitIfFastSpinning(minimumLoopTimeForFastErrorLoopProtection,
					startLoopTime);
		}
	}

	private void waitIfFastSpinning(
			long minimumLoopTimeForFastErrorLoopProtection, long startLoopTime) {
		if (System.currentTimeMillis() <= startLoopTime
				+ minimumLoopTimeForFastErrorLoopProtection) {
			try {
				log.debug("entered fast spinning loop deplay ...");
				Thread.sleep(minimumLoopTimeForFastErrorLoopProtection);
			} catch (InterruptedException e) {
				// do nothing
			}
		}
	}

	private void sendMessagesToOutQueue(Session session,
			List<CollectedMessage> collMsgs) throws JMSException {
		if (collMsgs.isEmpty()) {
			log.debug("no collected msgs in buffer to transmit");
			return;
		}

		TextMessage outMsg = session.createTextMessage();
		MessageProducer producer = session.createProducer(session
				.createQueue(jmsOutputQueue));
		producer.setDeliveryMode(DeliveryMode.PERSISTENT);
		for (CollectedMessage collMsg : collMsgs) {
			log.debug("sending msg to output queue: {}", jmsOutputQueue);
			outMsg.setText(collMsg.getPayload());
			producer.send(outMsg);
		}
	}

	private void sendMessageToErrorQueue(Session session, Message msg)
			throws JMSException {
		log.debug("sending message to error queue: {}", jmsErrorQueue);
		MessageProducer producer = session.createProducer(session
				.createQueue(jmsErrorQueue));
		producer.setDeliveryMode(DeliveryMode.PERSISTENT);
		producer.send(msg);
	}

}