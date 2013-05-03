package se.skltp.ei.svc.service.api;

import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationResponseType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;

public interface ProcessInterface {

    /**
     *
     * @param header
     * @param parameters
     */
    public void validateUpdate(Header header, UpdateType parameters); 

    /**
     *
     * @param header
     * @param parameters
     * @return
     */
    public UpdateResponseType update(Header header, UpdateType parameters); 

    /**
    *
    * @param header
    * @param parameters
    */
	public void validateProcessNotification(Header header, ProcessNotificationType parameters); 

    /**
    *
    * @param header
    * @param parameters
    * @return
    */
	public ProcessNotificationResponseType processNotification(Header header, ProcessNotificationType parameters);
}
