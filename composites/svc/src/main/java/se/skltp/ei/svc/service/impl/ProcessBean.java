/**
 * Copyright (c) 2013 Sveriges Kommuner och Landsting (SKL). <http://www.skl.se/>
 * <p>
 * This file is part of SKLTP.
 * <p>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package se.skltp.ei.svc.service.impl;

import org.mule.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.EngagementType;
import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationResponseType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.entity.model.util.Hash;
import se.skltp.ei.svc.entity.repository.EngagementRepository;
import se.skltp.ei.svc.service.api.Header;
import se.skltp.ei.svc.service.api.ProcessInterface;
import se.skltp.ei.svc.service.impl.util.EngagementValidator;
import se.skltp.ei.svc.service.impl.util.IncomingEngagementProcessData;

import javax.validation.constraints.NotNull;
import java.util.*;

import static se.skltp.ei.svc.service.impl.util.EntityTransformer.formatDate;
import static se.skltp.ei.svc.service.impl.util.EntityTransformer.toEntity;

/**
 * Updates engagement index with either update or process notification requests.
 *
 * @author Magnus Larsson
 */
public class ProcessBean implements ProcessInterface {


    private enum ResultSave {SET_RESULT_AND_SAVE, SAVE_ONLY, NEITHER}

    private static final Logger LOG = LoggerFactory.getLogger(ProcessBean.class);

    private String owner;

    private EngagementValidator validator;

    private EngagementRepository engagementRepository;

    public ProcessBean() {
        this.validator = new EngagementValidator();
    }

    @SuppressWarnings("unused")
    private static UpdateResponseType RESPONSE_OK = new UpdateResponseType() {
        @Override
        public ResultCodeEnum getResultCode() {
            return ResultCodeEnum.OK;
        }
    };

    @SuppressWarnings("unused")
    private static ProcessNotificationResponseType NOTIFICATION_RESPONSE_OK = new ProcessNotificationResponseType() {
        @Override
        public ResultCodeEnum getResultCode() {
            return ResultCodeEnum.OK;
        }
    };


    public void setOwner(String owner) {
        this.owner = owner;
        validator.setOwner(owner);
    }

    public void setPseudonym(String pseudonym) {
        validator.setPseudonym(pseudonym);
    }

    /**
     * Sets/injects the database repository.
     *
     * @param engagementRepository the actual repository to use
     */
    @Autowired
    public void setEngagementRepository(EngagementRepository engagementRepository) {
        LOG.info("ProcessBean got its engagementRepository injected");

        this.engagementRepository = engagementRepository;
    }

    public void setUpdateNotificationNotAllowedHsaIdList(String setUpdateNotificationNotAllowedHsaIdListString) {
        validator.setUpdateNotificationNotAllowedHsaIdList(Arrays.asList(setUpdateNotificationNotAllowedHsaIdListString.split(",")));
    }


    @Override
    public void validateUpdate(Header header, UpdateType parameters) {
        validator.validateUpdate(header, parameters);
    }

