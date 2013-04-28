package se.skltp.ei.intsvc.integrationtests.endtoend;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.net.SocketException;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.junit.Before;
import org.junit.Test;
import org.mule.api.MuleMessage;
import org.mule.context.notification.EndpointMessageNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;
import org.soitoolkit.commons.mule.test.Dispatcher;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationResponseType;
import riv.itintegration.engagementindex.updateresponder._1.ObjectFactory;
import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.intsvc.EiMuleServer;
import se.skltp.ei.intsvc.integrationtests.AbstractTestCase;
import se.skltp.ei.intsvc.integrationtests.notifyservice.ProcessNotificationTestProducer;
import se.skltp.ei.intsvc.integrationtests.updateservice.UpdateTestConsumer;
import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.entity.repository.EngagementRepository;
import se.skltp.ei.svc.service.GenServiceTestDataUtil;

public class EndToEndIntegrationTest extends AbstractTestCase {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(EndToEndIntegrationTest.class);
	 
	private static final JaxbUtil jabxUtil = new JaxbUtil(UpdateType.class);
	private static final ObjectFactory of = new ObjectFactory();
	
    private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("ei-config");

	private static final long SERVICE_TIMOUT_MS = Long.parseLong(rb.getString("SERVICE_TIMEOUT_MS"));
    
    private static final String NOTIFICATION_TOPIC = rb.getString("NOTIFICATION_TOPIC");
    private static final String LOGICAL_ADDRESS = rb.getString("EI_HSA_ID");
    
    private static final String SOITOOLKIT_LOG_ERROR_QUEUE = rb.getString("SOITOOLKIT_LOG_ERROR_QUEUE");
    
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
	}

	/**
	 * Perform a test that is expected to return one hit
	 * 
	 * @throws JMSException 
	 */
    @Test
    public void endToEnd_update_OK() throws JMSException {
    	
		long residentId = 1212121212L;
		String fullResidentId = "19" + residentId;
		
		doOneTest(createUdateRequest(residentId));

		// Verify that we got something in the database as well
        List<Engagement> result = (List<Engagement>) engagementRepository.findAll();
        assertEquals(1, result.size());
        assertThat(result.get(0).getBusinessKey().getRegisteredResidentIdentification(), is(fullResidentId));
        
    }

	/**
	 * Perform a test that is expected to create a timeout
	 * @throws JMSException 
	 */
    @Test
    public void endToEnd_update_ERR_timeout_in_subscriber() throws JMSException {

		UpdateType request = createUdateRequest(ProcessNotificationTestProducer.TEST_ID_FAULT_TIMEOUT);
		new DoOneTestDispatcher(request).doDispatch();
		Exception e = waitForException(SERVICE_TIMOUT_MS + 2000);
		
		assertEquals(SocketException.class, e.getClass());
		
		SocketException se = (SocketException)e;
		
		System.err.println("Cause: " + se.getCause());
		Throwable[] suppressed = se.getSuppressed();
		System.err.println("suppressed cnt: " + suppressed.length);
		for (int i = 0; i < suppressed.length; i++) {
			System.err.println(suppressed[i]);
		}

		// FIXME check error queues and DL-queue

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

	private void doOneTest(final UpdateType request) throws JMSException {

		MuleMessage r = dispatchAndWaitForServiceComponent(new DoOneTestDispatcher(request), "process-notification-teststub-service", 5000);
        
		ProcessNotificationResponseType nr = (ProcessNotificationResponseType)r.getPayload();
		assertEquals( ResultCodeEnum.OK, nr.getResultCode());
	}

}