package se.skltp.ei.intsvc.notify;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import se.rivta.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontractresponder.v2.FilterType;
import se.skltp.ei.intsvc.subscriber.api.Subscriber;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.EngagementType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;


public class ProcessNotificationFilter {
	
	/**
	 * This map used for storing all filters. The keys is the logicaladdress that maps to a list
	 * of FilterTypes.
	 */
	private static Map<String,List<FilterType>> filters = new HashMap<String,List<FilterType>>();

	
	/**
	 * Filters out only wanted engagements from the ProcessNotificationType and returns them.
	 * 
	 * @param pn
	 * @param logicalAddress logicalAddress that will be used for identify the correct set of filer rules.
	 * 
	 * @return
	 */
	public static ProcessNotificationType filter(ProcessNotificationType pn, String logicalAddress) {
		
		// Filters for this user
		List<FilterType> filterList = filters.get(logicalAddress);
		
		// Loop over all engagements in the transaction and remove the unwanted ones
		Iterator<EngagementTransactionType> iterator = pn.getEngagementTransaction().iterator();
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
		
		return pn;
	}
	

	
	/**
	 * Used for setting all filters that should be used when unwanted processnotifications should
	 * be filtered away.
	 * 
	 * The data should be a list of Subscribers that should contains at least a logicaladdress 
	 * and zero or more filters per subscriber
	 * 
	 * @param subscribersList list of subscribers
	 */
	public static void setFilters(List<Subscriber> subscribersList) {
		Map<String,List<FilterType>> map = new HashMap<String,List<FilterType>>();
		
		for(Subscriber s : subscribersList) {
			map.put(s.getLogicalAdress(), s.getFilterList());
		}
		
		filters = map;
	}
	
}
