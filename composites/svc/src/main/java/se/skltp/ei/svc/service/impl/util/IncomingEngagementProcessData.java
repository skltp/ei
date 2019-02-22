package se.skltp.ei.svc.service.impl.util;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.EngagementType;
import se.skltp.ei.svc.entity.model.Engagement;

import java.util.*;

/**
 * Holds Engagement data used during the process of handling engagements
 */
public class IncomingEngagementProcessData implements Iterable<EngagementTransactionType> {

    private List<Engagement> markedForRemoval = new ArrayList<>();

    private List<EngagementTransactionType> processResult;
    private List<EngagementTransactionType> engagementTransactions;

    public boolean isEngagementsFetchedForSave() {
        return engagementsFetchedForSave;
    }

    private boolean engagementsFetchedForSave = false;


    public List<EngagementTransactionType> getAllEngagementTransactionType() {
        return engagementTransactions;
    }

    private Map<Engagement, EngagementTransactionType> saveList = new HashMap<>();

    private Map<Engagement, EngagementTransactionType> deleteList = new HashMap<>();
    private Map<String, Engagement> existingContent;

    private IncomingEngagementProcessData(List<EngagementTransactionType> engagementTransaction) {
        this.engagementTransactions = engagementTransaction;
        if (this.engagementTransactions == null) {
            this.engagementTransactions = new ArrayList<>();
        }
        processResult = new ArrayList<>();
    }

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
     * @param engagementTransactions update Source
     * @param owner HSA-id for provider of this instance
     * @return DTO for handling the Update
     */
    public static IncomingEngagementProcessData createForUpdate(List<EngagementTransactionType> engagementTransactions, String owner) {
        if (engagementTransactions != null) {

            for (final EngagementTransactionType inEngagementTransaction : engagementTransactions) {
                inEngagementTransaction.getEngagement().setOwner(owner);
            }
        }
        return new IncomingEngagementProcessData(engagementTransactions);
    }

    public static IncomingEngagementProcessData createForProcessNotification(List<EngagementTransactionType> engagementTransactions) {

        return new IncomingEngagementProcessData(engagementTransactions);
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
     * @return all engagements supposed to be saved/persisted
     */
    public Iterable<Engagement> engagementsToSave(boolean pRemove) {
        // markedForRemoval.forEach(engagement->saveList.remove(engagement));
        if (pRemove) {
            for (Engagement engagement : markedForRemoval) saveList.remove(engagement);

            engagementsFetchedForSave = true;
        }
        return saveList.keySet();
    }

    public boolean existsAnythingToSave() {
        return saveList.keySet().size() - markedForRemoval.size() > 0;
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



    public int size() {
        return engagementTransactions.size();
    }

    /**
     * @param existingContent a map of persisted Engagements with key corresponding to any of these sent in for processing
     */
    public void setPersistedEngagementMap(Map<String, Engagement> existingContent) {
        if(existingContent==null)throw new IllegalArgumentException("existingContent Assignment=null please use empty map when no persisted engagemenst where found");
        this.existingContent = existingContent;
    }

    public Engagement getPersistedEngagement(String id) {
        if(existingContent==null)throw new IllegalStateException("It seems like setPersistedEngagementMap not been initiated yet",new NullPointerException("existingContent==null"));
        return existingContent.getOrDefault(id, null);
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

    public List<EngagementTransactionType> getProcessResult() {
        return processResult;
    }

}
