package se.skltp.ei.integrationtests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.skltp.ei.EiTeststubRoute.NOTIFICATION_MOCK;
import static se.skltp.ei.service.util.EntityTransformer.toEntity;
import static se.skltp.ei.util.EngagementTransactionTestUtil.createET;
import static se.skltp.ei.util.NotificationAssert.getEngagementTransaction;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.EiBackendApplication;
import se.skltp.ei.EiTeststubRoute;
import se.skltp.ei.entity.model.Engagement;
import se.skltp.ei.entity.repository.EngagementRepository;
import se.skltp.ei.util.DatabaseAssert;
import se.skltp.ei.util.EngagementTestUtil.DomainType;
import se.skltp.ei.util.NotificationAssert;
import se.skltp.ei.util.UpdateRequestUtil;

@CamelSpringBootTest
@SpringBootTest(classes = {EiBackendApplication.class, EiTeststubRoute.class})
@ActiveProfiles("teststub")
public class ProcessQueueUpdateIT {

  @Produce
  protected ProducerTemplate producerTemplate;

  @Value("${ei.hsa.id}")
  String owner;

  @Autowired
  private EngagementRepository engagementRepository;

  @EndpointInject(NOTIFICATION_MOCK)
  private MockEndpoint notificationMock;


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
    final String request = UpdateRequestUtil.createUpdateTxtMsg(owner, DomainType.TWO_SUBSCRIBERS, 1111111111L, 2222222222L);
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
  public void twoUpdatesWithOneSubscriberHappyDay() throws InterruptedException {

    notificationMock.expectedMessageCount(1);
    notificationMock.setResultWaitTime(4000);

    // Create request and put to processQueue
    final String request = UpdateRequestUtil.createUpdateTxtMsg(owner, DomainType.ONE_SUBSCRIBER, 1111111111L, 2222222222L);
    producerTemplate.sendBody("activemq:queue:{{process.queue.name}}", request);

    // Assert notifications
    notificationMock.assertIsSatisfied();
    final List<Exchange> receivedNotifications = notificationMock.getReceivedExchanges();
    NotificationAssert.assertContainsLogicalAddresses(receivedNotifications, "TEST-LOGICAL-ADDRESS-2");
    NotificationAssert.assertOwnerOnEngagements(receivedNotifications, owner);

    // Assert database
    final List<Engagement> dbEntities = engagementRepository.findAll();
    assertEquals(2, dbEntities.size());
    DatabaseAssert.assertContains(request, dbEntities);

  }

  @Test
  public void ownerAlwaysSetToPlattformOwnerR6() throws InterruptedException {

    notificationMock.expectedMessageCount(1);
    notificationMock.setResultWaitTime(4000);

    // Create request and put to processQueue
    final String request = UpdateRequestUtil
        .createUpdateTxtMsg("SOME_OTHER_OWNER", DomainType.ONE_SUBSCRIBER, 1111111111L, 2222222222L);
    producerTemplate.sendBody("activemq:queue:{{process.queue.name}}", request);

    // Assert notifications
    notificationMock.assertIsSatisfied();
    final List<Exchange> receivedNotifications = notificationMock.getReceivedExchanges();
    NotificationAssert.assertContainsLogicalAddresses(receivedNotifications, "TEST-LOGICAL-ADDRESS-2");
    NotificationAssert.assertOwnerOnEngagements(receivedNotifications, owner); // Owner changed here

    // Assert database
    final List<Engagement> dbEntities = engagementRepository.findAll();
    assertEquals(2, dbEntities.size());
    DatabaseAssert.assertContains(request, owner, dbEntities); // And owner changed here

  }

