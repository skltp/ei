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
