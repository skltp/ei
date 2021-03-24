package se.skltp.ei.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
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

  @Value("${dlq.maximum-redeliveries:0}")
  private int maximumRedeliveries;

  @Value("${dlq.redelivery-delay:0}")
  private int redeliveryDelay;
  
  @Override
  public void configure() throws Exception {
	  
 	 errorHandler(deadLetterChannel(String.format("activemq:queue:DLQ.%s", collectQueueName))
 			 .useOriginalMessage()
 			 .maximumRedeliveries(maximumRedeliveries)
 			 .redeliveryDelay(redeliveryDelay)
 	         .onRedelivery(new Processor() {
 	             @Override
 	             public void process(Exchange exchange) throws Exception {
 	                 log.error("Redelivery no " 
 	            	 + exchange.getIn().getHeader(Exchange.REDELIVERY_COUNTER, Integer.class)
 	            	 + " from " + collectQueueName);
 	             }
 	         }));
	 	 
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
