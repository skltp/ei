package se.skltp.ei.intsvc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.svc.service.api.UpdateInterface;

public class UpdateDbBean {

	private static final Logger LOG = LoggerFactory.getLogger(UpdateDbBean.class);

	private static JaxbUtil jabxUtil = new JaxbUtil(UpdateType.class);
	    
    private UpdateInterface blBean = null;
    
    public void setBlBean(UpdateInterface blBean) {
    	this.blBean = blBean;
    }
    
    /**
     *
     * @param parameters
     * @return
     */
    public String update(String requestStr) {
    	
    	LOG.debug("Received the request: {}", requestStr);

		UpdateType requestJaxb = (UpdateType)jabxUtil.unmarshal(requestStr);
    	
    	blBean.update(null, requestJaxb);

    	return requestStr;
    }

}
