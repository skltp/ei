package se.skltp.ei.intsvc.subscriber.api;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import se.rivta.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontractresponder.v2.FilterType;

@XmlRootElement
public class Subscriber {
	
	public static String NOTIFICATION_QUEUE_PREFIX = "EI.NOTIFICATION.";
	
	/**
	 * LogicalAddress that identifies this subscriber
	 */
	@XmlElement
	private String logicalAdress;
	
	@XmlElement
	private String queueName;

	/**
	 * List of filters for this subscriber
	 */
	@XmlElement
	private List<FilterType> filterList;
	
	public Subscriber() {}
	
	public Subscriber(String logicalAdress, List<FilterType> filterList) {
		this.logicalAdress = logicalAdress;
		this.filterList = filterList;
		
		queueName = NOTIFICATION_QUEUE_PREFIX + logicalAdress;
	}

	public String getLogicalAdress() {
		return logicalAdress;
	}

	public List<FilterType> getFilterList() {
		return filterList;
	}
	
	public String getNotificationQueueName() {
		return queueName;
	}

	
}