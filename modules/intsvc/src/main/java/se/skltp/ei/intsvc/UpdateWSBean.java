package se.skltp.ei.intsvc;

import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.update._1.rivtabp21.UpdateResponderInterface;
import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;

@WebService(
        serviceName = "UpdateResponderService", 
        portName = "UpdateResponderPort", 
        targetNamespace = "urn:riv:itintegration:engagementindex:Update:1:rivtabp21")
public class UpdateWSBean implements UpdateResponderInterface {

    @SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(UpdateWSBean.class);

    /**
     *
     * @param logicalAddress
     * @param parameters
     * @return
     */
    @Override
    public UpdateResponseType update(String logicalAddress, UpdateType parameters) {

    	// Create a default response
        UpdateResponseType response = new UpdateResponseType();
        response.setComment(null);
        response.setResultCode(ResultCodeEnum.OK);
        return response;
    }

}
