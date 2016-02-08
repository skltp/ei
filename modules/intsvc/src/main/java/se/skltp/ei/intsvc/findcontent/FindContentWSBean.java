/**
 * Copyright (c) 2013 Sveriges Kommuner och Landsting (SKL). <http://www.skl.se/>
 *
 * This file is part of SKLTP.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package se.skltp.ei.intsvc.findcontent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.context.MuleContextAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import riv.itintegration.engagementindex._1.EngagementType;
import riv.itintegration.engagementindex.findcontent._1.rivtabp21.FindContentResponderInterface;
import riv.itintegration.engagementindex.findcontentresponder._1.FindContentResponseType;
import riv.itintegration.engagementindex.findcontentresponder._1.FindContentType;
import se.rivta.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontractresponder.v2.GetLogicalAddresseesByServiceContractResponseType;
import se.rivta.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontractresponder.v2.LogicalAddresseeRecordType;
import se.skltp.ei.svc.service.api.FindContentInterface;

public class FindContentWSBean implements FindContentResponderInterface, MuleContextAware {

    @SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(FindContentWSBean.class);
    
    private MuleContext muleContext;
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
    	FindContentResponseType findContentRespType = blBean.findContent(null, parameters);
    	
    	if (parameters.getServiceContract() != null) {
    		List<String> accessibleLogicalAddresses = getAccessibleLogicalAddresses(logicalAddress, parameters.getServiceContract());
    		findContentRespType = filterAccessibleLogicalAddresses(findContentRespType, accessibleLogicalAddresses);
    	}
    	return findContentRespType;
    }
    
    private FindContentResponseType filterAccessibleLogicalAddresses(FindContentResponseType findContentRespType, List<String> accessibleLogicalAddresses) {
    	Iterator<EngagementType> iterator = findContentRespType.getEngagement().iterator();
    	while (iterator.hasNext()) {
    		String lAddress = iterator.next().getLogicalAddress();
    		if (!accessibleLogicalAddresses.contains(lAddress)) {
    			iterator.remove();
    			LOG.warn("No authorization to access logical address: " + lAddress);
    		}
    	}
    	return findContentRespType;
    }
    
	@Override
	public void setMuleContext(MuleContext context) {
		this.muleContext = context;		
	}
	
	private List<String> getAccessibleLogicalAddresses(String logicalAddress, String serviceContract) {
		Map<String, String> messageProperties = new HashMap<String, String>();
		messageProperties.put("LOGICAL_ADDRESS", logicalAddress);
		messageProperties.put("SERVICE_CONTRACT", serviceContract);
		
		List<String> logicalAdresses = new ArrayList<String>();
		MuleMessage response = null;
		try {
			response = muleContext.getClient().send("vm://get-logical-addressees", messageProperties, null);
			
			GetLogicalAddresseesByServiceContractResponseType logicalAddressesResponse = (GetLogicalAddresseesByServiceContractResponseType) response.getPayload();
			for (LogicalAddresseeRecordType record : logicalAddressesResponse.getLogicalAddressRecord()) {
				logicalAdresses.add(record.getLogicalAddress());
			}
		} catch (MuleException e) {
			LOG.error("Unable to request service GetLogicalAddressesByServiceContract with parameters: " + messageProperties, e.getMessage());
		}		
		
		return logicalAdresses;
	}
}