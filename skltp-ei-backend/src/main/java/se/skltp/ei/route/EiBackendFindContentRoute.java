/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v2.1 or later.
 * Please refer to the full license files at the project root.
 */
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
import se.skltp.ei.service.CheckInboundHeadersProcessor;
import se.skltp.ei.service.EICxfConfigurer;

@Component
public class EiBackendFindContentRoute extends RouteBuilder {

  public static final String FINDCONTENT_WSDL="/schemas/TD_ENGAGEMENTINDEX_1_0_R/interactions/FindContentInteraction/FindContentInteraction_1.0_RIVTABP21.wsdl";

  @Autowired
  FindContentProcessor findContentProcessor;

  @Autowired
  GenericApplicationContext applicationContext;

  @Autowired
  CheckInboundHeadersProcessor checkInboundHeadersProcessor;

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
        .process(checkInboundHeadersProcessor)
        .process(findContentProcessor);


  }
}
