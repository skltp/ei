package se.skltp.ei.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.util.AssertionErrors.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.service.api.EiErrorCodeEnum;
import se.skltp.ei.service.api.EiException;

class EngagementValidatorTest {

  private static final String OWNER = "logical-address";

  private static EngagementValidator engagementValidator;

  @BeforeAll
  public static void beforeAll() {
    engagementValidator = new EngagementValidator();
    engagementValidator.setOwner(OWNER);
  }

  /**
   * test R1 for Update service with an negative test, i.e. null engagements (that is not allowed)
   */
  @Test
  public void update_r1_negative_null() throws Exception {

    try {
      UpdateType request = new UpdateType();
      EngagementTransactionType e1 = null;
      request.getEngagementTransaction().add(e1);

      engagementValidator.validateUpdate(OWNER, request);
      fail("Expected exception here");

    } catch (NullPointerException e) {
      // That was the expected exception, carry on...
      // TODO: Validate expected error message
    }
  }

  /**
   * test R1 for Update service with an negative test, i.e. verify that the validation detects two equal engagements in the same
   * request (that is not allowed)
   */
  @Test
  public void update_r1_negative_validate_non_equal() throws Exception {

    try {
      UpdateType request = new UpdateType();
      EngagementTransactionType et1 = GenServiceTestDataUtil.generateEngagementTransaction(1111111111L);
      EngagementTransactionType et2 = GenServiceTestDataUtil.generateEngagementTransaction(2222222222L);
      EngagementTransactionType et3 = GenServiceTestDataUtil.generateEngagementTransaction(3333333333L);

      request.getEngagementTransaction().add(et3);
      request.getEngagementTransaction().add(et1);
      request.getEngagementTransaction().add(et2);
      request.getEngagementTransaction().add(et1);

      engagementValidator.validateUpdate(OWNER, request);
      fail("Expected exception here");

    } catch (EiException e) {
      // That was the expected exception, carry on...
      assertEquals("EI002", e.getCode());
      assertEquals(
          "EI002: EngagementTransaction #2 and #4 have the same key. That is not allowed. See rule for Update-R1 in service contract",
          e.getMessage());
    }
  }

  @Test
  public void update_r7_positive_owner_matches_logicaladdress() throws Exception {
    try {
      UpdateType request = new UpdateType();
      EngagementTransactionType et1 = GenServiceTestDataUtil.generateEngagementTransaction(1111111111L);
      request.getEngagementTransaction().add(et1);

      engagementValidator.validateUpdate(OWNER, request);
    } catch (EiException e) {
      fail("Expected ok, not excpetion");
    }
    assertTrue("Test went ok", true);
  }

  @Test
  public void update_r7_negative_owner_dont_match_logicaladdress() throws Exception {
    try {
      engagementValidator.validateUpdate("wrongLogicalAddress", new UpdateType());
      fail("Expected EiException here");
    } catch (EiException e) {
      assertEquals(
          "EI003: Invalid routing. Logical address is wrongLogicalAddress but the owner is logical-address. They must be the same. See rule for Update-R7 in service contract",
          e.getMessage());
    }
  }

  @Test
  public void update_r7_neagtive_null_header() throws Exception {
    try {
      engagementValidator.validateUpdate(null, new UpdateType());
      fail("Expected EiException here");
    } catch (EiException e) {
      assertEquals(
          "EI003: Invalid routing. Logical address is missing but the owner is logical-address. They must be the same. See rule for Update-R7 in service contract",
          e.getMessage());
    }

    try {
      engagementValidator.validateUpdate(null, new UpdateType());
      fail("Expected EiException here");
    } catch (EiException e) {
      assertEquals(
          "EI003: Invalid routing. Logical address is missing but the owner is logical-address. They must be the same. See rule for Update-R7 in service contract",
          e.getMessage());
    }
  }


