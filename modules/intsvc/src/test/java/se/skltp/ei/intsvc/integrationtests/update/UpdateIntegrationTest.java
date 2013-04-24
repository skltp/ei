package se.skltp.ei.intsvc.integrationtests.update;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

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
import riv.itintegration.engagementindex.updateresponder._1.ObjectFactory;
import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.intsvc.EiMuleServer;
import se.skltp.ei.intsvc.integrationtests.AbstractTestCase;
import se.skltp.ei.intsvc.integrationtests.processnotification.ProcessNotificationTestProducer;
import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.entity.repository.EngagementRepository;
import se.skltp.ei.svc.service.GenServiceTestDataUtil;

public class UpdateIntegrationTest extends AbstractTestCase {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(UpdateIntegrationTest.class);
	 
	private static final JaxbUtil jabxUtil = new JaxbUtil(UpdateType.class);
	private static final ObjectFactory of = new ObjectFactory();
	
    private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("ei-config");

	private static final long SERVICE_TIMOUT_MS = Long.parseLong(rb.getString("SERVICE_TIMEOUT_MS"));
    
    private static final String NOTIFICATION_TOPIC = rb.getString("NOTIFICATION_TOPIC");
    private static final String INVALID_LOGICAL_ADDRESS = "wrongLogicalAddress";
    private static final String LOGICAL_ADDRESS = rb.getString("EI_HSA_ID");
    
	@SuppressWarnings("unused")
	private static final String EXPECTED_ERR_TIMEOUT_MSG = "Read timed out";
//	private static final String EXPECTED_ERR_INVALID_ID_MSG = "Invalid Id: " + TEST_RR_ID_FAULT_INVALID_ID;
	private static final String SERVICE_ADDRESS = EiMuleServer.getAddress("UPDATE_WEB_SERVICE_URL");
  
    public UpdateIntegrationTest() {
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
	 * @throws JMSException 
	 */
    @Test
    public void test_ok_one_tx() throws JMSException {
    	
		long residentId = 1212121212L;
		String fullResidentId = "19" + residentId;
		
		doOneTest(residentId);

		// Verify that we got something in the database as well
        List<Engagement> result = (List<Engagement>) engagementRepository.findAll();
        assertEquals(1, result.size());
        assertThat(result.get(0).getBusinessKey().getRegisteredResidentIdentification(), is(fullResidentId));
        
    }

	/**
	 * Perform a test that is expected to return one hit
	 * @throws JMSException 
	 */
    @Test
    public void update_r1_negative_equal() throws JMSException {
		
		UpdateType request = new UpdateType();
		EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
		request.getEngagementTransaction().add(et1);
		request.getEngagementTransaction().add(et1);

		try {
			// Call the update web service without waiting for an asynch event since we expect the web service to return an error directly without triggering any asynch processing
			new DoOneTestDispatcher(request).doDispatch();
			fail("Expected exception here");

		} catch (javax.xml.ws.soap.SOAPFaultException e) {
			// TODO: Add more SOAP Fault specific tests, can we get the actual SOAP fault XML to validate against???
			assertEquals("javax.xml.ws.soap.SOAPFaultException: EI002: EngagementTransaction #1 and #2 have the same key. That is not allowed. See rule for Update-R1 in service contract", e.toString());
		};
    }

    @Test
    public void update_r7_negative_owner_dont_match_logicaladdress() throws JMSException {
		
		UpdateType request = new UpdateType();
		EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
		request.getEngagementTransaction().add(et1);
		try {
			// Call the update web service without waiting for an asynch event since we expect the web service to return an error directly without triggering any asynch processing
			new DoOneTestDispatcher(INVALID_LOGICAL_ADDRESS,request).doDispatch();
			fail("Expected exception here");

		} catch (javax.xml.ws.soap.SOAPFaultException e) {
			// TODO: Add more SOAP Fault specific tests, can we get the actual SOAP fault XML to validate against???
			assertEquals("javax.xml.ws.soap.SOAPFaultException: EI003: Invalid routing. Logical address is wrongLogicalAddress but the owner is logical-address. They must be the same. See rule for Update-R7 in service contract", e.toString());
		};
    }

    
	/**
	 * Perform a test that is expected to create a timeout
	 * @throws JMSException 
	 */
    @Test
    public void test_error_timeout() throws JMSException {

		doOneTest(ProcessNotificationTestProducer.TEST_ID_FAULT_TIMEOUT);

		System.err.println("### WAIT FOR RETRY HANDLING");
        try {
        	// The test is configured to perform 3 retries so in total 4 attempts, if we wait 5 times the timeout time we should be fins  
			Thread.sleep(4 * SERVICE_TIMOUT_MS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// FIXME check error queues and DL-queue

    }

	private void doOneTest(long in_residentId) throws JMSException {

		// Create a new engagement and call the update web service
		EngagementTransactionType et = GenServiceTestDataUtil.genEngagementTransaction(in_residentId);
    	
		UpdateType request = new UpdateType();
		request.getEngagementTransaction().add(et);
		
		doOneTest(request);

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

		// Use dispatchAndWaitForDelivery() and a custom Dispatcher to ensure that the listener on the notification topic ir regiered before the web service call is made
        
        System.err.println("### DISPATCH AND WAIT FOR DELIVERY ON " + "jms://topic:" + NOTIFICATION_TOPIC);
        MuleMessage r = dispatchAndWaitForDelivery(new DoOneTestDispatcher(request), "jms://topic:" + NOTIFICATION_TOPIC, EndpointMessageNotification.MESSAGE_DISPATCH_END, 5000);

        // Compare the notified message with the request message, they should be the same
        TextMessage jmsMsg = (TextMessage)r.getPayload();
        String notificationXml = jmsMsg.getText();
		String requestXml = jabxUtil.marshal(of.createUpdate(request));
		assertEquals(requestXml, notificationXml);

        // FIXME: Split tests so that both separate parts are tested but also the complete chain and adopt listeners so that they listen to the last endpoint
        try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}