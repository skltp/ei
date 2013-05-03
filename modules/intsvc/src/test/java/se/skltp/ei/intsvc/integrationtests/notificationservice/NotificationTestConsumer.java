package se.skltp.ei.intsvc.integrationtests.notificationservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import riv.itintegration.engagementindex.processnotification._1.rivtabp21.ProcessNotificationResponderInterface;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationResponseType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import se.skltp.ei.intsvc.EiMuleServer;
import se.skltp.ei.intsvc.integrationtests.AbstractTestConsumer;

public class NotificationTestConsumer extends AbstractTestConsumer<ProcessNotificationResponderInterface> {

	private static final Logger log = LoggerFactory.getLogger(NotificationTestConsumer.class);

	public static void main(String[] args) {
		String serviceAddress = EiMuleServer.getAddress("UPDATE_WEB_SERVICE_URL");

		NotificationTestConsumer consumer = new NotificationTestConsumer(serviceAddress);

		ProcessNotificationType request = new ProcessNotificationType();
		ProcessNotificationResponseType response = consumer.callService("logical-adress", request);
        
		log.info("Returned status = " + response.getResultCode());
	}

	public NotificationTestConsumer(String serviceAddress) {
	    
		// Setup a web service proxy for communication using HTTPS with Mutual Authentication
		super(ProcessNotificationResponderInterface.class, serviceAddress);
	}

	public ProcessNotificationResponseType callService(String logicalAddress, ProcessNotificationType request) {

		log.debug("Calling ProcessNotification-soap-service ");

		ProcessNotificationResponseType response = _service.processNotification(logicalAddress, request);
        return response;
	}
}
