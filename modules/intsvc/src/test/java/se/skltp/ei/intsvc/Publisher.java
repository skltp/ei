package se.skltp.ei.intsvc;

import java.util.Date;

import javax.jms.*;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

public class Publisher {

	/**
	 * @param args
	 * @throws JMSException 
	 */
	public static void main(String[] args) throws JMSException {

		String activeMqUrl = ActiveMQConnection.DEFAULT_BROKER_URL;
		String topicName = "skltp.ei.notify";

		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(activeMqUrl);
		Connection connection = connectionFactory.createConnection();
		connection.start();
		Session session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
		Destination destination = session.createTopic(topicName);
		MessageProducer messageProducer = session.createProducer(destination);
		TextMessage textMessage = session .createTextMessage("Hello Subscriber! " + new Date());
		textMessage.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
		for (int i = 0; i < 3; i++) {
			messageProducer.send(textMessage);
			System.out.println("Message sent to subscriber: '" + textMessage.getText() + "'");
		}
		connection.close();
	}
}
