/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v2.1 or later.
 * Please refer to the full license files at the project root.
 */
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
