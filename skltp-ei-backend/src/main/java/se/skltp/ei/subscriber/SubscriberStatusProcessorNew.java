package se.skltp.ei.subscriber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import javax.cache.Cache;
import javax.cache.Caching;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component
public class SubscriberStatusProcessorNew implements Processor {

  @Autowired
  CacheManager cacheManager;

  @Override
  public void process(Exchange exchange) throws Exception {
    // Fetch a JSR107-compatible caching implementation from Spring Boot, using Javax standards.
    javax.cache.CacheManager javaxCacheManager = Caching
        .getCachingProvider("org.ehcache.jsr107.EhcacheCachingProvider")
        .getCacheManager();

    Cache<String, ArrayList<Subscriber>> javaxCache = javaxCacheManager.getCache("subscriber-cache");

    Map<String, ArrayList<Subscriber>> map = new HashMap<>();

    // TODO: The move from EhCache 2.x to EhCache 3.x resulted in the removal of the Element class wrapper for cache payloads.
    //   There is therefore no native way in EhCache 3.x to retrieve cache entry metadata. Not without reflection into EhCache inner workings.
    //   Consider if there is a need for such data in the future.
    //   Below map-put:s are the three metadata status values that are no longer retrievable.
    //   The payload itself could still be included.

//      map.put("CreationTime", new Date(element.getCreationTime()).toString());
//      map.put("UpdatedTime", new Date(element.getLastUpdateTime()).toString());
//      map.put("ExpirationTime", new Date(element.getExpirationTime()).toString());

    ArrayList<Subscriber> subscribers = javaxCache.get("subscribers");
    map.put("Subscribers", subscribers);

    try {
      ObjectMapper mapper = new ObjectMapper();
      DefaultPrettyPrinter p = new DefaultPrettyPrinter();
      p.indentArraysWith(new DefaultIndenter().withLinefeed(System.lineSeparator()));
      mapper.setDefaultPrettyPrinter(p);
      exchange.getIn().setBody(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map)
          .replace("\\/", "/"));
    } catch (JsonProcessingException e) {
      exchange.getIn().setBody(map.toString().replace("\\/", "/"));
    }
  }
}
