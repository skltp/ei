package se.skltp.ei;

import lombok.extern.log4j.Log4j2;
import org.apache.camel.CamelContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * This Spring Boot Component does explicit checks on the Camel Context during startup,
 *    and also tells Spring Boot that a Camel Context needs to be created and held on to.<br>
 * If this class is removed or disabled, Spring Boot might not realize that a Camel Context is needed
 *    when starting on a server environment, while the Component scan is performed.<br>
 * Disabling this class can cause a discrepancy between local development and server runtime,
 *    as there might be debugging frameworks in use locally, that internally created a need for a Camel Context;
 *    and those debug frameworks might not be active in a production environment.
 */
@Log4j2
@Component
public class CamelContextComponent {

  private final CamelContext camelContext;

  /**
   * Constructor that takes a CamelContext. Will get automatically called during Spring Boot's component scan and autoconfiguration.
   * @param camelContext This gets provided by Spring Boot automatically.
   */
  public CamelContextComponent(CamelContext camelContext) {
    log.info("Startup Breadcrumbs: Camel Startup checker constructed.");
    this.camelContext = camelContext;
  }

  @PostConstruct
  public void checkCamelContext() {
    log.info("Startup Breadcrumbs: Camel Context State: {}", camelContext.getStatus());
  }
}

