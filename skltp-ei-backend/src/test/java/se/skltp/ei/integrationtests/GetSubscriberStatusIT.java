package se.skltp.ei.integrationtests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import se.skltp.ei.EiBackendApplication;

@CamelSpringBootTest
@SpringBootTest(classes = {EiBackendApplication.class})
@ActiveProfiles("teststub")
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class GetSubscriberStatusIT {
  @Produce
  protected ProducerTemplate producerTemplate;

  @Test
  public void getStatusResponseTest() {

    String statusResponse = producerTemplate.requestBody("{{subscriber.cache.status.url}}", "body", String.class);
    assertTrue (statusResponse .startsWith("{") && statusResponse .endsWith("}"));
    assertTrue (statusResponse.contains("CreationTime"));
    assertTrue (statusResponse.contains("TEST-DOMAIN"));

  }

}
