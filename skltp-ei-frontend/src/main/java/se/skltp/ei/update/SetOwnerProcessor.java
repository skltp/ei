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
