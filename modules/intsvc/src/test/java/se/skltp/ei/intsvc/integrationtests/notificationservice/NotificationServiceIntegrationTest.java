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
package se.skltp.ei.intsvc.integrationtests.notificationservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.jms.JMSException;

import org.junit.Before;
import org.junit.Test;
import org.mule.api.MuleMessage;
import org.mule.context.notification.EndpointMessageNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.test.Dispatcher;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationResponseType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import se.skltp.ei.intsvc.EiMuleServer;
import se.skltp.ei.intsvc.integrationtests.AbstractTestCase;
import se.skltp.ei.svc.service.GenServiceTestDataUtil;
import se.skltp.ei.svc.service.impl.ProcessBean;

public class NotificationServiceIntegrationTest extends AbstractTestCase {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(NotificationServiceIntegrationTest.class);

	@SuppressWarnings("unused")
	private static final long SERVICE_TIMOUT_MS = Long.parseLong(rb.getString("SERVICE_TIMEOUT_MS"));

	private static final String LOGICAL_ADDRESS = rb.getString("EI_HSA_ID");
	private static final String OWNER = rb.getString("EI_HSA_ID");

	@SuppressWarnings("unused")
	private static final String EXPECTED_ERR_TIMEOUT_MSG = "Read timed out";
	//	private static final String EXPECTED_ERR_INVALID_ID_MSG = "Invalid Id: " + TEST_RR_ID_FAULT_INVALID_ID;
	private static final String SERVICE_ADDRESS = EiMuleServer.getAddress("NOTIFICATION_WEB_SERVICE_URL");

	public NotificationServiceIntegrationTest() {
		// Only start up Mule once to make the tests run faster...
		// Set to false if tests interfere with each other when Mule is started only once.
		setDisposeContextPerClass(false);
	}

	protected String getConfigResources() {
		return
				"soitoolkit-mule-jms-connector-activemq-embedded.xml," +
						"ei-common.xml," +
						"skltp-ei-svc-spring-context.xml," +
						"notification-service.xml";
	}

	@Before
	public void setUp() throws Exception {

		// Clear queues used for the tests
		getJmsUtil().clearQueues(INFO_LOG_QUEUE, ERROR_LOG_QUEUE, PROCESS_QUEUE);
	}

	/**
	 * Validate expected behavior of the notification-service with OK input
	 */
	@Test
	public void notification_OK() {

		ProcessNotificationType request = createProcessNotificationRequest(1212121212L);

		// Use dispatchAndWaitForDelivery() and a custom Dispatcher to ensure that the listener on the queue is registered before the web service call is made
		MuleMessage response = dispatchAndWaitForDelivery(new DoOneTestDispatcher(request), "jms://" + PROCESS_QUEUE, EndpointMessageNotification.MESSAGE_DISPATCH_END, EI_TEST_TIMEOUT);

		// Compare the notified message with the request message, they should be the same
		assertProcessNotificationRequest(request, response);

		// Expect no error logs and three info log entries
		assertQueueDepth(ERROR_LOG_QUEUE, 0);
		assertQueueDepth(INFO_LOG_QUEUE, 3);

		// Assert that the response is the only message on the queue
		assertQueueDepth(PROCESS_QUEUE, 1);
	}

	/**
	 * Validate correct error message when two Engagement Transactions have the same key
	 */
	@Test
	public void update_R1_ERR_duplicate_tx_found() {

		ProcessNotificationType request = createProcessNotificationRequest(1111111111L, 1111111111L);
		String expectedError = "EI002: EngagementTransaction #1 and #2 have the same key. That is not allowed. See rule for Update-R1 in service contract";

		try {
			// Call the update web service without waiting for an asynch event since we expect the web service to return an error directly without triggering any asynch processing
			ProcessNotificationResponseType response = new DoOneTestDispatcher(request).doDispatchResponse();
			assertEquals(expectedError, response.getComment());

		} catch (javax.xml.ws.soap.SOAPFaultException e) {
			// TODO: Add more SOAP Fault specific tests, can we get the actual SOAP fault XML to validate against???
			fail("Did NOT expect exception here");
		};

		// Expect one error log and info log entry
		assertQueueDepth(ERROR_LOG_QUEUE, 1);
		assertQueueContainsMessage(ERROR_LOG_QUEUE, expectedError);
		assertQueueDepth(INFO_LOG_QUEUE, 2);
		// Expect nothing on the processing queue due to the error
		assertQueueDepth(PROCESS_QUEUE, 0);
	}

	/**
	 * Verify that we ignore circular engagements and only process engagements with owners
	 * different from the current index.
	 */
	@Test
	public void processNotification_R4_OK_filter_should_remove_circular_notifications() {

		ProcessNotificationType request = createProcessNotificationRequest(1111111111L, 1212121212L);
		request.getEngagementTransaction().get(1).getEngagement().setOwner(OWNER); // This engagement should be filtered out

		// Request with only one engagement left
		ProcessNotificationType request2 = createProcessNotificationRequest(1111111111L);

		// Use dispatchAndWaitForDelivery() and a custom Dispatcher to ensure that the listener on the queue is registered before the web service call is made
		MuleMessage response = dispatchAndWaitForDelivery(new DoOneTestDispatcher(request), "jms://" + PROCESS_QUEUE, EndpointMessageNotification.MESSAGE_DISPATCH_END, EI_TEST_TIMEOUT);

		// Verify that we only sending 1 engagement to the PROCESS_QUEUE
		assertProcessNotificationRequest(request2, response);

		// Expect no error logs and three info log entries
		assertQueueDepth(ERROR_LOG_QUEUE, 0);
		assertQueueDepth(INFO_LOG_QUEUE, 3);

		// Assert that the response is the only message on the queue
		assertQueueDepth(PROCESS_QUEUE, 1);
	}

