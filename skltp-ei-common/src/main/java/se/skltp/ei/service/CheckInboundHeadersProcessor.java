package se.skltp.ei.service;

import static se.skltp.ei.service.constants.EiConstants.X_SKLTP_CORRELATION_ID;

import lombok.extern.log4j.Log4j2;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Service;


@Service
@Log4j2
public class CheckInboundHeadersProcessor implements Processor {

  private static final String HEADER_MISSING_MSG = "Mandatory HTTP header x-skltp-correlation-id is missing\n";

  @Override
  public void process(Exchange exchange)  {

    String correlationId = exchange.getIn().getHeader(X_SKLTP_CORRELATION_ID, String.class);

    if (log.isDebugEnabled()) {
      log.debug(X_SKLTP_CORRELATION_ID + " = " + correlationId);
    }

    if (correlationId == null) {
      throw new IllegalArgumentException(HEADER_MISSING_MSG);
    }
  }
}
