package se.skltp.ei.intsvc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import riv.itintegration.registry._1.ServiceContractNamespaceType;
import riv.itintegration.registry.getlogicaladdresseesbyservicecontractresponder._1.GetLogicalAddresseesByServiceContractType;

public class GetLogicalAddresseesByServiceContractRequest {

	private static final Logger LOG = LoggerFactory.getLogger(GetLogicalAddresseesByServiceContractRequest.class);

    private String logicalAddress = null;
    
    public void setLogicalAddress(String logicalAddress) {
    	this.logicalAddress = logicalAddress;
    }
    
    /**
     *
     * @param requestStr
     * @return
     */
    public Object[] createGetLogicalAddresseesByServiceContractRequest(String requestStr) {
    	
    	LOG.debug("Received the request: {}", requestStr);

    	GetLogicalAddresseesByServiceContractType getReq = new GetLogicalAddresseesByServiceContractType(); 
    	getReq.setServiceConsumerHsaId(logicalAddress);
    	ServiceContractNamespaceType ns = new ServiceContractNamespaceType();
    	ns.setServiceContractNamespace("urn:riv:itintegration:registry:GetLogicalAddresseesByServiceContractResponder:1");
		getReq.setServiceContractNameSpace(ns);
    	
		Object[] request = new Object[] {logicalAddress, getReq};
		return request;
    }
}
