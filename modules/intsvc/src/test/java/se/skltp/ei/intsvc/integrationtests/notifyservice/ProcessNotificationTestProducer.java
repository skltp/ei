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
package se.skltp.ei.intsvc.integrationtests.notifyservice;

import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;

import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.processnotification._1.rivtabp21.ProcessNotificationResponderInterface;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationResponseType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;

@WebService(
        serviceName = "ProcessNotificatioService", 
        portName = "ProcessNotificatioPort", 
        targetNamespace = "urn:riv:itintegration:engagementindex:ProcessNotificatio:1:rivtabp21")
public class ProcessNotificationTestProducer implements ProcessNotificationResponderInterface {

    public static final long TEST_ID_FAULT_TIMEOUT = 0;
    public static final String FULL_TEST_ID_FAULT_TIMEOUT = "19" + TEST_ID_FAULT_TIMEOUT;
    
	private static final Logger log = LoggerFactory.getLogger(ProcessNotificationTestProducer.class);
    private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("ei-config");
	private static final long SERVICE_TIMOUT_MS = Long.parseLong(rb.getString("SERVICE_TIMEOUT_MS"));

	private static Object lastPaylaod = null;
	
	@Override
	public ProcessNotificationResponseType processNotification(String logicalAddress, ProcessNotificationType request) {

		log.info("ProcessNotificationTestProducer received a notification request with {} transactions for logical-address {}", request.getEngagementTransaction().size(), logicalAddress);

		lastPaylaod = request;

        // Force a timeout if timeout Id
        String residentId = request.getEngagementTransaction().get(0).getEngagement().getRegisteredResidentIdentification();
		if (FULL_TEST_ID_FAULT_TIMEOUT.equals(residentId)) forceTimeout();

        ProcessNotificationResponseType response = new ProcessNotificationResponseType();
        response.setComment("");
        response.setResultCode(ResultCodeEnum.OK);
		return response;
	}

    private void forceTimeout() {
        try {
            log.info("TestProducer force a timeout to happen...");
            Thread.sleep(SERVICE_TIMOUT_MS + 1000);
        } catch (InterruptedException e) {}
    }

	public static Object getLastPayload() {
		return lastPaylaod;
	}
}