  @Test
  public void update_ERR_max_number_of_engagements() throws Exception {

    UpdateType request = new UpdateType();

    long start = 1111111111L;
    for (int i = 0; i < EngagementValidator.MAX_NUMBER_OF_ENGAGEMENTS + 10; i++) {
      EngagementTransactionType et = GenServiceTestDataUtil.generateEngagementTransaction(start + i);
      request.getEngagementTransaction().add(et);
    }

    try {

      engagementValidator.validateUpdate(OWNER, request);
      fail("Test Failed - No EIException thrown");

    } catch (EiException e) {
      assertEquals(EiErrorCodeEnum.EI000_TECHNICAL_ERROR.getErrorCode(), e.getCode());
    }
  }

  @Test
  public void update_ERR_min_number_of_engagements() throws Exception {

    UpdateType request = new UpdateType();

    try {
      engagementValidator.validateUpdate(OWNER, request);
      fail("Test Failed - No EIException thrown");

    } catch (EiException e) {
      assertEquals(EiErrorCodeEnum.EI000_TECHNICAL_ERROR.getErrorCode(), e.getCode());
    }
  }

  /**
   * Validates that use of not allowed hsa-id's in engagement transactions logical-address are detected.
   */
  @Test
  public void update_ERR_not_allowed_logical_address() throws Exception {

    EngagementValidator bean = createBeanWithNotAllowedHsaIdList();
    UpdateType request = createUpdateRequestWithTransactions(5);

    request.getEngagementTransaction().get(2).getEngagement().setLogicalAddress("NOT-ALLOWED-2");

    try {
      bean.validateUpdate(OWNER, request);
      fail("Test Failed - No EIException thrown");
    } catch (EiException e) {
      assertEquals(EiErrorCodeEnum.EI005_VALIDATION_ERROR_INVALID_LOGICAL_ADDRESS.getErrorCode(), e.getCode());
      assertEquals(e.getMessage(),
          "EI005: The logicalAddress in EngagementTransaction #3 is reserved and not allowed, hsa-id: NOT-ALLOWED-2");
    }
  }

  /**
   * Validates that use of not allowed hsa-id's in engagement transactions source-system are detected.
   */
  @Test
  public void update_ERR_not_allowed_source_system() throws Exception {

    EngagementValidator bean = createBeanWithNotAllowedHsaIdList();
    UpdateType request = createUpdateRequestWithTransactions(5);

    request.getEngagementTransaction().get(4).getEngagement().setSourceSystem("NOT-ALLOWED-1");

    try {
      bean.validateUpdate(OWNER, request);
      fail("Test Failed - No EIException thrown");
    } catch (EiException e) {
      assertEquals(EiErrorCodeEnum.EI006_VALIDATION_ERROR_INVALID_SOURCE_SYSTEM.getErrorCode(), e.getCode());
      assertEquals(e.getMessage(),
          "EI006: The sourceSystem in EngagementTransaction #5 is reserved and not allowed, hsa-id: NOT-ALLOWED-1");

    }
  }


