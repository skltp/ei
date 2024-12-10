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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import se.skltp.ei.subscriber.SubscriberService;


@Log4j2
@Component
public class StartupEventNotifier extends EventNotifierSupport {

    @Autowired
    SubscriberCacheConfiguration subscriberCacheConfiguration;

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
    @DependsOn("ehCacheManager")
    public void notify(CamelEvent event) throws IOException {
        if (event instanceof CamelContextStartedEvent) {

            CamelContext camelEventContext = (CamelContext) event.getSource();
            subscriberCacheConfiguration.setCamelContextOnce(camelEventContext); // Record Camel Context within configuration body for future usage.
            initializeSubscribers(camelEventContext);

        } else if (event instanceof ExchangeCreatedEvent ||
                event instanceof ExchangeSentEvent) {
            final Object source = event.getSource();
            if (source instanceof Exchange exchange) {
                final String correlationId = exchange.getIn().getHeader(X_SKLTP_CORRELATION_ID, String.class);
                if (correlationId != null) {
                    ThreadContext.put("corr.id", String.format("[%s]", correlationId));
                }
            }
        }
    }

    private void initializeSubscribers(CamelContext camelContext) {

        final SubscriberService subscriberService =
            camelContext.getRegistry().lookupByNameAndType("subscriberCachableService", SubscriberService.class);

        // A CacheEventListener should already be registered for the 'subscriberCachableService'.

        // This will initially update the cache and trigger creation of routes in the eventlistener
        subscriberService.getSubscribers();
    }
}
