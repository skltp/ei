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
package se.skltp.ei.intsvc.integrationtests.endtoend;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.net.SocketException;
import java.util.List;

import javax.jms.JMSException;

import org.junit.Before;
import org.junit.Test;
import org.mule.api.MuleMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.test.Dispatcher;

import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationResponseType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.intsvc.EiMuleServer;
import se.skltp.ei.intsvc.integrationtests.AbstractTestCase;
import se.skltp.ei.intsvc.integrationtests.getlogicaladdressees.GetLogicalAddresseesByServiceContractTestProducerLogger;
import se.skltp.ei.intsvc.integrationtests.notificationservice.NotificationTestConsumer;
import se.skltp.ei.intsvc.integrationtests.notifyservice.ProcessNotificationTestProducer;
import se.skltp.ei.intsvc.integrationtests.notifyservice.ProcessNotificationTestProducerLogger;
import se.skltp.ei.intsvc.integrationtests.updateservice.UpdateTestConsumer;
import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.entity.repository.EngagementRepository;

public class EndToEndIntegrationTest extends AbstractTestCase {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(EndToEndIntegrationTest.class);
	 
	private static final long SERVICE_TIMOUT_MS = Long.parseLong(rb.getString("SERVICE_TIMEOUT_MS"));
    
    private static final String LOGICAL_ADDRESS = rb.getString("EI_HSA_ID");
    private static final String VP_INSTANCE_ID = rb.getString("VP_INSTANCE_ID");
    private static final String OWNER = rb.getString("EI_HSA_ID");
        
	@SuppressWarnings("unused")
	private static final String EXPECTED_ERR_TIMEOUT_MSG = "Read timed out";
//	private static final String EXPECTED_ERR_INVALID_ID_MSG = "Invalid Id: " + TEST_RR_ID_FAULT_INVALID_ID;
	private static final String UPDATE_SERVICE_ADDRESS = EiMuleServer.getAddress("UPDATE_WEB_SERVICE_URL");
	private static final String NOTIFICATION_SERVICE_ADDRESS = EiMuleServer.getAddress("NOTIFICATION_WEB_SERVICE_URL");
  
	private EngagementRepository engagementRepository;

    public EndToEndIntegrationTest() {
        // Only start up Mule once to make the tests run faster...
        // Set to false if tests interfere with each other when Mule is started only once.
        setDisposeContextPerClass(false);
    }

    protected String getConfigResources() {
		return 
			"soitoolkit-mule-jms-connector-activemq-embedded.xml," + 
	  		"ei-common.xml," +
	  		"skltp-ei-svc-spring-context.xml," +
	        "get-logical-addressees-service.xml," + 
	        "update-service.xml," + 
	        "notification-service.xml," + 
	        "process-service.xml," + 
	        "init-dynamic-flows.xml," +
	        "teststub-services/get-logical-addressees-by-service-contract-teststub-service.xml," +
	        "teststub-services/process-notification-teststub-service.xml";
    }

    @Before
    public void setUp() throws Exception {
    	
    	// Lookup the entity repository if not already done
    	if (engagementRepository == null) {
    		engagementRepository = muleContext.getRegistry().lookupObject(EngagementRepository.class);
    	}

    	// Clean the storage
    	engagementRepository.deleteAll();

    	// Clear queues used for the tests
		getJmsUtil().clearQueues(INFO_LOG_QUEUE, ERROR_LOG_QUEUE, PROCESS_QUEUE);
    
    }

