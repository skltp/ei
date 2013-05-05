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
package se.skltp.ei.intsvc.getlogicaladdressees;

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
