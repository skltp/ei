package se.skltp.ei.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.DeadLetterChannelBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EiBackendCollectRoute extends RouteBuilder {

    @Value("${collect.queue.name:collect}")
    String collectQueueName;

    @Value("${process.queue.name:process}")
    String processQueueName;

    @Value("${collect.queue.completion.size:1000}")
    Integer collectQueueCompletionSize;

    @Value("${collect.queue.completion.timeout:30}")
    Integer collectQueueCompletionTimeout;

    @Value("${activemq.broker.maximum-redeliveries:0}")
    private int maximumRedeliveries;

    @Value("${activemq.broker.redelivery-delay:0}")
    private int redeliveryDelay;

    @Value("${activemq.broker.use-exponential-backoff:false}")
    private Boolean useExponentialBackoff;

    @Value("${activemq.broker.backoff-multiplier:0.0}")
    private Double backOffMultiplier;

    @Value("${activemq.broker.maximum-redelivery-delay:0}")
    private int maximumRedeliveryDelay;

    @Override
    public void configure() throws Exception {

        DeadLetterChannelBuilder builder = deadLetterChannel(String.format("activemq:queue:DLQ.%s", collectQueueName));
        builder.useOriginalMessage()
                .maximumRedeliveries(maximumRedeliveries)
                .redeliveryDelay(redeliveryDelay)
                .onRedelivery(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        log.error("Redelivery no "
                                + exchange.getIn().getHeader(Exchange.REDELIVERY_COUNTER, Integer.class)
                                + " from " + collectQueueName);
                    }
                });
        if (useExponentialBackoff) {
            log.info("Using exponential backoff for {} with backoff multiplier: {} and maximum delay: {}",
                    collectQueueName, backOffMultiplier, maximumRedeliveryDelay);
            builder.useExponentialBackOff()
                    .backOffMultiplier(backOffMultiplier)
                    .maximumRedeliveryDelay(maximumRedeliveryDelay);
        }
        errorHandler(builder);

        // Collect from collect queue
        fromF("sjms-batch:queue:%s"
                + "?completionTimeout=%d"
                + "&completionSize=%d"
                + "&keepAliveDelay=2000"
                + "&aggregationStrategy=#eiCollectionAggregationStrategy"
                + "&connectionFactory=pooledConnectionFactory",
                 collectQueueName,
                 collectQueueCompletionTimeout * 1000,
                 collectQueueCompletionSize)
                .id("backend-collection-route")
                .log(LoggingLevel.DEBUG, "eiBackendLog", "Got an update collection:\n${body}")
                .toF("activemq:queue:%s?transacted=true", processQueueName);
    }
}
