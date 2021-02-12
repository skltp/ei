package se.skltp.ei.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.skltp.ei.updateprocess.NotificationSplitterBean;
import se.skltp.ei.updateprocess.UpdatePersistentStorageProcessor;

@Component
public class EiBackendUpdateRoute extends RouteBuilder {


  @Value("${process.queue.name:process}")
  String processQueueName;

  @Autowired
  UpdatePersistentStorageProcessor updatePersistentStorageProcessor;

  @Autowired
  NotificationSplitterBean notificationSplitterBean;

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



    }

}
