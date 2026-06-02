/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v2.1 or later.
 * Please refer to the full license files at the project root.
 */
package se.skltp.ei.notification;

import java.util.Iterator;
import lombok.Data;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.cxf.message.MessageContentsList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;

@Service
@Data
public class RemoveCircularProcessNotificationsProcessor implements Processor {

  @Value("${ei.hsa.id}")
  private String owner;

  @Override
  public void process(Exchange exchange) throws Exception {

    MessageContentsList updateContentsList = exchange.getIn().getBody(MessageContentsList.class);
    ProcessNotificationType processNotificationType = (ProcessNotificationType) updateContentsList.get(1);
    removeCircularNotifications(processNotificationType);
  }

  protected ProcessNotificationType removeCircularNotifications(ProcessNotificationType processNotificationType) {
    final Iterator<EngagementTransactionType> iter = processNotificationType.getEngagementTransaction().iterator();
    while (iter.hasNext()) {
      EngagementTransactionType e = iter.next();
      if (e.getEngagement().getOwner().equals(owner)) {
        iter.remove();
      }
    }
    return processNotificationType;
  }
}
