//package se.skltp.ei.subscriber;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.core.util.DefaultIndenter;
//import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import net.sf.ehcache.Element;
//import org.apache.camel.Exchange;
//import org.apache.camel.Processor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cache.CacheManager;
//import org.springframework.cache.ehcache.EhCacheCacheManager;
//import org.springframework.stereotype.Component;
//
//@Component
//public class SubcriberStatusProcessor implements Processor {
//
//  @Autowired
//  CacheManager cacheManager;
//
//  @Override
//  public void process(Exchange exchange) throws Exception {
//
//    final net.sf.ehcache.Cache cache = ((EhCacheCacheManager) cacheManager).getCacheManager().getCache("subscriber-cache");
//    final Element element = cache.get("subscribers");
//
//    Map<String, Object> map = new HashMap();
//
//    if (element != null) {
//      Date updatedTime = new Date(element.getLastUpdateTime());
//      Date creationTime = new Date(element.getCreationTime());
//      List<Subscriber> subscribers = (List<Subscriber>) element.getObjectValue();
//
//      map.put("CreationTime", creationTime.toString());
//      map.put("UpdatedTime", updatedTime.toString());
//      map.put("ExpirationTime", new Date(element.getExpirationTime()).toString());
//      map.put("Subscribers", subscribers);
//    }
//
//    try {
//      ObjectMapper mapper = new ObjectMapper();
//      DefaultPrettyPrinter p = new DefaultPrettyPrinter();
//      p.indentArraysWith(new DefaultIndenter().withLinefeed(System.lineSeparator()));
//      mapper.setDefaultPrettyPrinter(p);
//      exchange.getIn().setBody(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map)
//          .replace("\\/", "/"));
//    } catch (JsonProcessingException e) {
//      exchange.getIn().setBody(map.toString().replace("\\/", "/"));
//    }
//  }
//}
