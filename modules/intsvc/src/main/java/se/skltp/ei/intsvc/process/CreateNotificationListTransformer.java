package se.skltp.ei.intsvc.process;

import java.util.ArrayList;
import java.util.List;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ObjectFactory;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.intsvc.subscriber.api.Subscriber;
import se.skltp.ei.intsvc.subscriber.api.SubscriberCache;

public class CreateNotificationListTransformer extends AbstractMessageTransformer {

	private static final Logger log = LoggerFactory.getLogger(CreateNotificationListTransformer.class);
	private static JaxbUtil jabxUtil = new JaxbUtil(UpdateType.class, ProcessNotificationType.class);
	private static riv.itintegration.engagementindex.processnotificationresponder._1.ObjectFactory objectFactoryProcessNotification = new ObjectFactory();
	private static riv.itintegration.engagementindex.updateresponder._1.ObjectFactory objectFactoryUpdate = new riv.itintegration.engagementindex.updateresponder._1.ObjectFactory();

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
    	
    	// Unmarshal payload
    	ProcessNotificationType process = new ProcessNotificationType();
    	List<EngagementTransactionType> engagements = (List<EngagementTransactionType>)msg;

    	List<Subscriber> subscribers = subscriberCache.getSubscribers();    	

    	List<Object[]> msgs = new ArrayList<Object[]>();
    	for (Subscriber subscriber : subscribers) {
    		
    		int msgCount = 0;
    		
    		// all outgoing payload is ProcessNotifications!
    		process.getEngagementTransaction().clear();
    		process.getEngagementTransaction().addAll(subscriber.filter(engagements));
    			
    		msgCount = process.getEngagementTransaction().size();
    			
        	msg = jabxUtil.marshal(objectFactoryProcessNotification.createProcessNotification(process));
    		
    		// Only add the payload if there is any messages to send
    		if (msgCount > 0) {
    			msgs.add(new Object[] {subscriber.getNotificationQueueName(), msg});
    		}
		}

    	log.debug("msgs-size: {}", msgs.size());

    	message.setPayload(msgs);
    	return message;
    }

}