package se.skltp.ei;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EiStatusRoute extends RouteBuilder {

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