    @Override
    public void validateProcessNotification(Header header, ProcessNotificationType parameters) {
        validator.validateProcessNotification(parameters);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * <p>
     * Due to the fact that no underlying XA resources is in use, this transaction will
     * be completely standalone and not to be confused with other transactions, i.e. already
     * started JMS transactions in modules/insvc.
     */
    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public List<EngagementTransactionType> update(Header header, UpdateType request) {
        LOG.debug("The svc.update service is called");


        IncomingEngagementProcessData updateProcessData = IncomingEngagementProcessData.createForUpdate(request.getEngagementTransaction(), this.owner);

        if (updateProcessData.size() == 0) {
            return updateProcessData.getProcessResult();
        }
        // Separate deletes from the saves...
        convertToEntityEngagementDataAndSortByDeleteOrSave(updateProcessData);

        //All Incoming items that's deleted are included in the result
        updateProcessData.getProcessResult().addAll(updateProcessData.getEngagementTransactionTypesMarkedForDeletion());

        return getCommonUpdateProcessNotification(updateProcessData);

    }
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public List<EngagementTransactionType> processNotification(Header header, ProcessNotificationType request) {
        LOG.debug("The svc.processNotification service is called");

        IncomingEngagementProcessData notificationProcessData = IncomingEngagementProcessData.createForProcessNotification(request.getEngagementTransaction());

        if (notificationProcessData.size() == 0) {
            return notificationProcessData.getProcessResult();
        }

        // Separate deletes from the saves...
        convertToEntityEngagementDataAndSortByDeleteOrSave(notificationProcessData);

        notificationProcessData.getProcessResult().addAll(notificationProcessData.getEngagementTransactionTypesMarkedForDeletion());


        // R5 - fetch all posts that should be removed since the incoming Engagement changed owner
        List<Engagement> deleteBecauseOwnerChanged = findCorrespondingDbItemsNoLongerBelongingToThis(notificationProcessData);

        if (deleteBecauseOwnerChanged.size() > 0) {
            for (final Engagement e : deleteBecauseOwnerChanged) {
                LOG.warn("The owner has changed for Engagement with ID: " + e.getId());
                notificationProcessData.addForDeletion(e, null);
            }
        }

        return getCommonUpdateProcessNotification(notificationProcessData);
    }

    private List<EngagementTransactionType> getCommonUpdateProcessNotification(IncomingEngagementProcessData updateProcessData) {
        //Removes obsolete updates based on most recent content
        removeSaveItemsThatShouldBeIgnoredAndAddRestToProcessResult(updateProcessData);

        // Perform the delete if any
        if (updateProcessData.existsAnythingToDelete()) {
            engagementRepository.delete(updateProcessData.engagementsToDelete());
        }

        // Perform the save
        if (updateProcessData.existsAnythingToSave()) {
            engagementRepository.save(updateProcessData.engagementsToSave(true));
        }

        //
        updateResultDateIfAppropriate(updateProcessData);

        // Return a list of incoming EngagementTransactions for that was processed
        return updateProcessData.getProcessResult();
    }


    /**
     * MostRecentContent shall always point at the timestamp when the latest EI-Update is called. Hence shall that date
     * never be reset to a older date pointing at another latest information in the source system.
     *
     * @param incomingEngagement  "new" incoming engagement data candidate for replacing existing
     * @param persistedEngagement a existing engagement having the same logical key (same person, location etc)
     * @return if the incoming engagement should be saved and if it should result in a notification
     */
    private ResultSave evaluateIgnore(@NotNull Engagement incomingEngagement, @NotNull Engagement persistedEngagement) {
        Preconditions.checkArgument(
                persistedEngagement != null, "PersistedEngagement must not be null, Please check that there is such engagement before invoke");


        if (!ifBothHasSameMostRecentContent(incomingEngagement, persistedEngagement)) {
            if (ifIncomingHasMostRecentContentOlderThanPersisted(incomingEngagement, persistedEngagement)) {
                //Don't add to result list!
                //And remove from save list (Basically ignore the new Engagement)
                LOG.warn("incomingEngagement:" + incomingEngagement + "\n had a MostRecentContent older than the MostRecentContent of corresponding persistedEngagement:" + persistedEngagement + "\n the new engagement was hence ignored");
                //incomingEngagementProcessData.removeFromSaveList(incomingEngagement);
                return ResultSave.NEITHER;
            } else {
                return ResultSave.SET_RESULT_AND_SAVE;
            }
        }
        //There was a "identical" engagement with same most recent content the engagement will not be included in the
        //result (basically has no effect but still saved)
        return ResultSave.SAVE_ONLY;
    }

    /**
     *
     * Note that an unassigned value in the incoming is treated as min date when compared to a set MostRecentContent
     * value
     * @param incomingEngagement the new engagement
     * @param persistedEngagement a prior engagement with same logical id
     * @return true if most recent content is older than the new or if new is null but not persisted
     */
    private boolean ifIncomingHasMostRecentContentOlderThanPersisted(Engagement incomingEngagement, Engagement persistedEngagement) {
        if (neitherEngagementHasMostRecentContent(incomingEngagement, persistedEngagement)) {
            return false;
        } else if (bothEngagementHasMostRecentContent(incomingEngagement, persistedEngagement)) {
            return incomingEngagement.getMostRecentContent().before(persistedEngagement.getMostRecentContent());
        } else if(onlyPersistedHasMostRecentContent(incomingEngagement, persistedEngagement)){
            return true;
        }else{//New has Most recent but not old/persisted
            return false;
        }
    }

    private boolean onlyPersistedHasMostRecentContent(Engagement incomingEngagement, Engagement persistedEngagement) {
        return (incomingEngagement.getMostRecentContent()==null&&persistedEngagement.getMostRecentContent()!=null);
    }

    private boolean ifBothHasSameMostRecentContent(Engagement incomingEngagement, Engagement persistedEngagement) {
        if (neitherEngagementHasMostRecentContent(incomingEngagement, persistedEngagement)) {
            return true;
        } else if (bothEngagementHasMostRecentContent(incomingEngagement, persistedEngagement)) {
            return incomingEngagement.getMostRecentContent().equals(persistedEngagement.getMostRecentContent());
        } else {
            return false;
        }
    }

    private boolean bothEngagementHasMostRecentContent(Engagement incomingEngagement, Engagement persistedEngagement) {
        return incomingEngagement.getMostRecentContent() != null && persistedEngagement.getMostRecentContent() != null;
    }

    private boolean neitherEngagementHasMostRecentContent(Engagement incomingEngagement, Engagement persistedEngagement) {
        return incomingEngagement.getMostRecentContent() == null && persistedEngagement.getMostRecentContent() == null;
    }

    /**
     * The creationTime and updateTime is updated when the engagements are saved/updated (onBefore update/save)
     * This method ensures that this is also reflected in the result data
     *
     * @param incomingEngagementProcessData source/destination
     */
    private void updateResultDateIfAppropriate(IncomingEngagementProcessData incomingEngagementProcessData) {

        Preconditions.checkState(isAllNededToSaveSaved(incomingEngagementProcessData), "Please save engagements before calling this method");

        for(Engagement engagement:incomingEngagementProcessData.engagementsToSave()){

            EngagementType engagementTransactionType = incomingEngagementProcessData.getSaveListCorrespondingEngagementType(engagement);
            if(engagementTransactionType!=null){
                Date creationTime = engagement.getCreationTime();
                if(creationTime!=null){
                    engagementTransactionType.setCreationTime(formatDate(creationTime));
                }
                Date updateTime = engagement.getUpdateTime();
                if(updateTime!=null){
                    engagementTransactionType.setUpdateTime(formatDate(updateTime));
                }
            }

        }
    }

    private boolean isAllNededToSaveSaved(IncomingEngagementProcessData processData) {
        return (!processData.existsAnythingToSave())||(processData.isEngagementsFetchedForSave());
    }


    /**
     * @param incomingEngagementProcessData source of
     */
    private void convertToEntityEngagementDataAndSortByDeleteOrSave(IncomingEngagementProcessData incomingEngagementProcessData) {
        if (incomingEngagementProcessData == null) {
            return;
        }
        for (final EngagementTransactionType engagementTransaction : incomingEngagementProcessData) {

            EngagementType engagementType = engagementTransaction.getEngagement();
            if (engagementType != null) {

                final Engagement engagement = toEntity(engagementType);

                if (engagementTransaction.isDeleteFlag()) {
                    incomingEngagementProcessData.addForDeletion(engagement, engagementTransaction);
                } else {
                    incomingEngagementProcessData.addForSaving(engagement, engagementTransaction);
                }
            }

        }
    }




    private void removeSaveItemsThatShouldBeIgnoredAndAddRestToProcessResult(IncomingEngagementProcessData pProcessData) {
        //Fetch all persisted Engagements that matches any of the incoming engagements
        pProcessData.setPersistedEngagementMap(getGetPersistedEngagementEntityMapByIds(pProcessData.getSaveCandidateIds()));

        for (Engagement incomingEngagement : pProcessData.engagementsToSave()) {
            Engagement persistedEngagement = pProcessData.getPersistedEngagement(incomingEngagement.getId());
            if (persistedEngagement != null) {
                ResultSave rs = evaluateIgnore(incomingEngagement, persistedEngagement);
                switch (rs) {
                    //Mark  as not save No Notification
                    case NEITHER:
                        pProcessData.markAsRemoveFromSaveList(incomingEngagement);
                        break;
                        //Keep in save list add notify
                    case SET_RESULT_AND_SAVE:
                        pProcessData.getProcessResult().add(pProcessData.getSaveListCorrespondingEngagementTransactionType(incomingEngagement));
                        //default = SAVE_ONLY: keep in save
                        break;
                    default:
                        break;
                }
            } else {
                pProcessData.getProcessResult().add(pProcessData.getSaveListCorrespondingEngagementTransactionType(incomingEngagement));
            }

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessNotificationType filterProcessNotification(ProcessNotificationType request) {

        final Iterator<EngagementTransactionType> iter = request.getEngagementTransaction().iterator();
        while (iter.hasNext()) {
            EngagementTransactionType e = iter.next();
            if (e.getEngagement().getOwner().equals(this.owner)) {
                iter.remove();
            }
        }

        //   request.getEngagementTransaction().removeIf(engagementTransactionType -> engagementTransactionType.getEngagement().getOwner().equals(this.owner));

        return request;
    }


    /**
     * R5 - fetch all posts that should be removed since the incoming Engagement changed owner.
     * Eg finds any persisted engagements that belongs to this instance and correspond to the incoming engagement
     * except that the incoming engagement belongs to another instance
     *
     * <p>
     * This method is not part of the public API
     * TODO (patrik) - refactor the test so the this method can be private
     *
     * @param incomingEngagementProcessData source of Engagements id
     * @return List with engagements to remove
     */
    private List<Engagement> findCorrespondingDbItemsNoLongerBelongingToThis(IncomingEngagementProcessData incomingEngagementProcessData) {
        Preconditions.checkArgument(sortedInDeleteAndSave(incomingEngagementProcessData), "Either engagementsToDelete(),engagementsToSave() should have items please apply convertToEntityEngagementDataAndSortByDeleteOrSave on incomingEngagementProcessData");

        final List<String> ids = new ArrayList<>(incomingEngagementProcessData.size());

        generateCorrespondingIdsForEngagementsWithDifferentOwner(incomingEngagementProcessData.engagementsToDelete(), this.owner, ids);
        generateCorrespondingIdsForEngagementsWithDifferentOwner(incomingEngagementProcessData.engagementsToSave(), this.owner, ids);
        return getGetPersistedEngagementEntityListByIds(ids);

    }

    /**
     * For test only
     *
     * @param request test data
     * @return processed
     */
    public List<Engagement> _getEngagementsWithNewOwners(ProcessNotificationType request) {

        IncomingEngagementProcessData testData = IncomingEngagementProcessData.createForProcessNotification(request.getEngagementTransaction());
        if (testData.size() == 0) {
            return Collections.emptyList();
        }
        convertToEntityEngagementDataAndSortByDeleteOrSave(testData);
        // Separate deletes from the saves...
        return findCorrespondingDbItemsNoLongerBelongingToThis(testData);


    }

    private boolean sortedInDeleteAndSave(IncomingEngagementProcessData incomingEngagementProcessData) {

        return incomingEngagementProcessData.existsAnythingToSave() || incomingEngagementProcessData.existsAnythingToDelete();
    }

    /**
     * @param engagementsIdSource Candidate for id generation
     * @param pOwner              owner that may differ from engagements owner
     * @param pDestination        destination of ids generated (if the pOwner differs from any given engagements owner)
     */
    private void generateCorrespondingIdsForEngagementsWithDifferentOwner(Iterable<Engagement> engagementsIdSource, String pOwner, List<String> pDestination) {
        //engagementsIdSource.forEach(engagement->pDestination.add(Hash.generateHashId(engagement,pOwner)));
        for (Engagement engagement : engagementsIdSource) {

            if (!Objects.equals(engagement.getOwner(), pOwner)) {
                pDestination.add(Hash.generateHashId(engagement, pOwner));
            }

        }
    }

    private Map<String, Engagement> getGetPersistedEngagementEntityMapByIds(List<String> ids) {

        List<Engagement> tmp = getGetPersistedEngagementEntityListByIds(ids);
        // The request fail if findByIdIn receives an empty list
        if (tmp == null || tmp.size() == 0) {
            return Collections.emptyMap();
        } else {
            // Create a new HashMap with id as key and most_recent_time as value
            HashMap<String, Engagement> returnMap = new HashMap<>();
            for (Engagement engagement : tmp) {
                returnMap.put(engagement.getId(), engagement);
            }
            return returnMap;
        }
    }

    private List<Engagement> getGetPersistedEngagementEntityListByIds(List<String> ids) {

        // The request fail if findByIdIn receives an empty list
        if (ids == null || ids.size() == 0) {
            return Collections.emptyList();
        } else {
            // Create a new HashMap with id as key and most_recent_time as value

            return engagementRepository.findByIdIn(ids);
        }
    }

}