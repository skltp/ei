/**
 * Copyright (c) 2013 Sveriges Kommuner och Landsting (SKL). <http://www.skl.se/>
 *
 * This file is part of SKLTP.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package se.skltp.ei.intsvc.integrationtests.notifyservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;

import org.junit.Before;
import org.junit.Test;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.module.client.MuleClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationResponseType;
import riv.itintegration.engagementindex.updateresponder._1.ObjectFactory;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.rivta.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontractresponder.v2.FilterType;
import se.rivta.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontractresponder.v2.LogicalAddresseeRecordType;
import se.skltp.ei.intsvc.integrationtests.AbstractTestCase;
import se.skltp.ei.intsvc.notify.ProcessNotificationFilter;
import se.skltp.ei.intsvc.subscriber.api.Subscriber;
import se.skltp.ei.intsvc.subscriber.api.SubscriberCache;
import se.skltp.ei.svc.entity.repository.EngagementRepository;

public class NotifyServiceIntegrationTest extends AbstractTestCase {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(NotifyServiceIntegrationTest.class);
	 
	private static final JaxbUtil jabxUtil = new JaxbUtil(UpdateType.class);
	private static final ObjectFactory of = new ObjectFactory();

	@SuppressWarnings("unused")
	private static final String EXPECTED_ERR_TIMEOUT_MSG = "Read timed out";
//	private static final String EXPECTED_ERR_INVALID_ID_MSG = "Invalid Id: " + TEST_RR_ID_FAULT_INVALID_ID;
  
    public NotifyServiceIntegrationTest() {
        // Only start up Mule once to make the tests run faster...
        // Set to false if tests interfere with each other when Mule is started only once.
        setDisposeContextPerClass(true);
    }

    protected String getConfigResources() {
		return 
			"soitoolkit-mule-jms-connector-activemq-embedded.xml," + 
	  		"ei-common.xml," +
	  		"skltp-ei-svc-spring-context.xml," +
	        "get-logical-addressees-service.xml," + 
	        "teststub-services/init-dynamic-flows.xml," +
	        "teststub-services/get-logical-addressees-by-service-contract-teststub-service.xml," +
	        "teststub-services/process-notification-teststub-service.xml";
    }

    private EngagementRepository engagementRepository;

	private SubscriberCache subscriberCache;

    @Before
    public void setUp() throws Exception {

    	// Lookup the entity repository if not already done
    	if (engagementRepository == null) {
    		engagementRepository = muleContext.getRegistry().lookupObject(EngagementRepository.class);
    	}

    	// Clean the storage
    	engagementRepository.deleteAll();

    	// Lookup the subscriber cache if not already done
    	if (subscriberCache == null) {
    		subscriberCache = muleContext.getRegistry().lookupObject(SubscriberCache.class);
    	}
    	
    	// Clear queues used for the tests
		getJmsUtil().clearQueues(INFO_LOG_QUEUE, ERROR_LOG_QUEUE);
    }

	/**
	 * Perform a test that is expected to return one hit
	 * All subscribers should get one message.
	 * 
	 * @throws JMSException 
	 */
    @Test
    public void notify_OK() throws JMSException {
    	
		long residentId = 1212121212L;
		
		doOneTest(createUdateRequest(residentId));

		// Wait a short while for all background processing to complete
		waitForBackgroundProcessing();

		// Expect no error logs and 3 * 3 info log entries
		assertQueueDepth(ERROR_LOG_QUEUE, 0);
		assertQueueDepth(INFO_LOG_QUEUE, 9);
    
    }
    
    /**
     * Verifies that no messages are filtered away when only servicedomain is used in the filter.
     * All subscribers should get on message.
     * 
     * @throws JMSException
     */
    @Test
    public void no_filtering_OK() throws JMSException {
    	
		long residentId = 1212121212L;
		
		UpdateType createUdateRequest = createUdateRequest(residentId);
		createUdateRequest.getEngagementTransaction().get(0).getEngagement().setServiceDomain("SERVICEDOMAIN-A");
		createUdateRequest.getEngagementTransaction().get(0).getEngagement().setCategorization("CATEGORY-A");
		
		// Create filter
		LogicalAddresseeRecordType logicalAddresseeRecordType = createLogicalAddresseeRecordType("HSA_ID_A");
		logicalAddresseeRecordType.getFilter().add(createFilter("SERVICEDOMAIN-A"));

		List<LogicalAddresseeRecordType> filters = new ArrayList<LogicalAddresseeRecordType>();
		filters.add(logicalAddresseeRecordType);
		
		ProcessNotificationFilter.setFilters(filters);
		
		
		doOneTest(createUdateRequest);

		// Wait a short while for all background processing to complete
		waitForBackgroundProcessing();
		
		// Expect no error logs and 3 * 3 info log entries
		assertQueueDepth(ERROR_LOG_QUEUE, 0);
		assertQueueDepth(INFO_LOG_QUEUE, 9);
    }
    
    
    /**
     * Verifies that a filter remove only 1 update for 1 subscriber
     * The two remaining subscribers should get 1 message each.
     * 
     * @throws JMSException
     * @throws MuleException 
     */
    @Test
    public void filter_should_remove_one_message_for_1_subscriber() throws JMSException, MuleException {
    	
		long residentId = 1212121212L;
		
		// Create request and set correct serviceDomain and categorization
		UpdateType createUdateRequest = createUdateRequest(residentId);
		createUdateRequest.getEngagementTransaction().get(0).getEngagement().setServiceDomain("SERVICEDOMAIN-A");
		createUdateRequest.getEngagementTransaction().get(0).getEngagement().setCategorization("CATEGORY-A");
		
		// Create filter
		LogicalAddresseeRecordType logicalAddresseeRecordType = createLogicalAddresseeRecordType("HSA_ID_A");
		logicalAddresseeRecordType.getFilter().add(createFilter("SERVICEDOMAIN-A", "CATEGORY-B"));

		List<LogicalAddresseeRecordType> filters = new ArrayList<LogicalAddresseeRecordType>();
		filters.add(logicalAddresseeRecordType);
		
		// Set filters
		ProcessNotificationFilter.setFilters(filters);

		
		doOneTestWithActiveFilter(createUdateRequest);

		// Wait a short while for all background processing to complete
		waitForBackgroundProcessing();
		
		// Expect no error logs and 7 info log entries
		assertQueueDepth(ERROR_LOG_QUEUE, 0);
		assertQueueDepth(INFO_LOG_QUEUE, 7);
    }
    

    /**
     * Verifies that only subscriber gets a message
     * 
     * @throws JMSException
     * @throws MuleException 
     */
    @Test
    public void filter_should_one_send_message_to_1_subscriber() throws JMSException, MuleException {
    	
		long residentId = 1212121212L;
		
		// Create request and set correct serviceDomain and categorization
		UpdateType createUdateRequest = createUdateRequest(residentId);
		createUdateRequest.getEngagementTransaction().get(0).getEngagement().setServiceDomain("SERVICEDOMAIN-A");
		createUdateRequest.getEngagementTransaction().get(0).getEngagement().setCategorization("CATEGORY-A");
		
		// Creating all 3 filters
		List<LogicalAddresseeRecordType> filters = new ArrayList<LogicalAddresseeRecordType>();
		
		// Filter 1 - should get one message
		LogicalAddresseeRecordType logicalAddresseeRecordType = createLogicalAddresseeRecordType("HSA_ID_A");
		logicalAddresseeRecordType.getFilter().add(createFilter("SERVICEDOMAIN-A", "CATEGORY-A"));
		filters.add(logicalAddresseeRecordType);

		// Filter 2 - should not get a message
		logicalAddresseeRecordType = createLogicalAddresseeRecordType("HSA_ID_B");
		logicalAddresseeRecordType.getFilter().add(createFilter("SERVICEDOMAIN-B"));
		filters.add(logicalAddresseeRecordType);
		
		// Filter 3 - should not get a message
		logicalAddresseeRecordType = createLogicalAddresseeRecordType("HSA_ID_C");
		logicalAddresseeRecordType.getFilter().add(createFilter("SERVICEDOMAIN-C"));
		filters.add(logicalAddresseeRecordType);
		
		// Set filters
		ProcessNotificationFilter.setFilters(filters);
		
		doOneTestWithActiveFilter(createUdateRequest);

		// Wait a short while for all background processing to complete
		waitForBackgroundProcessing();
		
		// Expect no error logs and 5 info log entries
		assertQueueDepth(ERROR_LOG_QUEUE, 0);
		assertQueueDepth(INFO_LOG_QUEUE, 5);
    }
    
    
	private void doOneTest(final UpdateType request) throws JMSException {

		// Simulate the sending of notifications from the processing service
		String requestXml = jabxUtil.marshal(of.createUpdate(request));
		List<Subscriber> subscribers = subscriberCache.getSubscribers();
		MuleMessage mr = null;
		for (Subscriber subscriber : subscribers) {
			String queueName = subscriber.getNotificationQueueName();
			mr = dispatchAndWaitForServiceComponent("jms://" + queueName + "?connector=soitoolkit-jms-connector", requestXml, null, "process-notification-teststub-service", EI_TEST_TIMEOUT);
		}

		ProcessNotificationResponseType nr = (ProcessNotificationResponseType)mr.getPayload();
		assertEquals( ResultCodeEnum.OK, nr.getResultCode());
	}
	

	/**
	 * Variant of doOneTest where we expect that at least one of the teststubs will not be called due to the filter in place.
	 * Instead of having to wait for a timeout we simply monitor the number of remaing messages on the notificaton queues and return when they are down to zero.
	 * 
	 * @param request
	 * @throws JMSException
	 */
	private void doOneTestWithActiveFilter(final UpdateType request) throws JMSException, MuleException {

		MuleClient muleClient = new MuleClient(muleContext);
		
		// Simulate the sending of notifications from the processing service
		String requestXml = jabxUtil.marshal(of.createUpdate(request));
		List<Subscriber> subscribers = subscriberCache.getSubscribers();
		for (Subscriber subscriber : subscribers) {
			String queueName = subscriber.getNotificationQueueName();

			// Perform the actual dispatch without waiting
			muleClient.dispatch("jms://" + queueName + "?connector=soitoolkit-jms-connector", requestXml, null);
		}
		// Ensure that there now are some messages to be processes on the notification queues 
		// (could be a timing problem, i.e. all messages are already processed now, if the workers are really super fast but that is more of an theoretical exercise...)
		assertFalse(0 == getJmsUtil().browseMessagesOnQueue(Subscriber.NOTIFICATION_QUEUE_PREFIX + "*").size());

		// Wait for the messages to be processed
		for (int i = 0; i < 5; i++) {
			if (0 == getJmsUtil().browseMessagesOnQueue(Subscriber.NOTIFICATION_QUEUE_PREFIX + "*").size()) break;
			waitForBackgroundProcessing();
		}

		// If the messages are still there we better throw an assert-exception now
		assertEquals(0, getJmsUtil().browseMessagesOnQueue(Subscriber.NOTIFICATION_QUEUE_PREFIX + "*").size());
	}
	

	/**
	 * Creates a filter with a serviceDomain and zero or more categories
	 * 
	 * @param serviceDomain
	 * @param categorizations
	 * @return
	 */
	private FilterType createFilter(String serviceDomain, String ...categorizations) {
		
		FilterType filterType = new FilterType();
		filterType.setServiceDomain(serviceDomain);
		
		if (categorizations != null) {
			for (String categorization : categorizations) {
				filterType.getCategorization().add(categorization);
			}
		}
		
		return filterType;
	}
	
	/**
	 * Creates a LogicalAddresseeRecordType with only a logicalAddress.
	 * 
	 * @param logicalAddress
	 * @return
	 */
	private LogicalAddresseeRecordType createLogicalAddresseeRecordType(String logicalAddress) {
		LogicalAddresseeRecordType logicalAddresseeRecordType = new LogicalAddresseeRecordType();
		logicalAddresseeRecordType.setLogicalAddress(logicalAddress);
		
		return logicalAddresseeRecordType;
	}
	

}
