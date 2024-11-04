package se.skltp.ei.subscriber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import riv.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontractresponder._2.FilterType;
import riv.itintegration.engagementindex._1.EngagementTransactionType;
import se.skltp.ei.util.FilterCreator;
import se.skltp.ei.util.EngagementTransactionTestUtil;

class SubscriberTest {
  private List<EngagementTransactionType> engagements;

  private EngagementTransactionType et1;
  private EngagementTransactionType et2;

  @BeforeEach
  public void setup() {

    engagements = new ArrayList();

    et1 = EngagementTransactionTestUtil.createET(1111111111L);
    et1.getEngagement().setCategorization("CATEGORY-A");
    et1.getEngagement().setServiceDomain("SERVICEDOMAIN-A");

    et2 = EngagementTransactionTestUtil.createET(121111111L);
    et2.getEngagement().setCategorization("CATEGORY-B");
    et2.getEngagement().setServiceDomain("SERVICEDOMAIN-B");

    engagements.add(et1);
    engagements.add(et2);

  }



  /**
   * No engagements should be removed when no filters are supplied
   */
  @Test
  public void no_filters() {

    // Create a subscriber with no filters
    Subscriber subscriber = FilterCreator.createOneSubscriber("HSA_ID_A", null).get(0);

    List<EngagementTransactionType> filteredEngagements = subscriber.filter(engagements);

    // Verify that we still got two engagements
    assertEquals(2, filteredEngagements.size());
    assertEquals(engagements.get(0), filteredEngagements.get(0));
  }



  /**
   * Verifies that one engagement is filtered away because of wrong serviceDomain
   */
  @Test
  public void filter_with_servicedomain() {

    // Create a subscriber with a matching serviceDomain
    Subscriber subscriber = FilterCreator.createOneSubscriber("HSA_ID_A", "SERVICEDOMAIN-A").get(0);

    // Filter
    List<EngagementTransactionType> filteredEngagements = subscriber.filter(engagements);

    // Should be one engagement left
    assertSame(1, filteredEngagements.size());

    assertSame(et1.getEngagement(), filteredEngagements.get(0).getEngagement());
  }

  @Test
  public void filter_with_servicedomain_and_categorization() {

    // Filter that should not match...
    FilterType filterType2 = new FilterType();
    filterType2.setServiceDomain("SERVICEDOMAIN-X");
    filterType2.getCategorization().add("CATEGORY-A");
    filterType2.getCategorization().add("CATEGORY-X");

    // Create a subscriber with a matching filter with servicedomain and categorization
    Subscriber subscriber = FilterCreator.createOneSubscriber("HSA_ID_A", "SERVICEDOMAIN-A", "CATEGORY-A", "CATEGORY-UNKNOWN").get(0);

    // Add a filter that should not match
    subscriber.getFilterList().add(filterType2);

    // Filter
    List<EngagementTransactionType> filteredEngagements = subscriber.filter(engagements);

    assertSame(1, filteredEngagements.size());
    assertSame(et1.getEngagement(), filteredEngagements.get(0).getEngagement());
  }

  @Test
  public void filter_with_wrong_category() {

    // Create a subscriber with nonmatching categorization
    Subscriber subscriber = FilterCreator.createOneSubscriber("HSA_ID_A", "SERVICEDOMAIN-A", "CATEGORY-UNKNOWN").get(0);

    // Filter
    List<EngagementTransactionType> filteredEngagements = subscriber.filter(engagements);

    // Should not be any engagements left
    assertSame(0, filteredEngagements.size());
  }



}