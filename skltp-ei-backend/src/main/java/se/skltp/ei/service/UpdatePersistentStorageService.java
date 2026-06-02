/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v2.1 or later.
 * Please refer to the full license files at the project root.
 */
package se.skltp.ei.service;

import java.util.List;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;

public interface UpdatePersistentStorageService {

  @Transactional(isolation= Isolation.READ_UNCOMMITTED)
  List<EngagementTransactionType> update(UpdateType request);

  @Transactional(isolation= Isolation.READ_UNCOMMITTED)
  List<EngagementTransactionType> update(ProcessNotificationType request);
}
