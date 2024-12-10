package se.skltp.ei;

import lombok.Getter;
import org.apache.camel.CamelContext;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.*;
import org.ehcache.event.EventType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.skltp.ei.subscriber.SubscriberCacheEventListenerNew;

import java.time.Duration;
import java.util.ArrayList;

@Configuration
@EnableCaching
public class SubscriberCacheConfiguration {

  @Value("${subscriber.cache.timeToLiveSeconds}")
  Long subscriberCacheTTLSeconds;

  private final String subscriberCacheName = "subscriber-cache";

  private final long maxEntriesLocalHeap = 1;

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
      System.out.println("QWERQWERQWER: Recording CamelContext into Config.");
      this.camelContext = providedContext;
    } else {
      System.out.println("QWERQWERQWER: ILLEGAL duplicate record of CamelContext.");
      throw new UnsupportedOperationException("It is not allowed to set a second camel context.");
    }
  }

  @Bean("CustomCacheManager")
  public CacheManager cacheManager() {

    System.out.println("QWERQWERQWER: Setup of Cache Manager.");

    // Provide the listener a reference to this configuration item.
    SubscriberCacheEventListenerNew listener = SubscriberCacheEventListenerNew.createInstance(this);

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

    CacheConfiguration<String, ArrayList> cacheConfiguration
        = CacheConfigurationBuilder.newCacheConfigurationBuilder(
            String.class,
            ArrayList.class,
            ResourcePoolsBuilder.heap(maxEntriesLocalHeap))
        .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(subscriberCacheTTLSeconds)))
        .withService(cacheEventListenerConfiguration)
        .build();

    CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
        .withCache(subscriberCacheName,
            cacheConfiguration)
        .build(); // invoking build() returns a fully instantiated, but uninitialized, CacheManager.

    cacheManager.init(); // Before using the CacheManager it needs to be initialized.

    // CACHE RETRIEVAL EXAMPLE

    // A cache is retrieved by passing its alias, key type and value type to the CacheManager...
    // - For type-safety, we ask for both key and value types to be passed in. ...
    // - This guards the Cache from being polluted by random types.
//    Cache<String, ArrayList> preConfigured =
//        cacheManager.getCache(subscriberCacheName, String.class, ArrayList.class);

    // CACHE CLOSURES.

    // SINGLE CACHE

    // We can call CacheManager.removeCache(String) for a given Cache.
    //   The CacheManager will not only remove its reference to the Cache, but will also close it.
//    cacheManager.removeCache(subscriberCacheName);

    // ALL CACHES

    // In order to release all transient resources (memory, threads, ...)
    //   a CacheManager provides to Cache instances it manages,
    //   you have to invoke CacheManager.close(), which in turns closes
    //   all Cache instances known at the time.
//    cacheManager.close();

    return cacheManager;
  }
}
