package se.skltp.ei.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import se.skltp.ei.util.EngagementTransactionTestUtil;

class FilterProcessNotificaionsOnOwnerProcessorTest {

  private static final String OWNER = "logical-address";

  private static RemoveCircularProcessNotificationsProcessor filterProcessNotification;

  @BeforeAll
  static void beforeAll() {
    filterProcessNotification = new RemoveCircularProcessNotificationsProcessor();
    filterProcessNotification.setOwner(OWNER);
  }
    /**
   * Tests $10.5 - R4 that engagements with the same owner as the current index
   * should be removed from the request
   *
   * @throws Exception
   */
  @Test
  public void processNotification_R4_OK_filter_should_remove_circular_notifications() throws Exception {

    ProcessNotificationType request = new ProcessNotificationType();
    EngagementTransactionType et1 = EngagementTransactionTestUtil.createET(1111111111L);
    EngagementTransactionType et2 = EngagementTransactionTestUtil.createET(2222222222L);

    et2.getEngagement().setOwner(OWNER);

    request.getEngagementTransaction().add(et1);
    request.getEngagementTransaction().add(et2);


    filterProcessNotification.removeCircularNotifications(request);
    assertEquals(1, request.getEngagementTransaction().size());
    assertEquals(et1.getEngagement(), request.getEngagementTransaction().get(0).getEngagement());

  }

  /**
   * R4 - verifies that everything works when all engagements have been removed from the request
   */
  @Test
  public void processNotification_R4_OK_no_engagements_left() throws Exception {

    ProcessNotificationType request = new ProcessNotificationType();
    EngagementTransactionType et1 = EngagementTransactionTestUtil.createET(1111111111L);
    request.getEngagementTransaction().add(et1);

    et1.getEngagement().setOwner(OWNER);

    filterProcessNotification.removeCircularNotifications(request);
    assertEquals(0, request.getEngagementTransaction().size());
    assertEquals(0, request.getEngagementTransaction().size());
  }



}