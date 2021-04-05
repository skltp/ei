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
