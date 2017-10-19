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
package se.skltp.ei.intsvc.integrationtests.collect;

import static org.junit.Assert.assertEquals;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.api.MuleMessage;
import org.mule.context.notification.EndpointMessageNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.test.Dispatcher;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.intsvc.EiMuleServer;
import se.skltp.ei.intsvc.integrationtests.AbstractTestCase;
import se.skltp.ei.intsvc.integrationtests.updateservice.UpdateTestConsumer;
import se.skltp.ei.svc.service.GenServiceTestDataUtil;

public class UpdateServiceCollectIntegrationTest extends AbstractTestCase {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(UpdateServiceCollectIntegrationTest.class);
	 	
	@SuppressWarnings("unused")
	private static final long SERVICE_TIMOUT_MS = Long.parseLong(rb.getString("SERVICE_TIMEOUT_MS"));
    
	//private static final String INVALID_LOGICAL_ADDRESS = "wrongLogicalAddress";
    private static final String LOGICAL_ADDRESS = rb.getString("EI_HSA_ID");
    private static final String OWNER = rb.getString("EI_HSA_ID");
	// private static final String COLLECT_TRESHOLD_ORIGINAL = rb.getString("COLLECT_TRESHOLD");

	@SuppressWarnings("unused")
	private static final String EXPECTED_ERR_TIMEOUT_MSG = "Read timed out";
	private static final String SERVICE_ADDRESS = EiMuleServer.getAddress("UPDATE_WEB_SERVICE_URL");
  
	public UpdateServiceCollectIntegrationTest() {
        // Only start up Mule once to make the tests run faster...
        // Set to false if tests interfere with each other when Mule is started only once.
        setDisposeContextPerClass(true);
    }

    protected String getConfigResources() {
    	// Set treshold to 1
    	System.setProperty("COLLECT_TRESHOLD", "1");
		return 
			"soitoolkit-mule-jms-connector-activemq-embedded.xml," + 
	  		"ei-common.xml," +
			"skltp-ei-svc-spring-context.xml," +
	        "update-service.xml";
    }

    @Before
    public void setUp() throws Exception {
    	// Clear queues used for the tests
		getJmsUtil().clearQueues(INFO_LOG_QUEUE, ERROR_LOG_QUEUE, PROCESS_QUEUE, COLLECT_QUEUE);
    }

    @After
    public void tearDown() throws Exception {
    	// Set treshold to original value
    	System.clearProperty("COLLECT_TRESHOLD");
    }

    
	/**
	 * Validate expected behavior of the update-service with OK input 1 post
	 */
    @Test
    public void update_1_post_OK() {
    	
		UpdateType request = createUdateRequest(null, 1212121212L);
		
		// Use dispatchAndWaitForDelivery() and a custom Dispatcher to ensure that the listener on the queue is registered before the web service call is made
        MuleMessage response = dispatchAndWaitForDelivery(new DoOneTestDispatcher(request), "jms://" + COLLECT_QUEUE, EndpointMessageNotification.MESSAGE_DISPATCH_END, EI_TEST_TIMEOUT);

        // Compare the notified message with the request message, they should be the same except that the owner has been set 
        for (EngagementTransactionType ett : request.getEngagementTransaction()) {
        	ett.getEngagement().setOwner(OWNER);
        }
        assertUpdateRequest(request, response);

		// Expect no error logs and three info log entries
		assertQueueDepth(ERROR_LOG_QUEUE, 0);
		assertQueueDepth(INFO_LOG_QUEUE, 3);

		// Assert that the response is the only message on the queue
		assertQueueDepth(COLLECT_QUEUE, 1);
    }

	/**
	 * Validate expected behavior of the update-service with OK input 2 posts
	 */
    @Test
    public void update_2_posts_OK() {
    	
		UpdateType request = createUdateRequest(null, 1212121212L, 101010101010L );
		
		// Use dispatchAndWaitForDelivery() and a custom Dispatcher to ensure that the listener on the queue is registered before the web service call is made
        MuleMessage response = dispatchAndWaitForDelivery(new DoOneTestDispatcher(request), "jms://" + PROCESS_QUEUE, EndpointMessageNotification.MESSAGE_DISPATCH_END, EI_TEST_TIMEOUT);

        // Compare the notified message with the request message, they should be the same except that the owner has been set 
        for (EngagementTransactionType ett : request.getEngagementTransaction()) {
        	ett.getEngagement().setOwner(OWNER);
        }
        assertUpdateRequest(request, response);

		// Expect no error logs and three info log entries
		assertQueueDepth(ERROR_LOG_QUEUE, 0);
		assertQueueDepth(INFO_LOG_QUEUE, 3);

		// Assert that the response is the only message on the queue
		assertQueueDepth(PROCESS_QUEUE, 1);
    }

	/**
	 * Validate expected behavior of the update-service with OK input
	 */
    @Test
    public void update_2_msgs_1_post_OK() {
    	
		UpdateType request = createUdateRequest(null, 1212121212L, 101010101010L );
		
		// Use dispatchAndWaitForDelivery() and a custom Dispatcher to ensure that the listener on the queue is registered before the web service call is made
        MuleMessage response = dispatchAndWaitForDelivery(new DoOneTestDispatcher(request), "jms://" + PROCESS_QUEUE, EndpointMessageNotification.MESSAGE_DISPATCH_END, EI_TEST_TIMEOUT);

        // Compare the notified message with the request message, they should be the same except that the owner has been set 
        for (EngagementTransactionType ett : request.getEngagementTransaction()) {
        	ett.getEngagement().setOwner(OWNER);
        }
        assertUpdateRequest(request, response);

		// Expect no error logs and three info log entries
		assertQueueDepth(ERROR_LOG_QUEUE, 0);
		assertQueueDepth(INFO_LOG_QUEUE, 3);

		// Assert that the response is the only message on the queue
		assertQueueDepth(PROCESS_QUEUE, 1);
    }

    private class DoOneTestDispatcher implements Dispatcher {
		
		private UpdateType request = null;
		private String logicalAddress = null;

		private DoOneTestDispatcher(UpdateType request) {
			this.request  = request;
			this.logicalAddress = LOGICAL_ADDRESS;
		}
		
		private DoOneTestDispatcher(String logicalAddress, UpdateType request) {
			this.request  = request;
			this.logicalAddress = logicalAddress;
		}
		
		@Override
		public void doDispatch() {
			UpdateTestConsumer consumer = new UpdateTestConsumer(SERVICE_ADDRESS);

			UpdateResponseType response = consumer.callService(logicalAddress, request);
	        
			// Assert OK response from the web service
	        assertEquals(ResultCodeEnum.OK, response.getResultCode());
		}
	}

	@SuppressWarnings("unused")
	private UpdateType createUpdateRequestWithTransactions(int count) {
		UpdateType request = new UpdateType();

		long start = 1111111111L;
		for(int i = 0 ; i < count; i++) {
			EngagementTransactionType et = GenServiceTestDataUtil.genEngagementTransaction(start + i);
			request.getEngagementTransaction().add(et);
		}
		return request;
	}

}