  /**
   * Validates that all mandatory fields is supplied in an engagement.
   */
  @Test
  public void update_ERR_mandatory_fields_are_missing() throws Exception {

    // registeredResidentIdentification
    UpdateType request = new UpdateType();
    EngagementTransactionType et = GenServiceTestDataUtil.generateEngagementTransaction(1111111111L);
    request.getEngagementTransaction().add(et);
    et.getEngagement().setRegisteredResidentIdentification(null);
    assertRequest(request);

    // serviceDomain
    request = new UpdateType();
    et = GenServiceTestDataUtil.generateEngagementTransaction(1111111111L);
    request.getEngagementTransaction().add(et);
    et.getEngagement().setServiceDomain(null);
    assertRequest(request);

    // categorization
    request = new UpdateType();
    et = GenServiceTestDataUtil.generateEngagementTransaction(1111111111L);
    request.getEngagementTransaction().add(et);
    et.getEngagement().setCategorization(null);
    assertRequest(request);

    // logicalAddress
    request = new UpdateType();
    et = GenServiceTestDataUtil.generateEngagementTransaction(1111111111L);
    request.getEngagementTransaction().add(et);
    et.getEngagement().setLogicalAddress(null);
    assertRequest(request);

    // BusinessObjectInstanceIdentifier
    request = new UpdateType();
    et = GenServiceTestDataUtil.generateEngagementTransaction(1111111111L);
    request.getEngagementTransaction().add(et);
    et.getEngagement().setBusinessObjectInstanceIdentifier(null);
    assertRequest(request);

    // clinicalProcessInterestId
    request = new UpdateType();
    et = GenServiceTestDataUtil.generateEngagementTransaction(1111111111L);
    request.getEngagementTransaction().add(et);
    et.getEngagement().setClinicalProcessInterestId(null);
    assertRequest(request);

    // sourceSystem
    request = new UpdateType();
    et = GenServiceTestDataUtil.generateEngagementTransaction(1111111111L);
    request.getEngagementTransaction().add(et);
    et.getEngagement().setSourceSystem(null);
    assertRequest(request);

    // dataController
    request = new UpdateType();
    et = GenServiceTestDataUtil.generateEngagementTransaction(1111111111L);
    request.getEngagementTransaction().add(et);
    et.getEngagement().setDataController(null);
    assertRequest(request);
  }

  /**
   * test R1 for Update service used for processNotifcation with an negative test, i.e. null engagements (that is not allowed)
   */
  @Test
  public void processNotification_update_R1_ERR_null() throws Exception {

    try {
      ProcessNotificationType request = new ProcessNotificationType();
      EngagementTransactionType e1 = null;
      request.getEngagementTransaction().add(e1);

      engagementValidator.validateProcessNotification(request);
      fail("Expected exception here");

    } catch (NullPointerException e) {
      // That was the expected exception, carry on...
      // TODO: Validate expected error message
    }
  }


  /**
   * test R1 for Update service used for processNotification with an negative test, i.e. verify that the validation detects two
   * equal engagements in the same request (that is not allowed)
   */
  @Test
  public void processNotification_update_R1_ERR_validate_non_equal() throws Exception {

    try {
      ProcessNotificationType request = new ProcessNotificationType();
      EngagementTransactionType et1 = GenServiceTestDataUtil.generateEngagementTransaction(1111111111L);
      EngagementTransactionType et2 = GenServiceTestDataUtil.generateEngagementTransaction(2222222222L);
      EngagementTransactionType et3 = GenServiceTestDataUtil.generateEngagementTransaction(3333333333L);

      request.getEngagementTransaction().add(et3);
      request.getEngagementTransaction().add(et1);
      request.getEngagementTransaction().add(et2);
      request.getEngagementTransaction().add(et1);

      engagementValidator.validateProcessNotification(request);
      fail("Expected exception here");

    } catch (EiException e) {
      // That was the expected exception, carry on...
      assertEquals("EI002", e.getCode());
      assertEquals(
          "EI002: EngagementTransaction #2 and #4 have the same key. That is not allowed. See rule for Update-R1 in service contract",
          e.getMessage());
    }
  }


  /**
   *
   */
  @Test
  public void processNotification_ERR_max_number_of_engagements() throws Exception {

    ProcessNotificationType request = new ProcessNotificationType();

    long start = 1111111111L;
    for (int i = 0; i < EngagementValidator.MAX_NUMBER_OF_ENGAGEMENTS + 100; i++) {
      EngagementTransactionType et = GenServiceTestDataUtil.generateEngagementTransaction(start + i);
      request.getEngagementTransaction().add(et);
    }

    try {

      engagementValidator.validateProcessNotification(request);
      fail("Test Failed - No EIException thrown");

    } catch (EiException e) {
      assertEquals(EiErrorCodeEnum.EI000_TECHNICAL_ERROR.getErrorCode(), e.getCode());
    }
  }

