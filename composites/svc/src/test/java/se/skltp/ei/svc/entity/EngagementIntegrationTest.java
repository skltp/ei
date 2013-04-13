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

        String businessObjectInstanceIdentifier = "boi";
        String categorization = "categorization";
        String logicalAddress = "logicalAddress";
        String residentId = "191212121212";
        String owner = "HSA-001";
        String serviceDomain = "urn:riv:healthprocess:test";
        String sourceSystem = "sourceSystem";

    	// given
        Engagement engagement = new Engagement();
        Engagement.Key key = Engagement.createKey();
        key.setRegisteredResidentIdentification(residentId);
        key.setBusinessObjectInstanceIdentifier(businessObjectInstanceIdentifier);
		key.setCategorization(categorization);
		key.setLogicalAddress(logicalAddress);
		key.setOwner(owner);
		key.setServiceDomain(serviceDomain);
		key.setSourceSystem(sourceSystem);
		engagement.setKey(key);
        engagementRepository.save(engagement);

        // when
        List<Engagement> result = (List<Engagement>) engagementRepository.findAll();

        // then
        assertThat(result, hasSize(1));

        Engagement foundEngagement = result.get(0);
        assertThat(foundEngagement.getKey().getRegisteredResidentIdentification(), is(residentId));
        assertThat(foundEngagement.getKey().getBusinessObjectInstanceIdentifier(), is(businessObjectInstanceIdentifier));
        assertThat(foundEngagement.getKey().getCategorization(), is(categorization));
        assertThat(foundEngagement.getKey().getLogicalAddress(), is(logicalAddress));
        assertThat(foundEngagement.getKey().getServiceDomain(), is(serviceDomain));
        assertThat(foundEngagement.getKey().getSourceSystem(), is(sourceSystem));
        assertThat(foundEngagement.getKey().getOwner(), is(owner));
    }
    
    
    @Test
    public void keyTest() {
    	Engagement.Key key1 = BenchmarkTest.genKey(1);
    	Engagement.Key key2 = BenchmarkTest.genKey(1);
    	Engagement.Key key3 = BenchmarkTest.genKey(2);
    	
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
