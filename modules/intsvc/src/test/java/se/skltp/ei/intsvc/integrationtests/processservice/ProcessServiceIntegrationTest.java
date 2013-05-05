/**
 * Copyright (c) 2013 Sveriges Kommuner och Landsting (SKL). <http://www.skl.se/>
 *
 * This file is part of SKLTP.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package se.skltp.ei.intsvc.integrationtests.processservice;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mule.api.MuleMessage;
import org.mule.context.notification.EndpointMessageNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;

import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import riv.itintegration.engagementindex.updateresponder._1.ObjectFactory;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.intsvc.integrationtests.AbstractTestCase;
import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.entity.repository.EngagementRepository;

public class ProcessServiceIntegrationTest extends AbstractTestCase implements MessageListener {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(ProcessServiceIntegrationTest.class);
	 
	private static final JaxbUtil jabxUtil = new JaxbUtil(UpdateType.class, ProcessNotificationType.class);
	private static final ObjectFactory update_of = new ObjectFactory();
	private static final riv.itintegration.engagementindex.processnotificationresponder._1.ObjectFactory processNotification_of = new riv.itintegration.engagementindex.processnotificationresponder._1.ObjectFactory();
	
    private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("ei-config");

	
	private static final String PROCESS_QUEUE = rb.getString("PROCESS_QUEUE");
	private static final String NOTIFY_TOPIC = rb.getString("NOTIFY_TOPIC");
    
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
    public void process_update_OK() throws JMSException {
    	
    	// Setup testdata
		long residentId = 1212121212L;
		String fullResidentId = "19" + residentId;
		String requestXml = jabxUtil.marshal(update_of.createUpdate(createUdateRequest(residentId)));

		// Setup a test-subscriber on the notification-topic
		TopicSession topicSession = getJmsUtil().getTopicSession();
		Topic topic = topicSession.createTopic(NOTIFY_TOPIC);
		TopicSubscriber topicSubscriber = topicSession.createSubscriber(topic);
		topicSubscriber.setMessageListener(this);

		// Send an update message to the process-service and wait for a publish on the notification topic
		MuleMessage response = dispatchAndWaitForDelivery("jms://" + PROCESS_QUEUE + "?connector=soitoolkit-jms-connector", requestXml, null, "jms://topic:" + NOTIFY_TOPIC, EndpointMessageNotification.MESSAGE_DISPATCH_END, EI_TEST_TIMEOUT);

        // Compare the notified message with the request message, they should be the same
		assertRequest(requestXml, response);

		// Verify that we got something in the database as well
        List<Engagement> result = (List<Engagement>) engagementRepository.findAll();
        assertEquals(1, result.size());
        assertThat(result.get(0).getBusinessKey().getRegisteredResidentIdentification(), is(fullResidentId));

		// Expect no error logs and three info log entries
		assertQueueDepth(ERROR_LOG_QUEUE, 0);
		assertQueueDepth(INFO_LOG_QUEUE, 2);

		// Assert that the response is the only message on the queue
		assertQueueDepth(PROCESS_QUEUE, 0);
		
		// Finally verify that we got the expected notification to our own subscriber
		assertNotNull("No processNotification received", processNotificationMessage);
		assertRequest(requestXml, processNotificationMessage);
    }

	// FIXME - ML: Add PN tests with and without owner = me!

	/**
	 * Perform a test that is expected to return one hit
	 * @throws JMSException 
	 */
    // TODO. Patrik. Remove Ignore annotation once business logic is in place.
    @Ignore
    @Test
    public void process_notification_OK() throws JMSException {
    	
    	// Setup testdata
		long residentId = 1212121212L;
		String fullResidentId = "19" + residentId;
		String requestXml = jabxUtil.marshal(processNotification_of.createProcessNotification(createProcessNotificationRequest(residentId)));

		// Setup a test-subscriber on the notification-topic
		TopicSession topicSession = getJmsUtil().getTopicSession();
		Topic topic = topicSession.createTopic(NOTIFY_TOPIC);
		TopicSubscriber topicSubscriber = topicSession.createSubscriber(topic);
		topicSubscriber.setMessageListener(this);

		// Send an update message to the process-service and wait for a publish on the notification topic
		MuleMessage response = dispatchAndWaitForDelivery("jms://" + PROCESS_QUEUE + "?connector=soitoolkit-jms-connector", requestXml, null, "jms://topic:" + NOTIFY_TOPIC, EndpointMessageNotification.MESSAGE_DISPATCH_END, EI_TEST_TIMEOUT);

        // Compare the notified message with the request message, they should be the same
		assertRequest(requestXml, response);

		// Verify that we got something in the database as well
        List<Engagement> result = (List<Engagement>) engagementRepository.findAll();
        assertEquals(1, result.size());
        assertThat(result.get(0).getBusinessKey().getRegisteredResidentIdentification(), is(fullResidentId));

		// Expect no error logs and three info log entries
		assertQueueDepth(ERROR_LOG_QUEUE, 0);
		assertQueueDepth(INFO_LOG_QUEUE, 2);

		// Assert that the response is the only message on the queue
		assertQueueDepth(PROCESS_QUEUE, 0);
		
		// Finally verify that we got the expected notification to our own subscriber
		assertNotNull("No processNotification received", processNotificationMessage);
		assertRequest(requestXml, processNotificationMessage);
    }

    
    // TODO - Implement (negative) tests for testing failure and resending update_of notifications
    
    
    private TextMessage processNotificationMessage = null;

    @Override
	public void onMessage(Message message) {
    	processNotificationMessage = (TextMessage)message;
	}
}