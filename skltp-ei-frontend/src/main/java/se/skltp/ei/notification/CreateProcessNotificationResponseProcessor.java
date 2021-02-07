package se.skltp.ei.notification;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Service;
import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationResponseType;

@Service
public class CreateProcessNotificationResponseProcessor implements Processor {

  @Override
  public void process(Exchange exchange) throws Exception {
    ProcessNotificationResponseType response = new ProcessNotificationResponseType();
    response.setComment(null);
    response.setResultCode(ResultCodeEnum.OK);
    exchange.getIn().setBody(response);
  }
}
