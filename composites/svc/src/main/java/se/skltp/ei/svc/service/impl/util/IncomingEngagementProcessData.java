package se.skltp.ei.svc.service.impl.util;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.EngagementType;
import se.skltp.ei.svc.entity.model.Engagement;

import java.util.*;

/**
 * Holds Engagement data used during the process of handling new engagements
 */
public class IncomingEngagementProcessData implements Iterable<EngagementTransactionType> {

    private List<Engagement> markedForRemoval = new ArrayList<>();

    private List<EngagementTransactionType> notifications;
    private List<EngagementTransactionType> engagementTransactions;

    private Map<Engagement, EngagementTransactionType> saveList = new HashMap<>();

    private Map<Engagement, EngagementTransactionType> deleteList = new HashMap<>();
    private Map<String, Engagement> existingContent;

    public IncomingEngagementProcessData(List<EngagementTransactionType> engagementTransaction, String owner) {
        this.engagementTransactions = engagementTransaction;

        for (final EngagementTransactionType inEngagementTransaction : this) {
            inEngagementTransaction.getEngagement().setOwner(owner);
        }
        notifications = new ArrayList<>();
    }

    public void addForSaving(Engagement toBeSavedLater, EngagementTransactionType origin) {
        saveList.put(toBeSavedLater, origin);
    }

    public void addForDeletion(Engagement deleteCandidate, EngagementTransactionType origin) {
        deleteList.put(deleteCandidate, origin);
    }

    public Iterable<Engagement> engagementsToDelete() {
        return deleteList.keySet();
    }

    public Iterable<Engagement> engagementsToSave() {
        return saveList.keySet();
    }

    /**
     * Since list returned is the keySet of a hashMap we don't want to remove stuff using the keySetIterator (Thus
     * may causing discrepancy's between key value sets). Hence we rather mark these items for removal during iteration
     * over that set (otherwise getting: java.util.ConcurrentModificationException).
     *
     * @param pRemove if true any items marked for removal is removed before returning list
     * @return
     */
    public Iterable<Engagement> engagementsToSave(boolean pRemove) {
        // markedForRemoval.forEach(engagement->saveList.remove(engagement));
        if (pRemove) {
            for (Engagement engagement : markedForRemoval) saveList.remove(engagement);
        }
        return saveList.keySet();
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


    @Override
    public Iterator<EngagementTransactionType> iterator() {
        return engagementTransactions.iterator();
    }


    public boolean existsAnythingToDelete() {
        return deleteList.keySet().size() > 0;
    }

    public boolean existsAnythingToSave() {
        return saveList.keySet().size() > 0;
    }

    public int size() {
        return engagementTransactions.size();
    }

    public void setPersistedEngagementMap(Map<String, Engagement> existingContent) {
        this.existingContent = existingContent;
    }

    public Engagement getPersistedEngagement(String id) {
        return existingContent.getOrDefault(id, null);
    }

    public Collection<EngagementTransactionType> getEngagementTransactionTypesMarkedForDeletion() {
        return deleteList.values();
    }

    public List<String> getSaveCandidateIds() {
        List<String> result = new ArrayList<>();
        for (Engagement engagement : saveList.keySet()) {
            result.add(engagement.getId());

        }
        //saveList.keySet().forEach(engagement -> result.add(engagement.getId()));
        return result;
    }


    public void markAsRemoveFromSaveList(Engagement incomingEngagement) {

        markedForRemoval.add(incomingEngagement);
    }

    public List<EngagementTransactionType> getNotifications() {
        return notifications;
    }

}
