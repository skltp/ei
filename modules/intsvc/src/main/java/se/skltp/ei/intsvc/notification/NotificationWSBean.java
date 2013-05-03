package se.skltp.ei.intsvc.notification;

import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.processnotification._1.rivtabp21.ProcessNotificationResponderInterface;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationResponseType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import se.skltp.ei.svc.service.api.Header;
import se.skltp.ei.svc.service.api.ProcessInterface;

@WebService(
        serviceName = "ProcessNotificationResponderService", 
        portName = "ProcessNotificationResponderPort", 
        targetNamespace = "urn:riv:itintegration:engagementindex:ProcessNotification:1:rivtabp21")
public class NotificationWSBean implements ProcessNotificationResponderInterface {

    @SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(NotificationWSBean.class);

    private ProcessInterface blBean = null;
    
    public void setBlBean(ProcessInterface blBean) {
    	this.blBean = blBean;
    }

    /**
     *
     * @param logicalAddress
     * @param parameters
     * @return
     */
    @Override
    public ProcessNotificationResponseType processNotification(String logicalAddress, ProcessNotificationType parameters) {

    	// Validate the request (note no db-access will be performed)
    	blBean.validateProcessNotification(new Header(null,logicalAddress,null), parameters);
    	
    	// Create a default response
    	ProcessNotificationResponseType response = new ProcessNotificationResponseType();
        response.setComment(null);
        response.setResultCode(ResultCodeEnum.OK);
        return response;
    }
}