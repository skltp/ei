package se.skltp.ei.subscriber;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
public class SubscriberStatusProcessorNew implements Processor {

  @Autowired
  CacheManager cacheManager;

  @Override
  public void process(Exchange exchange) throws Exception {
    // TODO: Implement processing.
  }
}
