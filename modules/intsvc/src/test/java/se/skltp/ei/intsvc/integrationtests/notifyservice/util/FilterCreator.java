package se.skltp.ei.intsvc.integrationtests.notifyservice.util;

import java.util.ArrayList;
import java.util.List;

import se.rivta.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontractresponder.v2.FilterType;
import se.skltp.ei.intsvc.subscriber.api.Subscriber;

/**
 * Utility-class for creating filters and subscriber when building unittests
 */
public class FilterCreator {

	/**
	 * Create a list of FilterType with only one FilterType element
	 * 
	 * @param filter
	 * @return 
	 */
	public static List<FilterType> createFilterList(FilterType filter) {
		List<FilterType> filterList = new ArrayList<FilterType>();
		filterList.add(filter);
		
		return filterList;
	}
	
	/**
	 * Creates a list of subscribers with one subscriber
	 * 
	 * @param logicalAddress the logialAddress that the subscriber will use - mandatory
	 * @param serviceDomain servicedomain as filter
	 * @param categorizations zero or more categorizations
	 * @return
	 */
	public static List<Subscriber> createOneSubscriber(String logicalAddress, String serviceDomain, String ...categorizations) {
	
		// List of subscribers
		List<Subscriber> subscribers = new ArrayList<Subscriber>();
		
		// Create a subscriber
		Subscriber s;
		if (serviceDomain != null) {
			s = new Subscriber(logicalAddress, createFilterList(createFilter(serviceDomain, categorizations)));
		} else {
			s = new Subscriber(logicalAddress, new ArrayList<FilterType>());
		}
		
		subscribers.add(s);
		
		return subscribers;
	}
	

	/**
	 * Creates a filter with a serviceDomain and zero or more categories
	 * 
	 * @param serviceDomain
	 * @param categorizations
	 * @return
	 */
	public static FilterType createFilter(String serviceDomain, String ...categorizations) {
		
		FilterType filterType = new FilterType();
		filterType.setServiceDomain(serviceDomain);
		
		if (categorizations != null) {
			for (String categorization : categorizations) {
				filterType.getCategorization().add(categorization);
			}
		}
		
		return filterType;
	}
	
	
	
}
