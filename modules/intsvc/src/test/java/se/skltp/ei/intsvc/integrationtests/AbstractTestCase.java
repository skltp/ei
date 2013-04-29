package se.skltp.ei.intsvc.integrationtests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.mule.api.MuleMessage;
import org.mule.api.context.notification.EndpointMessageNotificationListener;
import org.mule.api.context.notification.ServerNotification;
import org.mule.context.notification.EndpointMessageNotification;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;
import org.soitoolkit.commons.mule.test.AbstractJmsTestUtil;
import org.soitoolkit.commons.mule.test.ActiveMqJmsTestUtil;
import org.soitoolkit.commons.mule.test.Dispatcher;
import org.soitoolkit.commons.mule.test.DispatcherMuleClientImpl;
import org.soitoolkit.commons.mule.util.MuleUtil;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;
import org.soitoolkit.commons.mule.util.ValueHolder;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex.updateresponder._1.ObjectFactory;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.svc.service.GenServiceTestDataUtil;

/**
 * Extends the base class in Mule, org.mule.tck.junit4.FuntionalTestCase.
 * 
 * @author Magnus Larsson
 *
 */
public abstract class AbstractTestCase extends org.soitoolkit.commons.mule.test.junit4.AbstractTestCase {
    
	protected static final int EI_TEST_TIMEOUT   = 5000;
	protected static final int EI_SHORT_WAITTIME =  500;

	private static final JaxbUtil jabxUtil = new JaxbUtil(UpdateType.class);
	private static final ObjectFactory of = new ObjectFactory();

    protected static final RecursiveResourceBundle rb = new RecursiveResourceBundle("ei-config");
	protected static final String PROCESS_QUEUE = rb.getString("PROCESS_QUEUE");
	protected static final String INFO_LOG_QUEUE  = rb.getString("SOITOOLKIT_LOG_INFO_QUEUE");
	protected static final String ERROR_LOG_QUEUE = rb.getString("SOITOOLKIT_LOG_ERROR_QUEUE");

	private AbstractJmsTestUtil jmsUtil = null;

	public AbstractTestCase() {
		super();
	}
    
	protected AbstractJmsTestUtil getJmsUtil() {
		
		// TODO: Fix lazy init of JMS connection et al so that we can create jmsutil in the declaration
		// (The embedded ActiveMQ queue manager is not yet started by Mule when jmsutil is declared...)
		if (jmsUtil == null) jmsUtil = new ActiveMqJmsTestUtil();
		
		return jmsUtil;
	}

	protected List<Message> assertQueueDepth(String queueName, int expectedDepth) {
	    List<Message> messages = getJmsUtil().browseMessagesOnQueue(queueName);
		assertEquals(expectedDepth, messages.size());
		return messages;
	}

