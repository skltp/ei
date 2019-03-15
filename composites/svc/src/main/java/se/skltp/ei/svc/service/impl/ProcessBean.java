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
import se.skltp.ei.svc.service.impl.util.PersistedEngagementsHolder;
import se.skltp.ei.svc.service.impl.util.SortedEngagementsData;

import javax.validation.constraints.NotNull;
import java.util.*;

import static se.skltp.ei.svc.service.impl.util.EntityTransformer.*;
import static se.skltp.ei.svc.service.impl.util.IncomingRequestInitializer.initEngagementOwner;

/**
 * Updates engagement index with either update or process notification requests.
 *
 * @author Magnus Larsson
 */
public class ProcessBean implements ProcessInterface {


    private enum IgnoreOrNotifySave {NOTIFY_AND_SAVE_CHANGES, SAVE_WITH_NO_PROCESS_NOTIFICATION, IGNORE_NEW_ENGAGEMENT}

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


        if (!initEngagementOwner(request, owner)) {
            return request.getEngagementTransaction();
        }


        // Separate deletes from the saves...
        SortedEngagementsData sortedEngagements = convertToEntityEngagementDataAndSortByDeleteOrSave(request.getEngagementTransaction());

        List<EngagementTransactionType> resultProcessNotifications = new ArrayList<>();


        Map<String, Engagement> persistedEngagementsCorrespondingToIncoming = getGetPersistedEngagementEntityMapByIds(
                extractEngagementsId(sortedEngagements.engagementsToSave())
        );

        removeIncomingItemsThatTriesToResetMostRecentContentAndAddUpdatedDataToProcessResult(sortedEngagements,
                persistedEngagementsCorrespondingToIncoming,
                resultProcessNotifications);


        // Perform the delete if any
        deleteEntityItemsAndAddToResult(sortedEngagements, resultProcessNotifications);

        // Perform the save
        if (sortedEngagements.existsAnythingToSave()) {
            engagementRepository.save(sortedEngagements.engagementsToSave());
        }

        //Sets the last updated date on incoming data that resulted in an update
        updateResultDateIfAppropriate(sortedEngagements);

        // Return a list of incoming EngagementTransactions for that was processed
        return resultProcessNotifications;


    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public List<EngagementTransactionType> processNotification(Header header, ProcessNotificationType request) {
        LOG.debug("The svc.processNotification service is called");

        if (request.getEngagementTransaction().size() == 0) {
            return request.getEngagementTransaction();
        }


        // Separate deletes from the saves...
        SortedEngagementsData sortedEngagements = convertToEntityEngagementDataAndSortByDeleteOrSave(request.getEngagementTransaction());

        //result
        List<EngagementTransactionType> resultProcessNotifications = new ArrayList<>();

        //Avoids making two hits on the persistent layer
        PersistedEngagementsHolder allPersistedEngagementsSortedOnThoseNoLongerBelongsToUsAndThoseMatchingIncoming =
                getAllPersistedEngagementsSortedOnThoseNoLongerBelongsToUsAndThoseMatchingIncoming(sortedEngagements);

        List<Engagement> persistedThatNoLongerBelongsToUs =
                allPersistedEngagementsSortedOnThoseNoLongerBelongsToUsAndThoseMatchingIncoming.
                        getPersistedThatNoLongerBelongsToUs();

        Map<String, Engagement> persistedEngagementsCorrespondingToIncoming =
                allPersistedEngagementsSortedOnThoseNoLongerBelongsToUsAndThoseMatchingIncoming.
                        getPersistedEngagementsCorrespondingToIncoming();

        if (persistedThatNoLongerBelongsToUs.size() > 0) {
            // R5 - Remove the incoming Engagement that changed owner
            for (final Engagement e : persistedThatNoLongerBelongsToUs) {
                LOG.warn("The owner has changed for Engagement with ID: " + e.getId());
                sortedEngagements.addForDeletion(e, null);
            }
        }


        removeIncomingItemsThatTriesToResetMostRecentContentAndAddUpdatedDataToProcessResult(
                sortedEngagements,
                persistedEngagementsCorrespondingToIncoming,
                resultProcessNotifications);


        // Perform the delete if any
        deleteEntityItemsAndAddToResult(sortedEngagements, resultProcessNotifications);

        // Perform the save
        if (sortedEngagements.existsAnythingToSave()) {
            engagementRepository.save(sortedEngagements.engagementsToSave());
        }

        //
        updateResultDateIfAppropriate(sortedEngagements);

        // Return a list of incoming EngagementTransactionTypes that either resulted in an updated state of a the
        // corresponding persisted engagement (delete or update) or a new persisted item
        return resultProcessNotifications;

    }

