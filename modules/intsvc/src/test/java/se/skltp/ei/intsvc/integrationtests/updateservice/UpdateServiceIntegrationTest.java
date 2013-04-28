package se.skltp.ei.intsvc.integrationtests.updateservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.jms.JMSException;

import org.junit.Before;
import org.junit.Test;
import org.mule.api.MuleMessage;
import org.mule.context.notification.EndpointMessageNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.test.Dispatcher;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;

import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.intsvc.EiMuleServer;
import se.skltp.ei.intsvc.integrationtests.AbstractTestCase;

public class UpdateServiceIntegrationTest extends AbstractTestCase {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(UpdateServiceIntegrationTest.class);
	 	
    private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("ei-config");

	@SuppressWarnings("unused")
	private static final long SERVICE_TIMOUT_MS = Long.parseLong(rb.getString("SERVICE_TIMEOUT_MS"));
    
	private static final String PROCESS_QUEUE = rb.getString("PROCESS_QUEUE");
    private static final String INVALID_LOGICAL_ADDRESS = "wrongLogicalAddress";
    private static final String LOGICAL_ADDRESS = rb.getString("EI_HSA_ID");
    
	@SuppressWarnings("unused")
	private static final String EXPECTED_ERR_TIMEOUT_MSG = "Read timed out";
//	private static final String EXPECTED_ERR_INVALID_ID_MSG = "Invalid Id: " + TEST_RR_ID_FAULT_INVALID_ID;
	private static final String SERVICE_ADDRESS = EiMuleServer.getAddress("UPDATE_WEB_SERVICE_URL");
  
	private static final String INFO_LOG_QUEUE  = rb.getString("SOITOOLKIT_LOG_INFO_QUEUE");
	private static final String ERROR_LOG_QUEUE = rb.getString("SOITOOLKIT_LOG_ERROR_QUEUE");

	public UpdateServiceIntegrationTest() {
        // Only start up Mule once to make the tests run faster...
        // Set to false if tests interfere with each other when Mule is started only once.
        setDisposeContextPerClass(true);
    }

    protected String getConfigResources() {
		return 
			"soitoolkit-mule-jms-connector-activemq-embedded.xml," + 
	  		"ei-common.xml," +
	        "update-service.xml";
    }

    @Before
    public void setUp() throws Exception {

    	// Clear queues used for the tests
		getJmsUtil().clearQueues(INFO_LOG_QUEUE, ERROR_LOG_QUEUE, PROCESS_QUEUE);
    }

	/**
	 * Validate expected behavior of the update-service with OK input
	 * 
	 * @throws JMSException 
	 */
    @Test
    public void update_OK() throws JMSException {
    	
		UpdateType request = createUdateRequest(1212121212L);
		
		// Use dispatchAndWaitForDelivery() and a custom Dispatcher to ensure that the listener on the queue is registered before the web service call is made
        MuleMessage response = dispatchAndWaitForDelivery(new DoOneTestDispatcher(request), "jms://" + PROCESS_QUEUE, EndpointMessageNotification.MESSAGE_DISPATCH_END, DEFAULT_TEST_TIMEOUT);

        // Compare the notified message with the request message, they should be the same
        assertUpdateRequest(request, response);

		// Expect no error logs and three info log entries
		assertQueueDepth(ERROR_LOG_QUEUE, 0);
		assertQueueDepth(INFO_LOG_QUEUE, 3);

		// Assert that the response is the only message on the queue
		assertQueueDepth(PROCESS_QUEUE, 1);
    }

	/**
	 * Validate correct error message when two Engagement Transactions have the same key
	 */
    @Test
    public void update_R1_ERR_duplicate_tx_found() {
		
		UpdateType request = createUdateRequest(1111111111L, 1111111111L);
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

    /**
	 * Validate correct error message when an incorrect logical address is used
     */
    @Test
    public void update_R7_ERR_owner_dont_match_logicaladdress() {
		
		UpdateType request = createUdateRequest(1111111111L);
		String expectedError = "EI003: Invalid routing. Logical address is wrongLogicalAddress but the owner is logical-address. They must be the same. See rule for Update-R7 in service contract";

		try {
			// Call the update web service without waiting for an asynch event since we expect the web service to return an error directly without triggering any asynch processing
			new DoOneTestDispatcher(INVALID_LOGICAL_ADDRESS, request).doDispatch();
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


}