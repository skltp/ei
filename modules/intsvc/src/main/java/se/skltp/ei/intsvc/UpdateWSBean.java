package se.skltp.ei.intsvc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import riv.itintegration.engagementindex.update._1.rivtabp21.UpdateResponderInterface;
import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.svc.service.api.UpdateInterface;

public class UpdateWSBean implements UpdateResponderInterface {

    @SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(UpdateWSBean.class);

    private UpdateInterface blBean = null;
    
    public void setBlBean(UpdateInterface blBean) {
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
        return blBean.update(null, parameters);
    }

}
