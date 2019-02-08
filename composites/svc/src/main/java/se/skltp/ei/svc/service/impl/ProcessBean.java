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

import static se.skltp.ei.svc.service.api.EiErrorCodeEnum.EI000_TECHNICAL_ERROR;
import static se.skltp.ei.svc.service.api.EiErrorCodeEnum.EI002_DUPLICATE_UPDATE_ENTRIES;
import static se.skltp.ei.svc.service.api.EiErrorCodeEnum.EI003_LOGICALADDRESS_DONT_MATCH_OWNER;
import static se.skltp.ei.svc.service.api.EiErrorCodeEnum.EI004_VALIDATION_ERROR;
import static se.skltp.ei.svc.service.api.EiErrorCodeEnum.EI005_VALIDATION_ERROR_INVALID_LOGICAL_ADDRESS;
import static se.skltp.ei.svc.service.api.EiErrorCodeEnum.EI006_VALIDATION_ERROR_INVALID_SOURCE_SYSTEM;
import static se.skltp.ei.svc.service.impl.util.EntityTransformer.toEntity;
import static se.skltp.ei.svc.service.impl.util.EntityTransformer.formatDate;

import java.util.*;

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
import se.skltp.ei.svc.entity.repository.EngagementRepository;
import se.skltp.ei.svc.service.api.Header;
import se.skltp.ei.svc.service.api.ProcessInterface;
import se.skltp.ei.svc.service.impl.util.IncomingEngagementProcessData;
import se.skltp.ei.svc.service.impl.util.EntityTransformer;

import javax.validation.constraints.NotNull;

/**
 * Updates engagement index with either update or process notification requests.
 *
 * @author Magnus Larsson
 */
public class ProcessBean implements ProcessInterface {

    private enum NotifySave {NOTIFY_AND_SAVE, SAVE_ONLY, NEITHER}

    private static final Logger LOG = LoggerFactory.getLogger(ProcessBean.class);

    private String owner;
    private String pseudonym;

