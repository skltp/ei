/*
  Copyright (c) 2013 Sveriges Kommuner och Landsting (SKL). <http://www.skl.se/>

  This file is part of SKLTP.

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package se.skltp.ei.svc.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.svc.entity.repository.EngagementRepository;
import se.skltp.ei.svc.service.api.EiErrorCodeEnum;
import se.skltp.ei.svc.service.api.EiException;
import se.skltp.ei.svc.service.api.Header;
import se.skltp.ei.svc.service.impl.ProcessBean;

public class ProcessBeanTest {

	private static Header HEADER = null;
    private static ProcessBean BEAN = null; 
    private static final String OWNER = "logical-address";

    @BeforeClass
    public static void setUpClass() throws Exception {
        BEAN = new ProcessBean();
        
		BEAN.setOwner(OWNER);
        EngagementRepository er = mock(EngagementRepository.class);
        BEAN.setEngagementRepository(er);
        
		HEADER = new Header(null, OWNER, null);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * R1 test with n engagements
     */
    @Test
    public void labb_r1_update_positive() throws Exception {

        UpdateType request = new UpdateType();
        List<EngagementTransactionType> resultList = BEAN.update(null, request );
        assertEquals(0, resultList.size());
    }

    /**
     * test R1 for Update service with an positive test, i.e. two different engagements
     */
    @Test
    public void update_r1_positive() throws Exception {

        UpdateType request = new UpdateType();
        EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
        EngagementTransactionType et2 = GenServiceTestDataUtil.genEngagementTransaction(2222222222L);

		request.getEngagementTransaction().add(et1);
		request.getEngagementTransaction().add(et2);
        
		List<EngagementTransactionType> resultList = BEAN.update(null, request);
        assertEquals(2, resultList.size());
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
			
			BEAN.validateUpdate(HEADER, request);
			fail("Expected exception here");
		
        } catch (NullPointerException e) {
			// That was the expected exception, carry on...
        	// TODO: Validate expected error message
		}
    }

    /**
     * test R1 for Update service with an negative test, i.e. verify that the validation detects two equal engagements in the same request (that is not allowed)
     */
    @Test
    public void update_r1_negative_validate_non_equal() throws Exception {

        try {
			UpdateType request = new UpdateType();
			EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
			EngagementTransactionType et2 = GenServiceTestDataUtil.genEngagementTransaction(2222222222L);
		    EngagementTransactionType et3 = GenServiceTestDataUtil.genEngagementTransaction(3333333333L);

		    request.getEngagementTransaction().add(et3);
			request.getEngagementTransaction().add(et1);
			request.getEngagementTransaction().add(et2);
			request.getEngagementTransaction().add(et1);
			
			BEAN.validateUpdate(HEADER, request);
			fail("Expected exception here");

        } catch (EiException e) {
			// That was the expected exception, carry on...
        	assertEquals("EI002", e.getCode());
        	assertEquals("EI002: EngagementTransaction #2 and #4 have the same key. That is not allowed. See rule for Update-R1 in service contract", e.getMessage());
		}
    }
    
    @Test
    public void update_r7_positive_owner_matches_logicaladdress() throws Exception {
    	try {
            UpdateType request = new UpdateType();
            EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
            request.getEngagementTransaction().add(et1);
            
    		BEAN.validateUpdate(HEADER, request);
		} catch (EiException e) {
			fail("Expected ok, not excpetion");
		}
    	assertTrue("Test went ok", true);
    }
    
    @Test
    public void update_r7_negative_owner_dont_match_logicaladdress() throws Exception {
    	try {
    		BEAN.validateUpdate(new Header(null, "wrongLogicalAddress", null), new UpdateType());
    		fail("Expected EiException here");
		} catch (EiException e) {
			assertEquals("EI003: Invalid routing. Logical address is wrongLogicalAddress but the owner is logical-address. They must be the same. See rule for Update-R7 in service contract", e.getMessage());
		}
    }
    
    @Test
    public void update_r7_neagtive_null_header() throws Exception {
    	try {
    		BEAN.validateUpdate(null, new UpdateType());
    		fail("Expected EiException here");
		} catch (EiException e) {
			assertEquals("EI003: Invalid routing. Logical address is missing but the owner is logical-address. They must be the same. See rule for Update-R7 in service contract", e.getMessage());
		}
    	
    	try {
			BEAN.validateUpdate(new Header(null, null, null), new UpdateType());
    		fail("Expected EiException here");
		} catch (EiException e) {
			assertEquals("EI003: Invalid routing. Logical address is missing but the owner is logical-address. They must be the same. See rule for Update-R7 in service contract", e.getMessage());
		}
    }
    
    
    @Test
    public void update_ERR_max_number_of_engagements() throws Exception {
    	
		UpdateType request = new UpdateType();

		long start = 1111111111L;
		for(int i = 0 ; i < ProcessBean.MAX_NUMBER_OF_ENGAGEMENTS+100; i++) {
			EngagementTransactionType et = GenServiceTestDataUtil.genEngagementTransaction(start + i);
			request.getEngagementTransaction().add(et);
		}
		
    	try {
    		
    		BEAN.validateUpdate(HEADER, request);	
    		fail("Test Failed - No EIException thrown");
    		
		} catch (EiException e) {
			assertEquals(EiErrorCodeEnum.EI000_TECHNICAL_ERROR.getErrorCode(), e.getCode());
		}
    }

    @Test
    public void update_ERR_min_number_of_engagements() throws Exception {
    	
		UpdateType request = new UpdateType();

    	try {   		
    		BEAN.validateUpdate(HEADER, request);	
    		fail("Test Failed - No EIException thrown");
    		
		} catch (EiException e) {
			assertEquals(EiErrorCodeEnum.EI000_TECHNICAL_ERROR.getErrorCode(), e.getCode());
		}
    }
    
    /**
     * Validates that all mandatory fields is supplied in an engagement.
     * @throws Exception
     */
    @Test
    public void update_ERR_mandatory_fields_are_missing() throws Exception {
    	
    	// registeredResidentIdentification
		UpdateType request = new UpdateType();
		EngagementTransactionType et = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
		request.getEngagementTransaction().add(et);
		et.getEngagement().setRegisteredResidentIdentification(null);
		assertRequest(request);
    	
    	// serviceDomain
    	request = new UpdateType();
		et = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
		request.getEngagementTransaction().add(et);
		et.getEngagement().setServiceDomain(null);
		assertRequest(request);
		
    	// categorization
    	request = new UpdateType();
		et = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
		request.getEngagementTransaction().add(et);
		et.getEngagement().setCategorization(null);
		assertRequest(request);
		
    	// logicalAddress
    	request = new UpdateType();
		et = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
		request.getEngagementTransaction().add(et);
		et.getEngagement().setLogicalAddress(null);
		assertRequest(request);
    	
    	// BusinessObjectInstanceIdentifier
    	request = new UpdateType();
		et = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
		request.getEngagementTransaction().add(et);
		et.getEngagement().setBusinessObjectInstanceIdentifier(null);
		assertRequest(request);
    	
    	// clinicalProcessInterestId
    	request = new UpdateType();
		et = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
		request.getEngagementTransaction().add(et);
		et.getEngagement().setClinicalProcessInterestId(null);
		assertRequest(request);
    	
    	// sourceSystem
    	request = new UpdateType();
		et = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
		request.getEngagementTransaction().add(et);
		et.getEngagement().setSourceSystem(null);
		assertRequest(request);
    	
    	// dataController
    	request = new UpdateType();
		et = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
		request.getEngagementTransaction().add(et);
		et.getEngagement().setDataController(null);
		assertRequest(request);
    }
    
    /**
     * Validates that use of not allowed hsa-id's in engagement transactions logical-address are detected.
     * @throws Exception
     */
    @Test
    public void update_ERR_not_allowed_logical_address() throws Exception {
		
        ProcessBean bean = createBeanWithNotAllowedHsaIdList();
    	UpdateType request = createUpdateRequestWithTransactions(5);
		
		request.getEngagementTransaction().get(2).getEngagement().setLogicalAddress("NOT-ALLOWED-2");

    	try {
    		bean.validateUpdate(HEADER, request);	
    		fail("Test Failed - No EIException thrown");
		} catch (EiException e) {
			assertEquals(EiErrorCodeEnum.EI005_VALIDATION_ERROR_INVALID_LOGICAL_ADDRESS.getErrorCode(), e.getCode());
			assertEquals(e.getMessage(), "EI005: The logicalAddress in EngagementTransaction #3 is reserved and not allowed, hsa-id: NOT-ALLOWED-2");
		}
    }

    /**
     * Validates that use of not allowed hsa-id's in engagement transactions source-system are detected.
     * @throws Exception
     */
    @Test
    public void update_ERR_not_allowed_source_system() throws Exception {
		
        ProcessBean bean = createBeanWithNotAllowedHsaIdList();
    	UpdateType request = createUpdateRequestWithTransactions(5);
		
		request.getEngagementTransaction().get(4).getEngagement().setSourceSystem("NOT-ALLOWED-1");

    	try {
    		bean.validateUpdate(HEADER, request);	
    		fail("Test Failed - No EIException thrown");
		} catch (EiException e) {
			assertEquals(EiErrorCodeEnum.EI006_VALIDATION_ERROR_INVALID_SOURCE_SYSTEM.getErrorCode(), e.getCode());
			assertEquals(e.getMessage(), "EI006: The sourceSystem in EngagementTransaction #5 is reserved and not allowed, hsa-id: NOT-ALLOWED-1");

		}
    }

    /*** 
     * Test for processNotification 
     */
    
    
    /**
     * test R1 for Update service with an positive test, i.e. two different engagements
     */
    @Test
    public void processNotication_update_R1_OK() throws Exception {

    	ProcessNotificationType request = new ProcessNotificationType();
        EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
        EngagementTransactionType et2 = GenServiceTestDataUtil.genEngagementTransaction(2222222222L);

		request.getEngagementTransaction().add(et1);
		request.getEngagementTransaction().add(et2);
        
		List<EngagementTransactionType> resultList = BEAN.processNotification(null, request);
        assertEquals(2, resultList.size());
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
			
			BEAN.validateProcessNotification(HEADER, request);
			fail("Expected exception here");
		
        } catch (NullPointerException e) {
			// That was the expected exception, carry on...
        	// TODO: Validate expected error message
		}
    }

    /**
     * test R1 for Update service used for processNotification with an negative test, i.e. verify that the validation detects two equal engagements in the same request (that is not allowed)
     */
    @Test
    public void processNotification_update_R1_ERR_validate_non_equal() throws Exception {

        try {
        	ProcessNotificationType request = new ProcessNotificationType();
			EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
			EngagementTransactionType et2 = GenServiceTestDataUtil.genEngagementTransaction(2222222222L);
		    EngagementTransactionType et3 = GenServiceTestDataUtil.genEngagementTransaction(3333333333L);

		    request.getEngagementTransaction().add(et3);
			request.getEngagementTransaction().add(et1);
			request.getEngagementTransaction().add(et2);
			request.getEngagementTransaction().add(et1);
			
			BEAN.validateProcessNotification(HEADER, request);
			fail("Expected exception here");

        } catch (EiException e) {
			// That was the expected exception, carry on...
        	assertEquals("EI002", e.getCode());
        	assertEquals("EI002: EngagementTransaction #2 and #4 have the same key. That is not allowed. See rule for Update-R1 in service contract", e.getMessage());
		}
    }
    
    
    /**
     * Tests $10.5 - R4 that engagements with the same owner as the current index
     * should be removed from the request
     * 
     * @throws Exception
     */
    @Test
    public void processNotification_R4_OK_filter_should_remove_circular_notifications() throws Exception {

    	ProcessNotificationType request = new ProcessNotificationType();
        EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
        EngagementTransactionType et2 = GenServiceTestDataUtil.genEngagementTransaction(2222222222L);
        
        et2.getEngagement().setOwner(OWNER);
        
		request.getEngagementTransaction().add(et1);
		request.getEngagementTransaction().add(et2);
		
		
        ProcessNotificationType request2 = BEAN.filterProcessNotification(request);
    	assertEquals(1, request2.getEngagementTransaction().size());
    	assertEquals(et1.getEngagement(), request2.getEngagementTransaction().get(0).getEngagement());
    	
    }
    
    /**
     * R4 - verifies that everything works when all engagements have been removed from the request
     */
    @Test
    public void processNotification_R4_OK_no_engagements_left() throws Exception {
    	
    	ProcessNotificationType request = new ProcessNotificationType();
    	EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
		request.getEngagementTransaction().add(et1);

    	et1.getEngagement().setOwner(OWNER);
		
		ProcessNotificationType request2 = BEAN.filterProcessNotification(request);
		assertEquals(0, request2.getEngagementTransaction().size());
		assertEquals(0, request.getEngagementTransaction().size());
    }
    
    
    /**
     * @throws Exception
     */
    @Test
    public void processNotification_ERR_max_number_of_engagements() throws Exception {
    	
		ProcessNotificationType request = new ProcessNotificationType();

		long start = 1111111111L;
		for(int i = 0 ; i < ProcessBean.MAX_NUMBER_OF_ENGAGEMENTS+100; i++) {
			EngagementTransactionType et = GenServiceTestDataUtil.genEngagementTransaction(start + i);
			request.getEngagementTransaction().add(et);
		}
		
    	try {
    		
    		BEAN.validateProcessNotification(HEADER, request);	
    		fail("Test Failed - No EIException thrown");
    		
		} catch (EiException e) {
			assertEquals(EiErrorCodeEnum.EI000_TECHNICAL_ERROR.getErrorCode(), e.getCode());
		}
    }
    
    @Test
    public void processNotification_ERR_owner_is_missing() throws Exception {
    	ProcessNotificationType request = new ProcessNotificationType();
    	EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
		request.getEngagementTransaction().add(et1);

    	et1.getEngagement().setOwner(null);
		
		try {
			BEAN.validateProcessNotification(HEADER, request);	
			fail("Test failed - No EIException thrown");
		} catch(EiException e) {
			assertEquals(EiErrorCodeEnum.EI004_VALIDATION_ERROR.getErrorCode(), e.getCode());
		}
		
    }

    /**
     * Validates that use of not allowed hsa-id's in engagement transactions logical-address are detected.
     * @throws Exception
     */
    @Test
    public void processNotification_ERR_not_allowed_logical_address() throws Exception {
		
        ProcessBean bean = createBeanWithNotAllowedHsaIdList();
        ProcessNotificationType request = createProcessNotificationRequestWithTransactions(5);
		
		request.getEngagementTransaction().get(2).getEngagement().setLogicalAddress("NOT-ALLOWED-2");

    	try {
    		bean.validateProcessNotification(HEADER, request);	
    		fail("Test Failed - No EIException thrown");
		} catch (EiException e) {
			assertEquals(EiErrorCodeEnum.EI005_VALIDATION_ERROR_INVALID_LOGICAL_ADDRESS.getErrorCode(), e.getCode());
			assertEquals(e.getMessage(), "EI005: The logicalAddress in EngagementTransaction #3 is reserved and not allowed, hsa-id: NOT-ALLOWED-2");
		}
    }

    /**
     * Validates that use of not allowed hsa-id's in engagement transactions source-system are detected.
     * @throws Exception
     */
    @Test
    public void processNotification_ERR_not_allowed_source_system() throws Exception {
		
        ProcessBean bean = createBeanWithNotAllowedHsaIdList();
        ProcessNotificationType request = createProcessNotificationRequestWithTransactions(5);
		
		request.getEngagementTransaction().get(4).getEngagement().setSourceSystem("NOT-ALLOWED-1");

    	try {
    		bean.validateProcessNotification(HEADER, request);	
    		fail("Test Failed - No EIException thrown");
		} catch (EiException e) {
			assertEquals(EiErrorCodeEnum.EI006_VALIDATION_ERROR_INVALID_SOURCE_SYSTEM.getErrorCode(), e.getCode());
			assertEquals(e.getMessage(), "EI006: The sourceSystem in EngagementTransaction #5 is reserved and not allowed, hsa-id: NOT-ALLOWED-1");

		}
    }
    
    private void assertRequest(UpdateType request) {
    	try {
    		BEAN.validateUpdate(HEADER, request);	
    		fail("Test Failed - No EIException thrown");
		} catch (EiException e) {
			assertEquals(EiErrorCodeEnum.EI004_VALIDATION_ERROR.getErrorCode(), e.getCode());
		}
    }

	private UpdateType createUpdateRequestWithTransactions(int count) {
		UpdateType request = new UpdateType();

		long start = 1111111111L;
		for(int i = 0 ; i < count; i++) {
			EngagementTransactionType et = GenServiceTestDataUtil.genEngagementTransaction(start + i);
			request.getEngagementTransaction().add(et);
		}
		return request;
	}

	private ProcessNotificationType createProcessNotificationRequestWithTransactions(int count) {
		ProcessNotificationType request = new ProcessNotificationType();

		long start = 1111111111L;
		for(int i = 0 ; i < count; i++) {
			EngagementTransactionType et = GenServiceTestDataUtil.genEngagementTransaction(start + i);
			request.getEngagementTransaction().add(et);
		}
		return request;
	}

	private ProcessBean createBeanWithNotAllowedHsaIdList() {
		ProcessBean bean = new ProcessBean();
        
        bean.setOwner(OWNER);
        bean.setEngagementRepository(mock(EngagementRepository.class));
        bean.setUpdateNotificationNotAllowedHsaIdList("NOT-ALLOWED-1,NOT-ALLOWED-2,NOT-ALLOWED-3");
		return bean;
	}

}
