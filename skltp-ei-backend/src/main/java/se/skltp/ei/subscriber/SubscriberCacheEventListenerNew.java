package se.skltp.ei.subscriber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.camel.CamelContext;
import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import se.skltp.ei.SubscriberCacheConfiguration;
import se.skltp.ei.route.EiBackendDynamicNotificationRoute;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public final class SubscriberCacheEventListenerNew implements CacheEventListener<String, ArrayList<Subscriber>> {

  private static SubscriberCacheEventListenerNew instance;

  private static SubscriberCacheConfiguration config;

  public static final SubscriberCacheEventListenerNew createInstance(SubscriberCacheConfiguration config) {

    if (instance == null) {
      System.out.println("QWERQWERQWER: Creation of CacheEventListener.");
      // record static fields.
      SubscriberCacheEventListenerNew.config = config;
      SubscriberCacheEventListenerNew.instance = new SubscriberCacheEventListenerNew();
    }

    return instance;
  }

  private SubscriberCacheEventListenerNew() {
  }

  @Override
  public void onEvent(CacheEvent<? extends String, ? extends ArrayList<Subscriber>> event) {

    System.out.println("QWERQWERQWER: OnEVENT CacheEventListener.");

    switch (event.getType()) {

      case CREATED -> {
        logMeta("Cache entry CREATED.", event);
        ArrayList<Subscriber> subscribers = event.getNewValue();
        updateCamelNotificationRoutes(subscribers);
      }
      case UPDATED -> logMeta("Cache entry UPDATED.", event);

      // Basic logging below.
      case EVICTED -> log.info("Cache entry EVICTED.");
      case EXPIRED -> log.info("Cache entry EXPIRED.");
      case REMOVED -> log.info("Cache entry REMOVED.");

      // Should never occur.
      default -> throw new IllegalStateException("Unexpected value: " + event.getType());
    }
  }


  private void logMeta(String reason, CacheEvent<? extends String, ? extends ArrayList<Subscriber>> event) {

    ArrayList<Subscriber> subscribers = event.getNewValue();

    log.info("{},\n Key: {},\n Subscribers:\n{}",
        reason,
        event.getKey(),
        subscriberToJson(subscribers)
    );
  }

  private String subscriberToJson(List<Subscriber> subscribers) {
    try {
      return new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(subscribers);
    } catch (JsonProcessingException e) {
      return "error parsing Subscriber list";
    }
  }

  private void updateCamelNotificationRoutes(List<Subscriber> subscribers) {
    // Add notification routes if they are not already on CamelContext
    // Note: We are not stopping/removing routes that are removed from subscribers. It
    //  shouldn't be needed since no more Notifications is added to the queue for that subscriber.
    //  Feel free to add that functionality if needed.
    try {

      // Fetch these locally to allow faster looping below.
      CamelContext camelContext = config.getCamelContext();
      int maximumRedeliveries = config.getMaximumRedeliveries();
      int redeliveryDelay = config.getRedeliveryDelay();
      boolean useExponentialBackoff = config.getUseExponentialBackoff();
      double backOffMultiplier = config.getBackOffMultiplier();
      int maximumRedeliveryDelay = config.getMaximumRedeliveryDelay();

      for (Subscriber subscriber : subscribers) {
        if (camelContext.getRoute(subscriber.getNotificationRouteName()) == null) {
          camelContext.addRoutes(new EiBackendDynamicNotificationRoute(
              subscriber,
              maximumRedeliveries,
              redeliveryDelay,
              useExponentialBackoff,
              backOffMultiplier,
              maximumRedeliveryDelay));
        }
      }
    } catch (Exception e) {
      log.error("Failed create notification routes.", e);
    }
  }
}
