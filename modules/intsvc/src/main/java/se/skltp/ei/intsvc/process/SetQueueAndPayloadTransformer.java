package se.skltp.ei.intsvc.process;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetQueueAndPayloadTransformer extends AbstractMessageTransformer {

	private static final Logger log = LoggerFactory.getLogger(SetQueueAndPayloadTransformer.class);

    /**
     * Message aware transformer that ...
     */
    @Override
    public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		Object[] msgArr = (Object[])message.getPayload();
		
		String queue = (String)msgArr[0];
		Object payload = msgArr[1];

    	log.debug("queue: {}, payload: {}", queue, payload);

    	message.setInvocationProperty("EI-NOTIFICATION-QUEUE", queue);

    	message.setPayload(payload);
    	return message;
    }

}