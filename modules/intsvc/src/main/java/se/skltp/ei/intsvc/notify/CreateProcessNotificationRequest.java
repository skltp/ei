package se.skltp.ei.intsvc.notify;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;

public class CreateProcessNotificationRequest {

	private static final Logger LOG = LoggerFactory.getLogger(CreateProcessNotificationRequest.class);

	private static JaxbUtil jabxUtil = new JaxbUtil(UpdateType.class, ProcessNotificationType.class);
	    
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

    	List<EngagementTransactionType> et = null;
		Object requestJaxb = jabxUtil.unmarshal(requestStr);

    	if (requestJaxb instanceof UpdateType) {
    		UpdateType update = (UpdateType)requestJaxb;
        	et = update.getEngagementTransaction();
    	} else {
        	ProcessNotificationType notification = (ProcessNotificationType)requestJaxb;
        	et = notification.getEngagementTransaction();
    	}

    	ProcessNotificationType processNotification = new ProcessNotificationType();
    	processNotification.getEngagementTransaction().addAll(et);
    	
		Object[] request = new Object[] {logicalAddress, processNotification};
		return request;
    }
}
