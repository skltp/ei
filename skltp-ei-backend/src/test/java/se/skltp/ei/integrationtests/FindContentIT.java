package se.skltp.ei.integrationtests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.bean.ProxyHelper;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.cxf.binding.soap.SoapFault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import io.hawt.util.Files;
import riv.itintegration.engagementindex.findcontent._1.rivtabp21.FindContentResponderInterface;
import riv.itintegration.engagementindex.findcontent._1.rivtabp21.FindContentResponderService;
import riv.itintegration.engagementindex.findcontentresponder._1.FindContentResponseType;
import riv.itintegration.engagementindex.findcontentresponder._1.FindContentType;
import se.skltp.ei.EiBackendApplication;
import se.skltp.ei.entity.repository.EngagementRepository;
import se.skltp.ei.util.EngagementTestUtil;
import se.skltp.ei.util.EngagementTestUtil.DomainType;

@CamelSpringBootTest
@SpringBootTest(classes = {EiBackendApplication.class})
@ActiveProfiles("teststub")
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class FindContentIT {
	
	private static final String FINDCONTENT_FILE = "FindContent.xml";
	private static final String FINDCONTENT_WSDL="/schemas/TD_ENGAGEMENTINDEX_1_0_R/interactions/FindContentInteraction/FindContentInteraction_1.0_RIVTABP21.wsdl";

  @Produce
  protected ProducerTemplate producerTemplate;

  @Autowired
  private EngagementRepository engagementRepository;
  
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

    Exchange ex = producerTemplate.request(url, (e) -> {
        e.getIn().setBody(body);
        e.getIn().setHeader("Content-Type", "application/xml;charset=UTF-8");
    });
    String statusResponse = (String) ex.getMessage().getBody(String.class);
    Integer statusCode = ex.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
    String statusText = ex.getMessage().getHeader(Exchange.HTTP_RESPONSE_TEXT, String.class);
    //{Accept-Encoding=gzip,deflate, CamelHttpResponseCode=200, CamelHttpResponseText=OK, Content-Length=931, Content-Type=text/xml;charset=iso-8859-1, Date=Mon, 08 Mar 2021 09:39:05 GMT, Host=localhost:8082, Server=Jetty(9.4.31.v20200723), User-Agent=Apache-HttpClient/4.5.13 (Java/1.8.0_161)}
    assertTrue(Integer.compare(statusCode,200) == 0);
    assertTrue ("OK".equals(statusText));

    assertTrue (statusResponse .startsWith("<") && statusResponse .endsWith(">"));
    assertTrue (statusResponse.contains("<ns2:registeredResidentIdentification>191212121212</ns2:registeredResidentIdentification>"));
    assertFalse (statusResponse.contains("<ns2:registeredResidentIdentification>191312121212</ns2:registeredResidentIdentification>"));
    assertTrue (statusResponse.contains("<engagement>"));
  }

  @Test
  public void findContentResponseEI000Test() throws IOException {

	String body = getBody(FINDCONTENT_FILE).replace("<urn1:serviceDomain>TEST-DOMAIN</urn1:serviceDomain>", "");

    // Insert one entity
    engagementRepository.save(EngagementTestUtil.generateEngagement(1212121212L));
    engagementRepository.save(EngagementTestUtil.generateEngagement(1312121212L));

    Exchange ex = producerTemplate.request(url + "?throwExceptionOnFailure=false", (e) -> {
        e.getIn().setBody(body);
        e.getIn().setHeader("Content-Type", "application/xml;charset=UTF-8");
    });
    String statusResponse = (String) ex.getMessage().getBody(String.class);
    Integer statusCode = ex.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
    String statusText = ex.getMessage().getHeader(Exchange.HTTP_RESPONSE_TEXT, String.class);
    assertTrue(Integer.compare(statusCode,500) == 0);
    assertTrue ("Server Error".equals(statusText));
    assertTrue (statusResponse .startsWith("<") && statusResponse .endsWith(">"));
	assertTrue(statusResponse.contains("EI000"));
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

  @Test
  public void findContentCxfTest() throws Exception {
	  
	  String route= String.format("cxf:%s?wsdlURL=%s&serviceClass=%s&portName=%s"
	  , url
	  , FINDCONTENT_WSDL
	  , FindContentResponderInterface.class.getName()
	  , FindContentResponderService.FindContentResponderPort.toString());
	
	  // Insert one entity
	  engagementRepository.save(EngagementTestUtil.generateEngagement(1212121212L, DomainType.TWO_SUBSCRIBERS));
	  engagementRepository.save(EngagementTestUtil.generateEngagement(1212121212L, DomainType.NO_SUBSCRIBER_2));
	    
	  FindContentType fc = new FindContentType();
	  fc.setRegisteredResidentIdentification("191212121212");
	  fc.setServiceDomain("TEST-DOMAIN");
	  Endpoint startEndpoint = producerTemplate.getCamelContext().getEndpoint(route);
	  ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
	  FindContentResponderInterface proxy = ProxyHelper.createProxy(startEndpoint, classLoader, FindContentResponderInterface.class);
	  
	  FindContentResponseType response = (FindContentResponseType)proxy.findContent("123", fc);
	  
	  assertTrue(response.getEngagement().size() == 1);
  }
  
  @Test
  public void findContentCxfEI000FaultTest() throws Exception {
	  
	  
	  String route= String.format("cxf:%s?wsdlURL=%s&serviceClass=%s&portName=%s"
      , url
      , FINDCONTENT_WSDL
      , FindContentResponderInterface.class.getName()
      , FindContentResponderService.FindContentResponderPort.toString());


      // Insert one entity
      engagementRepository.save(EngagementTestUtil.generateEngagement(1212121212L, DomainType.TWO_SUBSCRIBERS));
      engagementRepository.save(EngagementTestUtil.generateEngagement(1212121212L, DomainType.NO_SUBSCRIBER_2));
    
	    
	  FindContentType fc = new FindContentType();
	  fc.setRegisteredResidentIdentification("191212121212");
	  //fc.setServiceDomain("TEST-DOMAIN");
	  Endpoint startEndpoint = producerTemplate.getCamelContext().getEndpoint(route);
	  ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
	  FindContentResponderInterface proxy = ProxyHelper.createProxy(startEndpoint, classLoader, FindContentResponderInterface.class);
	  
	  try {
		  proxy.findContent("123", fc);
		  fail("Supposed to throw SoapFault");
	  } catch(SoapFault e) {
		  assertTrue(e.getMessage().contains("EI000"));
	  } 
  }
}

