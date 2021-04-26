package se.skltp.ei.service.logging;

import lombok.Data;
import org.apache.cxf.ext.logging.event.LogEvent;

@Data
public class LogEntry {

  protected LogEvent logEvent;

  protected String msgType;

  protected String hostName;

  protected String componentId;

  protected String messageId;
  protected String businessCorrelationId;

  protected String payload;
}