  @Test
  public void processNotification_ERR_owner_is_missing() throws Exception {
    ProcessNotificationType request = new ProcessNotificationType();
    EngagementTransactionType et1 = GenServiceTestDataUtil.generateEngagementTransaction(1111111111L);
    request.getEngagementTransaction().add(et1);

    et1.getEngagement().setOwner(null);

    try {
      engagementValidator.validateProcessNotification(request);
      fail("Test failed - No EIException thrown");
    } catch (EiException e) {
      assertEquals(EiErrorCodeEnum.EI004_VALIDATION_ERROR.getErrorCode(), e.getCode());
    }

  }

  /**
   * Validates that use of not allowed hsa-id's in engagement transactions logical-address are detected.
   */
  @Test
  public void processNotification_ERR_not_allowed_logical_address() throws Exception {

    EngagementValidator bean = createBeanWithNotAllowedHsaIdList();
    ProcessNotificationType request = createProcessNotificationRequestWithTransactions(5);

    request.getEngagementTransaction().get(2).getEngagement().setLogicalAddress("NOT-ALLOWED-2");

    try {
      bean.validateProcessNotification(request);
      fail("Test Failed - No EIException thrown");
    } catch (EiException e) {
      assertEquals(EiErrorCodeEnum.EI005_VALIDATION_ERROR_INVALID_LOGICAL_ADDRESS.getErrorCode(), e.getCode());
      assertEquals(e.getMessage(),
          "EI005: The logicalAddress in EngagementTransaction #3 is reserved and not allowed, hsa-id: NOT-ALLOWED-2");
    }
  }

  /**
   * Validates that use of not allowed hsa-id's in engagement transactions source-system are detected.
   */
  @Test
  public void processNotification_ERR_not_allowed_source_system() throws Exception {

    EngagementValidator bean = createBeanWithNotAllowedHsaIdList();
    ProcessNotificationType request = createProcessNotificationRequestWithTransactions(5);

    request.getEngagementTransaction().get(4).getEngagement().setSourceSystem("NOT-ALLOWED-1");

    try {
      bean.validateProcessNotification(request);
      fail("Test Failed - No EIException thrown");
    } catch (EiException e) {
      assertEquals(EiErrorCodeEnum.EI006_VALIDATION_ERROR_INVALID_SOURCE_SYSTEM.getErrorCode(), e.getCode());
      assertEquals(e.getMessage(),
          "EI006: The sourceSystem in EngagementTransaction #5 is reserved and not allowed, hsa-id: NOT-ALLOWED-1");

    }
  }


  private UpdateType createUpdateRequestWithTransactions(int count) {
    UpdateType request = new UpdateType();

    long start = 1111111111L;
    for (int i = 0; i < count; i++) {
      EngagementTransactionType et = GenServiceTestDataUtil.generateEngagementTransaction(start + i);
      request.getEngagementTransaction().add(et);
    }
    return request;
  }

  private ProcessNotificationType createProcessNotificationRequestWithTransactions(int count) {
    ProcessNotificationType request = new ProcessNotificationType();

    long start = 1111111111L;
    for (int i = 0; i < count; i++) {
      EngagementTransactionType et = GenServiceTestDataUtil.generateEngagementTransaction(start + i);
      request.getEngagementTransaction().add(et);
    }
    return request;
  }

  private EngagementValidator createBeanWithNotAllowedHsaIdList() {
    EngagementValidator bean = new EngagementValidator();

    bean.setOwner(OWNER);
    bean.setUpdateNotificationNotAllowedHsaIdList(Arrays.asList("NOT-ALLOWED-1", "NOT-ALLOWED-2", "NOT-ALLOWED-3"));
    return bean;
  }


  private void assertRequest(UpdateType request) {
    try {
      engagementValidator.validateUpdate(OWNER, request);
      fail("Test Failed - No EIException thrown");
    } catch (EiException e) {
      assertEquals(EiErrorCodeEnum.EI004_VALIDATION_ERROR.getErrorCode(), e.getCode());
    }
  }

}

