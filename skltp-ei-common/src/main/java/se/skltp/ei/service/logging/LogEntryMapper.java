package se.skltp.ei.service.logging;

import static org.apache.camel.Exchange.CORRELATION_ID;
import static se.skltp.ei.service.constants.EiConstants.X_SKLTP_CORRELATION_ID;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.log4j.Log4j2;
import org.apache.cxf.ext.logging.event.DefaultLogEventMapper;
import org.apache.cxf.message.Message;
import org.apache.logging.log4j.ThreadContext;

@Log4j2
public class LogEntryMapper {

  public static final String MSG_TYPE_LOG_REQ_IN = "req-in";
  public static final String MSG_TYPE_LOG_REQ_OUT = "req-out";
  public static final String MSG_TYPE_LOG_RESP_IN = "resp-in";
  public static final String MSG_TYPE_LOG_RESP_OUT = "resp-out";
  public static final String MSG_TYPE_ERROR = "error";

  protected static final DefaultLogEventMapper eventMapper = new DefaultLogEventMapper();
  protected static Set<String> sensitiveProtocolHeaderNames = new HashSet();

  protected static String hostName = "UNKNOWN (UNKNOWN)";

  static {
    try {
      // Let's give it a try, fail silently...
      InetAddress host = InetAddress.getLocalHost();
      hostName = String.format("%s (%s)",
          host.getCanonicalHostName(),
          host.getHostAddress());
    } catch (Exception ex) {
      log.warn("Failed get runtime values for logging", ex);
    }
  }

  public static LogEntry map(Message message) {
    LogEntry logEntry = new LogEntry();
    logEntry.setLogEvent(eventMapper.map(message, sensitiveProtocolHeaderNames));

    logEntry.hostName = hostName;

    String correlationId = getCorrelationId(message);
    if(correlationId != null){
      message.getExchange().put(CORRELATION_ID, correlationId);
      ThreadContext.put("corr.id", String.format("[%s]", correlationId));
      logEntry.setBusinessCorrelationId(correlationId);
    }

    return logEntry;
  }

  private static String getCorrelationId(Message message) {
    String corrId = (String) message.getExchange().get(CORRELATION_ID);
    if (corrId != null) {
      return corrId;
    }
    return getHeader(X_SKLTP_CORRELATION_ID, message);
  }


  private static String getHeader(String headerName, Message message) {
    final Map headers = (Map) message.get(Message.PROTOCOL_HEADERS);
    if (headers != null) {
      List headerList = (List) headers.get(headerName);
      if (headerList != null && !headerList.isEmpty()) {
        return (String) headerList.get(0);
      }
    }
    return null;
  }
}

