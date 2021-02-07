package se.skltp.ei.notification;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.cxf.message.MessageContentsList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import se.skltp.ei.util.EngagementValidator;

@Service
public class ValidateProcessNotificationProcessor implements Processor {
  @Autowired
  EngagementValidator engagementValidator;

  @Override
  public void process(Exchange exchange) throws Exception {
    MessageContentsList update = exchange.getIn().getBody(MessageContentsList.class);
    engagementValidator.validateProcessNotification((ProcessNotificationType) update.get(1));
  }
}
