/**
 * Copyright (c) 2013 Sveriges Kommuner och Landsting (SKL). <http://www.skl.se/>
 *
 * This file is part of SKLTP.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package se.skltp.ei.svc.service;

import static org.junit.Assert.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import riv.itintegration.engagementindex.findcontentresponder._1.FindContentResponseType;
import riv.itintegration.engagementindex.findcontentresponder._1.FindContentType;
import se.skltp.ei.svc.entity.GenEntityTestDataUtil;
import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.entity.repository.EngagementRepository;
import se.skltp.ei.svc.service.api.EiErrorCodeEnum;
import se.skltp.ei.svc.service.api.EiException;
import se.skltp.ei.svc.service.impl.FindContentBean;

public class FindContentBeanTest {

    private static FindContentBean BEAN = null; 

    @BeforeClass
    public static void setUpClass() throws Exception {
        BEAN = new FindContentBean();
        EngagementRepository er = mock(EngagementRepository.class);
        BEAN.setEngagementRepository(er);

        when(er.findAll()).thenAnswer(new Answer<Iterable<Engagement>>() {
            @Override
            public Iterable<Engagement> answer(InvocationOnMock invocation) throws Throwable {
                List<Engagement> list = new ArrayList<Engagement>(); 
                return list;
            }
        });		
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
    public void findContent_R1_OK() throws Exception {
        
    	Engagement engagement  = GenEntityTestDataUtil.genEngagement(1212121212L);
    	
    	FindContentType request = new FindContentType();
    	request.setRegisteredResidentIdentification(engagement.getRegisteredResidentIdentification());
    	request.setServiceDomain(engagement.getServiceDomain());
    	
        FindContentResponseType r = BEAN.findContent(null, request);
        assertEquals(0, r.getEngagement().size());
    }
   
    
    @Test 
    public void  findContent_R1_OK_validate_findContent() {
    	
    	Engagement engagement  = GenEntityTestDataUtil.genEngagement(1212121212L);
    	
    	FindContentType request = new FindContentType();
    	request.setRegisteredResidentIdentification(engagement.getRegisteredResidentIdentification());
    	request.setServiceDomain(engagement.getServiceDomain());
    	
    	try {
			BEAN.validateFindContent(null, request);
		} catch (EiException e) {
			fail("Test failed");
		}   	
    
		assertTrue("Test went ok", true);
    }
    
    
    
    @Test
    public void findContent_R1_ERR_validate_findContent() {
    	
    	Engagement engagement  = GenEntityTestDataUtil.genEngagement(1212121212L);
    	FindContentType request = new FindContentType();
    	
    	// Test without registeredResidentIdentification and serviceDomain
    	try {
			BEAN.validateFindContent(null, request);
			fail("This should not happen");
    	} catch (EiException e) {
    		System.out.println(e.getMessage());
    		assertEquals(EiErrorCodeEnum.EI000_TECHNICAL_ERROR.getErrorCode(), e.getCode());
		}
    	
    	// Test without serviceDomain
    	request.setRegisteredResidentIdentification(engagement.getRegisteredResidentIdentification());
    	try {
			BEAN.validateFindContent(null, request);
			fail("This should not happen");
    	} catch (EiException e) {
    		System.out.println(e.getMessage());
    		assertEquals(EiErrorCodeEnum.EI000_TECHNICAL_ERROR.getErrorCode(), e.getCode());
    		assertTrue(e.getMessage().endsWith(FindContentBean.MISSING_SERVICEDOMAIN_MESSAGE));
		}
    	
    	// Test without registeredResidentIdentification
    	request.setRegisteredResidentIdentification(null);
    	request.setServiceDomain(engagement.getServiceDomain());
    	try {
			BEAN.validateFindContent(null, request);
			fail("This should not happen");
    	} catch (EiException e) {
    		System.out.println(e.getMessage());
    		assertEquals(EiErrorCodeEnum.EI000_TECHNICAL_ERROR.getErrorCode(), e.getCode());
    		assertTrue(e.getMessage().endsWith(FindContentBean.MISSING_PERSON_MESSAGE));
		}
    }
    
}
