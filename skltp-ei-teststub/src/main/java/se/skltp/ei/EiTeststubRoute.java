package se.skltp.ei;

import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import riv.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontract._2.rivtabp21.GetLogicalAddresseesByServiceContractResponderInterface;
import riv.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontract._2.rivtabp21.GetLogicalAddresseesByServiceContractResponderService;
import riv.itintegration.engagementindex.processnotification._1.rivtabp21.ProcessNotificationResponderInterface;
import riv.itintegration.engagementindex.processnotification._1.rivtabp21.ProcessNotificationResponderService;

@Component
@Profile("teststub")
public class EiTeststubRoute extends RouteBuilder {

  public static final String PROCESSNOTIFICATION_WSDL="/schemas/TD_ENGAGEMENTINDEX_1_0_R/interactions/ProcessNotificationInteraction/ProcessNotificationInteraction_1.0_RIVTABP21.wsdl";
  public static final String NOTIFICATION_MOCK = "mock:notification:input";
  public static final String LOGICALADDREESS_MOCK = "mock:logicaladdress:input";

  @Autowired
  ProcessNotificationTestStubProcessor processNotificationTestStubProcessor;

  @Autowired
  GetLogicalAddreessesTeststubProcessor getLogicalAddreessesTeststubProcessor;

  @EndpointInject(NOTIFICATION_MOCK)
  MockEndpoint notificationMock;

  @EndpointInject(LOGICALADDREESS_MOCK)
  MockEndpoint logicalAddressMock;

  public MockEndpoint getNotificationMock() {
    return notificationMock;
  }

  public MockEndpoint getLogicalAddressMock() {
    return logicalAddressMock;
  }

  @Override
  public void configure() throws Exception {
    fromF("cxf:{{teststub.notification.serviceEndpointUrl}}?wsdlURL=%s&serviceClass=%s&portName=%s"
        , PROCESSNOTIFICATION_WSDL
        , ProcessNotificationResponderInterface.class.getName()
        , ProcessNotificationResponderService.ProcessNotificationResponderPort.toString())
        .id("teststub-processnotification-route")
        .log("Teststub received processnotification")
        .to(NOTIFICATION_MOCK)
        .process(processNotificationTestStubProcessor);

    fromF("cxf:{{teststub.logicaladdreesses.serviceEndpointUrl}}?serviceClass=%s&portName=%s"
        , GetLogicalAddresseesByServiceContractResponderInterface.class.getName()
        , GetLogicalAddresseesByServiceContractResponderService.GetLogicalAddresseesByServiceContractResponderPort.toString())
        .id("teststub-getlogicaladdreeses-route")
        .log("getlogicaladdreeses called")
        .to(LOGICALADDREESS_MOCK)
        .process(getLogicalAddreessesTeststubProcessor);

  }


}
