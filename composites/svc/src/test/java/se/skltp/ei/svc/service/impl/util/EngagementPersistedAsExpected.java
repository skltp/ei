package se.skltp.ei.svc.service.impl.util;

import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.service.api.Header;

import java.util.List;

/**
 * Implementation is supposed to implement a specific test regarding a engagement in relation to what is persisted after
 * ProcessBean methods have been invoked
 * @see se.skltp.ei.svc.service.impl.ProcessBean#update(Header, UpdateType)
 * ProcessBean#processNotification(Header, ProcessNotificationType)
 * Known implementations are done anonymously by the ProcessBeanIntegrationTestHelper
 * ProcessBeanIntegrationTestHelper
 *
 */

public interface EngagementPersistedAsExpected extends TrialExitMessage {
       /**
        *
        * @param persistedEngagements all engagements that are persisted method is called
        * @return true if as expected
        */
       boolean  isTrueFor(List<Engagement> persistedEngagements);

}


