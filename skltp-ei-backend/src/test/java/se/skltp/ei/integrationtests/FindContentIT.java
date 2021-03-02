package se.skltp.ei.integrationtests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.File;
import java.io.IOException;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import io.hawt.util.Files;
import se.skltp.ei.EiBackendApplication;
import se.skltp.ei.entity.repository.EngagementRepository;
import se.skltp.ei.util.EngagementTestUtil;

@CamelSpringBootTest
@SpringBootTest(classes = {EiBackendApplication.class})
@ActiveProfiles("teststub")
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class FindContentIT {
	
	private static final String FINDCONTENT_FILE = "FindContent.xml";

  @Produce
  protected ProducerTemplate producerTemplate;

  @Autowired
  private EngagementRepository engagementRepository;
  
  @Autowired
  BuildProperties buildProperties;

  
  @Value("${findcontent.webservice.url}")
  String url;
 
  @BeforeEach
  public void beforeEach() {
    // Clear database between tests
    engagementRepository.deleteAll();

  }
  
  @Test
  public void findContentResponseOneHitTest() throws IOException {

	String body = getBody(FINDCONTENT_FILE);

    // Insert one entity
    engagementRepository.save(EngagementTestUtil.generateEngagement(1212121212L));
    engagementRepository.save(EngagementTestUtil.generateEngagement(1312121212L));

    String statusResponse = producerTemplate.requestBody(url, body, String.class);
    assertTrue (statusResponse .startsWith("<") && statusResponse .endsWith(">"));
    assertTrue (statusResponse.contains("<ns2:registeredResidentIdentification>191212121212</ns2:registeredResidentIdentification>"));
    assertFalse (statusResponse.contains("<ns2:registeredResidentIdentification>191312121212</ns2:registeredResidentIdentification>"));
    assertTrue (statusResponse.contains("<engagement>"));
  }

  @Test
  public void findContentResponseZeroHitsTest() throws IOException {

	String body = getBody(FINDCONTENT_FILE);

    // Insert one entity
    engagementRepository.save(EngagementTestUtil.generateEngagement(1312121212L));

    String statusResponse = producerTemplate.requestBody(url, body, String.class);
    assertTrue (statusResponse .startsWith("<") && statusResponse .endsWith(">"));
    assertFalse (statusResponse.contains("<ns2:registeredResidentIdentification>191212121212</ns2:registeredResidentIdentification>"));
    assertFalse (statusResponse.contains("<ns2:registeredResidentIdentification>191312121212</ns2:registeredResidentIdentification>"));
    assertFalse (statusResponse.contains("<engagement>"));
  }
  private String getBody(String resource) throws IOException {
		String file = this.getClass().getResource("/" + resource).getPath();
		return new String(Files.readBytes(new File(file)));	  
  }

}

