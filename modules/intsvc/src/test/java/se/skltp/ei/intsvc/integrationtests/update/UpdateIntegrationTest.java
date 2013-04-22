package se.skltp.ei.intsvc.integrationtests.update;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

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
import org.soitoolkit.commons.mule.test.junit4.AbstractTestCase;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.EngagementType;
import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.updateresponder._1.ObjectFactory;
import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.intsvc.EiMuleServer;
import se.skltp.ei.intsvc.integrationtests.processnotification.ProcessNotificationTestProducer;
import se.skltp.ei.svc.entity.GenTestDataUtil;
import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.entity.repository.EngagementRepository;
import se.skltp.ei.svc.service.impl.util.EntityTransformer;

public class UpdateIntegrationTest extends AbstractTestCase {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(UpdateIntegrationTest.class);
	 
	private static final JaxbUtil jabxUtil = new JaxbUtil(UpdateType.class);
	private static final ObjectFactory of = new ObjectFactory();
	
    private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("ei-config");

	private static final long SERVICE_TIMOUT_MS = Long.parseLong(rb.getString("SERVICE_TIMEOUT_MS"));
    
    private static final String NOTIFICATION_TOPIC = rb.getString("NOTIFICATION_TOPIC");
    private static final String LOGICAL_ADDRESS = "logical-address";
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
		
		doOneTest(1212121212L);
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
		Engagement entity = GenTestDataUtil.genEngagement(in_residentId);
		EngagementType engagement = EntityTransformer.fromEntity(entity);
    	EngagementTransactionType et = new EngagementTransactionType();
    	et.setDeleteFlag(false);
    	et.setEngagement(engagement);
    	
		UpdateType request = new UpdateType();
		request.getEngagementTransaction().add(et);

		UpdateTestConsumer consumer = new UpdateTestConsumer(SERVICE_ADDRESS);

		UpdateResponseType response = consumer.callService(LOGICAL_ADDRESS, request);
        
		// Assert OK response from the web service
        assertEquals(ResultCodeEnum.OK, response.getResultCode());
        
        // FIXME: Create a version of dispatchAndWaitForDelivery in soi-toolkit where no message is required to be sent like the existing method waitForServiceComponent()
        System.err.println("### WAIT FOR DELIVERY ON " + "jms://topic:" + NOTIFICATION_TOPIC);
        MuleMessage r = dispatchAndWaitForDelivery("jms://foo?connector=soitoolkit-jms-connector", "", null, "jms://topic:" + NOTIFICATION_TOPIC, EndpointMessageNotification.MESSAGE_DISPATCH_END, 5000);

        // Compare the notified message with the request message, they should be the same
        TextMessage jmsMsg = (TextMessage)r.getPayload();
        String notificationXml = jmsMsg.getText();
		String requestXml = jabxUtil.marshal(of.createUpdate(request));
		assertEquals(requestXml, notificationXml);

		// Verify that we got something in the database as well
        List<Engagement> result = (List<Engagement>) engagementRepository.findAll();
        assertEquals(1, result.size());
        assertThat(result.get(0).getBusinessKey().getRegisteredResidentIdentification(), is(engagement.getRegisteredResidentIdentification()));
        
        // FIXME: Split tests so that both separate parts are tested but also the complete chain and adopt listeners so that they listen to the last endpoint
        try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}