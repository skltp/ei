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
package se.skltp.ei.intsvc.integrationtests.collect;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mule.api.MuleException;
import org.mule.module.client.MuleClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.intsvc.integrationtests.AbstractTestCase;

public class UpdateServiceCompressIntegrationTest extends AbstractTestCase {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(UpdateServiceCompressIntegrationTest.class);

	private static final JaxbUtil jaxbUtil = new JaxbUtil(UpdateType.class);
	
	@SuppressWarnings("unused")
	private static final long SERVICE_TIMOUT_MS = Long.parseLong(rb.getString("SERVICE_TIMEOUT_MS"));
    
	@SuppressWarnings("unused")
	private static final String EXPECTED_ERR_TIMEOUT_MSG = "Read timed out";
	
	public UpdateServiceCompressIntegrationTest() {
        // Only start up Mule once to make the tests run faster...
        // Set to false if tests interfere with each other when Mule is started only once.
        setDisposeContextPerClass(true);
    }

    protected String getConfigResources() {
    	System.setProperty("COLLECT_TRESHOLD", "1");
    	System.setProperty("COLLECT_JMS_RECEIVE_TIMEOUT_MILLIS", "3000");
    	System.setProperty("COLLECT_MAX_BUFFER_AGE_MILLIS", "4000");
    	System.setProperty("COLLECT_MAX_RECORDS_IN_COLLECTED_MESSAGES", "2");
    	System.setProperty("COLLECT_MAX_BUFFERED_RECORDS", "10");
		return 
			"soitoolkit-mule-jms-connector-activemq-embedded.xml," + 
	  		"ei-common.xml," +
	  		"update-service.xml," +
	        "update-collect-service.xml";
    }

    @Before
    public void setUp() throws Exception {
    	// Clear queues used for the tests
		getJmsUtil().clearQueues(INFO_LOG_QUEUE, ERROR_LOG_QUEUE, PROCESS_QUEUE, COLLECT_QUEUE);
    }


    @After
    public void tearDown() throws Exception {
    	// Set back original values
    	System.clearProperty("COLLECT_TRESHOLD");
    	System.clearProperty("COLLECT_JMS_RECEIVE_TIMEOUT_MILLIS");
    	System.clearProperty("COLLECT_MAX_BUFFER_AGE_MILLIS");
    	System.clearProperty("COLLECT_MAX_RECORDS_IN_COLLECTED_MESSAGES");
    	System.clearProperty("COLLECT_MAX_BUFFERED_RECORDS");
    }
    
    /**
	 * Validate expected behavior of the compress-service with 5 unique posts resulting in 3 messages
	 */
    @Test
    public void compress_5_posts_to_3_messages_OK() {
    			
		List<String> requestList = new ArrayList<String>();
		requestList.add(createUpdateTextMessage("", false, 1212121212L));
		requestList.add(createUpdateTextMessage("", false, 1212121213L));
		requestList.add(createUpdateTextMessage("", false, 1212121214L));
		requestList.add(createUpdateTextMessage("", false, 1212121215L));
		requestList.add(createUpdateTextMessage("", false, 1212121216L));

		// Send messages to JMS queue
		sendMessagesToCollectQueue(requestList);
		
		// Expect to read three messages
		String message1 = getJmsUtil().consumeOneTextMessage(PROCESS_QUEUE, 10000);
		assert( ((UpdateType)jaxbUtil.unmarshal(message1)).getEngagementTransaction().size() == 2);
		
		String message2 = getJmsUtil().consumeOneTextMessage(PROCESS_QUEUE, 10000);
		assert( ((UpdateType)jaxbUtil.unmarshal(message2)).getEngagementTransaction().size() == 2);
		
		String message3 = getJmsUtil().consumeOneTextMessage(PROCESS_QUEUE, 10000);
		assert( ((UpdateType)jaxbUtil.unmarshal(message3)).getEngagementTransaction().size() == 1);
    }

