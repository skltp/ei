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
package se.skltp.ei.intsvc.update;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import riv.itintegration.engagementindex.updateresponder._1.ObjectFactory;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;

public class UpdateRequestToJmsMsgTransformer extends AbstractMessageTransformer {

	private static JaxbUtil jabxUtil = new JaxbUtil(UpdateType.class);
	private static ObjectFactory of = new ObjectFactory();
	
	@Override
	public Object transformMessage(MuleMessage message, String encoding) throws TransformerException {
	
		Object[] objArr = (Object[])message.getPayload();
		String logicalAddress = (String)objArr[0];
		UpdateType request = (UpdateType)objArr[1];
		String jmsMsg = jabxUtil.marshal(of.createUpdate(request));
		
		message.setPayload(jmsMsg);
		message.setOutboundProperty("logicalAddress", logicalAddress);

		return message;
	}

}
