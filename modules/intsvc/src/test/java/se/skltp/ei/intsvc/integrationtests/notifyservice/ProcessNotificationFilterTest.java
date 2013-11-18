package se.skltp.ei.intsvc.integrationtests.notifyservice;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import riv.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontractresponder._2.FilterType;
import riv.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontractresponder._2.LogicalAddresseeRecordType;
import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import se.skltp.ei.intsvc.notify.ProcessNotificationFilter;
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
		
		LogicalAddresseeRecordType logicalAddresseeRecordType = new LogicalAddresseeRecordType();
		logicalAddresseeRecordType.setLogicalAddress("HSA_ID_A");

		List<LogicalAddresseeRecordType> filters = new ArrayList<LogicalAddresseeRecordType>();
		filters.add(logicalAddresseeRecordType);
		
		ProcessNotificationFilter.setFilters(filters);
		ProcessNotificationType pn = ProcessNotificationFilter.filter(processNotificationType, "HSA_ID_A");
		
		assertSame(processNotificationType, pn);
		assertEquals(processNotificationType.getEngagementTransaction().size(), pn.getEngagementTransaction().size());
	}
	
	
	/**
	 * Verifies that the no notifcation is filtered away when only serviceDomain is set in the filtertype.
	 */
	@Test
	public void filter_with_servicedomain() {
		
		FilterType filterType = new FilterType();
		filterType.setServiceDomain("SERVICEDOMAIN-A");
		
		LogicalAddresseeRecordType logicalAddresseeRecordType = new LogicalAddresseeRecordType();
		logicalAddresseeRecordType.setLogicalAddress("HSA_ID_A");
		logicalAddresseeRecordType.getFilter().add(filterType);

		List<LogicalAddresseeRecordType> filters = new ArrayList<LogicalAddresseeRecordType>();
		filters.add(logicalAddresseeRecordType);
		
		ProcessNotificationFilter.setFilters(filters);
		ProcessNotificationType pn = ProcessNotificationFilter.filter(processNotificationType, "HSA_ID_A");
		
		assertSame(1, pn.getEngagementTransaction().size());
		assertSame(et1.getEngagement(), pn.getEngagementTransaction().get(0).getEngagement());
	}
	
	@Test
	public void filter_with_servicedomain_and_categorization() {
		
		// Filter that should match 
		FilterType filterType = new FilterType();
		filterType.setServiceDomain("SERVICEDOMAIN-A");
		filterType.getCategorization().add("CATEGORY-A");
		filterType.getCategorization().add("UNREALTED-CATEGORY");
		
		// Filter that should not match... 
		FilterType filterType2 = new FilterType();
		filterType2.setServiceDomain("SERVICEDOMAIN-X");
		filterType2.getCategorization().add("CATEGORY-A");
		filterType2.getCategorization().add("CATEGORY-X");
		
		LogicalAddresseeRecordType logicalAddresseeRecordType = new LogicalAddresseeRecordType();
		logicalAddresseeRecordType.setLogicalAddress("HSA_ID_A");
		logicalAddresseeRecordType.getFilter().add(filterType);
		logicalAddresseeRecordType.getFilter().add(filterType2);
		
		List<LogicalAddresseeRecordType> filters = new ArrayList<LogicalAddresseeRecordType>();
		filters.add(logicalAddresseeRecordType);
		
		ProcessNotificationFilter.setFilters(filters);
		ProcessNotificationType pn = ProcessNotificationFilter.filter(processNotificationType, "HSA_ID_A");
		
		assertSame(1, pn.getEngagementTransaction().size());
		assertSame(et1.getEngagement(), pn.getEngagementTransaction().get(0).getEngagement());
	}
	
	@Test
	public void filter_with_wrong_servicedomain() {
		
		FilterType filterType = new FilterType();
		filterType.setServiceDomain("SERVICEDOMAIN-A");
		filterType.getCategorization().add("UNREALTED-CATEGORY");
		
		LogicalAddresseeRecordType logicalAddresseeRecordType = new LogicalAddresseeRecordType();
		logicalAddresseeRecordType.setLogicalAddress("HSA_ID_A");
		logicalAddresseeRecordType.getFilter().add(filterType);
		
		List<LogicalAddresseeRecordType> filters = new ArrayList<LogicalAddresseeRecordType>();
		filters.add(logicalAddresseeRecordType);
		
		ProcessNotificationFilter.setFilters(filters);
		ProcessNotificationType pn = ProcessNotificationFilter.filter(processNotificationType, "HSA_ID_A");
		
		assertSame(0, pn.getEngagementTransaction().size());
	}
	
}
