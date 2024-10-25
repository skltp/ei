package se.skltp.ei.integrationtests;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.skltp.ei.EiBackendApplication;
import se.skltp.ei.EiTeststubRoute;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.skltp.ei.EiTeststubRoute.LOGICALADDREESS_MOCK;

@CamelSpringBootTest
@SpringBootTest(classes = {EiBackendApplication.class, EiTeststubRoute.class})
@ActiveProfiles("teststub")
public class ResetSubscriberCacheIT {
  @Produce
  protected ProducerTemplate producerTemplate;

  @EndpointInject(LOGICALADDREESS_MOCK)
  private MockEndpoint logicalAddresseesMock;

  @Test
  public void resetSubscriberCacheTest() throws InterruptedException {
    logicalAddresseesMock.expectedMessageCount(1);
    logicalAddresseesMock.assertIsSatisfied();

    String resetResponse = producerTemplate.requestBody("{{subscriber.cache.reset.url}}", "", String.class);

    assertTrue (resetResponse.contains("Subscriber cache reset"));
    logicalAddresseesMock.expectedMessageCount(2);
    logicalAddresseesMock.assertIsSatisfied();
  }

}
