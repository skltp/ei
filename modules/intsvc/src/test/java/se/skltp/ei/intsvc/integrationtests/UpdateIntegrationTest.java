package se.skltp.ei.intsvc.integrationtests;

import org.junit.Test;

import se.skltp.ei.intsvc.EiMuleServer;

import org.soitoolkit.commons.mule.test.junit4.AbstractTestCase;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.update._1.rivtabp21.UpdateResponderInterface;
import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;

public class UpdateIntegrationTest extends AbstractTestCase {

//	@SuppressWarnings("unused")
//	private static final Logger log = LoggerFactory.getLogger(RequestActivitiesIntegrationTest.class);
	 
    private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("ei-config");

    private static final String LOGICAL_ADDRESS = "logical-address";
	private static final String EXPECTED_ERR_TIMEOUT_MSG = "Read timed out";
//	private static final String EXPECTED_ERR_INVALID_ID_MSG = "Invalid Id: " + TEST_RR_ID_FAULT_INVALID_ID;
	private static final String SERVICE_ADDRESS = EiMuleServer.getAddress("UPDATE_WEB_SERVICE_URL");
  
    public UpdateIntegrationTest() {
        // Activate the spring bean definition profile "soitoolkit-integrationtests"
        System.getProperties().put("spring.profiles.active", "soitoolkit-integrationtests");
        
    	 
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
	 * Perform a test that is expected to return zero hits
	 */
    @Test
    public void test_ok() {

		UpdateTestConsumer consumer = new UpdateTestConsumer(SERVICE_ADDRESS);

		UpdateType request = new UpdateType();
		UpdateResponseType response = consumer.callService(LOGICAL_ADDRESS, request);
        
		System.out.println("Returned status = " + response.getResultCode());
        assertEquals(ResultCodeEnum.OK, response.getResultCode());
    }
}
