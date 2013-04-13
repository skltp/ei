package se.skltp.ei.intsvc.integrationtests;

import static org.junit.Assert.assertEquals;

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
import se.skltp.ei.svc.entity.repository.EngagementRepository;

public class UpdateIntegrationTest extends AbstractTestCase {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(UpdateIntegrationTest.class);
	 
	private static final JaxbUtil jabxUtil = new JaxbUtil(UpdateType.class);
	private static final ObjectFactory of = new ObjectFactory();
	
    private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("ei-config");

    
    private static final String NOTIFICATION_TOPIC = rb.getString("NOTIFICATION_TOPIC");
    private static final String LOGICAL_ADDRESS = "logical-address";
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
	        "update-service.xml";
    }

    // FIXME. Move to a common test-util in svc
    private EngagementRepository engagementRepository;

    static private String businessObjectInstanceIdentifier = "boi";
    static private String categorization = "categorization";
    static private String logicalAddress = "logicalAddress";
    static String residentId = "191212121212";
    static String owner = "HSA-001";
    static String serviceDomain = "urn:riv:healthprocess:test";
    static String sourceSystem = "sourceSystem";

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
	 * Perform a test that is expected to return zero hits
	 * @throws JMSException 
	 */
    @Test
    public void test_ok() throws JMSException {
		
		EngagementType engagement = new EngagementType();
        engagement.setBusinessObjectInstanceIdentifier(businessObjectInstanceIdentifier);
    	engagement.setCategorization(categorization);
    	engagement.setLogicalAddress(logicalAddress);
        engagement.setBusinessObjectInstanceIdentifier(businessObjectInstanceIdentifier);
        engagement.setRegisteredResidentIdentification(residentId);
        engagement.setServiceDomain(serviceDomain);
        engagement.setSourceSystem(sourceSystem);
        engagement.setOwner(owner);

    	
    	EngagementTransactionType et = new EngagementTransactionType();
    	et.setDeleteFlag(false);
    	et.setEngagement(engagement);
    	
		UpdateType request = new UpdateType();
		request.getEngagementTransaction().add(et);

		UpdateTestConsumer consumer = new UpdateTestConsumer(SERVICE_ADDRESS);

		UpdateResponseType response = consumer.callService(LOGICAL_ADDRESS, request);
        
        assertEquals(ResultCodeEnum.OK, response.getResultCode());
        
        // FIXME: Create a version of dispatchAndWaitForDelivery in soi-toolkit where no message is required to be sent like the existing method waitForServiceComponent()
        MuleMessage r = dispatchAndWaitForDelivery("jms://foo?connector=soitoolkit-jms-connector", "", null, "jms://topic:" + NOTIFICATION_TOPIC, EndpointMessageNotification.MESSAGE_DISPATCH_END, 3000);

        // Compare the notified message with the request message, they shouls be the same
        TextMessage jmsMsg = (TextMessage)r.getPayload();
        String notificationXml = jmsMsg.getText();
		String requestXml = jabxUtil.marshal(of.createUpdate(request));
		assertEquals(requestXml, notificationXml);

		// Verify that we got something in the database as well
        assertEquals(1, engagementRepository.count());
    }

}