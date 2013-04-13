package se.skltp.ei.svc.entity;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.entity.repository.EngagementRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:skltp-ei-svc-spring-context.xml")
public class EngagementIntegrationTest {

    @Autowired
    private EngagementRepository engagementRepository;

    @Test
    public void shouldFindPreviouslySavedPerson() {

     	// given
        Engagement engagement = new Engagement();
        BenchmarkTest.genKey(engagement, 1212121212L);
        engagementRepository.save(engagement);

        // when
        List<Engagement> result = (List<Engagement>) engagementRepository.findAll();

        // then
        assertThat(result, hasSize(1));

        Engagement foundEngagement = result.get(0);
        assertThat(foundEngagement.getBusinessKey(),is(engagement.getBusinessKey()));
    }
    
    
    @Test
    public void keyTest() {
    	Engagement e1 = new Engagement();
    	Engagement e2 = new Engagement();
    	Engagement e3 = new Engagement();
    	BenchmarkTest.genKey(e1, 1);
    	BenchmarkTest.genKey(e2, 1);
    	BenchmarkTest.genKey(e3, 2);
    	
    	Engagement.BusinessKey key1 = e1.getBusinessKey();
    	Engagement.BusinessKey key2 = e2.getBusinessKey();
    	Engagement.BusinessKey key3 = e3.getBusinessKey();

    	assertTrue(key1.equals(key2));
    	assertEquals(key1.hashCode(), key2.hashCode());
    	assertTrue(key1.hashCode() != key3.hashCode());
    	assertFalse(key1.equals(key3));
    }
    
    @Test
    public void findByMultipleKeys() {
    	// given
    	final int num = 1000;
        List<Engagement> list = BenchmarkTest.genEngagements(0, num);
        engagementRepository.save(list);
        
        List<String> ids = new LinkedList<String>();
        for (Engagement e : list) {
        	ids.add(e.getId());
        }
        
        List<Engagement> result = engagementRepository.findByIdIn(ids);
        
        assertEquals(num, result.size());
    }
}
