package se.skltp.ei.intsvc.integrationtests.notificationservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mule.api.MuleMessage;
import org.mule.context.notification.EndpointMessageNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.test.Dispatcher;

import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationResponseType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import se.skltp.ei.intsvc.EiMuleServer;
import se.skltp.ei.intsvc.integrationtests.AbstractTestCase;

public class NotificationServiceIntegrationTest extends AbstractTestCase {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(NotificationServiceIntegrationTest.class);
	 	
	@SuppressWarnings("unused")
	private static final long SERVICE_TIMOUT_MS = Long.parseLong(rb.getString("SERVICE_TIMEOUT_MS"));

    private static final String LOGICAL_ADDRESS = rb.getString("EI_HSA_ID");

	@SuppressWarnings("unused")
	private static final String EXPECTED_ERR_TIMEOUT_MSG = "Read timed out";
//	private static final String EXPECTED_ERR_INVALID_ID_MSG = "Invalid Id: " + TEST_RR_ID_FAULT_INVALID_ID;
	private static final String SERVICE_ADDRESS = EiMuleServer.getAddress("NOTIFICATION_WEB_SERVICE_URL");
  
	public NotificationServiceIntegrationTest() {
        // Only start up Mule once to make the tests run faster...
        // Set to false if tests interfere with each other when Mule is started only once.
        setDisposeContextPerClass(true);
    }

    protected String getConfigResources() {
		return 
			"soitoolkit-mule-jms-connector-activemq-embedded.xml," + 
	  		"ei-common.xml," +
	        "notification-service.xml";
    }

    @Before
    public void setUp() throws Exception {

    	// Clear queues used for the tests
		getJmsUtil().clearQueues(INFO_LOG_QUEUE, ERROR_LOG_QUEUE, PROCESS_QUEUE);
    }

	/**
	 * Validate expected behavior of the update-service with OK input
	 */
    @Test
    public void notification_OK() {
    	
		ProcessNotificationType request = createProcessNotificationRequest(1212121212L);
		
		// Use dispatchAndWaitForDelivery() and a custom Dispatcher to ensure that the listener on the queue is registered before the web service call is made
        MuleMessage response = dispatchAndWaitForDelivery(new DoOneTestDispatcher(request), "jms://" + PROCESS_QUEUE, EndpointMessageNotification.MESSAGE_DISPATCH_END, EI_TEST_TIMEOUT);

        // Compare the notified message with the request message, they should be the same
        assertProcessNotificationRequest(request, response);

		// Expect no error logs and three info log entries
		assertQueueDepth(ERROR_LOG_QUEUE, 0);
		assertQueueDepth(INFO_LOG_QUEUE, 3);

		// Assert that the response is the only message on the queue
		assertQueueDepth(PROCESS_QUEUE, 1);
    }

	/**
	 * Validate correct error message when two Engagement Transactions have the same key
	 */
    // TODO. Patrik. Remove once business logic is in place.
    @Ignore
    @Test
    public void update_R1_ERR_duplicate_tx_found() {
		
    	ProcessNotificationType request = createProcessNotificationRequest(1111111111L, 1111111111L);
		String expectedError = "EI002: EngagementTransaction #1 and #2 have the same key. That is not allowed. See rule for Update-R1 in service contract";

		try {
			// Call the update web service without waiting for an asynch event since we expect the web service to return an error directly without triggering any asynch processing
			new DoOneTestDispatcher(request).doDispatch();
			fail("Expected exception here");

		} catch (javax.xml.ws.soap.SOAPFaultException e) {
			// TODO: Add more SOAP Fault specific tests, can we get the actual SOAP fault XML to validate against???
			assertEquals("javax.xml.ws.soap.SOAPFaultException: " + expectedError, e.toString());
		};

		// Expect one error log and info log entry
		assertQueueDepth(ERROR_LOG_QUEUE, 1);
		assertQueueContainsMessage(ERROR_LOG_QUEUE, expectedError);
		assertQueueDepth(INFO_LOG_QUEUE, 1);

		// Expect nothing on the processing queue due to the error
		assertQueueDepth(PROCESS_QUEUE, 0);
    }

	private class DoOneTestDispatcher implements Dispatcher {
		
		private ProcessNotificationType request = null;
		private String logicalAddress = null;

		private DoOneTestDispatcher(ProcessNotificationType request) {
			this.request  = request;
			this.logicalAddress = LOGICAL_ADDRESS;
		}
		
		private DoOneTestDispatcher(String logicalAddress, ProcessNotificationType request) {
			this.request  = request;
			this.logicalAddress = logicalAddress;
		}
		
		@Override
		public void doDispatch() {
			NotificationTestConsumer consumer = new NotificationTestConsumer(SERVICE_ADDRESS);

			ProcessNotificationResponseType response = consumer.callService(logicalAddress, request);
	        
			// Assert OK response from the web service
	        assertEquals(ResultCodeEnum.OK, response.getResultCode());
		}
	}


}