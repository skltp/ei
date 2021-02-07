package se.skltp.ei;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.skltp.ei.subscriber.SubscriberCachableService;
import se.skltp.ei.updateprocess.NotificationSplitterBean;
import se.skltp.ei.updateprocess.UpdatePersistentStorageProcessor;

@Component
public class EiBackendUpdateRoute extends RouteBuilder {

  @Value("${collect.queue.name:collect}")
  String collectQueueName;

  @Value("${process.queue.name:process}")
  String processQueueName;

  @Value("${collect.queue.completion.size:1000}")
  Integer collectQueueCompletionSize;

  @Value("${collect.queue.completion.timeout:30}")
  Integer collectQueueCompletionTimeout;

  @Autowired
  UpdatePersistentStorageProcessor updatePersistentStorageProcessor;

  @Autowired
  NotificationSplitterBean notificationSplitterBean;

  @Autowired
  SubscriberCachableService subscriberCachableService;

  @Override
  public void configure() throws Exception {

    // Get from process queue
    fromF("activemq:queue:%s?transacted=true", processQueueName)
        .id("backend-process-route")
        .log("Got one from Process Queue:\n${body}")
        .process(updatePersistentStorageProcessor)
        .split(method(notificationSplitterBean, "createNotificationList"))
              .parallelProcessing(true)
          .log("Add notification to ${header.NotificationQueueName}")
          .toD("activemq:queue:${header.NotificationQueueName}")
        .end();

    // Collect from collect queue
    fromF("sjms-batch:queue:%s"
            + "?completionTimeout=%d"
            + "&completionSize=%d"
            + "&keepAliveDelay=2000"
            + "&aggregationStrategy=#eiCollectionAggregationStrategy"
            + "&connectionFactory=backendAmqConnectionFactory"
              , collectQueueName
              , collectQueueCompletionTimeout*1000
              , collectQueueCompletionSize)
        .id("backend-collection-route")
        .log("Got an update collection:\n${body}")
        .toF( "activemq:queue:%s?transacted=true", processQueueName);

    }

}
