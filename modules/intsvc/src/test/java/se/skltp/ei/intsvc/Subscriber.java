package se.skltp.ei.intsvc;

import javax.jms.*;

import org.apache.activemq.ActiveMQConnectionFactory;

public class Subscriber {

	/**
	 * @param args
	 * @throws JMSException
	 */
	public static void main(String[] args) throws JMSException {

		String activeMqUrl = "tcp://192.168.19.10:61616"; // ActiveMQConnection.DEFAULT_BROKER_URL;
		String topicName = "skltp.ei.notify";

		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
				activeMqUrl);
		Connection connection = connectionFactory.createConnection();
		connection.setClientID("SomeClientID");
		connection.start();
		Session session = connection.createSession(false,
				Session.AUTO_ACKNOWLEDGE);
		Topic topic = session.createTopic(topicName);

		TopicSubscriber durableSubscriber = session.createDurableSubscriber(
				topic, "Test_Durable_Subscriber");

		// Create a listener to process each received message
		MessageListener listener = new MessageListener() {
			public void onMessage(Message message) {
				try {
					TextMessage textMessage = (TextMessage) message;
					System.out.println("Message received from producer: '"
							+ textMessage.getText() + "', JMSDeliveryMode: "
							+ message.getJMSDeliveryMode());
				} catch (JMSException je) {
					System.out.println(je.getMessage());
				}
			}
		};

		// Add the message listener to the durable subscriber
		durableSubscriber.setMessageListener(listener);
		System.out.println("Waiting for messages...");
	}
}
