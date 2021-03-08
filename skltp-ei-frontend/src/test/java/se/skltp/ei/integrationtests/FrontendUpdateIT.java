package se.skltp.ei.integrationtests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;

import io.hawt.util.Files;
import se.skltp.ei.EiFrontendApplication;

@CamelSpringBootTest
@SpringBootTest(classes = {EiFrontendApplication.class})
public class FrontendUpdateIT {

  @Produce
  protected ProducerTemplate producerTemplate;

  @Autowired
  BuildProperties buildProperties;

  private static final String UPDATE_URL="{{update.webservice.url}}?throwExceptionOnFailure=false";
  
  private static final String UPDATE1 = "Update1.xml";
  private static final String UPDATE_DUPLICATE = "Update_duplicate.xml";

  @Value("${ei.hsa.id}")
  private String owner;
  
  @Test
  public void updateTestReturnsOK() throws IOException {

    String body = getBody(UPDATE1);
    
    Map<String, Object> headers = new HashMap<String, Object>();
    String statusResponse = producerTemplate.requestBodyAndHeaders(UPDATE_URL, body, headers, String.class);
    assertTrue (statusResponse .startsWith("<") && statusResponse .endsWith(">"));
    assertTrue (statusResponse.contains("<ns2:ResultCode>OK</ns2:ResultCode>"));
    assertTrue (statusResponse.contains("<ns2:UpdateResponse"));
  }

  @Test
  public void updateTestReturnsEI002() throws IOException {

    Exchange ex = producerTemplate.request(UPDATE_URL, (e) -> {
    	
        String body = getBody(UPDATE_DUPLICATE);
        
        e.getIn().setBody(body);
        e.getIn().setHeader("Content-Type", "application/xml;charset=UTF-8");
    });
    String statusResponse = (String) ex.getMessage().getBody(String.class);
    Integer statusCode = ex.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
    String statusText = ex.getMessage().getHeader(Exchange.HTTP_RESPONSE_TEXT, String.class);
    
    assertTrue(Integer.compare(statusCode,500) == 0);
    assertTrue ("Server Error".equals(statusText));
    
    assertTrue (statusResponse .startsWith("<") && statusResponse .endsWith(">"));
    assertTrue (statusResponse.contains("<soap:Fault>"));
    assertTrue (statusResponse.contains("EI002"));
  }

  @Test
  public void updateTestReturnsEI003() throws IOException {

    Exchange ex = producerTemplate.request(UPDATE_URL, (e) -> {
    	
        String body = getBody(UPDATE1);
        // Trigger EI003
        body = body.replaceAll(owner, "wronglogicaladdress");
        
        e.getIn().setBody(body);
        e.getIn().setHeader("Content-Type", "application/xml;charset=UTF-8");
    });
    String statusResponse = (String) ex.getMessage().getBody(String.class);
    Integer statusCode = ex.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
    String statusText = ex.getMessage().getHeader(Exchange.HTTP_RESPONSE_TEXT, String.class);
    
    assertTrue(Integer.compare(statusCode,500) == 0);
    assertTrue ("Server Error".equals(statusText));
    
    assertTrue (statusResponse .startsWith("<") && statusResponse .endsWith(">"));
    assertTrue (statusResponse.contains("<soap:Fault>"));
    assertTrue (statusResponse.contains("EI003"));
  }

  @Test
  public void updateTestReturnsEI004() throws IOException {

    Exchange ex = producerTemplate.request(UPDATE_URL, (e) -> {
    	
        String body = getBody(UPDATE1);
        // Trigger EI004
        body = body.replaceAll("urn2:logicalAddress", "urn2:logicalAddrezz");
       
        e.getIn().setBody(body);
        e.getIn().setHeader("Content-Type", "application/xml;charset=UTF-8");
    });
    String statusResponse = (String) ex.getMessage().getBody(String.class);
    Integer statusCode = ex.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
    String statusText = ex.getMessage().getHeader(Exchange.HTTP_RESPONSE_TEXT, String.class);
    
    assertTrue(Integer.compare(statusCode,500) == 0);
    assertTrue ("Server Error".equals(statusText));
    assertTrue (statusResponse .startsWith("<") && statusResponse .endsWith(">"));
    assertTrue (statusResponse.contains("<soap:Fault>"));
    assertTrue (statusResponse.contains("EI004"));
  }
  
  @Test
  public void updateTestReturnsEI004Whitespace() throws IOException {
   
   Exchange ex = producerTemplate.request(UPDATE_URL, (e) -> {
    	
	    String body = getBody(UPDATE1);
	    // Trigger EI004 (white space)
	    body = body.replaceAll("urn2:logicalAddress>", "urn2:logicalAddress> ");
       
        e.getIn().setBody(body);
        e.getIn().setHeader("Content-Type", "application/xml;charset=UTF-8");
    });
    String statusResponse = (String) ex.getMessage().getBody(String.class);
    Integer statusCode = ex.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
    String statusText = ex.getMessage().getHeader(Exchange.HTTP_RESPONSE_TEXT, String.class);
    
    assertTrue (statusResponse .startsWith("<") && statusResponse .endsWith(">"));
    assertTrue (statusResponse.contains("<soap:Fault>"));
    assertTrue (statusResponse.contains("EI004"));
    assertTrue (statusResponse.contains("white space"));
  }
  
  private String getBody(String resource) throws IOException {
		String file = this.getClass().getResource("/" + resource).getPath();
		return new String(Files.readBytes(new File(file)));	  
  }
}

