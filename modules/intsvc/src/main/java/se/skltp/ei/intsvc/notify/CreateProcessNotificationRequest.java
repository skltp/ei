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
package se.skltp.ei.intsvc.notify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;

public class CreateProcessNotificationRequest {

	private static final Logger LOG = LoggerFactory.getLogger(CreateProcessNotificationRequest.class);

	private static JaxbUtil jabxUtil = new JaxbUtil(UpdateType.class, ProcessNotificationType.class);
	    
    private String logicalAddress = null;
    
    public void setLogicalAddress(String logicalAddress) {
    	this.logicalAddress = logicalAddress;
    }
    
    /**
     *
     * @param requestStr
     * @return
     */
    public Object[] createProcessNotificationRequest(String requestStr) {
    	
    	LOG.debug("Received the request: {}", requestStr);

		Object requestJaxb = jabxUtil.unmarshal(requestStr);
    	ProcessNotificationType processNotification = (ProcessNotificationType)requestJaxb;
    	
		Object[] request = new Object[] {logicalAddress, processNotification};
		return request;
    }
}
