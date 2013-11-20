package se.skltp.ei.intsvc.process;

import java.util.ArrayList;
import java.util.List;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.skltp.ei.intsvc.subscriber.api.Subscriber;
import se.skltp.ei.intsvc.subscriber.api.SubscriberCache;

public class CreateNotificationListTransformer extends AbstractMessageTransformer {

	private static final Logger log = LoggerFactory.getLogger(CreateNotificationListTransformer.class);

	private SubscriberCache subscriberCache;
	public void setSubscriberCache (SubscriberCache subscriberCache) {
		this.subscriberCache = subscriberCache;
	}

	/**
     * Message aware transformer that ...
     */
    @Override
    public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		Object msg = message.getPayload();

    	log.debug("msg: {}", msg);

    	List<Subscriber> subscribers = subscriberCache.getSubscribers();    	

    	List<Object[]> msgs = new ArrayList<Object[]>();
    	for (Subscriber subscriber : subscribers) {
			msgs.add(new Object[] {subscriber.getNotificationQueueName(), msg});
		}

    	log.debug("msgs-size: {}", msgs.size());

    	message.setPayload(msgs);
    	return message;
    }

}