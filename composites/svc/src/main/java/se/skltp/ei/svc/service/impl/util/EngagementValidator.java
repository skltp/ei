package se.skltp.ei.svc.service.impl.util;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.EngagementType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.service.api.Header;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static se.skltp.ei.svc.service.api.EiErrorCodeEnum.EI000_TECHNICAL_ERROR;
import static se.skltp.ei.svc.service.api.EiErrorCodeEnum.EI002_DUPLICATE_UPDATE_ENTRIES;
import static se.skltp.ei.svc.service.api.EiErrorCodeEnum.EI003_LOGICALADDRESS_DONT_MATCH_OWNER;
import static se.skltp.ei.svc.service.api.EiErrorCodeEnum.EI004_VALIDATION_ERROR;
import static se.skltp.ei.svc.service.api.EiErrorCodeEnum.EI005_VALIDATION_ERROR_INVALID_LOGICAL_ADDRESS;
import static se.skltp.ei.svc.service.api.EiErrorCodeEnum.EI006_VALIDATION_ERROR_INVALID_SOURCE_SYSTEM;
import static se.skltp.ei.svc.service.api.ProcessInterface.MAX_NUMBER_OF_ENGAGEMENTS;
import static se.skltp.ei.svc.service.api.ProcessInterface.MIN_NUMBER_OF_ENGAGEMENTS;
import static se.skltp.ei.svc.service.impl.util.EntityTransformer.toEntity;

public class EngagementValidator {

    public EngagementValidator() {
        this.owner = null;
        this.pseudonym = null;
        this.updateNotificationNotAllowedHsaIdList = null;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setPseudonym(String pseudonym) {
        this.pseudonym = pseudonym;
    }

    public void setUpdateNotificationNotAllowedHsaIdList(Collection<String> updateNotificationNotAllowedHsaIdList) {
        this.updateNotificationNotAllowedHsaIdList = updateNotificationNotAllowedHsaIdList;
    }

    private String owner;
    private String pseudonym;
    private Collection<String> updateNotificationNotAllowedHsaIdList;


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
    private void maxLengthCheck(String name, String value, int max_length) {
        if (value != null && value.length() > max_length)
            throw EI004_VALIDATION_ERROR.createException("Field \"" + name + "\" is to long");
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

    /**
     * {@inheritDoc}
     */
    public void validateUpdate(Header header, UpdateType request) {
        // R7
        validateLogicalAddress(header);

        validateEngagementTransactions(request.getEngagementTransaction(), false);
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
            validateTransactionLogicalAdressAndSourceSystem(hashCodeIndex, et,updateNotificationNotAllowedHsaIdList);


            // mandatory fields
            validateMandatoryFields(et, ownerCheck);

            // validate max length
            validateFieldMaxLength(et);

            //validate date fields
            validateDates(et);

        }
    }
    /**
     * {@inheritDoc}
     */
    public void validateProcessNotification(ProcessNotificationType request) {
        validateEngagementTransactions(request.getEngagementTransaction(), true);
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

    private void validateTransactionLogicalAdressAndSourceSystem(int etIndex, EngagementType et, Collection<String> updateNotificationNotAllowedHsaIdList) {

        // If no black-list is set then simply bail out without any validations
        if (updateNotificationNotAllowedHsaIdList == null) return;

        if (updateNotificationNotAllowedHsaIdList.contains(et.getLogicalAddress())) {
            throw EI005_VALIDATION_ERROR_INVALID_LOGICAL_ADDRESS.createException(etIndex, et.getLogicalAddress());
        }
        if (updateNotificationNotAllowedHsaIdList.contains(et.getSourceSystem())) {
            throw EI006_VALIDATION_ERROR_INVALID_SOURCE_SYSTEM.createException(etIndex, et.getSourceSystem());
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
}
