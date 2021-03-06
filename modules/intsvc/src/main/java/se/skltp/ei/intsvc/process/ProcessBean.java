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
package se.skltp.ei.intsvc.process;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.svc.service.api.ProcessInterface;

public class ProcessBean {

	private static final Logger LOG = LoggerFactory.getLogger(ProcessBean.class);

	private static JaxbUtil jabxUtil = new JaxbUtil(UpdateType.class, ProcessNotificationType.class);
	    
    private ProcessInterface blBean = null;
    
    public void setBlBean(ProcessInterface blBean) {
    	this.blBean = blBean;
    }
    
    /**
     *
     * @param parameters
     * @return
     */
    public List<EngagementTransactionType> process(String requestStr) {
    	
    	LOG.debug("Received the request: {}", requestStr);

    	// FIXME: Add header to the call to the business layer!
		Object requestJaxb = jabxUtil.unmarshal(requestStr);
		List<EngagementTransactionType> listEngagements = new ArrayList<EngagementTransactionType>();
    	if (requestJaxb instanceof UpdateType) {
    		listEngagements.addAll(blBean.update(null, (UpdateType)requestJaxb));
    	} else {
    		listEngagements.addAll(blBean.processNotification(null, (ProcessNotificationType)requestJaxb));
    	}

    	// Error handling? Return list of <EngagementTransaction> with duplicates removed
    	
    	return listEngagements;
    }
}
