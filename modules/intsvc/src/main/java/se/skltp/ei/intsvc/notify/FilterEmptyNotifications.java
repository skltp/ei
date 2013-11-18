package se.skltp.ei.intsvc.notify;

import org.mule.api.MuleMessage;
import org.mule.api.routing.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;

public class FilterEmptyNotifications implements Filter {

	private static final Logger LOG = LoggerFactory.getLogger(FilterEmptyNotifications.class);
	
	/**
	 * Since we only want to publish message with one or more engagements we thins
	 * method is used for filter empty ProcessNotifications.
	 */
	@Override
	public boolean accept(MuleMessage message) {
		
		LOG.debug("Received the request: {}", message);
		
		Object[] payload = (Object[]) message.getPayload();
		ProcessNotificationType pn = (ProcessNotificationType) payload[1];

		// We don't want to send empty process notifications
		if (pn.getEngagementTransaction().size() > 0) {
			return true;				
		} else {
			return false;
		}
		
	}

}