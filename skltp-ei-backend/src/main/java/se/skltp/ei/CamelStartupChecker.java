package se.skltp.ei;

import lombok.extern.log4j.Log4j2;
import org.apache.camel.CamelContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Log4j2
@Component
public class CamelStartupChecker {

  private final CamelContext camelContext;

  public CamelStartupChecker(CamelContext camelContext) {
    log.info("Startup Breadcrumbs: Camel Startup checker constructed.");
    this.camelContext = camelContext;
  }

  @PostConstruct
  public void checkCamelContext() {
    //System.out.println("Camel Context State: " + camelContext.getStatus());
    log.info("Startup Breadcrumbs: Camel Context State: {}", camelContext.getStatus());
  }
}

