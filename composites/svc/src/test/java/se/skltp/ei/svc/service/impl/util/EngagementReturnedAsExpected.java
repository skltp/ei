package se.skltp.ei.svc.service.impl.util;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.svc.service.api.Header;

import java.util.List;

/**
 * Implementation is supposed to implement a specific test regarding a engagement in relation to the
 * result of ProcessBean methods
 * @see se.skltp.ei.svc.service.impl.ProcessBean#update(Header, UpdateType)
 * ProcessBean#processNotification(Header, ProcessNotificationType)
 * Known implementations are done anonymously by the ProcessBeanIntegrationTestHelper
 * ProcessBeanIntegrationTestHelper
 *
 */
public interface EngagementReturnedAsExpected extends TrialExitMessage {
    /**
     *
     * @param processResult the result of ProcessBean method invocation
     * @return true if as expected
     */
    boolean  isTrueFor(List<EngagementTransactionType> processResult);

}
