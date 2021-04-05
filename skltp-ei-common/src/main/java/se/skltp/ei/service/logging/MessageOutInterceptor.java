package se.skltp.ei.service.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.ext.logging.event.LogEvent;
import org.apache.cxf.interceptor.StaxOutInterceptor;
import org.apache.cxf.io.CacheAndWriteOutputStream;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.io.CachedOutputStreamCallback;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Log4j2
public class MessageOutInterceptor extends AbstractPhaseInterceptor<Message> {

  private MessageLogEventSender sender;
  protected int limit = 49152;

  public MessageOutInterceptor(MessageLogEventSender sender) {
    super(Phase.PRE_STREAM);
    this.sender = sender;
    addBefore(StaxOutInterceptor.class.getName());
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  public void handleMessage(Message message) {
    createExchangeId(message);
    final OutputStream os = message.getContent(OutputStream.class);
    if (os != null) {
      LoggingCallback callback = new LoggingCallback(sender, message, os, limit);
      message.setContent(OutputStream.class, createCachingOut(os, callback));
    }
  }

  private OutputStream createCachingOut(final OutputStream os, CachedOutputStreamCallback callback) {
    final CacheAndWriteOutputStream newOut = new CacheAndWriteOutputStream(os);
    if (limit > 0) {
      // make the limit for the cache greater than the limit for the truncated payload in the log event,
      // this is necessary for finding out that the payload was truncated
      //(see boolean isTruncated = cos.size() > limit && limit != -1;)  in method copyPayload
      newOut.setCacheLimit(getCacheLimit());
    }
    newOut.registerCallback(callback);
    return newOut;
  }

  private int getCacheLimit() {
    if (limit == Integer.MAX_VALUE) {
      return limit;
    }
    return limit + 1;
  }

  public void createExchangeId(Message message) {
    Exchange exchange = message.getExchange();
    String exchangeId = (String) exchange.get("exchangeId");
    if (exchangeId == null) {
      exchangeId = UUID.randomUUID().toString();
      exchange.put("exchangeId", exchangeId);
    }
  }

  public class LoggingCallback implements CachedOutputStreamCallback {

    private final Message message;
    private final OutputStream origStream;
    private final int lim;
    private MessageLogEventSender sender;

    public LoggingCallback(final MessageLogEventSender sender, final Message msg,
        final OutputStream os, int limit) {
      this.sender = sender;
      this.message = msg;
      this.origStream = os;
      this.lim = limit == -1 ? Integer.MAX_VALUE : limit;
    }

    @Override
    public void onFlush(CachedOutputStream cos) {
      // Do nothing on flush
    }

    @Override
    public void onClose(CachedOutputStream cos) {
      final LogEntry event = LogEntryMapper.map(message);
      if (sender.isDebugEnabled()) {
        copyPayload(cos, event.getLogEvent());
      }
      sender.send(event);
      try {
        // empty out the cache
        cos.lockOutputStream();
        cos.resetOut(null, false);
      } catch (Exception ex) {
        // ignore
      }
      message.setContent(OutputStream.class, origStream);
    }

    private void copyPayload(CachedOutputStream cos, final LogEvent event) {
      try {
        String encoding = (String) message.get(Message.ENCODING);
        StringBuilder payload = new StringBuilder();
        writePayload(payload, cos, encoding);
        event.setPayload(payload.toString());
        boolean isTruncated = cos.size() > limit && limit != -1;
        event.setTruncated(isTruncated);
      } catch (Exception ex) {
        // ignore
      }
    }

    protected void writePayload(StringBuilder builder, CachedOutputStream cos, String encoding)
        throws IOException {
      if (StringUtils.isEmpty(encoding)) {
        cos.writeCacheTo(builder, lim);
      } else {
        cos.writeCacheTo(builder, encoding, lim);
      }
    }
  }
}
