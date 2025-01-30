package se.skltp.ei;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.camel.CamelContext;
import org.springframework.cache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.*;
import org.ehcache.event.EventType;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.skltp.ei.subscriber.SubscriberCacheEventListener;

import javax.cache.Caching;
import java.time.Duration;
import java.util.ArrayList;

@Configuration
@EnableCaching
@Log4j2
public class SubscriberCacheConfiguration {

  @Value("${subscriber.cache.timeToLiveSeconds}")
  Long subscriberCacheTTLSeconds;

  private static final String SUBSCRIBER_CACHE_NAME = "subscriber-cache";

  private static final long MAX_ENTRIES_LOCAL_HEAP = 1;

  // EVENT LISTENER PROPERTIES
  @Getter
  @Value("${activemq.broker.notification.maximum-redeliveries}")
  private Integer maximumRedeliveries;

  @Getter
  @Value("${activemq.broker.notification.redelivery-delay:0}")
  private Integer redeliveryDelay;

  @Getter
  @Value("${activemq.broker.notification.use-exponential-backoff:false}")
  private Boolean useExponentialBackoff;

  @Getter
  @Value("${activemq.broker.notification.backoff-multiplier:0.0}")
  private Double backOffMultiplier;

  @Getter
  @Value("${activemq.broker.notification.maximum-redelivery-delay:0}")
  private int maximumRedeliveryDelay;

  @Getter
  CamelContext camelContext;


  // ### Camel Context SETTER ###
  void setCamelContextOnce(CamelContext providedContext) {
    if (this.camelContext == null) {
      log.info("Startup Breadcrumbs: SubscriberCacheConfiguration recording Camel Context.");
      this.camelContext = providedContext;
    } else {
      throw new UnsupportedOperationException("It is not allowed to set a second camel context.");
    }
  }

  @Bean("ehCacheManager")
  public CacheManager ehCacheManager() {

    log.info("Startup Breadcrumbs: SubscriberCacheConfiguration CacheManager Bean being set up.");

    // Provide the listener a reference to this configuration item.
    SubscriberCacheEventListener listener = SubscriberCacheEventListener.createInstance(this);

    // Prior implementation seemed to be listening to every type of event, so let's do the same here.
    CacheEventListenerConfigurationBuilder cacheEventListenerConfiguration = CacheEventListenerConfigurationBuilder
        .newEventListenerConfiguration(
            listener,
            EventType.CREATED,
            EventType.EVICTED,
            EventType.EXPIRED,
            EventType.REMOVED,
            EventType.UPDATED
        )
        .unordered().asynchronous();

    // Create a Cache configuration, pulling in parameters from both local fields, and SpringBoot-initialized Parameters.
    CacheConfiguration<String, ArrayList> cacheConfiguration
        = CacheConfigurationBuilder.newCacheConfigurationBuilder(
            String.class,
            ArrayList.class,
            ResourcePoolsBuilder.heap(MAX_ENTRIES_LOCAL_HEAP))
        .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(subscriberCacheTTLSeconds)))
        //   Also attach the listener item we created earlier.
        .withService(cacheEventListenerConfiguration)
        .build();

    // Fetch a JSR107-compatible caching implementation from Spring Boot, using Javax standards.
    javax.cache.CacheManager javaxCacheManager = Caching
        .getCachingProvider("org.ehcache.jsr107.EhcacheCachingProvider")
        .getCacheManager();

    // Destroy any cache with our desired name, if one happens to exist.
    javaxCacheManager.destroyCache(SUBSCRIBER_CACHE_NAME);
    // Create a new JSR107-compatible EhCache cache, using our above config.
    javaxCacheManager.createCache(SUBSCRIBER_CACHE_NAME, Eh107Configuration.fromEhcacheCacheConfiguration(cacheConfiguration));

    return new JCacheCacheManager(javaxCacheManager);
  }
}
