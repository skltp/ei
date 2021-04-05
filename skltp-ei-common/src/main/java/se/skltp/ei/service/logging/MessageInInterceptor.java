package se.skltp.ei.service.logging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.apache.cxf.ext.logging.event.LogEvent;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.commons.lang.StringUtils;


public class MessageInInterceptor extends AbstractPhaseInterceptor {

  private MessageLogEventSender sender;
  protected int limit = 49152;

  public MessageInInterceptor(MessageLogEventSender sender) {
    super(Phase.PRE_INVOKE);
    this.sender = sender;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  @Override
  public void handleMessage(Message message) {
    changeEncodingToUTF8(message);

    createExchangeId(message);
    final LogEntry event = LogEntryMapper.map(message);
    if (sender.isDebugEnabled()) {
      addPayload(message, event.getLogEvent());
    }
    sender.send(event);
  }

  void changeEncodingToUTF8(Message message) {
    String encoding = (String) message.get(Message.ENCODING);

    if (StringUtils.isEmpty(encoding) || !encoding.equals(StandardCharsets.UTF_8.name())) {
      message.put(Message.ENCODING, StandardCharsets.UTF_8.name());
    }
  }

  public void createExchangeId(Message message) {
    Exchange exchange = message.getExchange();
    String exchangeId = (String) exchange.get("exchangeId");
    if (exchangeId == null) {
      exchange.put("exchangeId", UUID.randomUUID().toString());
    }
  }


  private void addPayload(Message message, final LogEvent event) {
    try {
      CachedOutputStream cos = message.getContent(CachedOutputStream.class);
      if (cos != null) {
        handleOutputStream(event, message, cos);
      }
    } catch (IOException e) {
      throw new Fault(e);
    }
  }

    private void handleOutputStream(final LogEvent event, Message message, CachedOutputStream cos) throws IOException {
      String encoding = (String) message.get(Message.ENCODING);
      if (StringUtils.isEmpty(encoding)) {
        encoding = StandardCharsets.UTF_8.name();
      }
      StringBuilder payload = new StringBuilder();
      cos.writeCacheTo(payload, encoding, limit);
      cos.close();
      event.setPayload(payload.toString());
      boolean isTruncated = cos.size() > limit && limit != -1;
      event.setTruncated(isTruncated);
      event.setFullContentFile(cos.getTempFile());
    }



}
