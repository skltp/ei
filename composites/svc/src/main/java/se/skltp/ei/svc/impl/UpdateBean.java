package se.skltp.ei.svc.impl;

import se.skltp.ei.svc.api.Header;
import se.skltp.ei.svc.api.UpdateInterface;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.update._1.rivtabp21.UpdateResponderInterface;
import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;

public class UpdateBean implements UpdateResponderInterface {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateBean.class);


    /**
     *
     * @param logicalAddress
     * @param parameters
     * @return
     */
    @Override
    public UpdateResponseType update(Header header, UpdateType parameters) {
        UpdateResponseType response = new UpdateResponseType();
        response.setComment(null);
        response.setResultCode(ResultCodeEnum.OK);
        return response;
    }

}
