package se.skltp.ei.svc.entity;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.entity.repository.EngagementRepository;
import static se.skltp.ei.svc.entity.model.EngagementSpecifications.*;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:skltp-ei-svc-spring-context.xml", "classpath:skltp-ei-svc-test-spring-context.xml"})
public class EngagementIntegrationTest {

    @Autowired
    private EngagementRepository engagementRepository;

    @Before
    public void setUp() throws Exception {
    	// Clean the storage
    	engagementRepository.deleteAll();
    	engagementRepository.flush();
    }
    
    @Test
    public void shouldFindPreviouslySavedPerson() {

        // given
        Engagement engagement  = GenEntityTestDataUtil.genEngagement(1212121212L);
        engagementRepository.save(engagement);

        // when
        List<Engagement> result = (List<Engagement>) engagementRepository.findAll();

        // then
        assertThat(result, hasSize(1));

        Engagement foundEngagement = result.get(0);
        assertThat(foundEngagement.getId(),is(engagement.getId()));
    }

    @Test
    public void findByMultipleKeys() {

    	// given
        final int num = 1000;
        List<Engagement> list = GenEntityTestDataUtil.genEngagements(0, num);
        engagementRepository.save(list);

        // when
        List<String> ids = new LinkedList<String>();
        for (Engagement e : list) {
            ids.add(e.getId());
        }
        List<Engagement> result = engagementRepository.findByIdIn(ids);

        // then
        assertEquals(num, result.size());
    }
    
    @Test
    public void findContentAllFields() {
        Engagement engagement  = GenEntityTestDataUtil.genEngagement(1212121212L);
        engagement.setMostRecentContent(new Date());
        engagementRepository.save(engagement);
        
        List<Engagement> list = engagementRepository.findAll(createSpecifications(engagement));
        
        assertEquals(1, list.size());
    }
    
    @Test
    public void findContentMandatoryFields() {
        Engagement engagement  = GenEntityTestDataUtil.genEngagement(1212121212L);
        engagement.setMostRecentContent(new Date());
        engagementRepository.save(engagement);

        Engagement me = mock(Engagement.class);
        when(me.getRegisteredResidentIdentification()).thenReturn(engagement.getRegisteredResidentIdentification());
        when(me.getServiceDomain()).thenReturn(engagement.getServiceDomain());
        when(me.getBusinessObjectInstanceIdentifier()).thenReturn(null);
        when(me.getMostRecentContent()).thenReturn(null);
        when(me.getCategorization()).thenReturn(null);
        when(me.getClinicalProcessInterestId()).thenReturn(null);
        when(me.getDataController()).thenReturn(null);
        when(me.getLogicalAddress()).thenReturn(null);
        when(me.getSourceSystem()).thenReturn(null);
        when(me.getOwner()).thenReturn(null);

        
        List<Engagement>list = engagementRepository.findAll(createSpecifications(me));        
        assertEquals(1, list.size());        
    }

    @Test
    public void findContentMandatoryFieldsAndDate() {
        Engagement engagement  = GenEntityTestDataUtil.genEngagement(1212121212L);
        engagement.setMostRecentContent(new Date());
        engagementRepository.save(engagement);

        Engagement me = mock(Engagement.class);
        when(me.getRegisteredResidentIdentification()).thenReturn(engagement.getRegisteredResidentIdentification());
        when(me.getServiceDomain()).thenReturn(engagement.getServiceDomain());
        when(me.getBusinessObjectInstanceIdentifier()).thenReturn(null);
        when(me.getMostRecentContent()).thenReturn(new Date());
        when(me.getCategorization()).thenReturn(null);
        when(me.getClinicalProcessInterestId()).thenReturn(null);
        when(me.getDataController()).thenReturn(null);
        when(me.getLogicalAddress()).thenReturn(null);
        when(me.getSourceSystem()).thenReturn(null);
        when(me.getOwner()).thenReturn(null);
      
        List<Engagement>list = engagementRepository.findAll(createSpecifications(me));        
        assertEquals(1, list.size());
        
        when(me.getMostRecentContent()).thenReturn(new Date(0L));
        
        list = engagementRepository.findAll(createSpecifications(me));        
        assertEquals(0, list.size());
    }

    @Test
    public void timestamp_R5() {
        Engagement e = GenEntityTestDataUtil.genEngagement(1212121212L);
        assertNull(e.getUpdateTime());
        assertNull(e.getCreationTime());
        
        Engagement saved = engagementRepository.save(e);
        engagementRepository.flush();
        assertNull(saved.getUpdateTime());
        assertNotNull(saved.getCreationTime());
        saved.setMostRecentContent(new Date());
        
        Engagement updated = engagementRepository.save(saved);
        engagementRepository.flush();
        assertNotNull(updated.getUpdateTime());
        assertNotNull(updated.getCreationTime());
    }
    
    @Test
    public void findByRegisteredResidentIdentification() {

    	// given
    	final int num = 10;
        List<Engagement> list = GenEntityTestDataUtil.genEngagements(0, num);
        engagementRepository.save(list);

        // when
        Engagement e = GenEntityTestDataUtil.genEngagement(1);
        List<Engagement> result = engagementRepository.findByRegisteredResidentIdentification(e.getRegisteredResidentIdentification());

        // then
        assertEquals(1, result.size());
        assertThat(result.get(0).getId(),is(e.getId()));
    }
    
    public static Specifications<Engagement> createSpecifications(Engagement engagement) {


        Specifications<Engagement> specs = Specifications.where(isPerson(engagement.getRegisteredResidentIdentification()))   
                .and(hasServiceDomain(engagement.getServiceDomain()));


        if (engagement.getCategorization() != null) {
            specs = specs.and(hasCategorization(engagement.getCategorization()));
        }

        if (engagement.getMostRecentContent() != null) {
            specs = specs.and(isMostRecent(engagement.getMostRecentContent()));
        }

        if (engagement.getClinicalProcessInterestId() != null) {
            specs = specs.and(hasClinicalProcessInterestId(engagement.getClinicalProcessInterestId()));
        }

        if (engagement.getBusinessObjectInstanceIdentifier() != null) {
            specs = specs.and(hasBusinessObjectInstanceIdentifier(engagement.getBusinessObjectInstanceIdentifier()));            
        }

        if (engagement.getLogicalAddress() != null) {
            specs = specs.and(hasLogicalAddress(engagement.getLogicalAddress()));            
        }

        if (engagement.getSourceSystem() != null) {
            specs = specs.and(hasSourceSystem(engagement.getSourceSystem()));                        
        }

        if (engagement.getDataController() != null) {
            specs = specs.and(hasDataController(engagement.getDataController()));                        
        }

        if (engagement.getOwner() != null) {
            specs = specs.and(hasOwner(engagement.getOwner()));
        }

        return specs;

    }
}
