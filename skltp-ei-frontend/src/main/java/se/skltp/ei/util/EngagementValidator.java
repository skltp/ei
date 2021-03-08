package se.skltp.ei.util;


import static se.skltp.ei.service.api.EiErrorCodeEnum.EI000_TECHNICAL_ERROR;
import static se.skltp.ei.service.api.EiErrorCodeEnum.EI002_DUPLICATE_UPDATE_ENTRIES;
import static se.skltp.ei.service.api.EiErrorCodeEnum.EI003_LOGICALADDRESS_DONT_MATCH_OWNER;
import static se.skltp.ei.service.api.EiErrorCodeEnum.EI004_VALIDATION_ERROR;
import static se.skltp.ei.service.api.EiErrorCodeEnum.EI005_VALIDATION_ERROR_INVALID_LOGICAL_ADDRESS;
import static se.skltp.ei.service.api.EiErrorCodeEnum.EI006_VALIDATION_ERROR_INVALID_SOURCE_SYSTEM;
import static se.skltp.ei.service.util.EntityTransformer.toEntity;
import static se.skltp.ei.service.util.EIUtils.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.EngagementType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.entity.model.Engagement;
import se.skltp.ei.service.util.EntityTransformer;

@Component
public class EngagementValidator {

  public static final int MIN_NUMBER_OF_ENGAGEMENTS = 1;

  public static final int MAX_NUMBER_OF_ENGAGEMENTS = 1000;
  public static final String REGISTERED_RESIDENT_IDENTIFICATION = "registeredResidentIdentification";
  public static final String SERVICE_DOMAIN = "serviceDomain";
  public static final String CATEGORIZATION = "categorization";
  public static final String LOGICAL_ADDRESS = "logicalAddress";
  public static final String BUSINESS_OBJECT_INSTANCE_IDENTIFIER = "businessObjectInstanceIdentifier";
  public static final String CLINICAL_PROCESS_INTEREST_ID = "clinicalProcessInterestId";
  public static final String SOURCE_SYSTEM = "sourceSystem";
  public static final String DATA_CONTROLLER = "dataController";
  public static final String MOST_RECENT_CONTENT = "mostRecentContent";
  public static final String UPDATE_TIME = "updateTime";
  public static final String CREATION_TIME = "creationTime";

  @Value("${ei.hsa.id}")
  private String owner;

  @Value("${ei.alternative.hsa.id}")
  private String pseudonym;

  @Value("${update-notification.not.allowed.hsaid.list}")
  private Collection<String> updateNotificationNotAllowedHsaIdList;


  public void setOwner(String owner) {
    this.owner = owner;
  }

  public void setPseudonym(String pseudonym) {
    this.pseudonym = pseudonym;
  }

  public void setUpdateNotificationNotAllowedHsaIdList(Collection<String> updateNotificationNotAllowedHsaIdList) {
    this.updateNotificationNotAllowedHsaIdList = updateNotificationNotAllowedHsaIdList;
  }

  private void validateFieldMaxLength(final EngagementType et) {
    maxLengthCheck(REGISTERED_RESIDENT_IDENTIFICATION, et.getRegisteredResidentIdentification(), 32);
    maxLengthCheck(SERVICE_DOMAIN, et.getServiceDomain(), 255);
    maxLengthCheck(CATEGORIZATION, et.getCategorization(), 255);
    maxLengthCheck(LOGICAL_ADDRESS, et.getLogicalAddress(), 64);
    maxLengthCheck(BUSINESS_OBJECT_INSTANCE_IDENTIFIER, et.getBusinessObjectInstanceIdentifier(), 128);
    maxLengthCheck(CLINICAL_PROCESS_INTEREST_ID, et.getClinicalProcessInterestId(), 128);
    maxLengthCheck(SOURCE_SYSTEM, et.getSourceSystem(), 64);
    maxLengthCheck(DATA_CONTROLLER, et.getDataController(), 64);
    maxLengthCheck(MOST_RECENT_CONTENT, et.getMostRecentContent(), 14);
    maxLengthCheck(UPDATE_TIME, et.getUpdateTime(), 14);
    maxLengthCheck(CREATION_TIME, et.getCreationTime(), 14);
  }

