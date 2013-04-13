package se.skltp.ei.intsvc.integrationtests;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mule.api.registry.RegistrationException;
import org.soitoolkit.commons.mule.test.junit4.AbstractTestCase;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;

import riv.itintegration.engagementindex.findcontentresponder._1.FindContentResponseType;
import riv.itintegration.engagementindex.findcontentresponder._1.FindContentType;
import se.skltp.ei.intsvc.EiMuleServer;
import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.entity.repository.EngagementRepository;

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
    	
    	// Insert one entity
		Engagement engagement = new Engagement();
		engagement.setBusinessKey(residentId,
				serviceDomain,
				categorization,
				logicalAddress,
				businessObjectInstanceIdentifier,
    			sourceSystem,
    			owner, 
    			null);
    	engagementRepository.save(engagement);
	}

	/**
	 * Perform a test that is expected to return one hit
	 * @throws RegistrationException 
	 */
    @Test
    public void test_ok() throws RegistrationException {
        
        FindContentTestConsumer consumer = new FindContentTestConsumer(SERVICE_ADDRESS);

		FindContentType request = new FindContentType();
		request.setBusinessObjectInstanceIdentifier(businessObjectInstanceIdentifier);
		request.setCategorization(categorization);
		request.setLogicalAddress(logicalAddress);
		request.setRegisteredResidentIdentification(residentId);
		request.setServiceDomain(serviceDomain);
		request.setSourceSystem(sourceSystem);
		request.setOwner(owner);

		FindContentResponseType response = consumer.callService(LOGICAL_ADDRESS, request);
        
        assertEquals(1, response.getEngagement().size());
    }
}