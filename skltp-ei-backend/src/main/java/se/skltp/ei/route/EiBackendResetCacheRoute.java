package se.skltp.ei.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import se.skltp.ei.subscriber.SubscriberService;

@Component
public class EiBackendResetCacheRoute extends RouteBuilder {

  @Autowired
  private CacheManager cacheManager;

  @Autowired
  private SubscriberService subscriberService;

  @Override
  public void configure() throws Exception {
    from("jetty://{{subscriber.cache.reset.url}}").routeId("backend-reset-cache-route")
        .process(ex -> clearAndRefresh())
        .setBody(simple("Subscriber cache reset"));
  }

  private void clearAndRefresh() {
    cacheManager.getCache("subscriber-cache").clear();
    subscriberService.getSubscribers();
  }
}