    /**
     * Note that items deleted because they changed owner is only included in the result as the changed version. Hence
     * there might be more items deleted than included in the getEngagementTransactionTypesMarkedForDeletion result
     *
     * @param sortedEngagements          source of persisted items marked for deletion and corresponding in data
     * @param resultProcessNotifications destination of EngagementTransactionTypes that where deleted
     * @see #getAllPersistedEngagementsSortedOnThoseNoLongerBelongsToUsAndThoseMatchingIncoming
     * @see #addIdsCorrespondingToEngagementsWhereOwnerChanged(Iterable, String, List)
     */
    private void deleteEntityItemsAndAddToResult(SortedEngagementsData sortedEngagements, List<EngagementTransactionType> resultProcessNotifications) {
        if (sortedEngagements.existsAnythingToDelete()) {

            engagementRepository.delete(sortedEngagements.engagementsToDelete());

            resultProcessNotifications.addAll(sortedEngagements.getEngagementTransactionTypesMarkedForDeletion());
        }
    }


    /**
     * MostRecentContent shall always point at the timestamp when the latest EI-Update is called. Hence shall that date
     * never be reset to a older date pointing at another latest information in the source system.
     *
     * @param incomingEngagement  "new" incoming engagement data candidate for replacing existing
     * @param persistedEngagement a existing engagement having the same logical key (same person, location etc)
     * @return if the incoming engagement should be saved and if it should result in a process notification
     */
    private IgnoreOrNotifySave checkIfMostRecentContentIsBeforeThatOfPersistedAndShouldBeIgnored(
            @NotNull Engagement incomingEngagement, @NotNull Engagement persistedEngagement) {
        Preconditions.checkArgument(
                persistedEngagement != null, "PersistedEngagement must not be null, Please check that there is such engagement before invoke");

        if (!bothHasSameMostRecentContent(incomingEngagement, persistedEngagement)) {
            if (incomingHasMostRecentContentOlderThanPersisted(incomingEngagement, persistedEngagement)) {

                LOG.warn("incomingEngagement:" + incomingEngagement + "\n had a MostRecentContent older than the MostRecentContent of corresponding persistedEngagement:" + persistedEngagement + "\n the new engagement was hence ignored");

                return IgnoreOrNotifySave.IGNORE_NEW_ENGAGEMENT;
            } else {
                return IgnoreOrNotifySave.NOTIFY_AND_SAVE_CHANGES;
            }
        }
        //There was a "identical" engagement with same most recent content the engagement will not be included in the
        //result (basically has no effect but still saved except the updated date is updated)
        return IgnoreOrNotifySave.SAVE_WITH_NO_PROCESS_NOTIFICATION;
    }

    /**
     * Note that an unassigned value in the incoming is treated as min date when compared to a set MostRecentContent
     * value
     *
     * @param incomingEngagement  the new engagement
     * @param persistedEngagement a prior engagement with same logical id
     * @return true if most recent content is older than the new or if new is null but not persisted
     */
    private boolean incomingHasMostRecentContentOlderThanPersisted(Engagement incomingEngagement, Engagement persistedEngagement) {
        if (neitherEngagementHasMostRecentContent(incomingEngagement, persistedEngagement)) {
            return false;
        } else if (bothEngagementHasMostRecentContent(incomingEngagement, persistedEngagement)) {
            return incomingEngagement.getMostRecentContent().before(persistedEngagement.getMostRecentContent());
        } else //see note
            return onlyPersistedHasMostRecentContent(incomingEngagement, persistedEngagement);
    }

