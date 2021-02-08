package se.skltp.ei;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
public class EiBackendResetCacheRoute extends RouteBuilder {

  @Autowired
  private CacheManager cacheManager;

  @Override
  public void configure() throws Exception {
    from("jetty://{{subscriber.cache.reset.url}}").routeId("backend-reset-cache-route")
        .process(ex->{
          cacheManager.getCache("subscriber-cache").clear();
        })
        .setBody(simple("Subscriber cache reset"));
  }
}
