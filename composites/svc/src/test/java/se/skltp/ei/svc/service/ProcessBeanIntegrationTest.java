package se.skltp.ei.svc.service;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.entity.repository.EngagementRepository;
import se.skltp.ei.svc.service.impl.ProcessBean;
import se.skltp.ei.svc.service.impl.util.EntityTransformer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:skltp-ei-svc-spring-context.xml")
public class ProcessBeanIntegrationTest {

    private static ProcessBean BEAN = null; 
    private static final String OWNER = "logical-address";

    @Autowired
    private EngagementRepository engagementRepository;

    @Before
    public void setUp() throws Exception {
        Logger.getLogger(ProcessBeanIntegrationTest.class).info("SET UP **************");
        
        // Clean the storage
        engagementRepository.deleteAll();
        engagementRepository.flush();

        BEAN = new ProcessBean();
        BEAN.setEngagementRepository(engagementRepository);
        BEAN.setOwner(OWNER);
    }

    
    @Test
    public void update_R5_positive_creationtime_should_be_set_when_saving() {
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
    public void update_R5_positive_updatetime_should_be_when_updating() {
         UpdateType request = new UpdateType();
        EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);
        request.getEngagementTransaction().add(et1);
  
        // Update 1
        BEAN.update(null, request);
        Engagement engagement1 = getSingleEngagement();


        // Update 2, must update content, otherwise nothing happens.
        et1.getEngagement().setMostRecentContent(EntityTransformer.forrmatDate(new Date()));
        BEAN.update(null, request);
        Engagement engagement2 = getSingleEngagement();
 

        //CreationTime should be the same 
        assertThat(engagement1.getCreationTime(), equalTo(engagement2.getCreationTime())); 

        // UpdateTime should be set to a date and it should be newer (greater than) creationTime 
        assertThat(engagement2.getUpdateTime(), instanceOf(Date.class));
        assertTrue(engagement2.getCreationTime().getTime() < engagement2.getUpdateTime().getTime());
    }
    
    
    @Test
    public void update_R6_positive_owner_should_be_set_when_saved() {
         // Create a request
        UpdateType request = new UpdateType();
        EngagementTransactionType et1 = GenServiceTestDataUtil.genEngagementTransaction(1111111111L);

        // Using a wrong owner to test that is overwritten with the correct one
        et1.getEngagement().setOwner("wrong-owner");
        request.getEngagementTransaction().add(et1);

        BEAN.update(null, request);

        // Validate the owner is the correct one
        Engagement foundEngagement = getSingleEngagement();
        assertThat(foundEngagement.getBusinessKey().getOwner(), equalTo(OWNER));
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
    

}
