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
    write(b, "  ComponentId", componentId);
    write(b, "  LogMessage", type2LogMessage(msgEvent.logEvent.getType()));
    write(b, "  BusinessCorrelationId", msgEvent.businessCorrelationId);
    write(b, "Host", msgEvent.hostName);
    write(b, "ServiceImpl", msgEvent.logEvent.getServiceName().getLocalPart());
    write(b, "Endpoint", msgEvent.logEvent.getAddress());
    write(b, "MessageId", msgEvent.logEvent.getMessageId());
    write(b, "Payload", msgEvent.logEvent.getPayload());
    b.append("** logEvent-info.end *************************************************************");
    return b.toString();
  }



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
