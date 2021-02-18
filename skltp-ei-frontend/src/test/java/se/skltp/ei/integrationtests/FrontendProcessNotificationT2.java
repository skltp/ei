package se.skltp.ei.integrationtests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;

import io.hawt.util.Files;
import se.skltp.ei.EiFrontendApplication;

@CamelSpringBootTest
@SpringBootTest(classes = {EiFrontendApplication.class})
public class FrontendProcessNotificationT2 {

  @Produce
  protected ProducerTemplate producerTemplate;

  @Autowired
  BuildProperties buildProperties;

  private static final String PROCNOT_URL="{{processnotification.webservice.url}}?throwExceptionOnFailure=false";
  
  private static final String UPDATE1 = "src/test/resources/ProcessNotification.xml";
  private static final String UPDATE_DUPLICATE = "src/test/resources/ProcessNotification_duplicate.xml";

  @Test
  public void processnotificationTestReturnsOK() throws IOException {

    String body = new String(Files.readBytes(new File(UPDATE1)));
    
    Map<String, Object> headers = new HashMap<String, Object>();
    String statusResponse = producerTemplate.requestBodyAndHeaders(PROCNOT_URL, body, headers, String.class);
    assertTrue (statusResponse .startsWith("<") && statusResponse .endsWith(">"));
    assertTrue (statusResponse.contains("<ns2:ResultCode>OK</ns2:ResultCode>"));
    assertTrue (statusResponse.contains("<ns2:ProcessNotificationResponse"));
  }

  @Test
  public void processnotificationTestReturnsDuplicateError() throws IOException {

    String body = new String(Files.readBytes(new File(UPDATE_DUPLICATE)));
    
    Map<String, Object> headers = new HashMap<String, Object>();
    String statusResponse = producerTemplate.requestBodyAndHeaders(PROCNOT_URL, body, headers, String.class);
    assertTrue (statusResponse .startsWith("<") && statusResponse .endsWith(">"));
    assertTrue (statusResponse.contains("<soap:Fault>"));
    assertTrue (statusResponse.contains("EI002"));
  }

}

