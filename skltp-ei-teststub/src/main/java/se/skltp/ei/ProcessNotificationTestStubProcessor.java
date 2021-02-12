package se.skltp.ei;

import static se.skltp.ei.service.api.EiErrorCodeEnum.EI000_TECHNICAL_ERROR;
import static se.skltp.ei.service.constants.EiConstants.X_VP_INSTANCE_ID;
import static se.skltp.ei.service.constants.EiConstants.X_VP_SENDER_ID;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationResponseType;
import se.skltp.ei.service.api.EiException;

@Service
@Data
@Log4j2
public class ProcessNotificationTestStubProcessor implements Processor {

  private static final String EXCEPTION_MSG = "%s. \nExpected: %s Actual:%s";

  @Autowired
  TeststubProcessNotificationConfig config;

  @Override
  public void process(Exchange exchange) throws Exception {
    checkValue(config.getExpectedInstanceId(), (String) exchange.getIn().getHeader(X_VP_INSTANCE_ID), "Not expected instanceid" );
    checkValue(config.getExpectedSenderId(),  (String) exchange.getIn().getHeader(X_VP_SENDER_ID), "Not expected sender id" );

    exchange.getIn().setBody( createResponse());
  }

  private void checkValue(String expected, String actual, String message) {
    if(actual==null || !expected.equalsIgnoreCase(actual)){
      throw new EiException(EI000_TECHNICAL_ERROR, String.format(EXCEPTION_MSG, message, actual, expected));
    }
  }

  private ProcessNotificationResponseType createResponse() {
    ProcessNotificationResponseType response = new ProcessNotificationResponseType();
    response.setComment(null);
    response.setResultCode(ResultCodeEnum.OK);
    return response;
  }
}
