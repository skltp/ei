package se.skltp.ei.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.DeadLetterChannelBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.skltp.ei.updateprocess.NotificationSplitterBean;
import se.skltp.ei.updateprocess.UpdatePersistentStorageProcessor;

@Component
public class EiBackendUpdateRoute extends RouteBuilder {

    @Value("${process.queue.name:process}")
    String processQueueName;

    @Autowired
    UpdatePersistentStorageProcessor updatePersistentStorageProcessor;

    @Autowired
    NotificationSplitterBean notificationSplitterBean;

    @Value("${activemq.broker.maximum-redeliveries:0}")
    private int maximumRedeliveries;

    @Value("${activemq.broker.redelivery-delay:0}")
    private int redeliveryDelay;

    @Value("${activemq.broker.backoff-multiplier:0.0}")
    private Double backOffMultiplier;

    @Value("${activemq.broker.use-exponential-backoff:false}")
    private Boolean useExponentialBackoff;

    @Override
    public void configure() throws Exception {

        DeadLetterChannelBuilder builder = deadLetterChannel(String.format("activemq:queue:DLQ.%s", processQueueName));
        if (useExponentialBackoff) {
            log.info("Using exponential backoff for " + processQueueName + " with backoff multiplier: " + backOffMultiplier);
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
                                + " from " + processQueueName);
                    }
                });

        errorHandler(builder);

        // Get from process queue
        fromF("activemq:queue:%s?transacted=true", processQueueName)
                .id("backend-process-route")
                .log(LoggingLevel.DEBUG, "eiBackendLog", "Got one from Process Queue:\n${body}")
                .process(updatePersistentStorageProcessor)
                .split(method(notificationSplitterBean, "createNotificationList"))
                .parallelProcessing(true)
                .log(LoggingLevel.DEBUG, "eiBackendLog", "Add notification to ${header.NotificationQueueName}")
                .toD("activemq:queue:${header.NotificationQueueName}")
                .end();

    }

}
