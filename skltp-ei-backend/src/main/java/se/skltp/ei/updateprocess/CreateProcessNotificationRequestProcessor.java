/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v2.1 or later.
 * Please refer to the full license files at the project root.
 */
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
