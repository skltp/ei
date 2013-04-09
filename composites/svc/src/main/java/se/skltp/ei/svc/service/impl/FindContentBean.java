package se.skltp.ei.svc.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import riv.itintegration.engagementindex.findcontentresponder._1.FindContentResponseType;
import riv.itintegration.engagementindex.findcontentresponder._1.FindContentType;
import se.skltp.ei.svc.service.api.FindContentInterface;
import se.skltp.ei.svc.service.api.Header;

public class FindContentBean implements FindContentInterface {

    private static final Logger LOG = LoggerFactory.getLogger(FindContentBean.class);

    /**
     *
     * @param header
     * @param parameters
     * @return
     */
    @Override
    public FindContentResponseType findContent(Header header, FindContentType parameters) {
    	LOG.debug("The svc.findContent service is called");
    	
    	FindContentResponseType response = new FindContentResponseType();
        return response;
    }

}
