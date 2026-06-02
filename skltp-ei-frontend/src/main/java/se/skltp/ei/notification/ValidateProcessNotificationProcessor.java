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
