package se.skltp.ei.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EiBackendCollectRoute extends RouteBuilder {
  @Value("${collect.queue.name:collect}")
  String collectQueueName;

  @Value("${process.queue.name:process}")
  String processQueueName;

  @Value("${collect.queue.completion.size:1000}")
  Integer collectQueueCompletionSize;

  @Value("${collect.queue.completion.timeout:30}")
  Integer collectQueueCompletionTimeout;

  @Override
  public void configure() throws Exception {
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