    private boolean onlyPersistedHasMostRecentContent(Engagement incomingEngagement, Engagement persistedEngagement) {
        return (incomingEngagement.getMostRecentContent() == null && persistedEngagement.getMostRecentContent() != null);
    }

    private boolean bothHasSameMostRecentContent(Engagement incomingEngagement, Engagement persistedEngagement) {
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
    private void updateResultDateIfAppropriate(SortedEngagementsData incomingEngagementProcessData) {

        for (Engagement engagement : incomingEngagementProcessData.engagementsToSave()) {

            EngagementType engagementTransactionType = incomingEngagementProcessData.getSaveListCorrespondingEngagementType(engagement);
            if (engagementTransactionType != null) {
                Date creationTime = engagement.getCreationTime();
                if (creationTime != null) {
                    engagementTransactionType.setCreationTime(formatDate(creationTime));
                }
                Date updateTime = engagement.getUpdateTime();
                if (updateTime != null) {
                    engagementTransactionType.setUpdateTime(formatDate(updateTime));
                }
            }

        }
    }


    /**
     * @param pIncomingEngagementsData source of
     */
    private SortedEngagementsData convertToEntityEngagementDataAndSortByDeleteOrSave(List<EngagementTransactionType> pIncomingEngagementsData) {
        SortedEngagementsData result = new SortedEngagementsData();

        for (final EngagementTransactionType engagementTransaction : pIncomingEngagementsData) {

            EngagementType engagementType = engagementTransaction.getEngagement();
            if (engagementType != null) {

                final Engagement engagement = toEntity(engagementType);

                if (engagementTransaction.isDeleteFlag()) {
                    result.addForDeletion(engagement, engagementTransaction);
                } else {
                    result.addForSaving(engagement, engagementTransaction);
                }
            }

        }
        return result;
    }


    /**
     * This method was introduced in version 1.1.1 to ensure that attempts to "reset" most recent content is ignored
     *
     * @param sortedEngagements                           incoming engagements and their corresponding
     *                                                    EngagementTransactionType sorted by if they should be delete
     *                                                    or saved
     * @param persistedEngagementsCorrespondingToIncoming persisted engagement with same id as incoming engagement
     * @param resultProcessNotifications                  destination for Incoming data that's not removed
     * @see #checkIfMostRecentContentIsBeforeThatOfPersistedAndShouldBeIgnored
     * <p>
     * <p>
     * Note: There might be incoming engagements that are found to be identical to existing persisted engagements
     * these are not put in resultProcessNotifications (cause they haven't really changed). But eventually they will be
     * saved and the last changed date will bee updated.
     * <p>
     * The reason for this is that the method replaced by this method explicitly checked if there was "identical"
     * engagements and excluded these from the result notification list but saved them. Rather than breaking anything
     * that may depend on the last update date changes we desided to keep this behavoiur.
     */
    private void removeIncomingItemsThatTriesToResetMostRecentContentAndAddUpdatedDataToProcessResult(
            SortedEngagementsData sortedEngagements,
            Map<String, Engagement> persistedEngagementsCorrespondingToIncoming,
            List<EngagementTransactionType> resultProcessNotifications) {
        Iterator<Engagement> iterator = sortedEngagements.engagementsToSave().iterator();
        while (iterator.hasNext()) {
            Engagement incomingEngagement = iterator.next();

            Engagement persistedEngagement = persistedEngagementsCorrespondingToIncoming.getOrDefault(incomingEngagement.getId(), null);
            if (persistedEngagement != null) {
                IgnoreOrNotifySave rs = checkIfMostRecentContentIsBeforeThatOfPersistedAndShouldBeIgnored(incomingEngagement, persistedEngagement);
                switch (rs) {
                    //Mark  as not save No Notification
                    case IGNORE_NEW_ENGAGEMENT:
                        iterator.remove();
                        break;
                    //Keep in save list add notify
                    case NOTIFY_AND_SAVE_CHANGES:
                        resultProcessNotifications.add(sortedEngagements.getSaveListCorrespondingEngagementTransactionType(incomingEngagement));

                        break;
                    default:
                        //default = SAVE_WITH_NO_PROCESS_NOTIFICATION: keep in save
                        break;
                }
            } else {
                resultProcessNotifications.add(sortedEngagements.getSaveListCorrespondingEngagementTransactionType(incomingEngagement));
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
     *
     * @param pSortedEngagements source of Engagements id
     * @return List with engagements to remove
     */
    private PersistedEngagementsHolder getAllPersistedEngagementsSortedOnThoseNoLongerBelongsToUsAndThoseMatchingIncoming(SortedEngagementsData pSortedEngagements) {

        final List<String> idOfEngagementsNoLongerBelongingToUs = new ArrayList<>();

        addIdsCorrespondingToEngagementsWhereOwnerChanged(pSortedEngagements.engagementsToDelete(), this.owner, idOfEngagementsNoLongerBelongingToUs);
        addIdsCorrespondingToEngagementsWhereOwnerChanged(pSortedEngagements.engagementsToSave(), this.owner, idOfEngagementsNoLongerBelongingToUs);

        final List<String> allIdsCorrespondingToAnyThing = new ArrayList<>();

        allIdsCorrespondingToAnyThing.addAll(idOfEngagementsNoLongerBelongingToUs);

        //There will be no intersecting Ids since  addIdsCorrespondingToEngagementsWhereOwnerChanged have a owner that
        //differs from any of the ids in the save list
        allIdsCorrespondingToAnyThing.addAll(extractEngagementsId(pSortedEngagements.engagementsToSave()));
        PersistedEngagementsHolder allPersistedEngagementsSortedOnThoseNoLongerBelongsToUsAndThoseMatchingIncoming =
                new PersistedEngagementsHolder(getGetPersistedEngagementEntityListByIds(allIdsCorrespondingToAnyThing),
                        idOfEngagementsNoLongerBelongingToUs);
        return allPersistedEngagementsSortedOnThoseNoLongerBelongsToUsAndThoseMatchingIncoming;


    }

    /**
     * For test only
     *
     * @param request test data
     * @return processed
     */
    public List<Engagement> _getEngagementsWithNewOwners(ProcessNotificationType request) {


        if (request.getEngagementTransaction().size() == 0) {
            return Collections.emptyList();
        }

        SortedEngagementsData testData = convertToEntityEngagementDataAndSortByDeleteOrSave(request.getEngagementTransaction());
        // Separate deletes from the saves...
        return getAllPersistedEngagementsSortedOnThoseNoLongerBelongsToUsAndThoseMatchingIncoming(testData).getPersistedThatNoLongerBelongsToUs();


    }


    /**
     * @param engagementsIdSource Candidate for id generation
     * @param pOwner              owner that may differ from engagements owner
     * @param pDestination        destination of ids generated (if the pOwner differs from any given engagements owner)
     */
    private void addIdsCorrespondingToEngagementsWhereOwnerChanged(Iterable<Engagement> engagementsIdSource, String pOwner, List<String> pDestination) {
        //engagementsIdSource.forEach(engagement->pDestination.add(Hash.generateHashId(engagement,pOwner)));
        for (Engagement engagement : engagementsIdSource) {
            String incomingEngagementOwnerName = engagement.getOwner();

            if (!Objects.equals(incomingEngagementOwnerName, pOwner)) {
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