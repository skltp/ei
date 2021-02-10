package se.skltp.ei.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import riv.itintegration.engagementindex.findcontent._1.rivtabp21.FindContentResponderInterface;
import riv.itintegration.engagementindex.findcontent._1.rivtabp21.FindContentResponderService;
import se.skltp.ei.findcontent.FindContentProcessor;

@Component
public class EiBackendFindContentRoute extends RouteBuilder {

  public static final String FINDCONTENT_WSDL="/schemas/TD_ENGAGEMENTINDEX_1_0_R/interactions/FindContentInteraction/FindContentInteraction_1.0_RIVTABP21.wsdl";

  @Autowired
  FindContentProcessor findContentProcessor;

  @Value("${findcontent.webservice.url}")
  String findcontentWebserviceUrl;

  @Override
  public void configure() throws Exception {
    // Get from process queue
    fromF("cxf:%s?wsdlURL=%s&serviceClass=%s&portName=%s"
            , findcontentWebserviceUrl
            , FINDCONTENT_WSDL
            , FindContentResponderInterface.class.getName()
            , FindContentResponderService.FindContentResponderPort.toString())
    .id("backend-findcontent-route")
    .log("Findcontent SOAP call received")
    .process(findContentProcessor);


  }
}
