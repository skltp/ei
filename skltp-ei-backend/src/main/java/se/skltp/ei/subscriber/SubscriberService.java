package se.skltp.ei.subscriber;

import java.util.List;
import org.springframework.cache.annotation.Cacheable;

public interface SubscriberService {

  @Cacheable(value = "subscriber-cache", key = "'subscribers'",  sync = true) // TODO: I don't think this line is needed as long as it is specified where the interface is implemented.
  List<Subscriber> getSubscribers();
}