	/**
	 * Perform a test that is expected to return one hit
	 */
    @Test
    public void endToEnd_update_OK() {
    	
		long residentId = 1212121212L;
		String fullResidentId = "19" + residentId;
		
		MuleMessage r = dispatchAndWaitForServiceComponent(new DoOneTestUpdateDispatcher(createUdateRequest(null, residentId)), "process-notification-teststub-service", EI_TEST_TIMEOUT);
        
		ProcessNotificationResponseType nr = (ProcessNotificationResponseType)r.getPayload();
		assertEquals(ResultCodeEnum.OK, nr.getResultCode());

		
		// Verify that we got something in the database as well
        List<Engagement> result = (List<Engagement>) engagementRepository.findAll();
        assertEquals(1, result.size());
        assertThat(result.get(0).getBusinessKey().getRegisteredResidentIdentification(), is(fullResidentId));

		// Wait a short while for all background processing to complete
		waitForBackgroundProcessing();

		// Assert that no messages are left on the processing queue
		assertQueueDepth(PROCESS_QUEUE, 0);
		
		// Expect no error logs
		assertQueueDepth(ERROR_LOG_QUEUE, 0);

		// Expect 14 info log entries, 3 from update-service, 1+3 from process-service and 3*3 from the three notify-services
		assertQueueDepth(INFO_LOG_QUEUE, 16);

		/*
		 *  Verify that both the GetLogicalAddresseesByServiceContract service and the ProcessNotificationTestProducerLogger was called with 
		 *  the EI HSA-ID as the callers logical address.
		 *  Verify correct VP instance id is provided in outgoing calls from EI to VP.
		 */
		assertEquals(LOGICAL_ADDRESS, GetLogicalAddresseesByServiceContractTestProducerLogger.getLastConsumer());
    	assertEquals(VP_INSTANCE_ID, GetLogicalAddresseesByServiceContractTestProducerLogger.getLastVpInstance());
    	assertEquals(LOGICAL_ADDRESS, ProcessNotificationTestProducerLogger.getLastConsumer());
    	assertEquals(VP_INSTANCE_ID, ProcessNotificationTestProducerLogger.getLastVpInstance());
    }

	/**
	 * Perform a test that is expected to return one hit
	 */
    @Test
    public void endToEnd_notification_OK() {
    	
    	long residentId = 1212121212L;
		String fullResidentId = "19" + residentId;
		
		MuleMessage r = dispatchAndWaitForServiceComponent(new DoOneTestNotificationDispatcher(createProcessNotificationRequest(residentId)), "process-notification-teststub-service", EI_TEST_TIMEOUT);
        
		ProcessNotificationResponseType nr = (ProcessNotificationResponseType)r.getPayload();
		assertEquals(ResultCodeEnum.OK, nr.getResultCode());

		// Verify that we got something in the database as well
        List<Engagement> result = (List<Engagement>) engagementRepository.findAll();
        assertEquals(1, result.size());
        assertThat(result.get(0).getBusinessKey().getRegisteredResidentIdentification(), is(fullResidentId));

		// Wait a short while for all background processing to complete
		waitForBackgroundProcessing();

		// Assert that no messages are left on the processing queue
		assertQueueDepth(PROCESS_QUEUE, 0);

		// Wait a short while for all background processing to complete
		waitForBackgroundProcessing();
		
		// Expect no error logs
		assertQueueDepth(ERROR_LOG_QUEUE, 0);

		// Expect 14 info log entries, 3 from update-service, 1+3 from process-service and 3*3 from the three notify-services
		assertQueueDepth(INFO_LOG_QUEUE, 16);
		
		/*
		 *  Verify that both the GetLogicalAddresseesByServiceContract service and the ProcessNotificationTestProducerLogger was called with 
		 *  the EI HSA-ID as the callers logical address.
		 *  Verify correct VP instance id is provided in outgoing calls from EI to VP.
		 */
    	assertEquals(LOGICAL_ADDRESS, GetLogicalAddresseesByServiceContractTestProducerLogger.getLastConsumer());
    	assertEquals(VP_INSTANCE_ID, GetLogicalAddresseesByServiceContractTestProducerLogger.getLastVpInstance());
    	assertEquals(LOGICAL_ADDRESS, ProcessNotificationTestProducerLogger.getLastConsumer());
    	assertEquals(VP_INSTANCE_ID, ProcessNotificationTestProducerLogger.getLastVpInstance());
    }
    
    
    /**
     * Performs a test that verifies that engagements with same owner as the index are filtered out and not saved
     * in the data store.
     * @throws JMSException 
     */
    @Test
    public void endToEnd_processNotification_R4_OK_filter_should_remove_circular_notifications() throws JMSException {
    	
		// Setup testdata
		long residentId = 1111111111L;
		String fullResidentId = "19" + residentId;
		
		ProcessNotificationType request = createProcessNotificationRequest(residentId, 1212121212L);
		request.getEngagementTransaction().get(1).getEngagement().setOwner(OWNER); // This engagement should be filtered out
		
		MuleMessage r = dispatchAndWaitForServiceComponent(new DoOneTestNotificationDispatcher(request), "process-notification-teststub-service", EI_TEST_TIMEOUT);
					
		ProcessNotificationResponseType nr = (ProcessNotificationResponseType)r.getPayload();
		assertEquals(ResultCodeEnum.OK, nr.getResultCode());

		// Verify that we got something in the database as well
		List<Engagement> result = (List<Engagement>) engagementRepository.findAll();
		assertEquals(1, result.size());
		
		// Verify that the correct Engagement was saved
		assertThat(result.get(0).getRegisteredResidentIdentification(), is(fullResidentId));
		assertThat(result.get(0).getOwner(), is(request.getEngagementTransaction().get(0).getEngagement().getOwner()));
		
		// Assert that no messages are left on the processing queue
		assertQueueDepth(PROCESS_QUEUE, 0);
		
		// Wait a short while for all background processing to complete
		waitForBackgroundProcessing();
		
		// Expect no error logs
		assertQueueDepth(ERROR_LOG_QUEUE, 0);
		
		// Expect 14 info log entries, 3 from update-service, 1+3 from process-service and 3*3 from the three notify-services
		assertQueueDepth(INFO_LOG_QUEUE, 16);

		// Expect message sent to the subscriber only containing 1 engagementTransaction, meaning that the otherwise never ending loop effectively is broken :-)
		ProcessNotificationType pn = (ProcessNotificationType)ProcessNotificationTestProducer.getLastPayload();
		assertEquals(1, pn.getEngagementTransaction().size());
    }

