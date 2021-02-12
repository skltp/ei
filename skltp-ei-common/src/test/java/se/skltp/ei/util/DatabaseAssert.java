package se.skltp.ei.util;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.skltp.ei.service.util.EntityTransformer.toEntity;

import java.util.List;
import java.util.stream.Collectors;
import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.EngagementType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.entity.model.Engagement;
import se.skltp.ei.service.util.JaxbUtil;

public class DatabaseAssert {

  private static final JaxbUtil jaxbUtil = new JaxbUtil(UpdateType.class, ProcessNotificationType.class);

  // Static utility
  private DatabaseAssert() {
  }

  public static void assertContains(String expected, List<Engagement> dbEntitys) {
    final Object unmarshal = jaxbUtil.unmarshal(expected);
    if (unmarshal instanceof UpdateType) {
      assertContains(((UpdateType) unmarshal).getEngagementTransaction(), dbEntitys);
    } else {
      assertContains(((ProcessNotificationType) unmarshal).getEngagementTransaction(), dbEntitys);
    }
  }

  public static void assertContains(String expected, String expectedOwner, List<Engagement> dbEntitys) {
    final Object unmarshal = jaxbUtil.unmarshal(expected);
    if (unmarshal instanceof UpdateType) {
      assertContains(((UpdateType) unmarshal).getEngagementTransaction(), expectedOwner, dbEntitys);
    } else {
      assertContains(((ProcessNotificationType) unmarshal).getEngagementTransaction(), expectedOwner, dbEntitys);
    }
  }

  public static void assertContains(List<EngagementTransactionType> expected, List<Engagement> dbEntitys) {
    for (Engagement requestEntity : toEntityList(expected)) {
      assertTrue(dbEntitys.stream().anyMatch(en -> requestEntity.equals(en))
          , "Didn't find expected entity in database");
    }
  }

  public static void assertContains(EngagementType expectedEngagementType, List<Engagement> dbEntitys) {
    final Engagement expectedEntity = toEntity(expectedEngagementType);
    assertTrue(dbEntitys.stream().anyMatch(en -> expectedEntity.equals(en))
        , "Didn't find expected entity in database");
  }

  public static void assertContains(List<EngagementTransactionType> expected, String expectedOwner, List<Engagement> dbEntitys) {
    for (Engagement requestEntity : toEntityList(expected, expectedOwner)) {
      assertTrue(dbEntitys.stream().anyMatch(en -> requestEntity.equals(en))
          , "Didn't find expected entity in database");
    }
  }

  public static List<Engagement> toEntityList(List<EngagementTransactionType> engagementTransaction) {
    return engagementTransaction.stream().map(et -> toEntity(et.getEngagement())).collect(Collectors.toList());
  }

  public static List<Engagement> toEntityList(List<EngagementTransactionType> engagementTransaction, String expectedOwner) {
    return engagementTransaction.stream().map(et -> {
      et.getEngagement().setOwner(expectedOwner);
      return toEntity(et.getEngagement());
    }).collect(Collectors.toList());
  }


}
