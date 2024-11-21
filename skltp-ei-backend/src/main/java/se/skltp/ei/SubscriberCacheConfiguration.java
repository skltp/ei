package se.skltp.ei;

import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.ArrayList;

@Configuration
@EnableCaching
public class SubscriberCacheConfiguration {

  @Value("${subscriber.cache.timeToLiveSeconds}")
  Long subscriberCacheTTLSeconds;

  private final String subscriberCacheName = "subscriber-cache";

  private final long maxEntriesLocalHeap = 1;

  @Bean
  public CacheManager cacheManager() {

    CacheConfiguration<String, ArrayList> cacheConfiguration
        = CacheConfigurationBuilder.newCacheConfigurationBuilder(
            String.class,
            ArrayList.class,
            ResourcePoolsBuilder.heap(maxEntriesLocalHeap))
        .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(subscriberCacheTTLSeconds)))
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