	/**
	 * Performs a test that verifies that engagements with same owner as the index are filtered out and not saved
	 * in the data store.
	 * @throws JMSException
	 */
	@Test
	public void processNotification_R4_OK_filter_removes_all_transactions() throws JMSException {

		// Setup test data
		ProcessNotificationType request = createProcessNotificationRequest(1111111111L, 1212121212L);

		// Set all engagements as ours, i.e. all engagement transactions are expected to be filtered out
		request.getEngagementTransaction().get(0).getEngagement().setOwner(OWNER);
		request.getEngagementTransaction().get(1).getEngagement().setOwner(OWNER);

		// Make the synchronous call, nothing to wait for since we don't expect any messages on the queue in this test.
		new DoOneTestDispatcher(request).doDispatch();

		// Wait a short while for all background processing to complete
		waitForBackgroundProcessing();

		// Assert that no messages are posted on the processing queue
		assertQueueDepth(PROCESS_QUEUE, 0);

		// Expect no error logs
		assertQueueDepth(ERROR_LOG_QUEUE, 0);

		// Expect 14 info log entries, 3 from update-service, 2 from process-service and 3*3 from the three notify-services  ?? what?
		assertQueueDepth(INFO_LOG_QUEUE, 2);
	}

	/**
	 * Verifies that we get an error message when a request contains too many engagements.
	 */
	@Test
	public void processNotification_ERR_max_number_of_engagements() {

		// Setup test data
		ProcessNotificationType request = new ProcessNotificationType();
		long start = 1111111111L;
		for(int i = 0 ; i < ProcessBean.MAX_NUMBER_OF_ENGAGEMENTS+100; i++) {
			EngagementTransactionType et = GenServiceTestDataUtil.genEngagementTransaction(start + i);
			request.getEngagementTransaction().add(et);
		}

		String expectedError = "EI000: A technical error has occurred, error message: The request contains more than 1000 engagements. Maximum number of engagements per request is 1000.";

		try {
			// Call the update web service without waiting for an asynch event since we expect the web service to return an error directly without triggering any asynch processing
			ProcessNotificationResponseType response = new DoOneTestDispatcher(request).doDispatchResponse();
			assertEquals(expectedError, response.getComment());

		} catch (javax.xml.ws.soap.SOAPFaultException e) {
			// TODO: Add more SOAP Fault specific tests, can we get the actual SOAP fault XML to validate against???
			fail("Did NOT expect exception here");
		};

		// Expect one error log and info log entry
		assertQueueDepth(ERROR_LOG_QUEUE, 1);
		assertQueueContainsMessage(ERROR_LOG_QUEUE, expectedError);
		assertQueueDepth(INFO_LOG_QUEUE, 2);

		// Expect nothing on the processing queue due to the error
		assertQueueDepth(PROCESS_QUEUE, 0);
	}

	/**
	 * Validates that use of not allowed hsa-id's in engagement transactions logical-address are detected.
	 */
	@Test
	public void processNotification_ERR_not_allowed_logical_address() throws Exception {

		ProcessNotificationType request = createProcessNotificationRequestWithTransactions(5);
		String expectedError = "EI005: The logicalAddress in EngagementTransaction #3 is reserved and not allowed, hsa-id: " + OWNER;
		request.getEngagementTransaction().get(2).getEngagement().setLogicalAddress(OWNER);

		try {
			// Call the update web service without waiting for an asynch event since we expect the web service to return an error directly without triggering any asynch processing
			ProcessNotificationResponseType response = new DoOneTestDispatcher(request).doDispatchResponse();
			assertEquals(expectedError, response.getComment());

		} catch (javax.xml.ws.soap.SOAPFaultException e) {
			fail("Did NOT expect exception here");
		}
		// Expect one error log and info log entry
		assertQueueDepth(ERROR_LOG_QUEUE, 1);
		assertQueueContainsMessage(ERROR_LOG_QUEUE, expectedError);
		assertQueueDepth(INFO_LOG_QUEUE, 2);
		// Expect nothing on the processing queue due to the error
		assertQueueDepth(PROCESS_QUEUE, 0);
	}

	private class DoOneTestDispatcher implements Dispatcher {

		private ProcessNotificationType request = null;
		private String logicalAddress = null;

		private DoOneTestDispatcher(ProcessNotificationType request) {
			this.request  = request;
			this.logicalAddress = LOGICAL_ADDRESS;
		}

		private DoOneTestDispatcher(String logicalAddress, ProcessNotificationType request) {
			this.request  = request;
			this.logicalAddress = logicalAddress;
		}

		@Override
		public void doDispatch() {
			NotificationTestConsumer consumer = new NotificationTestConsumer(SERVICE_ADDRESS);

			ProcessNotificationResponseType response = consumer.callService(logicalAddress, request);

			// Assert OK response from the web service
			assertEquals(ResultCodeEnum.OK, response.getResultCode());
		}

		public ProcessNotificationResponseType doDispatchResponse() {
			NotificationTestConsumer consumer = new NotificationTestConsumer(SERVICE_ADDRESS);
			ProcessNotificationResponseType response = consumer.callService(logicalAddress, request);
			// Assert ERROR (validation error) response from the web service
			assertEquals(ResultCodeEnum.ERROR, response.getResultCode());
			return response;
		}
	}

	private ProcessNotificationType createProcessNotificationRequestWithTransactions(int count) {
		ProcessNotificationType request = new ProcessNotificationType();

		long start = 1111111111L;
		for(int i = 0 ; i < count; i++) {
			EngagementTransactionType et = GenServiceTestDataUtil.genEngagementTransaction(start + i);
			request.getEngagementTransaction().add(et);
		}
		return request;
	}

}