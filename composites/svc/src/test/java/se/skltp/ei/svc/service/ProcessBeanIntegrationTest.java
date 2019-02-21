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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.entity.repository.EngagementRepository;
import se.skltp.ei.svc.service.impl.ProcessBean;
import se.skltp.ei.svc.service.impl.util.EntityTransformer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:skltp-ei-svc-spring-context.xml", "classpath:skltp-ei-svc-test-spring-context.xml"})
public class ProcessBeanIntegrationTest {

    private static ProcessBean BEAN = null; 
    private static final String OWNER = "logical-address";
    
    @Autowired
    private EngagementRepository engagementRepository;

    @Before
    public void setUp() throws Exception {
        // Clean the storage
        engagementRepository.deleteAll();
        engagementRepository.flush();

        BEAN = new ProcessBean();
        BEAN.setEngagementRepository(engagementRepository);
        BEAN.setOwner(OWNER);
    }
    
    @Test
    public void update_R2_OK_delete_engagesments_before_save() {
    	
    	UpdateType request = new UpdateType();
        EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
        request.getEngagementTransaction().add(et1);
        et1.setDeleteFlag(true);
        
        BEAN.update(null, request);
    
        List<Engagement> result = (List<Engagement>) engagementRepository.findAll();
        assertThat(result, hasSize(0));
    }
    
    @Test
    public void update_R2_OK_delete_engagesments_after_save() {

    	UpdateType request = new UpdateType();
        EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
        request.getEngagementTransaction().add(et1);
        
        // Update 1
        BEAN.update(null, request);
        assertThat(getSingleEngagement(), notNullValue());
        
        // Update 2 - should delete the post
        et1.setDeleteFlag(true);
        BEAN.update(null, request);
        
        // Fetch all posts
        List<Engagement> result = (List<Engagement>) engagementRepository.findAll();
        assertThat(result, hasSize(0));
    }
    
    /**
     * Tests that the default value for deleteFlag dosen't actually delete the engagement
     */
    @Test
    public void update_R2_OK_should_not_delete_when_deleteflag_is_false() {
    	
    	UpdateType request = new UpdateType();
        EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
        request.getEngagementTransaction().add(et1);
        
        et1.setDeleteFlag(false); // Should be "default" state
        
        BEAN.update(null, request);
        
        // Fetch all posts
        List<Engagement> result = (List<Engagement>) engagementRepository.findAll();
        assertThat(result, hasSize(1));
    }
    
    @Test
    public void update_R4_OK_new_metadata_should_not_update_engagements() {
        UpdateType request = new UpdateType();
        EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
        request.getEngagementTransaction().add(et1);
  
        BEAN.update(null, request); 
        
        // Update setBusinessObjectInstanceIdentifier
        et1.getEngagement().setMostRecentContent(EntityTransformer.formatDate(new Date()));
        et1.getEngagement().setBusinessObjectInstanceIdentifier(et1.getEngagement().getBusinessObjectInstanceIdentifier()+"1");
        BEAN.update(null, request);
        assertThat(countEngagements(), is(2));
        
        // Update setCategorization
        et1.getEngagement().setMostRecentContent(EntityTransformer.formatDate(new Date()));
        et1.getEngagement().setCategorization(et1.getEngagement().getCategorization()+"1");
        BEAN.update(null, request);
        assertThat(countEngagements(), is(3));
        
        // Update setClinicalProcessInterestId
        et1.getEngagement().setMostRecentContent(EntityTransformer.formatDate(new Date()));
        et1.getEngagement().setClinicalProcessInterestId(et1.getEngagement().getClinicalProcessInterestId()+"1");
        BEAN.update(null, request);
        assertThat(countEngagements(), is(4));
        
        // Update setDataController
        et1.getEngagement().setMostRecentContent(EntityTransformer.formatDate(new Date()));
        et1.getEngagement().setDataController((et1.getEngagement().getDataController()+"1"));
        BEAN.update(null, request);
        assertThat(countEngagements(), is(5));
        
        // Update setLogicalAddress
        et1.getEngagement().setMostRecentContent(EntityTransformer.formatDate(new Date()));
        et1.getEngagement().setLogicalAddress((et1.getEngagement().getLogicalAddress()+"1"));
        BEAN.update(null, request);
        assertThat(countEngagements(), is(6));
        
        // Update setRegisteredResidentIdentification
        et1.getEngagement().setMostRecentContent(EntityTransformer.formatDate(new Date()));
        et1.getEngagement().setRegisteredResidentIdentification(et1.getEngagement().getRegisteredResidentIdentification()+"1");
        BEAN.update(null, request);
        assertThat(countEngagements(), is(7));
        
        // Update setServiceDomain
        et1.getEngagement().setMostRecentContent(EntityTransformer.formatDate(new Date()));
        et1.getEngagement().setServiceDomain(et1.getEngagement().getServiceDomain()+"1");
        BEAN.update(null, request);
        assertThat(countEngagements(), is(8));
        
        // Update setRegisteredResidentIdentification
        et1.getEngagement().setMostRecentContent(EntityTransformer.formatDate(new Date()));
        et1.getEngagement().setSourceSystem(et1.getEngagement().getSourceSystem()+"1");
        BEAN.update(null, request);
        assertThat(countEngagements(), is(9));
    }
    
    
    @Test
    public void update_R4_OK_new_content_should_result_in_an_update() {
        UpdateType request = new UpdateType();
        EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
        request.getEngagementTransaction().add(et1);
  
        BEAN.update(null, request); 
        
        Engagement engagement1 = getSingleEngagement();         

        // The only content that can be updated is mostRecentContent
        et1.getEngagement().setMostRecentContent(EntityTransformer.formatDate(new Date()));
        BEAN.update(null, request); 
      
        engagementRepository.flush();
        List<Engagement> result = (List<Engagement>) engagementRepository.findAll();
        assertThat(result, hasSize(1)); // Should only be one post
        
        Engagement engagement2 = result.get(0);
        assertThat(engagement1, equalTo(engagement2));
    }



