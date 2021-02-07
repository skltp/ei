package se.skltp.ei.updateprocess;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.cxf.message.MessageContentsList;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import se.skltp.ei.service.util.JaxbUtil;

public class CreateProcessNotificationRequestProcessor implements Processor {

  private static JaxbUtil jabxUtil = new JaxbUtil(ProcessNotificationType.class);

  private String logicalAddress;

  public CreateProcessNotificationRequestProcessor(String logicalAddress) {
    this.logicalAddress = logicalAddress;
  }

  @Override
  public void process(Exchange exchange) throws Exception {

    ProcessNotificationType processNotification = (ProcessNotificationType)jabxUtil.unmarshal(exchange.getIn().getBody());

    MessageContentsList messageContentsList = new MessageContentsList();
    messageContentsList.add(0, logicalAddress);
    messageContentsList.add(1, processNotification);
    exchange.getIn().setBody(messageContentsList);
  }
}
