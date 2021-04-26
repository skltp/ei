package se.skltp.ei.updateprocess;

import static se.skltp.ei.service.constants.EiConstants.X_SKLTP_CORRELATION_ID;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.apache.camel.Body;
import org.apache.camel.CamelContext;
import org.apache.camel.Header;
import org.apache.camel.Message;
import org.apache.camel.support.DefaultMessage;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ObjectFactory;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.subscriber.SubscriberCachableService;
import se.skltp.ei.service.util.JaxbUtil;
import se.skltp.ei.subscriber.Subscriber;

@Component
@Log4j2
public class NotificationSplitterBean {
  private static JaxbUtil jabxUtil = new JaxbUtil(UpdateType.class, ProcessNotificationType.class);
  private static riv.itintegration.engagementindex.processnotificationresponder._1.ObjectFactory objectFactoryProcessNotification = new ObjectFactory();

  @Autowired
  private SubscriberCachableService subscriberCache;

  public List<Message> createNotificationList(@Body List<EngagementTransactionType> engagementsChangedList, @Header(X_SKLTP_CORRELATION_ID) String correlationId, CamelContext camelContext) {

    log.debug("Incomming changed engagements: {}", engagementsChangedList.size());

    ProcessNotificationType process = new ProcessNotificationType();
    List<Subscriber> subscribers = subscriberCache.getSubscribers();

    List<Message> answerList = new ArrayList();
    for (Subscriber subscriber : subscribers) {
      process.getEngagementTransaction().clear();
      process.getEngagementTransaction().addAll(subscriber.filter(engagementsChangedList));


      if ( !process.getEngagementTransaction().isEmpty()) {
        final String marshalledNotification = jabxUtil.marshal(objectFactoryProcessNotification.createProcessNotification(process));
        answerList.add(createMessage(marshalledNotification, subscriber.getNotificationQueueName(), camelContext, correlationId));
      }
    }

    log.debug("notifications-queue-size: {}", answerList.size());
    return answerList;
  }

  private Message createMessage(String body, String queueName, CamelContext camelContext, String correlationId){
    DefaultMessage message = new DefaultMessage(camelContext);
    message.setHeader("NotificationQueueName", queueName);
    message.setHeader(X_SKLTP_CORRELATION_ID, correlationId);
    message.setBody(body);
    return message;
  }
}
