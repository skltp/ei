/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v2.1 or later.
 * Please refer to the full license files at the project root.
 */
package se.skltp.ei.notification;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.cxf.message.MessageContentsList;
import org.springframework.stereotype.Service;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ObjectFactory;
import se.skltp.ei.service.constants.EiConstants;
import se.skltp.ei.service.util.JaxbUtil;

@Service
public class ProcessNotificationRequestToJmsMsgProcessor implements Processor {

  private static JaxbUtil jabxUtil = new JaxbUtil(ProcessNotificationType.class);
  private static ObjectFactory of = new ObjectFactory();

  @Override
  public void process(Exchange exchange) {
    MessageContentsList updateContentsList = exchange.getIn().getBody(MessageContentsList.class);

    ProcessNotificationType processNotificationType = (ProcessNotificationType) updateContentsList.get(1);
    String jmsMsg = jabxUtil.marshal(of.createProcessNotification(processNotificationType));
    exchange.getIn().setBody(jmsMsg);

    exchange.setProperty(EiConstants.LOGICAL_ADRESS, updateContentsList.get(0));
    exchange.setProperty(EiConstants.EI_LOG_NUMBER_OF_RECORDS_IN_MESSAGE, processNotificationType.getEngagementTransaction().size());
    exchange.setProperty(EiConstants.EI_LOG_MESSAGE_TYPE, EiConstants.EI_LOG_MESSAGE_TYPE_PROCESS_NOTIFICATION);

  }
}
