package se.skltp.ei.svc.api;

import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;

public interface UpdateInterface {

    /**
     *
     * @param logicalAddress
     * @param parameters
     * @return
     */
    public UpdateResponseType update(Header header, UpdateType parameters); 

}