	@Test
    public void update_R5_OK_creationtime_should_be_set_when_saving() {
        UpdateType request = new UpdateType();
        EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
        request.getEngagementTransaction().add(et1);
  
        BEAN.update(null, request);
        
        // Fetch last post
        Engagement foundEngagement = getSingleEngagement(); 

        // CreationTime should be set while the UpdateTime should not be set (e.g. it should be null)
        assertThat(foundEngagement.getCreationTime(), instanceOf(Date.class));
        assertThat(foundEngagement.getUpdateTime(), nullValue());
    }
    
    
    @Test
    public void update_R5_OK_updatetime_should_be_set_when_updating() {
        UpdateType request = new UpdateType();
        EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
        request.getEngagementTransaction().add(et1);
  
        // Update 1
        BEAN.update(null, request);
        Engagement engagement1 = getSingleEngagement();


        // Update 2, must update content, otherwise nothing happens.
        et1.getEngagement().setMostRecentContent(EntityTransformer.formatDate(new Date()));
        BEAN.update(null, request);
        Engagement engagement2 = getSingleEngagement();
 

        //CreationTime should be the same 
        assertThat(engagement1.getCreationTime(), equalTo(engagement2.getCreationTime())); 

        // UpdateTime should be set to a date and it should be newer (greater than) creationTime 
        assertThat(engagement2.getUpdateTime(), instanceOf(Date.class));
        assertTrue(engagement2.getCreationTime().getTime() < engagement2.getUpdateTime().getTime());
    }
    
    @Test
    public void update_R5_OK_updatetime_should_only_be_set_on_update() {
    	
        UpdateType request = new UpdateType();
        EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
        request.getEngagementTransaction().add(et1);
    	
        // Update 1
        BEAN.update(null, request);
        Engagement engagement1 = getSingleEngagement();

        // Update 2 - should not result in a update in the data store since 
        // there is no new content
        BEAN.update(null, request);
        engagementRepository.flush();
       
        List<Engagement> result = (List<Engagement>) engagementRepository.findAll();
        Engagement engagement2 = result.get(0);
        
        assertThat(result, hasSize(1)); // There should only be one post in the data store
        assertThat(engagement1.getUpdateTime(), nullValue());
        assertThat(engagement2.getUpdateTime(), nullValue());
        assertThat(engagement1, equalTo(engagement2));
        
    }
    
    
    @Test
    public void update_R6_OK_owner_should_be_set_when_saved() {
         // Create a request
        UpdateType request = new UpdateType();
        EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);

        // Using a wrong owner to test that is overwritten with the correct one
        et1.getEngagement().setOwner("wrong-owner");
        request.getEngagementTransaction().add(et1);

        BEAN.update(null, request);

