package se.skltp.ei.subscriber;

import java.util.List;
import org.springframework.cache.annotation.Cacheable;

public interface SubscriberService {

  @Cacheable(value = "subscriber-cache", key = "'subscribers'",  sync = true)
  List<Subscriber> getSubscribers();
}
