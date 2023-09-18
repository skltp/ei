package se.skltp.ei.util;

import java.util.ArrayList;
import java.util.List;
import se.rivta.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontractresponder.v2.FilterType;
import se.skltp.ei.subscriber.Subscriber;

/**
 * Utility-class for creating filters and subscriber when building unittests
 */
public class FilterCreator {

  static String NOTIFICATION_QUEUE_PREFIX = "EI.NOTIFICATION.";

  /**
   * Create a list of FilterType with only one FilterType element
   *
   * @param filter
   * @return
   */
  public static List<FilterType> createFilterList(FilterType filter) {
    List<FilterType> filterList = new ArrayList();
    filterList.add(filter);

    return filterList;
  }

  /**
   * Creates a list of subscribers with one subscriber
   *
   * @param logicalAddress the logialAddress that the subscriber will use - mandatory
   * @param serviceDomain servicedomain as filter
   * @param categorizations zero or more categorizations
   * @return
   */
  public static List<Subscriber> createOneSubscriber(String logicalAddress, String serviceDomain, String ...categorizations) {

    // List of subscribers
    List<Subscriber> subscribers = new ArrayList();

    // Create a subscriber
    Subscriber s;
    if (serviceDomain != null) {
      s = new Subscriber(logicalAddress, createFilterList(createFilter(serviceDomain, categorizations)), NOTIFICATION_QUEUE_PREFIX);
    } else {
      s = new Subscriber(logicalAddress, new ArrayList(), NOTIFICATION_QUEUE_PREFIX);
    }

    subscribers.add(s);

    return subscribers;
  }


  /**
   * Creates a filter with a serviceDomain and zero or more categories
   *
   * @param serviceDomain
   * @param categorizations
   * @return
   */
  public static FilterType createFilter(String serviceDomain, String ...categorizations) {

    FilterType filterType = new FilterType();
    filterType.setServiceDomain(serviceDomain);

    if (categorizations != null) {
      for (String categorization : categorizations) {
        filterType.getCategorization().add(categorization);
      }
    }

    return filterType;
  }


  /**
   * Creates a filter with a serviceDomain and zero or more categories
   *
   * @param serviceDomain
   * @param categorizations
   * @return A list of 1 FilterType
   */
  public static List<FilterType> createFilterWithList(String serviceDomain, String ...categorizations) {
    FilterType createFilter = createFilter(serviceDomain, categorizations);
    return createFilterList(createFilter);
  }


}
