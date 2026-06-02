/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v2.1 or later.
 * Please refer to the full license files at the project root.
 */
package se.skltp.ei.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.skltp.ei.subscriber.SubscriberStatusProcessor;

@Component
public class EiBackendSubscriberStatusRoute extends RouteBuilder {

  @Autowired
  SubscriberStatusProcessor subscriberStatusProcessor;

  @Override
  public void configure() throws Exception {
    from("jetty://{{subscriber.cache.status.url}}").routeId("backend-status-cache-route")
        .process(subscriberStatusProcessor)
        .setHeader("Content-type", simple("application/json"));
  }

}
