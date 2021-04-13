package se.skltp.ei;

import net.sf.ehcache.config.CacheConfiguration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class EiBackendConfiguration  {

  @Value("${subscriber.cache.timeToLiveSeconds}")
  Long subscriberCacheTTLSeconds;

  @Bean
  public CacheManager cacheManager(){

    CacheConfiguration subscriberConfig = new CacheConfiguration();
    subscriberConfig.setName("subscriber-cache");
    subscriberConfig.setTimeToLiveSeconds(subscriberCacheTTLSeconds);
    subscriberConfig.setMaxEntriesLocalHeap(1);

    net.sf.ehcache.config.Configuration config = new  net.sf.ehcache.config.Configuration();
    config.addCache(subscriberConfig);
    return new EhCacheCacheManager(net.sf.ehcache.CacheManager.newInstance(config));
  }
}
