package se.skltp.ei.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.skltp.ei.subscriber.SubscriberStatusProcessorNew;

@Component
public class EiBackendSubscriberStatusRoute extends RouteBuilder {

  @Autowired
  SubscriberStatusProcessorNew subscriberStatusProcessor;

  @Override
  public void configure() throws Exception {
    from("jetty://{{subscriber.cache.status.url}}").routeId("backend-status-cache-route")
        .process(subscriberStatusProcessor)
        .setHeader("Content-type", simple("application/json"));
  }

}
