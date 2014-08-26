package se.skltp.ei.intsvc.update;

import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.update._1.rivtabp21.UpdateResponderInterface;
import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.svc.service.api.Header;
import se.skltp.ei.svc.service.api.ProcessInterface;

@WebService(
        serviceName = "UpdateResponderService", 
        portName = "UpdateResponderPort", 
        targetNamespace = "urn:riv:itintegration:engagementindex:Update:1:rivtabp21")
public class UpdateWSBean implements UpdateResponderInterface {

    @SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(UpdateWSBean.class);

    private String owner;    
    private ProcessInterface blBean = null;
    
    public void setOwner(String owner) {
        this.owner = owner;
    }
    
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
    public UpdateResponseType update(String logicalAddress, UpdateType parameters) {
    	
    	// Validate the request (note no db-access will be performed)
    	blBean.validateUpdate(new Header(null,logicalAddress,null), parameters);
    	
    	// According to R6 owner should always be set to owner of the index
    	for (EngagementTransactionType ett : parameters.getEngagementTransaction()) {
    		ett.getEngagement().setOwner(owner);
    	}
    	
    	// Create a default response
        UpdateResponseType response = new UpdateResponseType();
        response.setComment(null);
        response.setResultCode(ResultCodeEnum.OK);
        return response;
    }

}
