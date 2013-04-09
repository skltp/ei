package se.skltp.ei.intsvc.integrationtests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.soitoolkit.commons.mule.test.junit4.AbstractTestCase;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;

import riv.itintegration.engagementindex.findcontentresponder._1.FindContentResponseType;
import riv.itintegration.engagementindex.findcontentresponder._1.FindContentType;
import se.skltp.ei.intsvc.EiMuleServer;

public class FindContentIntegrationTest extends AbstractTestCase {

//	@SuppressWarnings("unused")
//	private static final Logger log = LoggerFactory.getLogger(RequestActivitiesIntegrationTest.class);
	 
    private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("ei-config");

    private static final String LOGICAL_ADDRESS = "logical-address";
	private static final String EXPECTED_ERR_TIMEOUT_MSG = "Read timed out";
//	private static final String EXPECTED_ERR_INVALID_ID_MSG = "Invalid Id: " + TEST_RR_ID_FAULT_INVALID_ID;
	private static final String SERVICE_ADDRESS = EiMuleServer.getAddress("FIND_CONTENT_WEB_SERVICE_URL");
  
    public FindContentIntegrationTest() {
        // Only start up Mule once to make the tests run faster...
        // Set to false if tests interfere with each other when Mule is started only once.
        setDisposeContextPerClass(true);
    }

    protected String getConfigResources() {
		return 
			"soitoolkit-mule-jms-connector-activemq-embedded.xml," + 
	  		"ei-common.xml," +
	  		"skltp-ei-svc-spring-context.xml," +
	        "find-content-service.xml";
    }

	/**
	 * Perform a test that is expected to return zero hits
	 */
    @Test
    public void test_ok() {

		FindContentTestConsumer consumer = new FindContentTestConsumer(SERVICE_ADDRESS);

		FindContentType request = new FindContentType();
		FindContentResponseType response = consumer.callService(LOGICAL_ADDRESS, request);
        
		System.out.println("Returned status = " + response.getEngagement().size());
        assertEquals(0, response.getEngagement().size());
    }
}
