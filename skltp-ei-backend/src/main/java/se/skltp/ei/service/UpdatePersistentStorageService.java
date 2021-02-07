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
