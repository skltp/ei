package se.skltp.ei;

import static org.apache.camel.ExchangePattern.InOnly;
import static se.skltp.ei.service.constants.EiConstants.EI_LOG_NUMBER_OF_RECORDS_IN_MESSAGE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.ErrorHandlerBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.cxf.binding.soap.SoapFault;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import riv.itintegration.engagementindex.processnotification._1.rivtabp21.ProcessNotificationResponderInterface;
import riv.itintegration.engagementindex.processnotification._1.rivtabp21.ProcessNotificationResponderService;
import riv.itintegration.engagementindex.update._1.rivtabp21.UpdateResponderInterface;
import riv.itintegration.engagementindex.update._1.rivtabp21.UpdateResponderService;
import se.skltp.ei.errorhandling.ExceptionMessageProcessor;
import se.skltp.ei.notification.CreateProcessNotificationResponseProcessor;
import se.skltp.ei.notification.RemoveCircularProcessNotificationsProcessor;
import se.skltp.ei.notification.ProcessNotificationRequestToJmsMsgProcessor;
import se.skltp.ei.notification.ValidateProcessNotificationProcessor;
import se.skltp.ei.service.api.EiException;
import se.skltp.ei.service.util.ConvertRequestCharset;
import se.skltp.ei.service.util.ConvertResponseCharset;
import se.skltp.ei.update.SetOwnerProcessor;
import se.skltp.ei.update.UpdateRequestToJmsMessageProcessor;
import se.skltp.ei.update.CreateUpdateResponseProcessor;
import se.skltp.ei.update.ValidateUpdateProcessor;

@Component
public class EiFrontendRoute extends RouteBuilder {

  public static final String UPDATE_WSDL="/schemas/TD_ENGAGEMENTINDEX_1_0_R/interactions/UpdateInteraction/UpdateInteraction_1.0_RIVTABP21.wsdl";
  public static final String PROCESSNOTIFICATION_WSDL="/schemas/TD_ENGAGEMENTINDEX_1_0_R/interactions/ProcessNotificationInteraction/ProcessNotificationInteraction_1.0_RIVTABP21.wsdl";

  public static final String UPDATE_SERVICE_CONFIGURATION = "cxf:%s"
      + "?wsdlURL=%s"
      + "&serviceClass=%s"
      + "&portName=%s";


  @Autowired
  ValidateUpdateProcessor validateUpdateProcessor;

  @Autowired
  UpdateRequestToJmsMessageProcessor updateRequestToJmsMessageProcessor;

  @Autowired
  CreateUpdateResponseProcessor createUpdateResponseProcessor;

  @Autowired
  ValidateProcessNotificationProcessor validateProcessNotificationProcessor;

  @Autowired
  SetOwnerProcessor setOwnerProcessor;

  @Autowired
  ProcessNotificationRequestToJmsMsgProcessor processNotificationRequestToJmsMsgProcessor;

  @Autowired
  CreateProcessNotificationResponseProcessor createProcessNotificationResponseProcessor;

  @Autowired
  RemoveCircularProcessNotificationsProcessor removeCircularProcessNotificationsProcessor;

  @Autowired
  ExceptionMessageProcessor exceptionMessageProcessor;
  
  @Value("${update.collect.threshold:0}")
  int collectThreshold;

  @Value("${collect.queue.name:collect}")
  String collectQueueName;

  @Value("${process.queue.name:process}")
  String processQueueName;

  @Value("${update.webservice.url}")
  String updateWebserviceUrl;

  @Value("${processnotification.webservice.url}")
  String notificationWebserviceUrl;

  @Autowired
  private ConvertRequestCharset convertRequestCharset;

  @Autowired
  private ConvertResponseCharset convertResponseCharset;
  
  public static final String LOG_ERROR_METHOD = "logError(*,${exception.stacktrace})";
  public static final String LOG_RESP_OUT_METHOD = "logRespOut(*)";
  public static final String LOG_REQ_IN_METHOD = "logReqIn(*)";
  public static final String LOG_REQ_OUT_METHOD = "logReqOut(*)";
  public static final String LOG_RESP_IN_METHOD = "logRespIn(*)";
  
  @Override
  public void configure() throws Exception {

    String updateConfigurationPath = String.format(UPDATE_SERVICE_CONFIGURATION
        , updateWebserviceUrl
        , UPDATE_WSDL
        , UpdateResponderInterface.class.getName()
        , UpdateResponderService.UpdateResponderPort.toString());


    String processNotificationConfigurationPath = String.format(UPDATE_SERVICE_CONFIGURATION
        , notificationWebserviceUrl
        , PROCESSNOTIFICATION_WSDL
        , ProcessNotificationResponderInterface.class.getName()
        , ProcessNotificationResponderService.ProcessNotificationResponderPort.toString());

    
    //onException(EiException.class)

    //.process(exceptionMessageProcessor);
    // .bean(MessageInfoLogger.class, LOG_ERROR_METHOD)
    // .process(convertResponseCharset)
    // .removeHeaders(headerFilter.getResponseHeadersToRemove(), headerFilter.getResponseHeadersToKeep())
    // .bean(MessageInfoLogger.class, LOG_RESP_OUT_METHOD)
    // .handled(true);
    
    from(updateConfigurationPath)
        .id("frontend-update-webservice-route")
        .log("Update SOAP call received")
        .process(validateUpdateProcessor)
        .process(setOwnerProcessor)
        .process(updateRequestToJmsMessageProcessor)
        .choice()
          .when(exchangeProperty(EI_LOG_NUMBER_OF_RECORDS_IN_MESSAGE).isGreaterThan(collectThreshold))
            .to(InOnly, "activemq:queue:"+processQueueName)
          .otherwise()
            .to(InOnly,"activemq:queue:"+collectQueueName)
        .end()
        .process(createUpdateResponseProcessor);
    	

    from(processNotificationConfigurationPath).streamCaching()
        .id("frontend-notification-webservice-route")
        .log("ProcessNotification SOAP call received")
        .process(validateProcessNotificationProcessor)
        .process(removeCircularProcessNotificationsProcessor)
        .process(processNotificationRequestToJmsMsgProcessor)
        .choice()
          .when(exchangeProperty(EI_LOG_NUMBER_OF_RECORDS_IN_MESSAGE).isGreaterThan(0))
            .to(InOnly, "activemq:queue:"+processQueueName)
        .end()
        .process(createProcessNotificationResponseProcessor);



  }

}
