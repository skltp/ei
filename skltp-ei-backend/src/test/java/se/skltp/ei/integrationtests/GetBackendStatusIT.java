package se.skltp.ei.integrationtests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import se.skltp.ei.EiBackendApplication;
import se.skltp.ei.EiTeststubRoute;

@CamelSpringBootTest
@SpringBootTest(classes = {EiBackendApplication.class, EiTeststubRoute.class})
@ActiveProfiles("teststub")
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class GetBackendStatusIT {

  @Produce
  protected ProducerTemplate producerTemplate;

  @Autowired
  BuildProperties buildProperties;

  @Test
  public void getStatusResponseTest() {

    String name = buildProperties.getName();
    String version = buildProperties.getVersion();

    String statusResponse = producerTemplate.requestBody("{{ei.status.url}}", "body", String.class);
    assertTrue (statusResponse .startsWith("{") && statusResponse .endsWith("}"));
    assertTrue (statusResponse.contains("Name\" : \"" + name));
    assertTrue (statusResponse.contains("Version\" : \"" + version));
    assertTrue (statusResponse.contains("ServiceStatus\" : \"Started"));
  }

}

