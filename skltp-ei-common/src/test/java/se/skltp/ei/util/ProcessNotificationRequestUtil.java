/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v2.1 or later.
 * Please refer to the full license files at the project root.
 */
package se.skltp.ei.util;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ObjectFactory;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import se.skltp.ei.service.util.JaxbUtil;
import se.skltp.ei.util.EngagementTestUtil.DomainType;

public class ProcessNotificationRequestUtil {
  private static final JaxbUtil jaxbUtil = new JaxbUtil(ProcessNotificationType.class);
  private static final ObjectFactory notification_of = new ObjectFactory();


  public static ProcessNotificationType createProcessNotification(String owner, long... residentIds) {

    ProcessNotificationType request = new ProcessNotificationType();

    for (int i = 0; i < residentIds.length; i++) {
      EngagementTransactionType et = EngagementTransactionTestUtil.createET(residentIds[i]);
      request.getEngagementTransaction().add(et);
      et.getEngagement().setOwner(owner);
    }

    return request;
  }

  public static ProcessNotificationType createProcessNotification(String owner, DomainType domainType, long... residentIds) {

    ProcessNotificationType request = new ProcessNotificationType();

    for (int i = 0; i < residentIds.length; i++) {
      EngagementTransactionType et = EngagementTransactionTestUtil.createET(residentIds[i], domainType);
      request.getEngagementTransaction().add(et);
      et.getEngagement().setOwner(owner);
    }

    return request;
  }

  public static ProcessNotificationType createProcessNotification(String owner, EngagementTransactionType... etList) {

    ProcessNotificationType request = new ProcessNotificationType();

    for (EngagementTransactionType et : etList) {
      request.getEngagementTransaction().add(et);
      et.getEngagement().setOwner(owner);
    }
    return request;
  }


  public static String createProcessNotificationTxt(String owner, DomainType domainType, long... residentIds) {
    return jaxbUtil.marshal(notification_of.createProcessNotification(createProcessNotification(owner, domainType, residentIds)));
  }

  public static String createProcessNotificationTxt(String owner, EngagementTransactionType... etList) {
    return jaxbUtil.marshal(notification_of.createProcessNotification(createProcessNotification(owner, etList)));
  }
}
