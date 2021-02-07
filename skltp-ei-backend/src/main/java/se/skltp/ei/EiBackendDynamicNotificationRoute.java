package se.skltp.ei;

import static se.skltp.ei.service.constants.EiConstants.X_VP_INSTANCE_ID;
import static se.skltp.ei.service.constants.EiConstants.X_VP_SENDER_ID;

import org.apache.camel.builder.RouteBuilder;
import riv.itintegration.engagementindex.processnotification._1.rivtabp21.ProcessNotificationResponderInterface;
import riv.itintegration.engagementindex.processnotification._1.rivtabp21.ProcessNotificationResponderService;
import se.skltp.ei.subscriber.Subscriber;
import se.skltp.ei.updateprocess.CreateProcessNotificationRequestProcessor;

public class EiBackendDynamicNotificationRoute extends RouteBuilder {

  Subscriber subscriber;

  public static final String PROCESSNOTIFICATION_WSDL="/schemas/TD_ENGAGEMENTINDEX_1_0_R/interactions/ProcessNotificationInteraction/ProcessNotificationInteraction_1.0_RIVTABP21.wsdl";

  public EiBackendDynamicNotificationRoute(Subscriber subscriber) {
    this.subscriber = subscriber;
  }

  @Override
  public void configure() {
    fromF("activemq:queue:%s?transacted=true", subscriber.getNotificationQueueName())
        .id(subscriber.getNotificationRouteName())
        .log("Got from notification queue")
        .process(new CreateProcessNotificationRequestProcessor(subscriber.getLogicalAdress()))
        .setHeader(X_VP_SENDER_ID, simple("{{processnotification.vpSenderId}}"))
        .setHeader(X_VP_INSTANCE_ID, simple("{{processnotification.vpInstanceId}}"))
        .toF("cxf:{{processnotification.serviceEndpointUrl}}?wsdlURL=%s&serviceClass=%s&portName=%s"
            , PROCESSNOTIFICATION_WSDL
            , ProcessNotificationResponderInterface.class.getName()
            , ProcessNotificationResponderService.ProcessNotificationResponderPort.toString());
  }


}