        // Validate the owner is the correct one
        Engagement foundEngagement = getSingleEngagement();
        assertThat(foundEngagement.getOwner(), equalTo(OWNER));
    }
    
    @Test
    public void update_Robusthet_most_recent_content_propagate_new_create() {
         // Create a request
        UpdateType request = new UpdateType();
        EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
        et1.getEngagement().setMostRecentContent(EntityTransformer.formatDate(new Date()));
        request.getEngagementTransaction().add(et1);

        List<EngagementTransactionType> processList = BEAN.update(null, request);

        // Validate the length of the list
        assertEquals(1, processList.size());
    }

    @Test
    public void update_Robusthet_most_recent_content_propagate_delete_flag() {
         // Create a request
        UpdateType request = new UpdateType();
        EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
        et1.setDeleteFlag(true);
        request.getEngagementTransaction().add(et1);

        List<EngagementTransactionType> processList = BEAN.update(null, request);

        // Validate the length of the list
        assertEquals(1, processList.size());
    }

    @Test
    public void update_Robusthet_most_recent_content_propagate_update_first_is_null() {
         // Create a request
        UpdateType request = new UpdateType();
        EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
        request.getEngagementTransaction().add(et1);
        
        // Do an update twice, second call should not generate a notification
        BEAN.update(null, request);

        et1.getEngagement().setMostRecentContent(EntityTransformer.formatDate(new Date()));
        List<EngagementTransactionType> processList = BEAN.update(null, request);

        // Validate the length of the list
        assertEquals(1, processList.size());
    }

    @Test
    public void update_Robusthet_most_recent_content_propagate_update_second_is_null() {
         // Create a request
        UpdateType request = new UpdateType();
        EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
        et1.getEngagement().setMostRecentContent(EntityTransformer.formatDate(new Date()));
        request.getEngagementTransaction().add(et1);
        
        // Do an update twice, second call should not generate a notification
        BEAN.update(null, request);

        et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
        request.getEngagementTransaction().clear();
        request.getEngagementTransaction().add(et1);
        List<EngagementTransactionType> processList = BEAN.update(null, request);

        // Validate the length of the list
        assertEquals(1, processList.size());
    }

    
    @Test
    public void update_Robusthet_most_recent_content_no_propagate_update_same_values() {
         // Create a request
        UpdateType request = new UpdateType();
        EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
        et1.getEngagement().setMostRecentContent(EntityTransformer.formatDate(new Date()));
        request.getEngagementTransaction().add(et1);
        
        // Do an update twice, second call should not generate a notification
        BEAN.update(null, request);
        List<EngagementTransactionType> processList = BEAN.update(null, request);

        // Validate the length of the list
        assertEquals(0, processList.size());
    }

    @Test
    public void update_Robusthet_most_recent_content_no_propagate_update_both_are_null() {
         // Create a request
        UpdateType request = new UpdateType();
        EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
        request.getEngagementTransaction().add(et1);
        
        // Do an update twice, second call should not generate a notification
        BEAN.update(null, request);
        List<EngagementTransactionType> processList = BEAN.update(null, request);

        // Validate the length of the list
        assertEquals(0, processList.size());
    }
    
    /*** Test for ProcessNotification logic ***/    
    
    @Test
    public void processNotification_R1_OK_owner_should_be_saved() {
    	
        ProcessNotificationType request = new ProcessNotificationType();
        EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);

        // Setting an owner different from the owner of the index to verify
        // that the incoming owner is stored
        String remoteOwner = "remote-owner";
        et1.getEngagement().setOwner(remoteOwner);
        request.getEngagementTransaction().add(et1);
        
        BEAN.processNotification(null, request);
        
        // Validate the owner is the correct one
        Engagement foundEngagement = getSingleEngagement();
        assertThat(foundEngagement.getOwner(), equalTo(remoteOwner));
    }
    
    
    @Test
    public void processNotification_R2_OK_delete_engagements_before_save() {
    	
    	ProcessNotificationType request = new ProcessNotificationType();        
        EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
        request.getEngagementTransaction().add(et1);
        et1.setDeleteFlag(true);
        
        BEAN.processNotification(null, request);
    
        List<Engagement> result = (List<Engagement>) engagementRepository.findAll();
        assertThat(result, hasSize(0));
    }
    
    @Test
    public void processNotification_R2_OK_delete_engagements_after_save() {
    	ProcessNotificationType request = new ProcessNotificationType();
        EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
        request.getEngagementTransaction().add(et1);
        
        // Update 1
        BEAN.processNotification(null, request);
        assertThat(getSingleEngagement(), notNullValue());
        
        // Update 2 - should delete the post
        et1.setDeleteFlag(true);
        BEAN.processNotification(null, request);
        
        // Fetch all posts
        List<Engagement> result = (List<Engagement>) engagementRepository.findAll();
        assertThat(result, hasSize(0));
    }
    
    /**
     * Tests that the default value for deleteFlag dosen't actually delete the engagement
     */
    @Test
    public void processNotification_R2_OK_should_not_delete_when_deleteflag_is_false() {
    	
    	ProcessNotificationType request = new ProcessNotificationType();
        EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
        request.getEngagementTransaction().add(et1);
        
        et1.setDeleteFlag(false); // Should be "default" state
        
        BEAN.processNotification(null, request);
        
        // Fetch all posts
        List<Engagement> result = (List<Engagement>) engagementRepository.findAll();
        assertThat(result, hasSize(1));
    }
    
    
    /**
     * R5 - Tests that no engagements are deleted if the owner is the same between requests
     */
    @Test
    public void processNotification_R5_OK_should_not_remove_posts_with_other_owner() {
    	
    	ProcessNotificationType request = new ProcessNotificationType();
        EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
        EngagementTransactionType et2 = GenServiceTestDataUtil.genEngagementTransaction(1212121212L);
        
        et1.getEngagement().setOwner("remote-owner");
        et2.getEngagement().setOwner(OWNER);

        request.getEngagementTransaction().add(et1);
        request.getEngagementTransaction().add(et2);
       
        BEAN.processNotification(null, request);
        BEAN.processNotification(null, request);
        
        // Fetch all posts
        List<Engagement> result = (List<Engagement>) engagementRepository.findAll();
        assertThat(result, hasSize(2));
        
        // Verify that owner is same as before the request
        assertThat(getOwner(result, et1), equalTo(et1.getEngagement().getOwner()));
        assertThat(getOwner(result, et2), equalTo(et2.getEngagement().getOwner()));
    }
    
    
    
    /**
     * R5 - verifies that an engagement with the index as owner will be replaced when
     * processNotification receives the same engagement but with a different owner.
     * @throws Exception
     */
    @Test
    public void processNotification_R5_OK_should_one_store_engagement_with_new_owner() throws Exception {
    	
    	ProcessNotificationType request = new ProcessNotificationType();
        EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
        EngagementTransactionType et2 = GenServiceTestDataUtil.genEngagementTransaction(1212121212L);
        
        et1.getEngagement().setOwner(OWNER);
        et2.getEngagement().setOwner(OWNER);

        request.getEngagementTransaction().add(et1);
        request.getEngagementTransaction().add(et2);
       
        BEAN.processNotification(null, request);
        
        // Request 2
        ProcessNotificationType request2 = new ProcessNotificationType();
        EngagementTransactionType et3 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
        et3.getEngagement().setOwner("remote-owner");
        request2.getEngagementTransaction().add(et3);

        // Set new owner and remove one of existing posts
        BEAN.processNotification(null, request2);

        // Fetch all posts
        List<Engagement> result = (List<Engagement>) engagementRepository.findAll();
        assertThat(result, hasSize(2));

        assertThat(getOwner(result, et2), equalTo(OWNER));
        assertThat(getOwner(result, et3), equalTo("remote-owner"));
        
    }
    
    
    /**
     * R5 - testing getEngagementsWithNewOwners that is the method the find all
     * engagements that should be removed in favor of new with updated Owner.
     */
    @Test
    public void processNotification_R5_OK_find_engagements_to_remove() throws Exception {
    	
    	ProcessNotificationType request = new ProcessNotificationType();
        EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
        et1.getEngagement().setOwner(OWNER);
        request.getEngagementTransaction().add(et1);
        
        BEAN.processNotification(null, request);
        
        assertEquals(1, countEngagements());
        
        request.getEngagementTransaction().get(0).getEngagement().setOwner("remote-owner");
        List<Engagement> list = BEAN._getEngagementsWithNewOwners(request);
        
        
        assertThat(list.get(0).getOwner(), equalTo(OWNER));
    }
    
    /**
     * Tests that getEngagementsWithNewOwners returns an empty list when there is no engagements to remove
     * @throws Exception
     */
    @Test
    public void processNotification_R5_OK_find_engagements_should_return_empty_list() throws Exception{
    	
    	ProcessNotificationType request = new ProcessNotificationType();
        List<Engagement> list = BEAN._getEngagementsWithNewOwners(request);

        assertThat(list, hasSize(0));
    }
    
    
    /**
     * Convenience method for getting the only saved Engagement from the data store. Asserts that it only finds one Engagement
     * @return Engagement
     */
    private Engagement getSingleEngagement() {
        engagementRepository.flush();
        List<Engagement> result = (List<Engagement>) engagementRepository.findAll();
        assertThat(result, hasSize(1));
        
        return result.get(0);
    }
   
    /**
     * Convenience method for getting numbers of Engagements in the data store
     * @return number of records in the data store
     */
    private int countEngagements() {
        engagementRepository.flush();
        List<Engagement> result = (List<Engagement>) engagementRepository.findAll();
        return result.size();
    }
    
    /**
     * Convenience method for getting getting the owner for an engagement in a list based on getRegisteredResidentIdentification
     * @param result
     * @param et
     * @return The owner
     */
    private String getOwner(List<Engagement> result, EngagementTransactionType et) {
    	for(Engagement e : result) {
    		if (e.getRegisteredResidentIdentification().equals(et.getEngagement().getRegisteredResidentIdentification())) {
    			return e.getOwner();
    		}
    	}
    	
    	return "";
    }    
}
