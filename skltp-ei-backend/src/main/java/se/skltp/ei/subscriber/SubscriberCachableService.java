package se.skltp.ei.subscriber;

import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import riv.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontractresponder._2.GetLogicalAddresseesByServiceContractResponseType;
import se.skltp.ei.service.GetLogicalAddresseesServiceClient;
import se.skltp.ei.subscriber.util.SubscriberFileTool;

@Service
@Log4j2
public class SubscriberCachableService implements SubscriberService {

  GetLogicalAddresseesServiceClient logicalAddresseesServiceClient;

  CamelContext camelContext;

  @Value("${subscriber.cache.file.name:#{null}}")
  private String subscriberCachefilePath;

  @Value("${notification.queue.prefix:notification.}")
  private String notificationQueuePrefix;

  @Autowired
  public SubscriberCachableService(GetLogicalAddresseesServiceClient logicalAddresseesServiceClient,
                                   CamelContext camelContext) {
    this.logicalAddresseesServiceClient = logicalAddresseesServiceClient;
    this.camelContext = camelContext;
  }

  @Override
  @Cacheable(value = "subscriber-cache", key = "'subscribers'",  sync = true)
  public List<Subscriber> getSubscribers(){
    log.debug("Looking up logical addresses for dynamic notify flows");

    try {
      GetLogicalAddresseesByServiceContractResponseType logicalAddressesResponse = logicalAddresseesServiceClient.callService();
      List<Subscriber> subscribers = logicalAddressesResponse.getLogicalAddressRecord().stream()
          .map(entry -> new Subscriber(entry.getLogicalAddress(), entry.getFilter(), notificationQueuePrefix))
          .collect(Collectors.toList());

      SubscriberFileTool.saveToLocalCopy(subscribers, subscriberCachefilePath);
      return subscribers;

    } catch (Exception e) {

      log.warn("Failed finding logical addresses", e);
      log.warn("Trying to restore cache from from file");
      return SubscriberFileTool.restoreFromLocalCopy(subscriberCachefilePath);
    }
  }

}
