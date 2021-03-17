package se.skltp.ei.integrationtests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static se.skltp.ei.EiTeststubRoute.NOTIFICATION_MOCK;
import static se.skltp.ei.util.EngagementTransactionTestUtil.createET;
import static se.skltp.ei.util.ProcessNotificationRequestUtil.createProcessNotificationTxt;

import java.util.List;
import javax.jms.JMSException;
import org.apache.activemq.spring.ActiveMQConnectionFactory;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.Processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import se.skltp.ei.EiBackendApplication;
import se.skltp.ei.entity.model.Engagement;
import se.skltp.ei.entity.repository.EngagementRepository;
import se.skltp.ei.util.DatabaseAssert;
import se.skltp.ei.util.EngagementTestUtil.DomainType;
import se.skltp.ei.util.JmsBrowser;
import se.skltp.ei.util.NotificationAssert;
import se.skltp.ei.util.ProcessNotificationRequestUtil;

@CamelSpringBootTest
@SpringBootTest(classes = {EiBackendApplication.class})
@ActiveProfiles("teststub")
public class ProcessQueueNotificationUpdateIT {

  @Produce
  protected ProducerTemplate producerTemplate;
  
  @Value("${ei.hsa.id}")
  String owner;

  @Autowired
  private EngagementRepository engagementRepository;

  @Autowired
  ActiveMQConnectionFactory connectionFactory;
  
  @EndpointInject(NOTIFICATION_MOCK)
  private MockEndpoint notificationMock;

  @Value("${activemq.broker.url}")
  String amqurl;

  @BeforeEach
  public void beforeEach() {
    // Clear database between tests
    engagementRepository.deleteAll();

    // Reset received notifications between tests
    notificationMock.reset();

  }

  @Test
  public void twoUpdatesWithTwoSubscribersHappyDay() throws InterruptedException {

    notificationMock.expectedMessageCount(2);
    notificationMock.setResultWaitTime(4000);

    // Create request and put to processQueue
    final String request = createProcessNotificationTxt(owner, DomainType.TWO_SUBSCRIBERS, 1111111111L, 2222222222L);
    producerTemplate.sendBody("activemq:queue:{{process.queue.name}}", request);

    // Assert notifications
    notificationMock.assertIsSatisfied();
    final List<Exchange> receivedNotifications = notificationMock.getReceivedExchanges();
    NotificationAssert.assertContainsLogicalAddresses(receivedNotifications, "TEST-LOGICAL-ADDRESS", "TEST-LOGICAL-ADDRESS-2");
    NotificationAssert.assertOwnerOnEngagements(receivedNotifications, owner);

    // Assert database
    final List<Engagement> dbEntities = engagementRepository.findAll();
    assertEquals(2, dbEntities.size());
    DatabaseAssert.assertContains(request, dbEntities);
  }

  @Test
  public void newOwnerInNotificationShouldBeUpdatedR5() throws InterruptedException {

    notificationMock.expectedMessageCount(1);
    notificationMock.setResultWaitTime(4000);

    final EngagementTransactionType et1 = createET(1111111111L, DomainType.ONE_SUBSCRIBER);
    final EngagementTransactionType et2 = createET(2222222222L, DomainType.ONE_SUBSCRIBER);

    // Init database with two entities
    InitDatabase(ProcessNotificationRequestUtil.createProcessNotification(owner, et1, et2));

    // Create request and put to processQueue
    producerTemplate.sendBody("activemq:queue:{{process.queue.name}}"
        , ProcessNotificationRequestUtil.createProcessNotificationTxt("NEW_OWNER", et1, et2));

    // Assert notifications
    notificationMock.assertIsSatisfied();
    NotificationAssert.assertOwnerOnEngagements(notificationMock.getReceivedExchanges(), "NEW_OWNER");

    // Assert database
    final List<Engagement> dbEntities = engagementRepository.findAll();
    assertEquals(2, dbEntities.size());
    dbEntities.forEach(entity -> assertEquals("NEW_OWNER", entity.getOwner()));
  }

  private void InitDatabase(ProcessNotificationType processNotificationType) {
    engagementRepository.saveAll(DatabaseAssert.toEntityList(processNotificationType.getEngagementTransaction()));
    assertEquals(processNotificationType.getEngagementTransaction().size(), engagementRepository.findAll().size(),
        "Database not set up correctly before test");
  }
  
  static int counter = 0;

  @Test
  public void failedNotificationsToDeadletterQueue() throws InterruptedException, JMSException {
	  
	  notificationMock.whenAnyExchangeReceived(new Processor(){  
	         public void process(Exchange ago0) throws Exception {
	             throw new RuntimeException("Client exception nr " + counter++);
	        }
	  });

    // Create request and put to processQueue
    final String request = createProcessNotificationTxt(owner, DomainType.TWO_SUBSCRIBERS, 1111111111L, 2222222222L);
    producerTemplate.sendBody("activemq:queue:{{process.queue.name}}", request);
    
    // Wait for backend to finish processing.
    Thread.sleep(10000);

    JmsBrowser jmsBrowser = new JmsBrowser(connectionFactory);
    	
    jmsBrowser.browse();
    Integer count1 = jmsBrowser.getQueueSize("DLQ.EI.NOTIFICATION.TEST-LOGICAL-ADDRESS");
    Integer count2 = jmsBrowser.getQueueSize("DLQ.EI.NOTIFICATION.TEST-LOGICAL-ADDRESS-2");
    assertEquals(count1,1);
    assertEquals(count2,1);


  }

}
