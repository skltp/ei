package se.skltp.ei.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.skltp.ei.service.util.EntityTransformer.toEntity;

import java.util.List;
import org.apache.camel.Exchange;
import org.apache.cxf.message.MessageContentsList;
import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.EngagementType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;

public class NotificationAssert {

  // Utility
  private NotificationAssert() {
  }

  public static void assertNumberEngagements(Exchange exchange, int expectedNumEngagements) {
    assertEquals(expectedNumEngagements, getProcessNotification(exchange).getEngagementTransaction().size());
  }

  public static void assertContainsLogicalAddresses(List<Exchange> exchanges, String... expectedAddresses) {
    for (String address : expectedAddresses) {
      final long numMatches = exchanges.stream()
          .filter(ex -> getLogicalAddress(ex).equalsIgnoreCase(address)).count();
      assertEquals(1, numMatches, "LogicalAddress in Notification wrong:" + address);
    }
  }

  public static void assertOwnerOnEngagements(List<Exchange> exchanges, String owner) {
    for(Exchange exchange : exchanges) {
      getEngagementTransaction(exchange).forEach(type->
          assertEquals(owner, type.getEngagement().getOwner(), "Not expected owner in ProcessNotification"));
    }
  }

  public static void assertEngagementEquals(Exchange exchange, List<EngagementType> expectedEngagements) {
    final List<EngagementTransactionType> notifications = getEngagementTransaction(exchange);
    assertEquals( expectedEngagements.size(), notifications.size(), "Notification size not equal expected size");
    for (EngagementType expected : expectedEngagements) {
      final boolean match = notifications.stream()
          .anyMatch(notification -> toEntity(expected).equals(toEntity(notification.getEngagement())));
      assertTrue(match, "Couldn't find expected notification");
    }

  }


  public static String getLogicalAddress(Exchange exchange) {
    return (String) exchange.getIn().getBody(MessageContentsList.class).get(0);
  }

  public static ProcessNotificationType getProcessNotification(Exchange exchange) {
    return (ProcessNotificationType) exchange.getIn().getBody(MessageContentsList.class).get(1);
  }

  public static List<EngagementTransactionType> getEngagementTransaction(Exchange exchange) {
    return getProcessNotification(exchange).getEngagementTransaction();
  }
}
