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
package se.skltp.ei.intsvc.notification;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import riv.itintegration.engagementindex.processnotificationresponder._1.ObjectFactory;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import se.skltp.ei.intsvc.EiConstants;

public class ProcessNotificationRequestToJmsMsgTransformer extends AbstractMessageTransformer {

	private static JaxbUtil jabxUtil = new JaxbUtil(ProcessNotificationType.class);
	private static ObjectFactory of = new ObjectFactory();
	
	@Override
	public Object transformMessage(MuleMessage message, String encoding) throws TransformerException {
	
		Object[] objArr = (Object[])message.getPayload();
		String logicalAddress = (String)objArr[0];
		ProcessNotificationType request = (ProcessNotificationType)objArr[1];
		String jmsMsg = jabxUtil.marshal(of.createProcessNotification(request));
		
		message.setPayload(jmsMsg);
		message.setOutboundProperty("logicalAddress", logicalAddress);
		
		// add metadata for logging
		message.setOutboundProperty(EiConstants.EI_LOG_NUMBER_OF_RECORDS_IN_MESSAGE, String.valueOf(request.getEngagementTransaction().size()));
		message.setOutboundProperty(EiConstants.EI_LOG_MESSAGE_TYPE, EiConstants.EI_LOG_MESSAGE_TYPE_PROCESS_NOTIFICATION);
		
		return message;
	}
}
