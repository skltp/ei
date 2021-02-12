
package se.skltp.ei.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import se.skltp.ei.entity.model.Engagement;

public class EngagementTestUtil {

  public enum DomainType {
    TWO_SUBSCRIBERS(0),
    ONE_SUBSCRIBER(1),
    NO_SUBSCRIBER_1(2),
    NO_SUBSCRIBER_2(3);

    private final int value;

    private DomainType(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }
  }

  /**
   * Generates test data.
   */
  public static List<Engagement> generateEngagements(int startId, int size) {
    List<Engagement> list = new ArrayList<Engagement>();
    for (long i = 0; i < size; i++) {
      Engagement e = generateEngagement(startId + i);
      Date now = new Date();
      e.setMostRecentContent(now);
      list.add(e);
    }
    return list;
  }

  /**
   * Generates a key, which is completely derived from the value of residentIdentification (repeatable).
   */
  public static Engagement generateEngagement(long residentIdentification) {
      return  generateEngagement( residentIdentification, (int) (residentIdentification % 4L));
  }

    /**
     * Generates a key, which is completely derived from the value of residentIdentification (repeatable).
     */
    public static Engagement generateEngagement(long residentIdentification, DomainType type) {
        return  generateEngagement( residentIdentification, type.getValue());
    }

    /**
   * Generates a key, which is completely derived from the value of residentIdentification (repeatable).
   */
  private static Engagement generateEngagement(long residentIdentification, int type) {
    final String[] domains = {"TEST-DOMAIN", "TEST-DOMAIN-2", "TEST-DOMAIN-3", "TEST-DOMAIN-4"};
    final String[] categories = {"TEST-CATEGORY", "TEST-CATEGORY-2", "TEST-CATEGORY-3", "TEST-CATEGORY-4"};
    final String[] logicalAdresses = {"SE100200400-600", "SE100200400-700", "SE100200400-800", "SE100200400-900"};
    final String[] sourceSystems = {"XXX100200400-600", "XXX100200400-700", "XXX100200400-800", "XXX100200400-900"};


    Engagement e = new Engagement();
    e.setBusinessKey("19" + residentIdentification,
        domains[type],
        categories[type],
        String.valueOf(residentIdentification),
        logicalAdresses[type],
        sourceSystems[type],
        "dataController",
        "Inera",
        "NA");

    return e;
  }

  public static Engagement generateEngagement(long residentIdentification, String domain, String category) {
    final String[] logicalAdresses = {"SE100200400-600", "SE100200400-700", "SE100200400-800", "SE100200400-900"};
    final String[] sourceSystems = {"XXX100200400-600", "XXX100200400-700", "XXX100200400-800", "XXX100200400-900"};

    int n = (int) (residentIdentification % 4L);
    Engagement e = new Engagement();
    e.setBusinessKey("19" + residentIdentification,
        domain,
        category,
        String.valueOf(residentIdentification),
        logicalAdresses[n],
        sourceSystems[n],
        "dataController",
        "Inera",
        "NA");

    return e;
  }

}