	/**
	 * Perform a test that is expected to create a timeout
	 */
    @Test
    public void endToEnd_update_ERR_timeout_in_subscriber() {

		UpdateType request = createUdateRequest(null, ProcessNotificationTestProducer.TEST_ID_FAULT_TIMEOUT);
		new DoOneTestUpdateDispatcher(request).doDispatch();
		Throwable e = waitForException(SERVICE_TIMOUT_MS + 5000);

		// Assert that we got the expected root-exception
		while(e.getCause() != null) {
			e = e.getCause();
		}
		assertEquals(SocketException.class, e.getClass());
		
		// Expect 3 error log and 19 info log entries
		assertQueueDepth(ERROR_LOG_QUEUE, 3);
		
		// TODO. Use assertQueueMatchesMessages() instead to match all three messages with their different service names, e.g. : <serviceImplementation>notify-service-HSA_ID_A</serviceImplementation>
		assertQueueContainsMessage(ERROR_LOG_QUEUE, "java.net.SocketTimeoutException: Read timed out");
		assertQueueDepth(INFO_LOG_QUEUE, 19);

		// Expect nothing on the processing queue due to the error
		assertQueueDepth(PROCESS_QUEUE, 0);
    }

	private class DoOneTestUpdateDispatcher implements Dispatcher {
		
		private UpdateType request = null;
		private String logicalAddress = null;

		private DoOneTestUpdateDispatcher(UpdateType request) {
			this.request  = request;
			this.logicalAddress = LOGICAL_ADDRESS;
		}
		
		private DoOneTestUpdateDispatcher(String logicalAddress, UpdateType request) {
			this.request  = request;
			this.logicalAddress = logicalAddress;
		}
		
		@Override
		public void doDispatch() {
			UpdateTestConsumer consumer = new UpdateTestConsumer(UPDATE_SERVICE_ADDRESS);

			UpdateResponseType response = consumer.callService(logicalAddress, request);
	        
			// Assert OK response from the web service
	        assertEquals(ResultCodeEnum.OK, response.getResultCode());
		}
	}
	
	private class DoOneTestNotificationDispatcher implements Dispatcher {
		
		private ProcessNotificationType request = null;
		private String logicalAddress = null;

		private DoOneTestNotificationDispatcher(ProcessNotificationType request) {
			this.request  = request;
			this.logicalAddress = LOGICAL_ADDRESS;
		}
		
		private DoOneTestNotificationDispatcher(String logicalAddress, ProcessNotificationType request) {
			this.request  = request;
			this.logicalAddress = logicalAddress;
		}
		
		@Override
		public void doDispatch() {
			NotificationTestConsumer consumer = new NotificationTestConsumer(NOTIFICATION_SERVICE_ADDRESS);

			ProcessNotificationResponseType response = consumer.callService(logicalAddress, request);
	        
			// Assert OK response from the web service
	        assertEquals(ResultCodeEnum.OK, response.getResultCode());
		}
	}
}