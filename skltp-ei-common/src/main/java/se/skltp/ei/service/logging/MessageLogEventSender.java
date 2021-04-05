package se.skltp.ei.service.logging;

import static se.skltp.ei.service.logging.LogEntryMapper.MSG_TYPE_ERROR;
import static se.skltp.ei.service.logging.LogEntryMapper.MSG_TYPE_LOG_REQ_OUT;
import static se.skltp.ei.service.logging.LogEntryMapper.MSG_TYPE_LOG_RESP_IN;
import static se.skltp.ei.service.logging.LogEntryMapper.MSG_TYPE_LOG_RESP_OUT;


import lombok.extern.log4j.Log4j2;
import org.apache.cxf.ext.logging.event.EventType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

@Log4j2
public class MessageLogEventSender {

  private static final Marker MESSAGE_LOG_MARKER = MarkerManager.getMarker("EI_MESSAGE_LOG");

  private Logger logger;
  private String componentId;

  public MessageLogEventSender(String loggerName, String componentId){
    this.componentId = componentId;
    this.logger = LogManager.getLogger(loggerName);
  }

  protected boolean isDebugEnabled(){
    return logger.isDebugEnabled();
  }

  public void send(LogEntry event) {
    logger.info(MESSAGE_LOG_MARKER, getLogMessage(event));
  }

  public String getLogMessage(LogEntry msgEvent) {
    StringBuilder b = new StringBuilder();
    b.append("skltp-messages\n");
    b.append("** logEvent-info.start ***********************************************************\n");
    write(b, "LogMessage", type2LogMessage(msgEvent.logEvent.getType()));
    write(b, "Host", msgEvent.hostName);
    write(b, "ServiceImpl", msgEvent.logEvent.getServiceName().getLocalPart());
    write(b, "ComponentId", componentId);
    write(b, "Endpoint", msgEvent.logEvent.getAddress());
    write(b, "MessageId", msgEvent.logEvent.getMessageId());
    write(b, "BusinessCorrelationId", msgEvent.businessCorrelationId);
    write(b, "Payload", msgEvent.logEvent.getPayload());
    b.append("** logEvent-info.end *************************************************************");
    return b.toString();
  }

 /*
 2021-03-25 13:45:03,220 INFO  [[skltp-ei-application-mule-frontend-app].soitoolkit-http-connector.receiver.97] org.soitoolkit.commons.mule.messageLogger - soi-toolkit.log
** logEvent-info.start ***********************************************************

ServiceImpl=update-service
Host=ind-stjp-ei1.ind1.sth.basefarm.net (10.252.7.173)
ComponentId=skltp-ei-application-mule-frontend-app
Endpoint=http://0.0.0.0:8081/skltp-ei/update-service/v1 (POST on /skltp-ei/update-service/v1)
MessageId=eb350340-8d67-11eb-825c-005056a1101d
BusinessCorrelationId=7e18e71c-6f9c-4afd-9163-f7650f456e0b
BusinessContextId=
ExtraInfo=
-originalServiceconsumerHsaid=SE2321000016-A38X
Payload=org.apache.commons.httpclient.ContentLengthInputStream@7510deec
** logEvent-info.end *************************************************************
  */

  private static String type2LogMessage(EventType type) {
    switch(type){
      case REQ_IN:
        return LogEntryMapper.MSG_TYPE_LOG_REQ_IN;

      case REQ_OUT:
        return MSG_TYPE_LOG_REQ_OUT;

      case RESP_IN:
        return MSG_TYPE_LOG_RESP_IN;

      case RESP_OUT:
        return MSG_TYPE_LOG_RESP_OUT;

      case FAULT_IN:
        return MSG_TYPE_ERROR;

      case FAULT_OUT:
        return MSG_TYPE_ERROR;

      default:
        return type.name();
    }
  }

  protected static void write(StringBuilder b, String key, String value) {
    if (value != null) {
      b.append(key).append("=").append(value).append("\n");
    }
  }
}
