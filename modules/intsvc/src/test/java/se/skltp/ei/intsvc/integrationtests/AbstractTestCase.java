package se.skltp.ei.intsvc.integrationtests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.mule.api.MuleMessage;
import org.mule.api.context.notification.EndpointMessageNotificationListener;
import org.mule.api.context.notification.ServerNotification;
import org.mule.context.notification.EndpointMessageNotification;
import org.soitoolkit.commons.mule.test.Dispatcher;
import org.soitoolkit.commons.mule.test.DispatcherMuleClientImpl;
import org.soitoolkit.commons.mule.util.MuleUtil;
import org.soitoolkit.commons.mule.util.ValueHolder;

/**
 * Extends the base class in Mule, org.mule.tck.junit4.FuntionalTestCase.
 * 
 * @author Magnus Larsson
 *
 */
public abstract class AbstractTestCase extends org.soitoolkit.commons.mule.test.junit4.AbstractTestCase {
    
    public AbstractTestCase() {
		super();
	}
    
	/**
	 * Waits <code>timeout</code> ms for a <code>MuleMessage</code> to arrive on outboundEndpoint with the name <code>outboundEndpointName</code> and with the action <code>action</code>. 
	 * 
	 * Sample usage: TBS
	 * 
	 * @param serviceComponentName
	 * @param timeout in ms
	 * @return the MuleMessage sent to the named service component
	 */
	protected MuleMessage waitForDelivery(final String outboundEndpointName, final int action, long timeout) {
		return dispatchAndWaitForDelivery(null, outboundEndpointName, action, timeout);
    }

	/**
	 * Sends the <code>payload</code> and <code>headers</code> to the <code>inboundEndpointAddress</code> and waits <code>timeout</code> ms for a <code>MuleMessage</code> to arrive on outboundEndpoint with the name <code>outboundEndpointName</code>. 
	 * 
	 * Sample usage:
	 * <tt>
	 *	public void testTransferKorttransaktioner() throws Exception {
	 *		String expectedPayload = "Yada, yada, yada...";
	 *
	 *		MuleMessage message = dispatchAndWaitForDelivery(
	 *			"sftp://dfcx0346@vfin8003.volvofinans.net/sftp/vfkonto/ut",
	 *			expectedPayload,
	 *			createFileHeader("from_vfkonto.dat"),
	 *			"volvokort-test-endpoint",
	 *			TIMEOUT);
	 *
	 *		String actualPayload = message.getPayloadAsString();
	 *		assertEquals(expectedPayload, actualPayload); 
	 *	}	 
	 * </tt>
	 * 
	 * @param inboundEndpointAddress
	 * @param payload
	 * @param headers
	 * @param outboundEndpointName
	 * @param action as specified by org.mule.context.notification.EndpointMessageNotification: MESSAGE_RECEIVED, MESSAGE_DISPATCHED, MESSAGE_SENT or MESSAGE_REQUESTED
	 * @param timeout in ms
	 * @return the received MuleMEssage on the outboundEndpoint
	 */
	protected MuleMessage dispatchAndWaitForDelivery(String inboundEndpointAddress, Object payload, Map<String, String> headers, final String outboundEndpointName, final int action, long timeout) {
		return dispatchAndWaitForDelivery(new DispatcherMuleClientImpl(muleContext, inboundEndpointAddress, payload, headers), outboundEndpointName, action, timeout);
    }

	
	/**
	 * Use the Dispatcher to send a asynchronous message and waits <code>timeout</code> ms for a <code>MuleMessage</code> to arrive on outboundEndpoint with the name <code>outboundEndpointName</code> and with the action <code>action</code>. 
	 * 
	 * Sample usage: TBS
	 * 
	 * @param inboundEndpointAddress
	 * @param payload
	 * @param headers
	 * @param outboundEndpointName
	 * @param action as specified by org.mule.context.notification.EndpointMessageNotification: MESSAGE_RECEIVED, MESSAGE_DISPATCHED, MESSAGE_SENT or MESSAGE_REQUESTED
	 * @param timeout in ms
	 * @return the received MuleMEssage on the outboundEndpoint
	 */
	@SuppressWarnings("rawtypes")
	protected MuleMessage dispatchAndWaitForDelivery(Dispatcher dispatcher, final String outboundEndpointName, final int action, long timeout) {
		
		// Declare MuleMessage to return
		final ValueHolder<MuleMessage> receivedMessageHolder = new ValueHolder<MuleMessage>();
		
		// Declare countdown latch and listener
		final CountDownLatch latch = new CountDownLatch(1);
		EndpointMessageNotificationListener listener = null;

		try {

			// Next create a listener that listens for dispatch events on the outbound endpoint
			listener = new EndpointMessageNotificationListener() {
				public void onNotification(ServerNotification notification) {
					if (logger.isDebugEnabled()) logger.debug("notification received on " + notification.getResourceIdentifier() + " (action: " + notification.getActionName() + ")");

					// Only care about EndpointMessageNotification
					if (notification instanceof EndpointMessageNotification) {
						EndpointMessageNotification endpointNotification = (EndpointMessageNotification)notification;

						// Extract action and name of the endpoint
						int    actualAction   = endpointNotification.getAction();
						String actualEndpoint = MuleUtil.getEndpointName(endpointNotification);

						// If it is a dispatch event on our outbound endpoint then countdown the latch.
						if (logger.isDebugEnabled()) {
							logger.debug(actualAction == action);
							logger.debug(actualEndpoint.equals(outboundEndpointName));
						}
						if (actualAction == action && actualEndpoint.equals(outboundEndpointName)) {
							if (logger.isDebugEnabled()) logger.debug("Expected notification received on " + actualEndpoint + " (action: " + endpointNotification.getActionName() + "), time to countdown the latch");
							receivedMessageHolder.value = (MuleMessage)endpointNotification.getSource();
							latch.countDown();

						} else {
							if (logger.isDebugEnabled()) logger.debug("A not matching notification received on " + actualEndpoint + " (action: " + endpointNotification.getActionName() + "), continue to wait for the right one...");							
						}
					}
				}
			};

			// Now register the listener
			muleContext.getNotificationManager().addListener(listener);

			// Perform the actual dispatch, if any...
			if (dispatcher != null) {
				dispatcher.doDispatch();
			}
			
			// Wait for the delivery to occur...
			if (logger.isDebugEnabled()) logger.debug("Waiting for message to be delivered to the endpoint...");
			boolean workDone = latch.await(timeout, TimeUnit.MILLISECONDS);
			if (logger.isDebugEnabled()) logger.debug((workDone) ? "Message delivered, continue..." : "No message delivered, timeout occurred!");

			// Raise a fault if the test timed out
			assertTrue("Test timed out. It took more than " + timeout + " milliseconds. If this error occurs the test probably needs a longer time out (on your computer/network)", workDone);

		} catch (Exception e) {
			e.printStackTrace();
			fail("An unexpected error occurred: " + e.getMessage());

		} finally {
			// Always remove the listener if created
			if (listener != null) muleContext.getNotificationManager().removeListener(listener);
		}
		
		return receivedMessageHolder.value;		
    }
    
}