	protected List<Message> assertQueueContainsMessage(String queueName, String expectedText) {
	    try {
			List<Message> messages = getJmsUtil().browseMessagesOnQueue(queueName);

			System.err.println("MSG CNT: " + messages.size());
			for (Message message : messages) {
				String text = ((TextMessage)message).getText();
				System.err.println("MSG: " + text);
				if (text.contains(expectedText)) return messages;
			}
			
			fail("Faild to find any message on the queue " + queueName + " that contains the text: " + expectedText);
			return messages;
		} catch (JMSException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected List<Message> assertQueueMatchesMessages(String queueName, String... expectedRegexps) {
	    try {
			List<Message> messages = getJmsUtil().browseMessagesOnQueue(queueName);
			System.err.println("MSG CNT: " + messages.size());

			// Go through the messages in the queue checking each expected regexp, one by one
			for (int i = 0; i < expectedRegexps.length; i++) {

				// The regexp to check this time
				String expectedRegexp = expectedRegexps[i];

				// Look for a match
				boolean found = false;
				for (Message message : messages) {
					String text = ((TextMessage)message).getText();
					System.err.println("MSG: " + text);
					if (text.matches(expectedRegexp)) found = true;
				}
				if (!found) {
					// If not found then fail with a proper mesage
					fail("Faild to find any message on the queue " + queueName + " that contains the text: " + expectedRegexp);
				}
			}

			return messages;
		} catch (JMSException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected void assertUpdateRequest(UpdateType expected, MuleMessage actual) {
		try {
			TextMessage actualJms = (TextMessage)actual.getPayload();
			String actualXml = actualJms.getText();
			String expectedXml = jabxUtil.marshal(of.createUpdate(expected));
			assertEquals(expectedXml, actualXml);
		} catch (JMSException e) {
			throw new RuntimeException(e);
		}
	}

	protected UpdateType createUdateRequest(long... residentIds) {

		UpdateType request = new UpdateType();

		for (int i = 0; i < residentIds.length; i++) {
			EngagementTransactionType et = GenServiceTestDataUtil.genEngagementTransaction(residentIds[i]);
			request.getEngagementTransaction().add(et);
		}
		
		return request;
    }
	
	protected void waitForBackgroundProcessing() {
		try {
			Thread.sleep(EI_SHORT_WAITTIME);
		} catch (InterruptedException e) {}
	}

	/**
	 * Waits <code>timeout</code> ms for a <code>MuleMessage</code> to arrive on outboundEndpoint with the name <code>outboundEndpointName</code> and with the action <code>action</code>. 
	 * 
	 * Sample usage: TBS
	 * 
	 * @param serviceComponentName
	 * @param timeout in ms
	 * @return the MuleMessage sent to the named service component
	 */
	protected MuleMessage waitForDelivery(final String outboundEndpointName, final int action, long timeout) {
		return dispatchAndWaitForDelivery(null, outboundEndpointName, action, timeout);
    }

	/**
	 * Sends the <code>payload</code> and <code>headers</code> to the <code>inboundEndpointAddress</code> and waits <code>timeout</code> ms for a <code>MuleMessage</code> to arrive on outboundEndpoint with the name <code>outboundEndpointName</code>. 
	 * 
	 * Sample usage:
	 * <tt>
	 *	public void testTransferKorttransaktioner() throws Exception {
	 *		String expectedPayload = "Yada, yada, yada...";
	 *
	 *		MuleMessage message = dispatchAndWaitForDelivery(
	 *			"sftp://dfcx0346@vfin8003.volvofinans.net/sftp/vfkonto/ut",
	 *			expectedPayload,
	 *			createFileHeader("from_vfkonto.dat"),
	 *			"volvokort-test-endpoint",
	 *			TIMEOUT);
	 *
	 *		String actualPayload = message.getPayloadAsString();
	 *		assertEquals(expectedPayload, actualPayload); 
	 *	}	 
	 * </tt>
	 * 
	 * @param inboundEndpointAddress
	 * @param payload
	 * @param headers
	 * @param outboundEndpointName
	 * @param action as specified by org.mule.context.notification.EndpointMessageNotification: MESSAGE_RECEIVED, MESSAGE_DISPATCHED, MESSAGE_SENT or MESSAGE_REQUESTED
	 * @param timeout in ms
	 * @return the received MuleMEssage on the outboundEndpoint
	 */
	protected MuleMessage dispatchAndWaitForDelivery(String inboundEndpointAddress, Object payload, Map<String, String> headers, final String outboundEndpointName, final int action, long timeout) {
		return dispatchAndWaitForDelivery(new DispatcherMuleClientImpl(muleContext, inboundEndpointAddress, payload, headers), outboundEndpointName, action, timeout);
    }

	
	/**
	 * Use the Dispatcher to send a asynchronous message and waits <code>timeout</code> ms for a <code>MuleMessage</code> to arrive on outboundEndpoint with the name <code>outboundEndpointName</code> and with the action <code>action</code>. 
	 * 
	 * Sample usage: TBS
	 * 
	 * @param inboundEndpointAddress
	 * @param payload
	 * @param headers
	 * @param outboundEndpointName
	 * @param action as specified by org.mule.context.notification.EndpointMessageNotification: MESSAGE_RECEIVED, MESSAGE_DISPATCHED, MESSAGE_SENT or MESSAGE_REQUESTED
	 * @param timeout in ms
	 * @return the received MuleMEssage on the outboundEndpoint
	 */
	@SuppressWarnings("rawtypes")
	protected MuleMessage dispatchAndWaitForDelivery(Dispatcher dispatcher, final String outboundEndpointName, final int action, long timeout) {
		
		// Declare MuleMessage to return
		final ValueHolder<MuleMessage> receivedMessageHolder = new ValueHolder<MuleMessage>();
		
		// Declare countdown latch and listener
		final CountDownLatch latch = new CountDownLatch(1);
		EndpointMessageNotificationListener listener = null;

		try {

			// Next create a listener that listens for dispatch events on the outbound endpoint
			listener = new EndpointMessageNotificationListener() {
				public void onNotification(ServerNotification notification) {
					if (logger.isDebugEnabled()) logger.debug("notification received on " + notification.getResourceIdentifier() + " (action: " + notification.getActionName() + ")");

					// Only care about EndpointMessageNotification
					if (notification instanceof EndpointMessageNotification) {
						EndpointMessageNotification endpointNotification = (EndpointMessageNotification)notification;

						// Extract action and name of the endpoint
						int    actualAction   = endpointNotification.getAction();
						String actualEndpoint = MuleUtil.getEndpointName(endpointNotification);

						// If it is a dispatch event on our outbound endpoint then countdown the latch.
						if (logger.isDebugEnabled()) {
							logger.debug(actualAction == action);
							logger.debug(actualEndpoint.equals(outboundEndpointName));
						}
						if (actualAction == action && actualEndpoint.equals(outboundEndpointName)) {
							if (logger.isDebugEnabled()) logger.debug("Expected notification received on " + actualEndpoint + " (action: " + endpointNotification.getActionName() + "), time to countdown the latch");
							receivedMessageHolder.value = (MuleMessage)endpointNotification.getSource();
							latch.countDown();

						} else {
							if (logger.isDebugEnabled()) logger.debug("A not matching notification received on " + actualEndpoint + " (action: " + endpointNotification.getActionName() + "), continue to wait for the right one...");							
						}
					}
				}
			};

			// Now register the listener
			muleContext.getNotificationManager().addListener(listener);

			// Perform the actual dispatch, if any...
			if (dispatcher != null) {
				dispatcher.doDispatch();
			}
			
			// Wait for the delivery to occur...
			if (logger.isDebugEnabled()) logger.debug("Waiting for message to be delivered to the endpoint...");
			boolean workDone = latch.await(timeout, TimeUnit.MILLISECONDS);
			if (logger.isDebugEnabled()) logger.debug((workDone) ? "Message delivered, continue..." : "No message delivered, timeout occurred!");

			// Raise a fault if the test timed out
			assertTrue("Test timed out. It took more than " + timeout + " milliseconds. If this error occurs the test probably needs a longer time out (on your computer/network)", workDone);

		} catch (Exception e) {
			e.printStackTrace();
			fail("An unexpected error occurred: " + e.getMessage());

		} finally {
			// Always remove the listener if created
			if (listener != null) muleContext.getNotificationManager().removeListener(listener);
		}
		
		return receivedMessageHolder.value;		
    }
    
}