    /**
	 * Validate expected behavior of the compress-service with 5 posts with different most_recent_times resulting in 1 message
	 */
    @Test
    public void compress_5_posts_to_1_message_OK() {
    			
		List<String> requestList = new ArrayList<String>();
		requestList.add(createUpdateTextMessage("20150610120001", false, 1212121212L));
		requestList.add(createUpdateTextMessage("20150611120001", false, 1212121212L));
		requestList.add(createUpdateTextMessage("20150609120001", false, 1212121212L));
		requestList.add(createUpdateTextMessage("20150611120002", false, 1212121212L));
		requestList.add(createUpdateTextMessage("20150610120002", false, 1212121212L));

		// Send messages to JMS queue
		sendMessagesToCollectQueue(requestList);
		
		// Wait for messages to appear on PROCESS_QUEUE
		String message = getJmsUtil().consumeOneTextMessage(PROCESS_QUEUE, 10000);
		
		UpdateType update = (UpdateType)jaxbUtil.unmarshal(message);

		assert(update.getEngagementTransaction().get(0).getEngagement().getMostRecentContent().equalsIgnoreCase("20150611120002"));				
    }

    /**
	 * Validate expected behavior of the compress-service with 5 posts with a delete in the middle that overrides later updates!
	 */
    @Test
    public void compress_5_posts_to_1_message_with_delete_OK() {
    			
		List<String> requestList = new ArrayList<String>();
		requestList.add(createUpdateTextMessage("20150610120001", false, 1212121212L));
		requestList.add(createUpdateTextMessage("20150611120001", false, 1212121212L));
		requestList.add(createUpdateTextMessage("20150609120001", true, 1212121212L));
		requestList.add(createUpdateTextMessage("20150611120002", false, 1212121212L));
		requestList.add(createUpdateTextMessage("20150610120002", false, 1212121212L));

		// Send messages to JMS queue
		sendMessagesToCollectQueue(requestList);
		
		// Wait for messages to appear on PROCESS_QUEUE
		String message = getJmsUtil().consumeOneTextMessage(PROCESS_QUEUE, 10000);
		
		UpdateType update = (UpdateType)jaxbUtil.unmarshal(message);

		assert(update.getEngagementTransaction().get(0).isDeleteFlag());
    }

    /**
	 * Validate expected behavior of the compress-service with 5 posts and null most_recent_time data!
	 */
    @Test
    public void compress_2_posts_to_1_message_with_null_most_recent_time_last_OK() {
    			
		List<String> requestList = new ArrayList<String>();
		requestList.add(createUpdateTextMessage("20150611120001", false, 1212121212L));
		requestList.add(createUpdateTextMessage("", false, 1212121212L));

		// Send messages to JMS queue
		sendMessagesToCollectQueue(requestList);
		
		// Wait for messages to appear on PROCESS_QUEUE
		String message = getJmsUtil().consumeOneTextMessage(PROCESS_QUEUE, 10000);
		
		UpdateType update = (UpdateType)jaxbUtil.unmarshal(message);

		assert(update.getEngagementTransaction().get(0).getEngagement().getMostRecentContent().equalsIgnoreCase("20150611120001"));				
    }

    /**
	 * Validate expected behavior of the compress-service with 5 posts and null most_recent_time data!
	 */
    @Test
    public void compress_2_posts_to_1_message_with_null_most_recent_time_first_OK() {
    			
		List<String> requestList = new ArrayList<String>();
		requestList.add(createUpdateTextMessage("", false, 1212121212L));
		requestList.add(createUpdateTextMessage("20150611120001", false, 1212121212L));

		// Send messages to JMS queue
		sendMessagesToCollectQueue(requestList);
		
		// Wait for messages to appear on PROCESS_QUEUE
		String message = getJmsUtil().consumeOneTextMessage(PROCESS_QUEUE, 10000);
		
		UpdateType update = (UpdateType)jaxbUtil.unmarshal(message);

		assert(update.getEngagementTransaction().get(0).getEngagement().getMostRecentContent().equalsIgnoreCase("20150611120001"));				
    }

    
    private void sendMessagesToCollectQueue(List<String> requestList) {
		try {
				// First create a muleClient instance
			MuleClient muleClient = new MuleClient(muleContext);
	
			// Perform the actual dispatch
			for (String request : requestList) {
				muleClient.dispatch("jms://" + COLLECT_QUEUE, request, null);
			}
		} catch (MuleException e) {
			throw new RuntimeException(e);
		}
    }
}