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
package se.skltp.ei.intsvc.integrationtests.updateservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import riv.itintegration.engagementindex.update._1.rivtabp21.UpdateResponderInterface;
import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.intsvc.EiMuleServer;
import se.skltp.ei.intsvc.integrationtests.AbstractTestConsumer;

public class UpdateTestConsumer extends AbstractTestConsumer<UpdateResponderInterface> {

	private static final Logger log = LoggerFactory.getLogger(UpdateTestConsumer.class);

	public static void main(String[] args) {
		String serviceAddress = EiMuleServer.getAddress("UPDATE_WEB_SERVICE_URL");

		UpdateTestConsumer consumer = new UpdateTestConsumer(serviceAddress);

		UpdateType request = new UpdateType();
		UpdateResponseType response = consumer.callService("logical-adress", request);
        
		log.info("Returned status = " + response.getResultCode());
	}

	public UpdateTestConsumer(String serviceAddress) {
	    
		// Setup a web service proxy for communication using HTTPS with Mutual Authentication
		super(UpdateResponderInterface.class, serviceAddress);
	}

	public UpdateResponseType callService(String logicalAddress, UpdateType request) {

		log.debug("Calling Update-soap-service ");
		

		UpdateResponseType response = _service.update(logicalAddress, request);
        return response;
	}
}
