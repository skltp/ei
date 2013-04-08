package se.skltp.ei.intsvc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import riv.itintegration.engagementindex.findcontent._1.rivtabp21.FindContentResponderInterface;
import riv.itintegration.engagementindex.findcontentresponder._1.FindContentResponseType;
import riv.itintegration.engagementindex.findcontentresponder._1.FindContentType;
import se.skltp.ei.svc.api.FindContentInterface;

public class FindContentWSBean implements FindContentResponderInterface {

    @SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(FindContentWSBean.class);

    private FindContentInterface blBean = null;
    
    public void setBlBean(FindContentInterface blBean) {
    	this.blBean = blBean;
    }
    
    /**
     *
     * @param logicalAddress
     * @param parameters
     * @return
     */
    @Override
    public FindContentResponseType findContent(String logicalAddress, FindContentType parameters) {
        return blBean.findContent(null, parameters);
    }

}
