package se.skltp.ei.svc.service;

import org.junit.Before;
import org.junit.Test;
import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.svc.service.api.EiException;
import se.skltp.ei.svc.service.impl.util.EngagementValidator;
import se.skltp.ei.svc.service.api.Header;
import se.skltp.ei.svc.service.impl.util.TestDataHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
//import static se.skltp.ei.svc.service.impl.util.ValidatorTestTestData.ValidationTestDataEnums.*;
import static se.skltp.ei.svc.service.impl.util.ValidatorTestTestData.ValidationTestDataEnums.*;
import static se.skltp.ei.svc.service.api.EiErrorCodeEnum.*;

public class ValidatorTest {

    private EngagementValidator validator = new EngagementValidator();

    private Header INVALID_HEADER_ALL = new Header("","","");
    private Header HEADER = new Header("INERA","logical-address","");

    private UpdateType updateRequest = new UpdateType();

    private ProcessNotificationType processNotificationRequest = new ProcessNotificationType();

    @Before
    public void setUp() {
        // Clean the storage
        validator.setOwner("logical-address");
        validator.setPseudonym("logical-address");


    }


    @Test(expected = EiException.class)
    public void validateHeaderTest() {


        TestDataHelper.resetEnumsEngagement();

        try {
            validator.validateUpdate(INVALID_HEADER_ALL, updateRequest);
        }catch (EiException e){
            assertEquals(e.getCode(),
                    EI003_LOGICALADDRESS_DONT_MATCH_OWNER.getErrorCode());
            throw e;
        }


    }

    @Test(expected = EiException.class)
    public void validateToFewEngagementsTest() {

        TestDataHelper.resetEnumsEngagement();
        try {
            updateRequest.getEngagementTransaction().clear();
            validator.validateUpdate(HEADER, updateRequest);
        }catch (EiException e){
            assertEquals(e.getCode(),
                    EI000_TECHNICAL_ERROR.getErrorCode());
            throw e;
        }


    }

    @Test(expected = EiException.class)
    public void validateToManyEngagmentsTest() {

        TestDataHelper.resetEnumsEngagement();
        try {
            updateRequest.getEngagementTransaction().clear();
            for (int i = 0; i < 1001; i++) {
                updateRequest.getEngagementTransaction().add(new EngagementTransactionType());
            }


            validator.validateUpdate(HEADER, updateRequest);
        }catch (EiException e){
            assertEquals(e.getCode(),
                    EI000_TECHNICAL_ERROR.getErrorCode());
            throw e;
        }


    }

    @Test(expected = EiException.class)
    public void validateDateFormat() {

        TestDataHelper.resetEnumsEngagement();
        try {
            updateRequest.getEngagementTransaction().clear();
            updateRequest.getEngagementTransaction().add(INVALID_CREATION_DATE.getEngagement());

            validator.validateUpdate(HEADER, updateRequest);
        }catch (EiException e){
            assertEquals(e.getCode(),
                    EI004_VALIDATION_ERROR.getErrorCode());
            assertTrue(e.getMessage().toUpperCase().contains("CREATIONTIME"));
            throw e;
        }


    }

    @Test(expected = EiException.class)
    public void validateLength() {

        TestDataHelper.resetEnumsEngagement();
        try {
            updateRequest.getEngagementTransaction().clear();
            updateRequest.getEngagementTransaction().add(INVALID_SOURCE_SYSTEM_LENGT.getEngagement());

            validator.validateUpdate(HEADER, updateRequest);
        }catch (EiException e){
            assertEquals(e.getCode(),
                    EI004_VALIDATION_ERROR.getErrorCode());
            assertTrue(e.getMessage().toUpperCase().contains("SOURCESYSTEM"));
            throw e;
        }


    }

    @Test(expected = EiException.class)
    public void validateMandatory() {

        TestDataHelper.resetEnumsEngagement();
        try {
            updateRequest.getEngagementTransaction().clear();
            updateRequest.getEngagementTransaction().add(INVALID_SOURCE_SYSTEM_MANDATORY.getEngagement());

            validator.validateUpdate(HEADER, updateRequest);
        }catch (EiException e){
            assertEquals(e.getCode(),
                    EI004_VALIDATION_ERROR.getErrorCode());
            assertTrue(e.getMessage().toUpperCase().contains("SOURCESYSTEM"));
            throw e;
        }


    }

    @Test(expected = EiException.class)
    public void validateMandatoryWhiteSpace() {

        TestDataHelper.resetEnumsEngagement();
        try {
            processNotificationRequest.getEngagementTransaction().clear();
            processNotificationRequest.getEngagementTransaction().add(INVALID_SOURCE_SYSTEM_WHITE_SPACE.getEngagement());

            validator.validateProcessNotification(processNotificationRequest);
        }catch (EiException e){
            assertEquals(e.getCode(),
                    EI004_VALIDATION_ERROR.getErrorCode());
            assertTrue(e.getMessage().toUpperCase().contains("SOURCESYSTEM"));
            throw e;
        }


    }

}
