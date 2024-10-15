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
    private boolean useExponentialBackoff;

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
                        log.error("Redelivery no {} from {}.",
                            exchange.getIn().getHeader(Exchange.REDELIVERY_COUNTER, Integer.class),
                            collectQueueName);
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

        // Consumer: Receiving and batching messages from the SJMS queue
        fromF("sjms:queue:%s?connectionFactory=#pooledConnectionFactory", collectQueueName)
            .id("backend-collection-route")
            .aggregate(constant(true)) // aggregate all exchanges
            .aggregationStrategy("eiCollectionAggregationStrategy")
            .completionSize(collectQueueCompletionSize) // batch size: number of messages
            .completionTimeout(collectQueueCompletionTimeout * 1000) // batch timeout in milliseconds
            .log(LoggingLevel.DEBUG, "eiBackendLog", "Processing batch of messages: Got an update collection:\n${body}")
            .toF("activemq:queue:%s?transacted=true", processQueueName);
    }
}
