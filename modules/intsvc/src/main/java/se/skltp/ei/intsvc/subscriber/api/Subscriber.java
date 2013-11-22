package se.skltp.ei.intsvc.subscriber.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.EngagementType;
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
	
	/**
	 * Remove all engagements by applying filter rules for the current subscriber
	 * @param engagements a list of engagements to be filtered
	 * @return a filtered list of engagements
	 */
	public  List<EngagementTransactionType> filter(List<EngagementTransactionType> engagements) {
		
		List<EngagementTransactionType> workingCopy = new ArrayList<EngagementTransactionType>();
		for(EngagementTransactionType e : engagements) {
			workingCopy.add(e);
		}
		
		Iterator<EngagementTransactionType> iterator = workingCopy.iterator();
		while(iterator.hasNext()) {
			EngagementType engagement = iterator.next().getEngagement();
			
			// There is no need to filter if there is no filter to use
			if (filterList == null || filterList.size() == 0) {
				continue; 
			}
			
			// Unless there is a matching serviceDomain or serviceDomain and categorization the engagement
			// will be removed from the transaction.
			boolean removeEngagement = true;
			for(FilterType filter : filterList) {
				if (engagement.getServiceDomain().equalsIgnoreCase(filter.getServiceDomain())) {

					// We only delete engagements that have a serviceDomain and categorization
					if (filter.getCategorization().size() == 0) {
						removeEngagement = false;
					} else {
						
						for(String categorization : filter.getCategorization()) {
							if (engagement.getCategorization().equalsIgnoreCase(categorization)) {
								removeEngagement = false;
							}
						}
					}
				}
			}
			
			if (removeEngagement) {
				iterator.remove();
			}
		}
		
		return workingCopy;
	}

	
}