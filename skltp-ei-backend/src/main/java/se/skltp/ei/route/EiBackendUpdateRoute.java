package se.skltp.ei.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.LoggingLevel;
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

  @Value("${dlq.maximum-redeliveries:0}")
  private int maximumRedeliveries;

  @Value("${dlq.redelivery-delay:0}")
  private int redeliveryDelay;
  
  @Override
  public void configure() throws Exception {

 	 errorHandler(deadLetterChannel(String.format("activemq:queue:DLQ.%s", processQueueName))
 			 .useOriginalMessage()
 			 .maximumRedeliveries(maximumRedeliveries)
 			 .redeliveryDelay(redeliveryDelay)
 	         .onRedelivery(new Processor() {
 	             @Override
 	             public void process(Exchange exchange) throws Exception {
 	                 log.error("Redelivery no "
 	            	 + exchange.getIn().getHeader(Exchange.REDELIVERY_COUNTER, Integer.class)
 	            	 + " from " + processQueueName);
 	             }
	 	       }));
    // Get from process queue
    fromF("activemq:queue:%s?transacted=true", processQueueName)
        .id("backend-process-route")
        .log(LoggingLevel.DEBUG, "eiBackendLog","Got one from Process Queue:\n${body}")
        .process(updatePersistentStorageProcessor)
        .split(method(notificationSplitterBean, "createNotificationList"))
              .parallelProcessing(true)
          .log(LoggingLevel.DEBUG, "eiBackendLog","Add notification to ${header.NotificationQueueName}")
          .toD("activemq:queue:${header.NotificationQueueName}")
        .end();



    }

}
