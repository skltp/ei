package se.skltp.ei.svc.service.impl.util;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;

public class IncomingRequestInitializer {
    /**
     * To ensure the following:
     * <p>
     * "R6: owner of the persisted engagement item. Shall have a owner value corresponding to the organisation that
     * is the provider of this service. For example: the national instance provided/hosted by Inera AB
     * must ska mark all items created by this service-contract with a HSA-id corresponding Inera AB:s.
     * And for any instance implementing this contract that are provided by a county, that instance should use their
     * organisation-number.
     * The purpose is to enable differentiation of items persisted on invocation of update from those persisted during
     * invocation of ProcessNotification.
     * <p>
     * This method sets (for all items) the engagement owner to the "owner" of this instance
     *
     * @param request update Source
     * @param pOwner HSA-id for provider of this instance
     * @return true if anything to update exists
     */

    public static boolean initEngagementOwner(UpdateType request, String pOwner){

        for(EngagementTransactionType engagementTransactionType: request.getEngagementTransaction()){
            engagementTransactionType.getEngagement().setOwner(pOwner);
        }
        return (request.getEngagementTransaction().size()>0);
    }


}
