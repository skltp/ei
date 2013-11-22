package se.skltp.ei.intsvc.integrationtests.notifyservice;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;


import se.rivta.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontractresponder.v2.FilterType;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import se.skltp.ei.intsvc.integrationtests.notifyservice.util.FilterCreator;
import se.skltp.ei.intsvc.notify.ProcessNotificationFilter;
import se.skltp.ei.intsvc.subscriber.api.Subscriber;
import se.skltp.ei.svc.service.GenServiceTestDataUtil;

public class ProcessNotificationFilterTest {
	
	private ProcessNotificationType processNotificationType;
	private EngagementTransactionType et1;
	private EngagementTransactionType et2;

	@Before
	public void setup() {
		
        processNotificationType = new ProcessNotificationType();
       
        et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
        et1.getEngagement().setCategorization("CATEGORY-A");
        et1.getEngagement().setServiceDomain("SERVICEDOMAIN-A");
		
        et2 = GenServiceTestDataUtil.genEngagementTransaction(121111111L);
        et2.getEngagement().setCategorization("CATEGORY-B");
        et2.getEngagement().setServiceDomain("SERVICEDOMAIN-B");
        
        processNotificationType.getEngagementTransaction().add(et1);
        processNotificationType.getEngagementTransaction().add(et2);
	}
	
	
	/**
	 * No engagements should be removed when no filters are supplied
	 */
	@Test
	public void no_filters() {
		
		// Create a subscriber with no filters
		List<Subscriber> subscriberList = FilterCreator.createOneSubscriber("HSA_ID_A", null);
		ProcessNotificationFilter.setFilters(subscriberList);
		
		ProcessNotificationType pn = ProcessNotificationFilter.filter(processNotificationType, "HSA_ID_A");
		
		assertSame(processNotificationType, pn);

		// Verify that we still got two engagements 
		assertEquals(2, pn.getEngagementTransaction().size());
	}
	
	
	/**
	 * Verifies that the no notifications are filtered away when only serviceDomain is set in the filter.
	 */
	@Test
	public void filter_with_servicedomain() {
		
		List<Subscriber> list = FilterCreator.createOneSubscriber("HSA_ID_A", "SERVICEDOMAIN-A");
		ProcessNotificationFilter.setFilters(list);

		ProcessNotificationType pn = ProcessNotificationFilter.filter(processNotificationType, "HSA_ID_A");
		
		assertSame(1, pn.getEngagementTransaction().size());
		assertSame(et1.getEngagement(), pn.getEngagementTransaction().get(0).getEngagement());
	}
	
	@Test
	public void filter_with_servicedomain_and_categorization() {
		
		// Filter that should not match... 
		FilterType filterType2 = new FilterType();
		filterType2.setServiceDomain("SERVICEDOMAIN-X");
		filterType2.getCategorization().add("CATEGORY-A");
		filterType2.getCategorization().add("CATEGORY-X");
		
		// Create a subscriber with a matching filter with servicedomain and categorization
		List<Subscriber> list = FilterCreator.createOneSubscriber("HSA_ID_A", "SERVICEDOMAIN-A", "CATEGORY-A", "CATEGORY-UNKNOWN");
		
		// Add filter that should not match
		list.get(0).getFilterList().add(filterType2);

		// Set the filters
		ProcessNotificationFilter.setFilters(list);
		
		// Filter
		ProcessNotificationType pn = ProcessNotificationFilter.filter(processNotificationType, "HSA_ID_A");
		
		assertSame(1, pn.getEngagementTransaction().size());
		assertSame(et1.getEngagement(), pn.getEngagementTransaction().get(0).getEngagement());
	}
	
	
	@Test
	public void filter_with_wrong_category() {
		
		// Create a subscriber with nonmatching categorization
		List<Subscriber> list = FilterCreator.createOneSubscriber("HSA_ID_A", "SERVICEDOMAIN-A", "CATEGORY-UNKNOWN");
		ProcessNotificationFilter.setFilters(list);

		
		ProcessNotificationType pn = ProcessNotificationFilter.filter(processNotificationType, "HSA_ID_A");
		assertSame(0, pn.getEngagementTransaction().size());
	}
	
	
	/**
	 * Verify that filtering works when there is more than one subscriber (logialaddress)
	 */
	@Test
	public void filter_with_multiple_subscribers_set() {
		
		// Create two subscriber with matching filters
		List<Subscriber> list = FilterCreator.createOneSubscriber("HSA_ID_A", "SERVICEDOMAIN-A", "CATEGORY-A");
		list.add(FilterCreator.createOneSubscriber("HSA_ID_B", "SERVICEDOMAIN-A", "CATEGORY-A").get(0));
		
		ProcessNotificationFilter.setFilters(list);
		
		// Filter
		ProcessNotificationType pn = ProcessNotificationFilter.filter(processNotificationType, "HSA_ID_A");
		
		assertSame(1, pn.getEngagementTransaction().size());
		assertSame(et1.getEngagement(), pn.getEngagementTransaction().get(0).getEngagement());
	}
	
}
