package se.skltp.ei.update;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.cxf.message.MessageContentsList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.util.EngagementValidator;

@Service
public class ValidateUpdateProcessor implements Processor {

  @Autowired
  EngagementValidator engagementValidator;

  @Override
  public void process(Exchange exchange) throws Exception {
    MessageContentsList update = exchange.getIn().getBody(MessageContentsList.class);
    engagementValidator.validateUpdate((String)update.get(0), (UpdateType) update.get(1));
  }
}
