package se.skltp.ei.svc.service.impl.util;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.EngagementType;
import se.skltp.ei.svc.entity.model.Engagement;

import java.util.*;

/**
 * Holds Engagement data used during the process of handling engagements
 */
public class SortedEngagementsData {







    private Map<Engagement, EngagementTransactionType> saveList = new HashMap<>();

    private Map<Engagement, EngagementTransactionType> deleteList = new HashMap<>();


    public void addForSaving(Engagement toBeSavedLater, EngagementTransactionType origin) {
        saveList.put(toBeSavedLater, origin);
    }

    public void addForDeletion(Engagement deleteCandidate, EngagementTransactionType origin) {
        deleteList.put(deleteCandidate, origin);
    }

    public Iterable<Engagement> engagementsToDelete() {
        return deleteList.keySet();
    }

    public Set<Engagement> engagementsToSave() {
        return saveList.keySet();
    }




    public boolean existsAnythingToSave() {
        return saveList.keySet().size()  > 0;
    }

    public EngagementTransactionType getSaveListCorrespondingEngagementTransactionType(Engagement pKey) {
        return saveList.get(pKey);
    }

    public EngagementType getSaveListCorrespondingEngagementType(Engagement pKey) {
        EngagementTransactionType tmp = saveList.get(pKey);
        if (tmp != null) {
            return tmp.getEngagement();
        } else {
            return null;
        }
    }


    public boolean existsAnythingToDelete() {
        return deleteList.keySet().size() > 0;
    }




    public Collection<EngagementTransactionType> getEngagementTransactionTypesMarkedForDeletion() {
        Collection<EngagementTransactionType> result = new ArrayList<>();
        for (EngagementTransactionType engagementType : deleteList.values()) {
            if (engagementType != null) {
                result.add(engagementType);
            }
        }
        return result;
    }
}
