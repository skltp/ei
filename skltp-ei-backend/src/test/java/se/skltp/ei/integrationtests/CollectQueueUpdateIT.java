package se.skltp.ei.integrationtests;

import static se.skltp.ei.EiTeststubRoute.NOTIFICATION_MOCK;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.skltp.ei.EiBackendApplication;
import se.skltp.ei.EiTeststubRoute;
import se.skltp.ei.util.EngagementTestUtil.DomainType;
import se.skltp.ei.util.NotificationAssert;
import se.skltp.ei.util.UpdateRequestUtil;


// This test is ment to verify that the overall route flows is intact
// and that the collect queue is collected and sent for processing.
// The business logic in aggregation should be tested more in unit test
@CamelSpringBootTest
@SpringBootTest(classes = {EiBackendApplication.class, EiTeststubRoute.class})
@ActiveProfiles("teststub")
public class CollectQueueUpdateIT {

  @Produce
  protected ProducerTemplate producerTemplate;

  @Value("${collect.queue.completion.timeout}")
  Long queueTimeout;

  @EndpointInject(NOTIFICATION_MOCK)
  private MockEndpoint notificationMock;

  @BeforeEach
  public void beforeEach() {
    notificationMock.reset();
  }


  @Test
  public void twoDifferentRequestIsCollectedToSameRequest() throws InterruptedException {

    notificationMock.expectedMessageCount(1);
    notificationMock.setResultWaitTime(queueTimeout * 1000 + 3000);

    // Put two different request to collectQueue
    producerTemplate.sendBody("activemq:queue:{{collect.queue.name}}"
        , UpdateRequestUtil.createUpdateTxtMsg("owner", DomainType.ONE_SUBSCRIBER, 1111111111L));
    producerTemplate.sendBody("activemq:queue:{{collect.queue.name}}"
        , UpdateRequestUtil.createUpdateTxtMsg("owner", DomainType.ONE_SUBSCRIBER, 2222222222L));

    // Assert notifications
    notificationMock.assertIsSatisfied();
    NotificationAssert.assertNumberEngagements(notificationMock.getReceivedExchanges().get(0), 2);
  }

  @Test
  public void twoEqualRequestThenOneIsFiltered() throws InterruptedException {

    notificationMock.expectedMessageCount(1);
    notificationMock.setResultWaitTime(queueTimeout * 1000 + 3000);

    // Put two different request to collectQueue
    producerTemplate.sendBody("activemq:queue:{{collect.queue.name}}"
        , UpdateRequestUtil.createUpdateTxtMsg("owner", DomainType.ONE_SUBSCRIBER, 3333333333L));
    producerTemplate.sendBody("activemq:queue:{{collect.queue.name}}"
        , UpdateRequestUtil.createUpdateTxtMsg("owner", DomainType.ONE_SUBSCRIBER, 3333333333L));

    // Assert notifications
    notificationMock.assertIsSatisfied();
    NotificationAssert.assertNumberEngagements(notificationMock.getReceivedExchanges().get(0), 1);
  }
}
