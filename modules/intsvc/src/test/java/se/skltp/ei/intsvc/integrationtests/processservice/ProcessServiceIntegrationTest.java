package se.skltp.ei.intsvc.integrationtests.processservice;

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
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;

import riv.itintegration.engagementindex.updateresponder._1.ObjectFactory;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.intsvc.integrationtests.AbstractTestCase;
import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.entity.repository.EngagementRepository;

public class ProcessServiceIntegrationTest extends AbstractTestCase {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(ProcessServiceIntegrationTest.class);
	 
	private static final JaxbUtil jabxUtil = new JaxbUtil(UpdateType.class);
	private static final ObjectFactory of = new ObjectFactory();
	
    private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("ei-config");

	
	private static final String PROCESS_QUEUE = rb.getString("PROCESS_QUEUE");
	private static final String NOTIFICATION_TOPIC = rb.getString("NOTIFICATION_TOPIC");
    
	@SuppressWarnings("unused")
	private static final String EXPECTED_ERR_TIMEOUT_MSG = "Read timed out";
//	private static final String EXPECTED_ERR_INVALID_ID_MSG = "Invalid Id: " + TEST_RR_ID_FAULT_INVALID_ID;
  
    public ProcessServiceIntegrationTest() {
        // Only start up Mule once to make the tests run faster...
        // Set to false if tests interfere with each other when Mule is started only once.
        setDisposeContextPerClass(true);
    }

    protected String getConfigResources() {
		return 
			"soitoolkit-mule-jms-connector-activemq-embedded.xml," + 
	  		"ei-common.xml," +
	  		"skltp-ei-svc-spring-context.xml," +
	        "process-service.xml";
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

    	// Clear queues used for the tests
		getJmsUtil().clearQueues(INFO_LOG_QUEUE, ERROR_LOG_QUEUE, PROCESS_QUEUE);
	}

	/**
	 * Perform a test that is expected to return one hit
	 * @throws JMSException 
	 */
    @Test
    public void process_OK() throws JMSException {
    	
		long residentId = 1212121212L;
		String fullResidentId = "19" + residentId;
		String requestXml = jabxUtil.marshal(of.createUpdate(createUdateRequest(residentId)));

		MuleMessage r = dispatchAndWaitForDelivery("jms://" + PROCESS_QUEUE + "?connector=soitoolkit-jms-connector", requestXml, null, "jms://topic:" + NOTIFICATION_TOPIC, EndpointMessageNotification.MESSAGE_DISPATCH_END, EI_TEST_TIMEOUT);

        // Compare the notified message with the request message, they should be the same
        TextMessage jmsMsg = (TextMessage)r.getPayload();
        String notificationXml = jmsMsg.getText();
		assertEquals(requestXml, notificationXml);

		// Verify that we got something in the database as well
        List<Engagement> result = (List<Engagement>) engagementRepository.findAll();
        assertEquals(1, result.size());
        assertThat(result.get(0).getBusinessKey().getRegisteredResidentIdentification(), is(fullResidentId));

		// Expect no error logs and three info log entries
		assertQueueDepth(ERROR_LOG_QUEUE, 0);
		assertQueueDepth(INFO_LOG_QUEUE, 2);

		// Assert that the response is the only message on the queue
		assertQueueDepth(PROCESS_QUEUE, 0);
    }

}