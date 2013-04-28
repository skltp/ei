package se.skltp.ei.intsvc.integrationtests.updateservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.jms.JMSException;
import javax.jms.TextMessage;

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
import se.skltp.ei.svc.service.GenServiceTestDataUtil;

public class UpdateServiceIntegrationTest extends AbstractTestCase {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(UpdateServiceIntegrationTest.class);
	 
	private static final JaxbUtil jabxUtil = new JaxbUtil(UpdateType.class);
	private static final ObjectFactory of = new ObjectFactory();
	
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

	/**
	 * Perform a test that is expected to return one hit
	 * 
	 * END-TO-END + US
	 * 
	 * @throws JMSException 
	 */
    @Test
    public void update_OK() throws JMSException {
    	
		long residentId = 1212121212L;
		doOneTest(residentId);

    }

	/**
	 * Perform a test that is expected to return one hit
	 * 
	 * @throws JMSException 
	 */
    @Test
    public void update_R1_ERR_duplicate_tx_found() throws JMSException {
		
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

    /**
     * 
     * @throws JMSException
     */
    @Test
    public void update_R7_ERR_owner_dont_match_logicaladdress() throws JMSException {
		
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
        
        System.err.println("### DISPATCH AND WAIT FOR DELIVERY ON " + "jms://" + PROCESS_QUEUE);
        MuleMessage r = dispatchAndWaitForDelivery(new DoOneTestDispatcher(request), "jms://" + PROCESS_QUEUE, EndpointMessageNotification.MESSAGE_DISPATCH_END, 5000);

        // Compare the notified message with the request message, they should be the same
        TextMessage jmsMsg = (TextMessage)r.getPayload();
        String notificationXml = jmsMsg.getText();
		String requestXml = jabxUtil.marshal(of.createUpdate(request));
		assertEquals(requestXml, notificationXml);
	}

}