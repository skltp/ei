package se.skltp.ei;

import net.sf.ehcache.config.CacheConfiguration;

import javax.jms.ConnectionFactory;

import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.apache.activemq.spring.ActiveMQConnectionFactory;
import org.apache.camel.component.activemq.ActiveMQComponent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

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
