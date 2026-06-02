/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v2.1 or later.
 * Please refer to the full license files at the project root.
 */
package se.skltp.ei.update;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.cxf.message.MessageContentsList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;

@Service
public class SetOwnerProcessor implements Processor {

  @Value("${ei.hsa.id}")
  private String owner;

  @Override
  public void process(Exchange exchange) {

    // According to R6 owner should always be set to owner of the index
    MessageContentsList updateContentsList = exchange.getIn().getBody(MessageContentsList.class);
    UpdateType updateType = (UpdateType) updateContentsList.get(1);
    updateType.getEngagementTransaction().forEach(ett -> ett.getEngagement().setOwner(owner));
  }
}
