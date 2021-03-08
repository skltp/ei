package se.skltp.ei.integrationtests;

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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;

import io.hawt.util.Files;
import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.EngagementType;
import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.processnotification._1.rivtabp21.ProcessNotificationResponderInterface;
import riv.itintegration.engagementindex.processnotification._1.rivtabp21.ProcessNotificationResponderService;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationResponseType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import se.skltp.ei.EiFrontendApplication;
import se.skltp.ei.service.util.EntityTransformer;
import se.skltp.ei.util.EngagementTestUtil;
import se.skltp.ei.util.EngagementTestUtil.DomainType;

@CamelSpringBootTest
@SpringBootTest(classes = {EiFrontendApplication.class})
public class FrontendProcessNotificationIT {

  @Produce
  protected ProducerTemplate producerTemplate;

  @Autowired
  BuildProperties buildProperties;

  
  @Value("${processnotification.webservice.url}")
  String url;
  
  private static final String PROCNOT_URL="{{processnotification.webservice.url}}?throwExceptionOnFailure=false";
  
  private static final String PROCNOTE1 = "ProcessNotification.xml";
  private static final String PROCNOTE_DUPLICATE = "ProcessNotification_duplicate.xml";
  public static final String PROCESSNOTIFICATION_WSDL="/schemas/TD_ENGAGEMENTINDEX_1_0_R/interactions/ProcessNotificationInteraction/ProcessNotificationInteraction_1.0_RIVTABP21.wsdl";

  
  @Test
  public void processnotificationTestReturnsOK() throws IOException {

    Exchange ex = producerTemplate.request(PROCNOT_URL, (e) -> {
    	
        String body = getBody(PROCNOTE1);
        
        e.getIn().setBody(body);
        e.getIn().setHeader("Content-Type", "application/xml;charset=UTF-8");
    });
    String statusResponse = (String) ex.getMessage().getBody(String.class);
    Integer statusCode = ex.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
    String statusText = ex.getMessage().getHeader(Exchange.HTTP_RESPONSE_TEXT, String.class);
    
    assertTrue(Integer.compare(statusCode,200) == 0);
    assertTrue ("OK".equals(statusText));
    
    assertTrue (statusResponse .startsWith("<") && statusResponse .endsWith(">"));
    assertTrue (statusResponse.contains("<ns2:ResultCode>OK</ns2:ResultCode>"));
    assertTrue (statusResponse.contains("<ns2:ProcessNotificationResponse"));
  }

  @Test
  public void processnotificationTestReturnsEI004() throws IOException {

    
    Exchange ex = producerTemplate.request(PROCNOT_URL, (e) -> {
    	
    	// Trigger EI004
        String body = getBody(PROCNOTE1).replaceAll("urn2:logicalAddress", "urn2:logicalAddrezz");
        
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
  public void processnotificationTestReturnsEI002() throws IOException {

    Exchange ex = producerTemplate.request(PROCNOT_URL, (e) -> {
    	
    	// Trigger EI002
        String body = getBody(PROCNOTE_DUPLICATE);
        
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

  private String getBody(String resource) throws IOException {
		String file = this.getClass().getResource("/" + resource).getPath();
		return new String(Files.readBytes(new File(file)));	  
  }
  
  @Test
  public void procnotCxfTest() throws Exception {
	  
	  String route= String.format("cxf:%s?wsdlURL=%s&serviceClass=%s&portName=%s"
      , url
      , PROCESSNOTIFICATION_WSDL
      , ProcessNotificationResponderInterface.class.getName()
      , ProcessNotificationResponderService.ProcessNotificationResponderPort.toString());  
	    
	  ProcessNotificationType pn = new ProcessNotificationType();
	  EngagementTransactionType ett = new EngagementTransactionType();
	  ett.setDeleteFlag(false);
	  EngagementType et = EntityTransformer.fromEntity(EngagementTestUtil.generateEngagement(1212121212L, DomainType.TWO_SUBSCRIBERS));
	  ett.setEngagement(et);
	  pn.getEngagementTransaction().add(ett);
	  
	  Endpoint startEndpoint = producerTemplate.getCamelContext().getEndpoint(route);
	  ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
	  ProcessNotificationResponderInterface proxy = ProxyHelper.createProxy(startEndpoint, classLoader, ProcessNotificationResponderInterface.class);
	  
	  ProcessNotificationResponseType response = (ProcessNotificationResponseType)proxy.processNotification("123", pn);
	  
	  assertTrue(ResultCodeEnum.OK == response.getResultCode());
  }
  
  @Test
  public void procnotCxfEI000FaultTest() throws Exception {
	  
	  
	  String route= String.format("cxf:%s?wsdlURL=%s&serviceClass=%s&portName=%s"
      , url
      , PROCESSNOTIFICATION_WSDL
      , ProcessNotificationResponderInterface.class.getName()
      , ProcessNotificationResponderService.ProcessNotificationResponderPort.toString());


	  ProcessNotificationType pn = new ProcessNotificationType();
	  EngagementTransactionType ett = new EngagementTransactionType();
	  ett.setDeleteFlag(false);
	  EngagementType et = EntityTransformer.fromEntity(EngagementTestUtil.generateEngagement(1212121212L, DomainType.TWO_SUBSCRIBERS));
	  et.setLogicalAddress(null);
	  ett.setEngagement(et);
	  pn.getEngagementTransaction().add(ett);
	  
	  Endpoint startEndpoint = producerTemplate.getCamelContext().getEndpoint(route);
	  ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
	  ProcessNotificationResponderInterface proxy = ProxyHelper.createProxy(startEndpoint, classLoader, ProcessNotificationResponderInterface.class);
	  
	  try {
		  proxy.processNotification("123", pn);
		  fail("Supposed to throw SoapFault");
	  } catch(SoapFault e) {
		  assertTrue(e.getMessage().contains("EI004"));
	  } 
  }

}