    private EngagementRepository engagementRepository;
    private List<String> updateNotificationNotAllowedHsaIdList;


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
    }

    public void setPseudonym(String pseudonym) {
        this.pseudonym = pseudonym;
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
        updateNotificationNotAllowedHsaIdList = Arrays.asList(setUpdateNotificationNotAllowedHsaIdListString.split(","));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void validateUpdate(Header header, UpdateType request) {
        // R7
        validateLogicalAddress(header);

        validateEngagementTransactions(request.getEngagementTransaction(), false);
    }

    /**
     * Validates all aspects of the list of engagement transactions.
     *
     * @param engagementTransactions the list.
     * @param ownerCheck             true if mandatory owner check shall be carried out as well, otherwise false.
     */
    private void validateEngagementTransactions(final List<EngagementTransactionType> engagementTransactions, boolean ownerCheck) {
        validateMinLength(engagementTransactions);

        validateMaxLength(engagementTransactions);

        final Map<String, Integer> hashCodes = new HashMap<>(engagementTransactions.size());
        int hashCodeIndex = 0;
        for (final EngagementTransactionType engagementTransaction : engagementTransactions) {
            final EngagementType et = engagementTransaction.getEngagement();

            // R1 uniqueness
            final Engagement engagementCandidate = toEntity(et);
            final Integer otherIndex = hashCodes.put(engagementCandidate.getId(), ++hashCodeIndex);

            if (otherIndex != null) {
                throw EI002_DUPLICATE_UPDATE_ENTRIES.createException(otherIndex, hashCodeIndex);
            }

            // Validate that mandatory fields do not start/end with white space
            validateWhiteSpace(et);

            // Validate that reserved hsa-id's (the platforms own hsa-id's for example) are not used by mistake
            // If used it could cause a aggregating servie to call itself with no end, a looping service...
            validateTransactionLogicalAdressAndSourceSystem(hashCodeIndex, et);

            // mandatory fields
            validateMandatoryFields(et, ownerCheck);

            // validate max length
            validateFieldMaxLength(et);

            //validate date fields
            validateDates(et);

        }
    }

    private void validateTransactionLogicalAdressAndSourceSystem(int etIndex, EngagementType et) {

        // If no black-list is set then simply bail out without any validations
        if (updateNotificationNotAllowedHsaIdList == null) return;

        if (updateNotificationNotAllowedHsaIdList.contains(et.getLogicalAddress())) {
            throw EI005_VALIDATION_ERROR_INVALID_LOGICAL_ADDRESS.createException(etIndex, et.getLogicalAddress());
        }
        if (updateNotificationNotAllowedHsaIdList.contains(et.getSourceSystem())) {
            throw EI006_VALIDATION_ERROR_INVALID_SOURCE_SYSTEM.createException(etIndex, et.getSourceSystem());
        }
    }

    /**
     * Checks that a mandatory value exists.
     *
     * @param name  the field name.
     * @param value the field value.
     */
    private void mandatoryValueCheck(String name, String value) {
        if (value == null || value.length() == 0) {
            throw EI004_VALIDATION_ERROR.createException("mandatory field \"" + name + "\" is missing");
        }
    }

    private void validateFieldMaxLength(final EngagementType et) {
        maxLengthCheck("registeredResidentIdentification", et.getRegisteredResidentIdentification(), 32);
        maxLengthCheck("serviceDomain", et.getServiceDomain(), 255);
        maxLengthCheck("categorization", et.getCategorization(), 255);
        maxLengthCheck("logicalAddress", et.getLogicalAddress(), 64);
        maxLengthCheck("businessObjectInstanceIdentifier", et.getBusinessObjectInstanceIdentifier(), 128);
        maxLengthCheck("clinicalProcessInterestId", et.getClinicalProcessInterestId(), 128);
        maxLengthCheck("sourceSystem", et.getSourceSystem(), 64);
        maxLengthCheck("dataController", et.getDataController(), 64);
        maxLengthCheck("mostRecentContent", et.getMostRecentContent(), 14);
        maxLengthCheck("updateTime", et.getUpdateTime(), 14);
        maxLengthCheck("creationTime", et.getCreationTime(), 14);

    }

    private void maxLengthCheck(String name, String value, int max_length) {
        if (value != null && value.length() > max_length)
            throw EI004_VALIDATION_ERROR.createException("Field \"" + name + "\" is to long");
    }

    private void validateDates(final EngagementType et) {
        dateCheck("mostRecentContent", et.getMostRecentContent());
        dateCheck("updateTime", et.getUpdateTime());
        dateCheck("creationTime", et.getCreationTime());
    }

    private void dateCheck(String name, String value) {
        if (value != null && value.length() > 0) {
            try {
                EntityTransformer.parseDate(value);
            } catch (Exception e) {
                throw EI004_VALIDATION_ERROR.createException("Field \"" + name + "\": " + e.getMessage());
            }
        }
    }

    /**
     * Checks that value does not have a white space in begining or end.
     *
     * @param name  the field name.
     * @param value the field value.
     */
    private void whitespaceValueCheck(String name, String value) {

        // Validated elsewhere
        if (value == null || value.length() == 0)
            return;

        // Check that trimmed string has the same length as original
        if (value.trim().length() != value.length()) {
            throw EI004_VALIDATION_ERROR.createException("mandatory field \"" + name + "\" contains white space in beginning or end");
        }
    }

    /**
     * Validates all mandatory fields.
     *
     * @param et         the engagement record to validate.
     * @param ownerCheck true if an owner check shall be performed as well, otherwise false.
     */
    private void validateMandatoryFields(final EngagementType et, boolean ownerCheck) {
        mandatoryValueCheck("registeredResidentIdentification", et.getRegisteredResidentIdentification());
        mandatoryValueCheck("serviceDomain", et.getServiceDomain());
        mandatoryValueCheck("categorization", et.getCategorization());
        mandatoryValueCheck("logicalAddress", et.getLogicalAddress());
        mandatoryValueCheck("businessObjectInstanceIdentifier", et.getBusinessObjectInstanceIdentifier());
        mandatoryValueCheck("clinicalProcessInterestId", et.getClinicalProcessInterestId());
        mandatoryValueCheck("sourceSystem", et.getSourceSystem());
        mandatoryValueCheck("dataController", et.getDataController());
        // owner
        if (ownerCheck) {
            mandatoryValueCheck("owner", et.getOwner());
        }
    }

    private void validateWhiteSpace(final EngagementType et) {
        whitespaceValueCheck("registeredResidentIdentification", et.getRegisteredResidentIdentification());
        whitespaceValueCheck("serviceDomain", et.getServiceDomain());
        whitespaceValueCheck("categorization", et.getCategorization());
        whitespaceValueCheck("logicalAddress", et.getLogicalAddress());
        whitespaceValueCheck("businessObjectInstanceIdentifier", et.getBusinessObjectInstanceIdentifier());
        whitespaceValueCheck("clinicalProcessInterestId", et.getClinicalProcessInterestId());
        whitespaceValueCheck("sourceSystem", et.getSourceSystem());
        whitespaceValueCheck("dataController", et.getDataController());
    }

    // Update, R7: Logical address in request equals owner of EI
    private void validateLogicalAddress(Header header) {
        if (header == null || header.getReceiverId() == null || header.getReceiverId().length() == 0) {
            throw EI003_LOGICALADDRESS_DONT_MATCH_OWNER.createException("missing", owner);
        }

        if (!header.getReceiverId().equals(owner) && !header.getReceiverId().equals(pseudonym)) {
            throw EI003_LOGICALADDRESS_DONT_MATCH_OWNER.createException(header.getReceiverId(), owner);
        }
    }


    // Update/processNotification - max 1000 engagements per request
    private void validateMaxLength(List<EngagementTransactionType> engagementTransactions) {
        if (engagementTransactions.size() > MAX_NUMBER_OF_ENGAGEMENTS) {
            throw EI000_TECHNICAL_ERROR.createException("The request contains more than " +
                    MAX_NUMBER_OF_ENGAGEMENTS + " engagements. Maximum number of engagements per request is " + MAX_NUMBER_OF_ENGAGEMENTS + ".");
        }
    }

    // Update/processNotification - min 1 engagement per request
    private void validateMinLength(List<EngagementTransactionType> engagementTransactions) {
        if (engagementTransactions.size() < MIN_NUMBER_OF_ENGAGEMENTS) {
            throw EI000_TECHNICAL_ERROR.createException("The request contains less than " +
                    MIN_NUMBER_OF_ENGAGEMENTS + " engagements. Minium number of engagements per request is " + MIN_NUMBER_OF_ENGAGEMENTS + ".");
        }
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

        IncomingEngagementProcessData incomingEngagementProcessData = new IncomingEngagementProcessData(request.getEngagementTransaction(), this.owner);

        // Separate deletes from the saves...
        convertToEntityEngagementDataAndSortByDeleteOrSave(incomingEngagementProcessData);

        // Create a list of EngagementTransactions that we will use as a base for Notifications


        //For all Engagements that are marked for deletion there should bee a notification
        incomingEngagementProcessData.getNotifications().addAll(incomingEngagementProcessData.getEngagementTransactionTypesMarkedForDeletion());

        //Fetch all persisted Engagements that matches any of the incoming engagements
        incomingEngagementProcessData.setPersistedEngagementMap(getGetPersistedEngagementEntityMapByIds(incomingEngagementProcessData.getSaveCandidateIds()));

        // Loop over our incoming list of data and move data to new list if a notification should be sent
        for (Iterator<Engagement> iterator = incomingEngagementProcessData.engagementsToSave().iterator(); iterator.hasNext(); ) {

            Engagement incomingEngagement = iterator.next();
            Engagement persistedEngagement = incomingEngagementProcessData.getPersistedEngagement(incomingEngagement.getId());
            // Check if we find this record in our map
            if (persistedEngagement != null) {
                updateNotificationDateIfAppropriate(incomingEngagement, incomingEngagementProcessData, persistedEngagement);
                switch (incomingShouldBeSavedAndNotifySent(incomingEngagement, persistedEngagement)){
                    //Mark  as not save No Notification
                    case NEITHER:incomingEngagementProcessData.markAsRemoveFromSaveList(incomingEngagement);
                    //Keep in save list add notify
                    case NOTIFY_AND_SAVE:incomingEngagementProcessData.getNotifications().add(incomingEngagementProcessData.getSaveListCorrespondingEngagementTransactionType(incomingEngagement));
                    //default = SAVE_ONLY: keep in save
                    default:
                }
            } else {//No corresponding engagement in persistent layer
                incomingEngagementProcessData.getNotifications().add(incomingEngagementProcessData.getSaveListCorrespondingEngagementTransactionType(incomingEngagement));
            }
        }

        // Perform the delete if any
        if (incomingEngagementProcessData.existsAnythingToDelete()) {
            engagementRepository.delete(incomingEngagementProcessData.engagementsToDelete());
        }

        // Perform the save
        if (incomingEngagementProcessData.existsAnythingToSave()) {
            engagementRepository.save(incomingEngagementProcessData.engagementsToSave(true));
        }


        // Return a list of EngagementTransactions for notification to subscribers
        return incomingEngagementProcessData.getNotifications();
    }

    /**
     * If the mostRecentContent(date) is assigned the semantics is: that a any Engagements for a "person" at the
     * "care giver" that may been in their system before that the given mostRecentContent date has been cancelled.
     *
     *if mostRecentContent is assigned to a date before that of a previous message, the new message should
     *be ignored (new message obsolete since the date EI already received implicitly include those engagements)
     *
     * @param incomingEngagement "new" incoming engagement data candidate for replacing existing
     * @param persistedEngagement a existing engagement having the same logical key (same person, location etc)
     * @return if the incoming engagement should be saved and if it should result in a notification
     */
    private NotifySave incomingShouldBeSavedAndNotifySent( @NotNull Engagement incomingEngagement, @NotNull Engagement persistedEngagement) {
        Preconditions.checkArgument(
                persistedEngagement != null, "PersistedEngagement must not be null, Please check that there is such engagement before invoke");

        if (!ifBothHasSameMostRecentContent(incomingEngagement, persistedEngagement)) {
            if (ifIncomingHasMostRecentContentOlderThanPersisted(incomingEngagement, persistedEngagement)) {
                //Don't add to processNotification list!
                //And remove from save list (Basically ignore the new Engagement)
                LOG.warn("incomingEngagement:" + incomingEngagement + "\n had a MostRecentContent older than the MostRecentContent of corresponding persistedEngagement:" + persistedEngagement + "\n the new engagement was hence ignored");
                //incomingEngagementProcessData.removeFromSaveList(incomingEngagement);
                return NotifySave.NEITHER;
            } else {
                return NotifySave.NOTIFY_AND_SAVE;
                //
            }
        }
        return NotifySave.SAVE_ONLY;
    }

    private boolean ifIncomingHasMostRecentContentOlderThanPersisted(Engagement incomingEngagement, Engagement persistedEngagement) {
        if (neitherEngagementHasMostRecentContent(incomingEngagement, persistedEngagement)) {
            return false;
        } else if (bothEngagementHasMostRecentContent(incomingEngagement, persistedEngagement)) {
            return incomingEngagement.getMostRecentContent().before(persistedEngagement.getMostRecentContent());
        } else {
            return false;
        }
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
     * If the new engagement is an update we want the this to be reflected in the notifications as well
     *
     * @param incomingEngagement            the new engagement
     * @param incomingEngagementProcessData source
     * @param persistedEngagement           an existing previously saved engagement corresponding to the new (may be null in cases where no such engagement exist)
     */
    private void updateNotificationDateIfAppropriate(Engagement incomingEngagement, IncomingEngagementProcessData incomingEngagementProcessData, Engagement persistedEngagement) {

        EngagementType inEngagementType = incomingEngagementProcessData.getSaveListCorrespondingEngagementType(incomingEngagement);

        Date creationTime = persistedEngagement.getCreationTime();

        if (creationTime != null) {
            inEngagementType.setUpdateTime(formatDate(new Date()));
            inEngagementType.setCreationTime(formatDate(creationTime));
        } else {//Since CreationTime is Annotated as not null this branch should never happen
            inEngagementType.setCreationTime(formatDate(new Date()));
        }
    }


    /**
     * @param incomingEngagementProcessData source of
     */
    private void convertToEntityEngagementDataAndSortByDeleteOrSave(IncomingEngagementProcessData incomingEngagementProcessData) {
        for (final EngagementTransactionType engagementTransaction : incomingEngagementProcessData) {

            EngagementType engagementType = engagementTransaction.getEngagement();
            engagementType.setOwner(this.owner); // According to R6 owner should always be set to owner of the index

            final Engagement engagement = toEntity(engagementType);

            if (engagementTransaction.isDeleteFlag()) {
                incomingEngagementProcessData.addForDeletion(engagement, engagementTransaction);
            } else {
                incomingEngagementProcessData.addForSaving(engagement, engagementTransaction);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateProcessNotification(Header header, ProcessNotificationType request) {
        validateEngagementTransactions(request.getEngagementTransaction(), true);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public List<EngagementTransactionType> processNotification(Header header, ProcessNotificationType request) {
        LOG.debug("The svc.processNotification service is called");

        // Separate deletes from the saves...
        final List<EngagementTransactionType> engagementTransactions = request.getEngagementTransaction();
        final List<Engagement> saveList = new ArrayList<>(engagementTransactions.size());
        List<Engagement> deleteList = null;
        for (final EngagementTransactionType engagementTransaction : engagementTransactions) {

            EngagementType et = engagementTransaction.getEngagement();

            final Engagement e = toEntity(et);
            if (engagementTransaction.isDeleteFlag()) {
                if (deleteList == null) {
                    deleteList = new ArrayList<>();
                }
                deleteList.add(e);
            } else {
                saveList.add(e);
            }
        }


        // R5 - fetch all posts that should be removed since the incoming Engagement changed owner
        List<Engagement> engagementsWithNewOwners = getEngagementsWithNewOwners(request);
        if (engagementsWithNewOwners.size() > 0) {
            if (deleteList == null) {
                deleteList = new ArrayList<>();
            }

            for (final Engagement e : engagementsWithNewOwners) {
                LOG.warn("The owner has changed for Engagement with ID: " + e.getId());
                deleteList.add(e);
            }
        }

        // Perform the delete if any
        if (deleteList != null) {
            engagementRepository.delete(deleteList);
        }

        // Perform the save
        engagementRepository.save(saveList);

        // Return a list of EngagementTransactions for now, we will remove duplicate add/updates later
        return engagementTransactions;
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
     * R5 - fetch all posts that should be removed since the incoming Engagement changed owner
     * <p>
     * This method is not part of the public API
     * TODO (patrik) - refactor the test so the this method can be private
     *
     * @param request source of Engagements id
     * @return List with engagements to remove
     */
    public List<Engagement> getEngagementsWithNewOwners(ProcessNotificationType request) {

        final List<EngagementTransactionType> engagementTransactions = request.getEngagementTransaction();
        final List<String> ids = new ArrayList<>(engagementTransactions.size());

        for (final EngagementTransactionType engagementTransaction : engagementTransactions) {
            EngagementType et = engagementTransaction.getEngagement();
            ids.add(toEntity(et, this.owner).getId());
        }

        // The request fail if findByIdIn receives an empty list
        if (ids.size() == 0) {
            return Collections.emptyList();
        } else {
            return engagementRepository.findByIdIn(ids);
        }
    }


    private Map<String, Engagement> getGetPersistedEngagementEntityMapByIds(List<String> ids) {

        // The request fail if findByIdIn receives an empty list
        if (ids == null || ids.size() == 0) {
            return Collections.emptyMap();
        } else {
            // Create a new HashMap with id as key and most_recent_time as value
            HashMap<String, Engagement> returnMap = new HashMap<>();
            for (Engagement engagement : engagementRepository.findByIdIn(ids)) {
                returnMap.put(engagement.getId(), engagement);
            }
            return returnMap;
        }
    }

}