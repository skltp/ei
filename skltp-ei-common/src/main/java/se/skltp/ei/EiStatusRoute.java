/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v2.1 or later.
 * Please refer to the full license files at the project root.
 */
package se.skltp.ei;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;
import se.skltp.ei.service.EICxfConfigurer;

@Component
public class EiStatusRoute extends RouteBuilder {

  @Autowired
  GenericApplicationContext applicationContext;

  @Value("${ei.status.url:#{null}}")
  String statusUrl;

  @Autowired
  GetStatusProcessor getStatusProcessor;

  @Override
  public void configure() throws Exception {
    if (statusUrl != null) {
      from("jetty://"+statusUrl).routeId("status-route")
          .process(getStatusProcessor);
    }
  }
}
