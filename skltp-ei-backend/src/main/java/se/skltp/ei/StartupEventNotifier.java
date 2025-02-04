package se.skltp.ei;

import static se.skltp.ei.service.constants.EiConstants.X_SKLTP_CORRELATION_ID;

import java.io.IOException;
import java.util.EventObject;

import jakarta.annotation.PostConstruct;
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
import org.springframework.stereotype.Component;
import se.skltp.ei.subscriber.SubscriberService;


@Log4j2
@Component
public class StartupEventNotifier extends EventNotifierSupport {

    StartupEventNotifier selfRef = null;

    SubscriberCacheConfiguration subscriberCacheConfiguration;

    @Autowired
    public StartupEventNotifier(SubscriberCacheConfiguration subscriberCacheConfiguration) {
        log.info("Startup Breadcrumbs: StartupEventNotifier being constructed via Autowire.");
        this.subscriberCacheConfiguration = subscriberCacheConfiguration;
        this.selfRef = this;
    }

    public StartupEventNotifier() {
        log.info("Startup Breadcrumbs: StartupEventNotifier being constructed via Standard constructor.");
    }

    public boolean isEnabled(EventObject event) {
        return true;
    }

    @Override
    protected void doStart() {
        log.info("Startup Breadcrumbs: StartupEventNotifier running doStart routine.");

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

    @PostConstruct
    public void init() {
        if(getCamelContext() != null) {
            //Add the Event Notifier to the Camel Context
            log.info("Startup Breadcrumbs: Camel context present post construction");
        } else {
            log.info("Startup Breadcrumbs: Unable to get non-null Camel context post construction");
        }
    }

    @Override
    public void notify(CamelEvent event) throws IOException {

      log.info("Startup Breadcrumbs: StartupEventNotifier received notification of type: {}", event.getType().name());

        if (event instanceof CamelEvent.CamelContextInitializingEvent) {
            log.info("Startup Breadcrumbs: StartupEventNotifier running CamelContextInitializingEvent routine. Will attempt to record camel context in config.");
            log.info("Camel event meta: {}", event.toString());

            CamelContext camelEventContext = (CamelContext) event.getSource();
            subscriberCacheConfiguration.setCamelContextOnce(camelEventContext); // Record Camel Context within configuration body for future usage.

        } else if (event instanceof CamelContextStartedEvent) {
            log.info("Startup Breadcrumbs: StartupEventNotifier running CamelContextStartedEvent routine. Will attempt to initialize Subscribers.");
            CamelContext camelEventContext = (CamelContext) event.getSource();
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
