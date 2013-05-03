package se.skltp.ei.intsvc.integrationtests.findcontentservice;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mule.api.registry.RegistrationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;

import riv.itintegration.engagementindex._1.EngagementType;
import riv.itintegration.engagementindex.findcontentresponder._1.FindContentResponseType;
import riv.itintegration.engagementindex.findcontentresponder._1.FindContentType;
import se.skltp.ei.intsvc.EiMuleServer;
import se.skltp.ei.intsvc.integrationtests.AbstractTestCase;

import se.skltp.ei.svc.entity.GenEntityTestDataUtil;
import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.entity.repository.EngagementRepository;
import se.skltp.ei.svc.service.impl.FindContentBean;
import sun.print.ServiceDialog;

public class FindContentServiceIntegrationTest extends AbstractTestCase {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(FindContentServiceIntegrationTest.class);

    @SuppressWarnings("unused")
    private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("ei-config");

    private static final String LOGICAL_ADDRESS = "logical-address";
    @SuppressWarnings("unused")
    private static final String EXPECTED_ERR_TIMEOUT_MSG = "Read timed out";
    //	private static final String EXPECTED_ERR_INVALID_ID_MSG = "Invalid Id: " + TEST_RR_ID_FAULT_INVALID_ID;
    private static final String SERVICE_ADDRESS = EiMuleServer.getAddress("FIND_CONTENT_WEB_SERVICE_URL");

    public FindContentServiceIntegrationTest() {
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

    private EngagementRepository engagementRepository;
    private String residentId = null;
    private String serviceDomain = null;

    @Before
    public void setUp() throws Exception {

        // Lookup the entity repository if not already done
        if (engagementRepository == null) {
            engagementRepository = muleContext.getRegistry().lookupObject(EngagementRepository.class);
        }

        // Clean the storage
        engagementRepository.deleteAll();

        // Insert one entity
        Engagement engagement  = GenEntityTestDataUtil.genEngagement(1212121212L);
        this.residentId = engagement.getBusinessKey().getRegisteredResidentIdentification();
        this.serviceDomain = engagement.getBusinessKey().getServiceDomain();

        engagementRepository.save(engagement);

        // Clear queues used for the tests
        getJmsUtil().clearQueues(INFO_LOG_QUEUE, ERROR_LOG_QUEUE);
    }

    /**
     * Perform a test that is expected to return one hit
     * @throws RegistrationException 
     */
    @Test
    public void findContent_OK() throws RegistrationException {

        FindContentTestConsumer consumer = new FindContentTestConsumer(SERVICE_ADDRESS);

        FindContentType request = new FindContentType();
        request.setRegisteredResidentIdentification(residentId);
        request.setServiceDomain(serviceDomain);

        FindContentResponseType response = consumer.callService(LOGICAL_ADDRESS, request);

        assertEquals(1, response.getEngagement().size());
        assertThat(response.getEngagement().get(0).getRegisteredResidentIdentification(), is(residentId));

        // Expect no error logs and two info log entries
        assertQueueDepth(ERROR_LOG_QUEUE, 0);
        assertQueueDepth(INFO_LOG_QUEUE, 2);

    }
    
    /**
     * Performs a test without registeredResidentIdentification and serviceDomain that is 
     * expected to throw an Exception
     */
    @Test
    public void findContent_ERR_R1_validate_findContent() {
    	
    	FindContentTestConsumer consumer = new FindContentTestConsumer(SERVICE_ADDRESS);
        FindContentType request = new FindContentType();
        String expectedErrorMessage = "EI000: A technical error has occurred, error message: registeredResidentIdentification is mandatory but missing";
        
        try {
			FindContentResponseType response = consumer.callService(LOGICAL_ADDRESS, request);
			fail("Test failed");
		} catch (javax.xml.ws.soap.SOAPFaultException e) {
			// TODO: Add more SOAP Fault specific tests, can we get the actual SOAP fault XML to validate against???
			
			System.out.println(e.getMessage());
			assertEquals("javax.xml.ws.soap.SOAPFaultException: " + expectedErrorMessage, e.toString());
		}

    }
    
    
    /**
     * Tests that we get only one of multiple engagements and that uses multiple query parameters
     */
    @Test 
    public void findContent_OK_R2_find_engagements() {
    	
    	// Insert another entity to the data store
        Engagement engagement  = GenEntityTestDataUtil.genEngagement(1111111111L);
        this.residentId = engagement.getBusinessKey().getRegisteredResidentIdentification();
        this.serviceDomain = engagement.getBusinessKey().getServiceDomain();

        engagementRepository.save(engagement);
        engagementRepository.flush();
        
        FindContentTestConsumer consumer = new FindContentTestConsumer(SERVICE_ADDRESS);

        FindContentType request = new FindContentType();
        request.setRegisteredResidentIdentification(residentId);
        request.setServiceDomain(serviceDomain);
        request.setCategorization(engagement.getCategorization());
        request.setClinicalProcessInterestId(engagement.getClinicalProcessInterestId());
        request.setLogicalAddress(engagement.getLogicalAddress());
        request.setBusinessObjectInstanceIdentifier(engagement.getBusinessObjectInstanceIdentifier());
        request.setSourceSystem(engagement.getSourceSystem());
        request.setDataController(engagement.getDataController());
        request.setOwner(engagement.getOwner());
        
        
        FindContentResponseType response = consumer.callService(LOGICAL_ADDRESS, request);

        assertEquals(1, response.getEngagement().size());
        assertThat(response.getEngagement().get(0).getRegisteredResidentIdentification(), is(residentId));        
        
        // Expect no error logs and two info log entries
        assertQueueDepth(ERROR_LOG_QUEUE, 0);
        assertQueueDepth(INFO_LOG_QUEUE, 2);

    }

}
