package se.skltp.ei.intsvc.integrationtests.endtoend;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.net.SocketException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mule.api.MuleMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.test.Dispatcher;

import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationResponseType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.intsvc.EiMuleServer;
import se.skltp.ei.intsvc.integrationtests.AbstractTestCase;
import se.skltp.ei.intsvc.integrationtests.notifyservice.ProcessNotificationTestProducer;
import se.skltp.ei.intsvc.integrationtests.updateservice.UpdateTestConsumer;
import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.entity.repository.EngagementRepository;

public class EndToEndIntegrationTest extends AbstractTestCase {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(EndToEndIntegrationTest.class);
	 
	private static final long SERVICE_TIMOUT_MS = Long.parseLong(rb.getString("SERVICE_TIMEOUT_MS"));
    
    private static final String LOGICAL_ADDRESS = rb.getString("EI_HSA_ID");
        
	@SuppressWarnings("unused")
	private static final String EXPECTED_ERR_TIMEOUT_MSG = "Read timed out";
//	private static final String EXPECTED_ERR_INVALID_ID_MSG = "Invalid Id: " + TEST_RR_ID_FAULT_INVALID_ID;
	private static final String SERVICE_ADDRESS = EiMuleServer.getAddress("UPDATE_WEB_SERVICE_URL");
  
    public EndToEndIntegrationTest() {
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
	        "update-service.xml," + 
	        "process-service.xml," + 
	        "teststub-services/init-dynamic-flows.xml," +
	        "teststub-services/get-logical-addressees-by-service-contract-teststub-service.xml," +
	        "teststub-services/process-notification-teststub-service.xml";
    }

    private EngagementRepository engagementRepository;

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
		
		MuleMessage r = dispatchAndWaitForServiceComponent(new DoOneTestDispatcher(createUdateRequest(residentId)), "process-notification-teststub-service", EI_TEST_TIMEOUT);
        
		ProcessNotificationResponseType nr = (ProcessNotificationResponseType)r.getPayload();
		assertEquals(ResultCodeEnum.OK, nr.getResultCode());

		
		// Verify that we got something in the database as well
        List<Engagement> result = (List<Engagement>) engagementRepository.findAll();
        assertEquals(1, result.size());
        assertThat(result.get(0).getBusinessKey().getRegisteredResidentIdentification(), is(fullResidentId));

		// Assert that no messages are left on the processing queue
		assertQueueDepth(PROCESS_QUEUE, 0);

		// Wait a short while for all background processing to complete
		waitForBackgroundProcessing();
		
		// Expect no error logs
		assertQueueDepth(ERROR_LOG_QUEUE, 0);

		// Expect 14 info log entries, 3 from update-service, 2 from process-service and 3*3 from the three notify-services
		assertQueueDepth(INFO_LOG_QUEUE, 14);

    }

	/**
	 * Perform a test that is expected to create a timeout
	 */
    @Test
    public void endToEnd_update_ERR_timeout_in_subscriber() {

		UpdateType request = createUdateRequest(ProcessNotificationTestProducer.TEST_ID_FAULT_TIMEOUT);
		new DoOneTestDispatcher(request).doDispatch();
		Exception e = waitForException(SERVICE_TIMOUT_MS + 2000);

		// Assert that we got the expected exception
		assertEquals(SocketException.class, e.getClass());
		
		// Expect 3 error log and 17 info log entries
		assertQueueDepth(ERROR_LOG_QUEUE, 3);
		
		// TODO. Use assertQueueMatchesMessages() instead to match all three messages with their different service names, e.g. : <serviceImplementation>notify-service-HSA_ID_A</serviceImplementation>
		assertQueueContainsMessage(ERROR_LOG_QUEUE, "java.net.SocketTimeoutException: Read timed out");
		assertQueueDepth(INFO_LOG_QUEUE, 17);

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