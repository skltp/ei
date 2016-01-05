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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.rivta.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontractresponder.v2.GetLogicalAddresseesByServiceContractType;
import se.rivta.infrastructure.itintegration.registry.v2.ServiceContractNamespaceType;

public class GetLogicalAddresseesByServiceContractRequest {

	private static final Logger LOG = LoggerFactory.getLogger(GetLogicalAddresseesByServiceContractRequest.class);

    /**
     *
     * @param requestStr
     * @return
     */
    public Object[] createGetLogicalAddresseesByServiceContractRequest(Map<String, String> messageProperties) {
    	
    	LOG.debug("Received the request: {}", messageProperties);

    	GetLogicalAddresseesByServiceContractType getReq = new GetLogicalAddresseesByServiceContractType(); 
    	getReq.setServiceConsumerHsaId(messageProperties.get("LOGICAL_ADDRESS"));
    	ServiceContractNamespaceType ns = new ServiceContractNamespaceType();
    	ns.setServiceContractNamespace(messageProperties.get("SERVICE_CONTRACT"));
		getReq.setServiceContractNameSpace(ns);
    	
		Object[] request = new Object[] { messageProperties.get("LOGICAL_ADDRESS"), getReq };
		return request;
    }
}
