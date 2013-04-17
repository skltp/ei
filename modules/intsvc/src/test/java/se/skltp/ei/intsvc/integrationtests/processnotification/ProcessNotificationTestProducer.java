package se.skltp.ei.intsvc.integrationtests.processnotification;

import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;

import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.processnotification._1.rivtabp21.ProcessNotificationResponderInterface;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationResponseType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;

@WebService(
        serviceName = "ProcessNotificatioService", 
        portName = "ProcessNotificatioPort", 
        targetNamespace = "urn:riv:itintegration:engagementindex:ProcessNotificatio:1:rivtabp21")
public class ProcessNotificationTestProducer implements ProcessNotificationResponderInterface {

    public static final String TEST_ID_FAULT_TIMEOUT = "0";
    
	private static final Logger log = LoggerFactory.getLogger(ProcessNotificationTestProducer.class);
    private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("ei-config");
	private static final long SERVICE_TIMOUT_MS = Long.parseLong(rb.getString("SERVICE_TIMEOUT_MS"));

	@Override
	public ProcessNotificationResponseType processNotification(String logicalAddress, ProcessNotificationType request) {

		log.info("ProcessNotificationTestProducer received a notification request with {} transactions for logical-address {}", request.getEngagementTransaction().size(), logicalAddress);

        // Force a timeout if timeout Id
        if (TEST_ID_FAULT_TIMEOUT.equals(request.getEngagementTransaction().get(0).getEngagement().getRegisteredResidentIdentification())) forceTimeout();

        ProcessNotificationResponseType response = new ProcessNotificationResponseType();
        response.setComment("");
        response.setResultCode(ResultCodeEnum.OK);
		return response;
	}

    private void forceTimeout() {
        try {
            log.info("TestProducer force a timeout to happen...");
            Thread.sleep(SERVICE_TIMOUT_MS + 1000);
        } catch (InterruptedException e) {}
    }
}