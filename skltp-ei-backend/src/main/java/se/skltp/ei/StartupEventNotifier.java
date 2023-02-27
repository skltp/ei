package se.skltp.ei;

import static se.skltp.ei.service.constants.EiConstants.X_SKLTP_CORRELATION_ID;

import java.io.IOException;

import lombok.extern.log4j.Log4j2;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.spi.CamelEvent;
import org.apache.camel.spi.CamelEvent.CamelContextStartedEvent;
import org.apache.camel.spi.CamelEvent.ExchangeCreatedEvent;
import org.apache.camel.spi.CamelEvent.ExchangeSentEvent;
import org.apache.camel.support.EventNotifierSupport;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.stereotype.Component;
import se.skltp.ei.subscriber.SubscriberCacheEventListener;
import se.skltp.ei.subscriber.SubscriberService;


@Log4j2
@Component
public class StartupEventNotifier extends EventNotifierSupport {

    @Value("${activemq.broker.maximum-redeliveries}")
    private Integer maximumRedeliveries;

    @Value("${activemq.broker.redelivery-delay:0}")
    private Integer redeliveryDelay;

    @Value("${activemq.broker.backoff-multiplier:0.0}")
    private Double backOffMultiplier;

    @Value("${activemq.broker.use-exponential-backoff:false}")
    private Boolean useExponentialBackoff;

    @Override
    protected void doStart() {
        setIgnoreCamelContextEvents(false);
        setIgnoreExchangeCreatedEvent(false);
        // filter out unwanted events
        setIgnoreExchangeSentEvents(true);
        setIgnoreExchangeCompletedEvent(true);
        setIgnoreExchangeFailedEvents(true);
        setIgnoreServiceEvents(true);
        setIgnoreRouteEvents(true);
        setIgnoreExchangeRedeliveryEvents(true);
    }

    @Override
    public void notify(CamelEvent event) throws IOException {
        if (event instanceof CamelContextStartedEvent) {
            initializeSubscribers((CamelContext) event.getSource());
        } else if (event instanceof ExchangeCreatedEvent ||
                event instanceof ExchangeSentEvent) {
            final Object source = event.getSource();
            if (source instanceof Exchange) {
                final String correlationId = ((Exchange) source).getIn().getHeader(X_SKLTP_CORRELATION_ID, String.class);
                if (correlationId != null) {
                    ThreadContext.put("corr.id", String.format("[%s]", correlationId));
                }
            }
        }
    }

    private void initializeSubscribers(CamelContext camelContext) {
        final CacheManager cacheManager = camelContext.getRegistry().lookupByNameAndType("cacheManager", CacheManager.class);
        final SubscriberService subscriberService = camelContext.getRegistry()
                .lookupByNameAndType("subscriberCachableService", SubscriberService.class);

        // Register a eventlistener for SubscriberCacheService. The eventlistener will create dynamic rotues that poll the
        // ProcessNotification amq queues when the Subscriber cache is updated
        net.sf.ehcache.CacheManager ehCacheManager = ((EhCacheCacheManager) cacheManager).getCacheManager();
        final SubscriberCacheEventListener subscriberCacheEventListener =
                SubscriberCacheEventListener.createInstance(camelContext, maximumRedeliveries, redeliveryDelay, backOffMultiplier, useExponentialBackoff);
        ehCacheManager.getCache("subscriber-cache").getCacheEventNotificationService().registerListener(subscriberCacheEventListener);

        // This will initially update the cache and trigger creation of routes in the eventlistener
        subscriberService.getSubscribers();
    }

}
