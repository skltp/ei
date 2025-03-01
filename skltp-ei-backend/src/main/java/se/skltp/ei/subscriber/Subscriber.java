package se.skltp.ei.subscriber;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import riv.infrastructure.itintegration.registry.getlogicaladdresseesbyservicecontractresponder._2.FilterType;
import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.EngagementType;

@XmlRootElement
public class Subscriber implements Serializable {

  /**
   * LogicalAddress that identifies this subscriber
   */
  @XmlElement
  private String logicalAdress;

  @XmlElement
  private String queueName;

  /**
   * List of filters for this subscriber
   */
  @XmlElement
  private List<FilterType> filterList;

  public Subscriber() {
  }

  public Subscriber(String logicalAdress, List<FilterType> filterList, String notificationQueuePrefix) {
    this.logicalAdress = logicalAdress;
    this.filterList = filterList;

    queueName = notificationQueuePrefix + logicalAdress;
  }

  public String getLogicalAdress() {
    return logicalAdress;
  }

  public List<FilterType> getFilterList() {
    return filterList;
  }

  public String getNotificationQueueName() {
    return queueName;
  }

  public String getNotificationRouteName() {
    return String.format("backend-%s-notification-route", logicalAdress);
  }

  /**
   * Remove all engagements by applying filter rules for the current subscriber
   *
   * @param engagements a list of engagements to be filtered
   * @return a filtered list of engagements
   */
  public List<EngagementTransactionType> filter(List<EngagementTransactionType> engagements) {

    if (filterList == null || filterList.isEmpty()) {
      return engagements;
    }

    return  engagements.stream().filter(engagementType -> isKeepEngagement(engagementType.getEngagement()))
        .collect(Collectors.toList());
  }


  private boolean isKeepEngagement(EngagementType engagement) {

    for (FilterType filter : filterList) {
      if (engagement.getServiceDomain().equalsIgnoreCase(filter.getServiceDomain())) {

      	// We only delete engagements that have a serviceDomain and categorization
        if (filter.getCategorization().isEmpty()) {
          return true;
        }

        for (String categorization : filter.getCategorization()) {
          if (engagement.getCategorization().equalsIgnoreCase(categorization)) {
            return true;
          }
        }

      }
    }
    return false;
  }


}