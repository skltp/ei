package se.skltp.ei.route;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;
import riv.itintegration.engagementindex.findcontent._1.rivtabp21.FindContentResponderInterface;
import riv.itintegration.engagementindex.findcontent._1.rivtabp21.FindContentResponderService;
import se.skltp.ei.findcontent.FindContentProcessor;
import se.skltp.ei.service.EICxfConfigurer;

@Component
public class EiBackendFindContentRoute extends RouteBuilder {

  public static final String FINDCONTENT_WSDL="/schemas/TD_ENGAGEMENTINDEX_1_0_R/interactions/FindContentInteraction/FindContentInteraction_1.0_RIVTABP21.wsdl";

  @Autowired
  FindContentProcessor findContentProcessor;

  @Autowired
  GenericApplicationContext applicationContext;

  @Value("${findcontent.webservice.url}")
  String findcontentWebserviceUrl;

  @Value("${log.backend.logger.name}")
  String backendLoggerName;

  @Value("${log.max.payload.size}")
  int maxPayloadSize;

  @Override
  public void configure() throws Exception {
    applicationContext.registerBean("eiBackendConfigBean", EICxfConfigurer.class,
        ()->new EICxfConfigurer(maxPayloadSize, backendLoggerName, "ei-backend"));


    // Get from process queue
    fromF("cxf:%s?wsdlURL=%s&serviceClass=%s&portName=%s&cxfConfigurer=#eiBackendConfigBean"
            , findcontentWebserviceUrl
            , FINDCONTENT_WSDL
            , FindContentResponderInterface.class.getName()
            , FindContentResponderService.FindContentResponderPort.toString())
    .id("backend-findcontent-route")
    .log(LoggingLevel.DEBUG, "eiBackendLog","Findcontent SOAP call received")
    .process(findContentProcessor);


  }
}
