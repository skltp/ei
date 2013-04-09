package se.skltp.ei.svc.service.api;

import riv.itintegration.engagementindex.findcontentresponder._1.FindContentResponseType;
import riv.itintegration.engagementindex.findcontentresponder._1.FindContentType;

public interface FindContentInterface {

    /**
     *
     * @param logicalAddress
     * @param parameters
     * @return
     */
    public FindContentResponseType findContent(Header header, FindContentType parameters);
}
