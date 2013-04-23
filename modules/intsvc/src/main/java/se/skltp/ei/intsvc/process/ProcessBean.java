package se.skltp.ei.intsvc.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.svc.service.api.ProcessInterface;

public class ProcessBean {

	private static final Logger LOG = LoggerFactory.getLogger(ProcessBean.class);

	private static JaxbUtil jabxUtil = new JaxbUtil(UpdateType.class);
	    
    private ProcessInterface blBean = null;
    
    public void setBlBean(ProcessInterface blBean) {
    	this.blBean = blBean;
    }
    
    /**
     *
     * @param parameters
     * @return
     */
    public String process(String requestStr) {
    	
    	LOG.debug("Received the request: {}", requestStr);

    	// FIXME: Add support for process notifications here when its time...
		UpdateType requestJaxb = (UpdateType)jabxUtil.unmarshal(requestStr);
    	
    	blBean.update(null, requestJaxb);

    	return requestStr;
    }

}
