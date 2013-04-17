package se.skltp.ei.intsvc.processnotification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;

public class CreateProcessNotificationRequest {

	private static final Logger LOG = LoggerFactory.getLogger(CreateProcessNotificationRequest.class);

	private static JaxbUtil jabxUtil = new JaxbUtil(UpdateType.class);
	    
    private String logicalAddress = null;
    
    public void setLogicalAddress(String logicalAddress) {
    	this.logicalAddress = logicalAddress;
    }
    
    /**
     *
     * @param requestStr
     * @return
     */
    public Object[] createProcessNotificationRequest(String requestStr) {
    	
    	LOG.debug("Received the request: {}", requestStr);

    	UpdateType update = (UpdateType)jabxUtil.unmarshal(requestStr);
    	ProcessNotificationType processNotification = new ProcessNotificationType();
    	processNotification.getEngagementTransaction().addAll(update.getEngagementTransaction());
    	
		Object[] request = new Object[] {logicalAddress, processNotification};
		return request;
    }
}
