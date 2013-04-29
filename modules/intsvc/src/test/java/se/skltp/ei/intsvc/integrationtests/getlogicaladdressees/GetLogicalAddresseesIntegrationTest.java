package se.skltp.ei.intsvc.integrationtests.getlogicaladdressees;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;

import riv.itintegration.registry.getlogicaladdresseesbyservicecontractresponder._1.GetLogicalAddresseesByServiceContractResponseType;
import se.skltp.ei.intsvc.EiMuleServer;
import se.skltp.ei.intsvc.integrationtests.AbstractTestCase;

public class GetLogicalAddresseesIntegrationTest extends AbstractTestCase {

//	@SuppressWarnings("unused")
//	private static final Logger log = LoggerFactory.getLogger(RequestActivitiesIntegrationTest.class);
	 
    @SuppressWarnings("unused")
	private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("ei-config");

    @SuppressWarnings("unused")
	private static final String LOGICAL_ADDRESS = "logical-address";
	@SuppressWarnings("unused")
	private static final String EXPECTED_ERR_TIMEOUT_MSG = "Read timed out";
//	private static final String EXPECTED_ERR_INVALID_ID_MSG = "Invalid Id: " + TEST_RR_ID_FAULT_INVALID_ID;
	@SuppressWarnings("unused")
	private static final String SERVICE_ADDRESS = EiMuleServer.getAddress("FIND_CONTENT_WEB_SERVICE_URL");
  
    public GetLogicalAddresseesIntegrationTest() {
        // Only start up Mule once to make the tests run faster...
        // Set to false if tests interfere with each other when Mule is started only once.
        setDisposeContextPerClass(true);
    }

    protected String getConfigResources() {
		return 
			"soitoolkit-mule-jms-connector-activemq-embedded.xml," + 
	  		"ei-common.xml," +
	        "get-logical-addressees-service.xml," +
			"teststub-services/get-logical-addressees-by-service-contract-teststub-service.xml";
    }

    @Before
    public void setUp() throws Exception {

    	// Clear queues used for the tests
		getJmsUtil().clearQueues(INFO_LOG_QUEUE, ERROR_LOG_QUEUE);
    }

    /**
	 * Perform a test that is expected to return one hit
     * @throws MuleException 
	 */
    @Test
    public void getLogicalAddresses_Ok() throws MuleException {
    	
    	MuleMessage response = muleContext.getClient().send("vm://get-logical-addressees", "", null);
    	GetLogicalAddresseesByServiceContractResponseType logicalAddresses = (GetLogicalAddresseesByServiceContractResponseType)response.getPayload();

    	assertEquals(3, logicalAddresses.getLogicalAddress().size());

    	// Expect no error logs and four info log entries
		assertQueueDepth(ERROR_LOG_QUEUE, 0);
		assertQueueDepth(INFO_LOG_QUEUE, 4);
    }
}