  /**
   * Validates all mandatory fields.
   *
   * @param et the engagement record to validate.
   * @param ownerCheck true if an owner check shall be performed as well, otherwise false.
   */
  private void validateMandatoryFields(final EngagementType et, boolean ownerCheck) {
    mandatoryValueCheck(REGISTERED_RESIDENT_IDENTIFICATION, et.getRegisteredResidentIdentification());
    mandatoryValueCheck(SERVICE_DOMAIN, et.getServiceDomain());
    mandatoryValueCheck(CATEGORIZATION, et.getCategorization());
    mandatoryValueCheck(LOGICAL_ADDRESS, et.getLogicalAddress());
    mandatoryValueCheck(BUSINESS_OBJECT_INSTANCE_IDENTIFIER, et.getBusinessObjectInstanceIdentifier());
    mandatoryValueCheck(CLINICAL_PROCESS_INTEREST_ID, et.getClinicalProcessInterestId());
    mandatoryValueCheck(SOURCE_SYSTEM, et.getSourceSystem());
    mandatoryValueCheck(DATA_CONTROLLER, et.getDataController());
    // owner
    if (ownerCheck) {
      mandatoryValueCheck("owner", et.getOwner());
    }
  }

  private void validateWhiteSpace(final EngagementType et) {
    whitespaceValueCheck(REGISTERED_RESIDENT_IDENTIFICATION, et.getRegisteredResidentIdentification());
    whitespaceValueCheck(SERVICE_DOMAIN, et.getServiceDomain());
    whitespaceValueCheck(CATEGORIZATION, et.getCategorization());
    whitespaceValueCheck(LOGICAL_ADDRESS, et.getLogicalAddress());
    whitespaceValueCheck(BUSINESS_OBJECT_INSTANCE_IDENTIFIER, et.getBusinessObjectInstanceIdentifier());
    whitespaceValueCheck(CLINICAL_PROCESS_INTEREST_ID, et.getClinicalProcessInterestId());
    whitespaceValueCheck(SOURCE_SYSTEM, et.getSourceSystem());
    whitespaceValueCheck(DATA_CONTROLLER, et.getDataController());
  }

  private void validateDates(final EngagementType et) {
    dateCheck(MOST_RECENT_CONTENT, et.getMostRecentContent());
    dateCheck(UPDATE_TIME, et.getUpdateTime());
    dateCheck(CREATION_TIME, et.getCreationTime());
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

  private void maxLengthCheck(String name, String value, int maxLength) {
    if (value != null && value.length() > maxLength) {
      throw EI004_VALIDATION_ERROR.createException("Field \"" + name + "\" is to long");
    }
  }


  /**
   * Checks that a mandatory value exists.
   *
   * @param name the field name.
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
  public void validateUpdate(String logicalAddress, UpdateType request) {
    // R7
    validateLogicalAddress(logicalAddress);

    validateEngagementTransactions(request.getEngagementTransaction(), false);
  }

  // Update, R7: Logical address in request equals owner of EI
  private void validateLogicalAddress(String logicalAddress) {
    if (logicalAddress == null || logicalAddress.isEmpty()) {
      throw EI003_LOGICALADDRESS_DONT_MATCH_OWNER.createException("missing", owner);
    }

    if (!logicalAddress.equals(owner) && !logicalAddress.equals(pseudonym)) {
      throw EI003_LOGICALADDRESS_DONT_MATCH_OWNER.createException(logicalAddress, owner);
    }
  }

  /**
   * Validates all aspects of the list of engagement transactions.
   *
   * @param engagementTransactions the list.
   * @param ownerCheck true if mandatory owner check shall be carried out as well, otherwise false.
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
      validateTransactionLogicalAdressAndSourceSystem(hashCodeIndex, et, updateNotificationNotAllowedHsaIdList);

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
   * @param name the field name.
   * @param value the field value.
   */
  private void whitespaceValueCheck(String name, String value) {

    // Validated elsewhere
    if (isEmpty(value)) {
      return;
    }

    // Check that trimmed string has the same length as original
    if (!isTrimmed(value)) {
      throw EI004_VALIDATION_ERROR.createException("mandatory field \"" + name + "\" contains white space in beginning or end");
    }
  }

  private void validateTransactionLogicalAdressAndSourceSystem(int etIndex, EngagementType et,
      Collection<String> updateNotificationNotAllowedHsaIdList) {

    // If no black-list is set then simply bail out without any validations
    if (updateNotificationNotAllowedHsaIdList == null) {
      return;
    }

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
          MAX_NUMBER_OF_ENGAGEMENTS + " engagements. Maximum number of engagements per request is " + MAX_NUMBER_OF_ENGAGEMENTS
          + ".");
    }
  }

  // Update/processNotification - min 1 engagement per request
  private void validateMinLength(List<EngagementTransactionType> engagementTransactions) {
    if (engagementTransactions.size() < MIN_NUMBER_OF_ENGAGEMENTS) {
      throw EI000_TECHNICAL_ERROR.createException("The request contains less than " +
          MIN_NUMBER_OF_ENGAGEMENTS + " engagements. Minium number of engagements per request is " + MIN_NUMBER_OF_ENGAGEMENTS
          + ".");
    }
  }
}
