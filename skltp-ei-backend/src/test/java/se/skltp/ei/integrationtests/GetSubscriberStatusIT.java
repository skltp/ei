/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v2.1 or later.
 * Please refer to the full license files at the project root.
 */
package se.skltp.ei.integrationtests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.skltp.ei.EiBackendApplication;
import se.skltp.ei.EiTeststubRoute;

@CamelSpringBootTest
@SpringBootTest(classes = {EiBackendApplication.class, EiTeststubRoute.class})
@ActiveProfiles("teststub")
class GetSubscriberStatusIT {
  @Produce
  protected ProducerTemplate producerTemplate;

  @Test
  void getStatusResponseTest() {

    String statusResponse = producerTemplate.requestBody("{{subscriber.cache.status.url}}", "body", String.class);

    assertTrue (statusResponse .startsWith("{") && statusResponse .endsWith("}")); // Test JSON wrapping.
    assertTrue (statusResponse.contains("Subscribers"));
    assertTrue (statusResponse.contains("TEST-DOMAIN"));
  }
}
