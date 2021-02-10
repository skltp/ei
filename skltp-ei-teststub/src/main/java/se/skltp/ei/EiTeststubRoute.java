package se.skltp.ei;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import riv.itintegration.engagementindex.processnotification._1.rivtabp21.ProcessNotificationResponderInterface;
import riv.itintegration.engagementindex.processnotification._1.rivtabp21.ProcessNotificationResponderService;
import se.rivta.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontract.v2.rivtabp21.GetLogicalAddresseesByServiceContractResponderInterface;
import se.rivta.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontract.v2.rivtabp21.GetLogicalAddresseesByServiceContractResponderService;

@Component
@Profile("teststub")
public class EiTeststubRoute extends RouteBuilder {

  public static final String PROCESSNOTIFICATION_WSDL="/schemas/TD_ENGAGEMENTINDEX_1_0_R/interactions/ProcessNotificationInteraction/ProcessNotificationInteraction_1.0_RIVTABP21.wsdl";

  @Autowired
  ProcessNotificationTestStubProcessor processNotificationTestStubProcessor;

  @Autowired
  GetLogicalAddreessesTeststubProcessor getLogicalAddreessesTeststubProcessor;

  @Override
  public void configure() throws Exception {
    fromF("cxf:{{teststub.notification.serviceEndpointUrl}}?wsdlURL=%s&serviceClass=%s&portName=%s"
        , PROCESSNOTIFICATION_WSDL
        , ProcessNotificationResponderInterface.class.getName()
        , ProcessNotificationResponderService.ProcessNotificationResponderPort.toString())
        .id("teststub-processnotification-route")
        .log("Teststub received processnotification")
        .process(processNotificationTestStubProcessor);

    fromF("cxf:{{teststub.logicaladdreesses.serviceEndpointUrl}}?serviceClass=%s&portName=%s"
        , GetLogicalAddresseesByServiceContractResponderInterface.class.getName()
        , GetLogicalAddresseesByServiceContractResponderService.GetLogicalAddresseesByServiceContractResponderPort.toString())
        .id("teststub-getlogicaladdreeses-route")
        .log("getlogicaladdreeses called")
        .process(getLogicalAddreessesTeststubProcessor);

  }
}
