package se.skltp.ei.svc.service.api;

import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;

public interface ProcessInterface {

    /**
     *
     * @param logicalAddress
     * @param parameters
     * @return
     */
    public UpdateResponseType update(Header header, UpdateType parameters); 

}