  @Test
  public void deleteOneOfTwoExistingEntities() throws InterruptedException {

    notificationMock.expectedMessageCount(1);
    notificationMock.setResultWaitTime(4000);

    final EngagementTransactionType et1 = createET(DomainType.ONE_SUBSCRIBER, 1111111111L, true);
    final EngagementTransactionType et2 = createET(DomainType.ONE_SUBSCRIBER, 2222222222L, false);

    // Init database with two entities
    InitDatabase(UpdateRequestUtil.createUpdate(owner, et1, et2));

    // Create request and put to processQueue
    producerTemplate.sendBody("activemq:queue:{{process.queue.name}}"
        , UpdateRequestUtil.createUpdateTxtMsg(owner, et1, et2));

    // Assert notifications
    // Only et1 should exist in notification since et2 already exists i DB.
    notificationMock.assertIsSatisfied();
    final List<Exchange> receivedNotifications = notificationMock.getReceivedExchanges();
    NotificationAssert.assertContainsLogicalAddresses(receivedNotifications, "TEST-LOGICAL-ADDRESS-2");
    NotificationAssert.assertEngagementEquals(receivedNotifications.get(0), Arrays.asList(et1.getEngagement()) );

    // Assert database
    final List<Engagement> dbEntities = engagementRepository.findAll();
    assertEquals(1, dbEntities.size());
    DatabaseAssert.assertContains(et2.getEngagement(), dbEntities);

  }

  @Test
  public void noNotificationWhenEntityExist() throws InterruptedException {

    notificationMock.expectedMessageCount(0);
    notificationMock.setSleepForEmptyTest(4000);

    final EngagementTransactionType et1 = createET(DomainType.ONE_SUBSCRIBER, 1111111111L, false);

    // Init database with one entity
    InitDatabase(UpdateRequestUtil.createUpdate(owner, et1));

    // Create request and put to processQueue
    producerTemplate.sendBody("activemq:queue:{{process.queue.name}}"
        , UpdateRequestUtil.createUpdateTxtMsg(owner, et1));

    // Assert notifications
    notificationMock.assertIsSatisfied();

    // Assert database
    final List<Engagement> dbEntities = engagementRepository.findAll();
    assertEquals(1, dbEntities.size());
    DatabaseAssert.assertContains(et1.getEngagement(), dbEntities);

  }

  @Test
  public void updateMostRecentContentHappyDay() throws InterruptedException {

    final String originalMostRecent = "20200101120000";// yyyyMMddHHmmss
    final String updatedMostRecent =  "20210101120000";// yyyyMMddHHmmss

    notificationMock.expectedMessageCount(1);
    notificationMock.setResultWaitTime(4000);

    final EngagementTransactionType et1 = createET(DomainType.ONE_SUBSCRIBER, 1111111111L, false);
    et1.getEngagement().setMostRecentContent(originalMostRecent);

    // Init database with entity mostrecent 2020xxx
    InitDatabase(UpdateRequestUtil.createUpdate(owner, et1));

    // Send update with mostrecent 2021xxx
    et1.getEngagement().setMostRecentContent(updatedMostRecent);
    System.out.println(engagementRepository.findAll().get(0).getMostRecentContent());
    producerTemplate.sendBody("activemq:queue:{{process.queue.name}}"
        , UpdateRequestUtil.createUpdateTxtMsg(owner, et1));

    // Assert notification
    final Engagement expectedEntity = toEntity(et1.getEngagement());
    notificationMock.assertIsSatisfied();
    final EngagementTransactionType engagementTransaction = getEngagementTransaction(notificationMock.getReceivedExchanges().get(0)).get(0);
    assertEquals(expectedEntity.getMostRecentContent(), toEntity(engagementTransaction.getEngagement()).getMostRecentContent());

    // Assert database
    final Optional<Engagement> dbEntityOptional = engagementRepository.findById(expectedEntity.getId());
    assertTrue(dbEntityOptional.isPresent());
    assertEquals(expectedEntity.getMostRecentContent(), dbEntityOptional.get().getMostRecentContent());

  }

  private void InitDatabase(UpdateType udateRequest) {
    engagementRepository.saveAll(DatabaseAssert.toEntityList(udateRequest.getEngagementTransaction()));
    assertEquals(udateRequest.getEngagementTransaction().size(), engagementRepository.findAll().size(),
        "Database not set up correctly before test");
  }


}