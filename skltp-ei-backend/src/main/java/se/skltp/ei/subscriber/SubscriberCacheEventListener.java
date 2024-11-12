package se.skltp.ei.subscriber;

//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import java.util.Date;
//import java.util.List;
//import lombok.extern.log4j.Log4j2;
//import net.sf.ehcache.Ehcache;
//import net.sf.ehcache.Element;
//import net.sf.ehcache.event.CacheEventListener;
//import org.apache.camel.CamelContext;
//
//import se.skltp.ei.route.EiBackendDynamicNotificationRoute;

//@Log4j2
public final class SubscriberCacheEventListener /*implements CacheEventListener*/ {

//    private static SubscriberCacheEventListener instance;
//
//    private int maximumRedeliveries = 0;
//    private int redeliveryDelay = 0;
//    private boolean useExponentialBackoff = false;
//    private double backOffMultiplier = 0.0d;
//    private int maximumRedeliveryDelay = 0;
//
//    public static final SubscriberCacheEventListener createInstance(
//            CamelContext camelContext, int maximumRedeliveries, int redeliveryDelay, boolean useExponentialBackoff, double backOffMultiplier, int maximumRedeliveryDelay) {
//        if (instance == null) {
//            instance = new SubscriberCacheEventListener(camelContext, maximumRedeliveries, redeliveryDelay, useExponentialBackoff, backOffMultiplier, maximumRedeliveryDelay);
//        }
//        return instance;
//    }
//
//    CamelContext camelContext;
//
//    private SubscriberCacheEventListener(CamelContext camelContext, int maximumRedeliveries, int redeliveryDelay, boolean useExponentialBackoff, double backOffMultiplier, int maximumRedeliveryDelay) {
//        this.camelContext = camelContext;
//        this.maximumRedeliveries = maximumRedeliveries;
//        this.redeliveryDelay = redeliveryDelay;
//        this.backOffMultiplier = backOffMultiplier;
//        this.useExponentialBackoff = useExponentialBackoff;
//        this.maximumRedeliveryDelay = maximumRedeliveryDelay;
//    }
//
//    @Override
//    public void notifyElementRemoved(Ehcache ehcache, Element element) {
//        log.info("notifyElementRemoved");
//    }
//
//    @Override
//    public void notifyElementPut(Ehcache ehcache, Element element) {
//        logElement("Element put to cache test", element);
//        List<Subscriber> subscribers = (List<Subscriber>) element.getObjectValue();
//        updateCamelNotificationRoutes(subscribers);
//
//    }
//
//    @Override
//    public void notifyElementUpdated(Ehcache ehcache, Element element) {
//        logElement("Element updated:", element);
//
//    }
//
//    @Override
//    public void notifyElementExpired(Ehcache ehcache, Element element) {
//        log.info("notifyElementExpired");
//    }
//
//    @Override
//    public void notifyElementEvicted(Ehcache ehcache, Element element) {
//        log.info("notifyElementEvicted");
//    }
//
//    @Override
//    public void notifyRemoveAll(Ehcache ehcache) {
//        log.info("notifyRemoveAll");
//    }
//
//    @Override
//    public Object clone() throws CloneNotSupportedException {
//        throw new CloneNotSupportedException("Singleton instance");
//    }
//
//    @Override
//    public void dispose() {
//        // Nothin to cleanup
//    }
//
//    private void updateCamelNotificationRoutes(List<Subscriber> subscribers) {
//        // Add notification routes if they are not already on CamelContext
//        // Note: We are not stopping/removing routes that are removed from subscribers. It
//        //  shouldn't be needed since no more Notifications is added to the queue for that subscriber.
//        //  Feel free to add that functionality if needed.
//        try {
//            for (Subscriber subscriber : subscribers) {
//                if (camelContext.getRoute(subscriber.getNotificationRouteName()) == null) {
//                    camelContext.addRoutes(new EiBackendDynamicNotificationRoute(subscriber, maximumRedeliveries, redeliveryDelay, useExponentialBackoff, backOffMultiplier, maximumRedeliveryDelay));
//                }
//            }
//        } catch (Exception e) {
//            log.error("Failed create notification routes.", e);
//        }
//    }
//
//    private void logElement(String reason, Element element) {
//        Date updatedTime = new Date(element.getLastUpdateTime());
//        Date creationTime = new Date(element.getCreationTime());
//        List<Subscriber> subscribers = (List<Subscriber>) element.getObjectValue();
//
//        log.info("{}\n Created: {}\n Updated: {}\n Key: {}\n Subcribers:\n{}",
//                 reason,
//                 creationTime,
//                 updatedTime,
//                 element.getObjectKey(),
//                 subscriberToJson(subscribers));
//
//    }
//
//    private String subscriberToJson(List<Subscriber> subscribers) {
//        try {
//            return new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(subscribers);
//        } catch (JsonProcessingException e) {
//            return "error parsing Subscriber list";
//        }
//    }
}
