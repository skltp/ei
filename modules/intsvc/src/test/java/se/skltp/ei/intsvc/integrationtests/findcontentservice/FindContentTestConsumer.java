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
package se.skltp.ei.intsvc.integrationtests.findcontentservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import riv.itintegration.engagementindex.findcontent._1.rivtabp21.FindContentResponderInterface;
import riv.itintegration.engagementindex.findcontentresponder._1.FindContentResponseType;
import riv.itintegration.engagementindex.findcontentresponder._1.FindContentType;
import se.skltp.ei.intsvc.EiMuleServer;
import se.skltp.ei.intsvc.integrationtests.AbstractTestConsumer;

public class FindContentTestConsumer extends AbstractTestConsumer<FindContentResponderInterface> {

	private static final Logger log = LoggerFactory.getLogger(FindContentTestConsumer.class);

	public static void main(String[] args) {
		String serviceAddress = EiMuleServer.getAddress("FIND_CONTENT_WEB_SERVICE_URL");

		FindContentTestConsumer consumer = new FindContentTestConsumer(serviceAddress);

		FindContentType request = new FindContentType();
		FindContentResponseType response = consumer.callService("logical-adress", request);
        
		log.info("Returned result size = " + response.getEngagement().size());
	}

	public FindContentTestConsumer(String serviceAddress) {
	    
		// Setup a web service proxy for communication using HTTPS with Mutual Authentication
		super(FindContentResponderInterface.class, serviceAddress);
	}

	public FindContentResponseType callService(String logicalAddress, FindContentType request) {

		log.debug("Calling FindContent-soap-service ");
		

		FindContentResponseType response = _service.findContent(logicalAddress, request);
        return response;
	}
}
