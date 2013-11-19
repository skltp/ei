package se.skltp.ei.intsvc.subscriber.api;

public class Subscriber {
	
	public static String NOTIFICATION_QUEUE_PREFIX = "EI.NOTIFICATION.";
	
	private String logicalAdress;
	private String queueName;

	public Subscriber(String logicalAdress) {
		this.logicalAdress = logicalAdress;
		queueName = NOTIFICATION_QUEUE_PREFIX + logicalAdress;
	}

	public String getLogicalAdress() {
		return logicalAdress;
	}

	public String getNotificationQueueName() {
		return queueName;
	}	
}