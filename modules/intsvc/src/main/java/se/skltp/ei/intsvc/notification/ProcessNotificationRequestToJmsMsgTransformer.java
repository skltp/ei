package se.skltp.ei.intsvc.notification;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ObjectFactory;

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

		return message;
	}
}
