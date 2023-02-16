package se.skltp.ei.route;

import static se.skltp.ei.service.constants.EiConstants.X_VP_INSTANCE_ID;
import static se.skltp.ei.service.constants.EiConstants.X_VP_SENDER_ID;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.DeadLetterChannelBuilder;
import org.apache.camel.builder.RouteBuilder;

import lombok.extern.log4j.Log4j2;
import riv.itintegration.engagementindex.processnotification._1.rivtabp21.ProcessNotificationResponderInterface;
import riv.itintegration.engagementindex.processnotification._1.rivtabp21.ProcessNotificationResponderService;
import se.skltp.ei.subscriber.Subscriber;
import se.skltp.ei.updateprocess.CreateProcessNotificationRequestProcessor;

/**
 * Route definition for sending notifications to subscribers.
 * <br><br>
 * Queue name: EI.NOTIFICATION.subscriber
 * <br>where subscriber is a logical address.
 *
 * <br><br>
 * The message is routed through vp (processnotification.serviceEndpointUrl) to
 * the subscriber. This requires that the X_VP_* headers are set to allow access
 * to vp.
 */
@Log4j2
public class EiBackendDynamicNotificationRoute extends RouteBuilder {

    Subscriber subscriber;

    private int maximumRedeliveries = 0;
    private int redeliveryDelay = 0;
    private double backOffMultiplier = 0.0d;
    private boolean useExponentialBackOff = false;

    public static final String PROCESSNOTIFICATION_WSDL = "/schemas/TD_ENGAGEMENTINDEX_1_0_R/interactions/ProcessNotificationInteraction/ProcessNotificationInteraction_1.0_RIVTABP21.wsdl";

    public EiBackendDynamicNotificationRoute(Subscriber subscriber, int maximumRedeliveries, int redeliveryDelay, double backOffMultiplier, boolean useExponentialBackOff) {
        this.subscriber = subscriber;
        this.maximumRedeliveries = maximumRedeliveries;
        this.redeliveryDelay = redeliveryDelay;
        this.backOffMultiplier = backOffMultiplier;
        this.useExponentialBackOff = useExponentialBackOff;
    }

    private String getDeadLetterQueueName() {
        return "DLQ.".concat(subscriber.getNotificationQueueName());
    }

    @Override
    public void configure() {

        DeadLetterChannelBuilder builder = deadLetterChannel(String.format("activemq:queue:%s", getDeadLetterQueueName()));

        if (useExponentialBackOff) {
            log.info("Using exponential backoff for " + getDeadLetterQueueName() + " with backoff multiplier: " + backOffMultiplier);
            builder.useExponentialBackOff()
                    .backOffMultiplier(backOffMultiplier);
        }
        builder.useOriginalMessage()
                .maximumRedeliveries(maximumRedeliveries)
                .redeliveryDelay(redeliveryDelay)
                .onRedelivery(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        log.error("Redelivery no "
                                + exchange.getIn().getHeader(Exchange.REDELIVERY_COUNTER, Integer.class)
                                + " from " + subscriber.getNotificationQueueName());
                    }
                });

        errorHandler(builder);

        fromF("activemq:queue:%s?transacted=true", subscriber.getNotificationQueueName())
                .id(subscriber.getNotificationRouteName())
                .log(LoggingLevel.DEBUG, "eiBackendLog", "Got from notification queue")
                .process(new CreateProcessNotificationRequestProcessor(subscriber.getLogicalAdress()))
                .setHeader(X_VP_SENDER_ID, simple("{{processnotification.vpSenderId}}"))
                .setHeader(X_VP_INSTANCE_ID, simple("{{processnotification.vpInstanceId}}"))
                .toF("cxf:{{processnotification.serviceEndpointUrl}}?wsdlURL=%s&serviceClass=%s&portName=%s&cxfConfigurer=#eiBackendConfigBean",
                        PROCESSNOTIFICATION_WSDL,
                        ProcessNotificationResponderInterface.class.getName(),
                        ProcessNotificationResponderService.ProcessNotificationResponderPort.toString());
    }

}
