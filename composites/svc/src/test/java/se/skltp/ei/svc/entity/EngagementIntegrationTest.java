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
    	Engagement.Key key1 = genKey(1);
    	Engagement.Key key2 = genKey(1);
    	Engagement.Key key3 = genKey(-1);
    	
    	assertTrue(key1.equals(key2));
    	assertEquals(key1.hashCode(), key2.hashCode());
    	assertTrue(key1.hashCode() != key3.hashCode());
    	assertFalse(key1.equals(key3));
    }
    
    @Test
    public void findByMultipleKeys() {
        String businessObjectInstanceIdentifier = "boi";
        String categorization = "categorization";
        String logicalAddress = "logicalAddress";
        String residentId = "191212121212";
        String owner = "HSA-001";
        String serviceDomain = "urn:riv:healthprocess:test";
        String sourceSystem = "sourceSystem";

        LinkedList<Engagement.Key> keys = new LinkedList<Engagement.Key>();
        
    	// given
        Engagement e1 = new Engagement();
        Engagement.Key key1 = Engagement.createKey();
        keys.add(key1);
        key1.setRegisteredResidentIdentification(residentId);
        key1.setBusinessObjectInstanceIdentifier(businessObjectInstanceIdentifier);
		key1.setCategorization(categorization);
		key1.setLogicalAddress(logicalAddress);
		key1.setOwner(owner);
		key1.setServiceDomain(serviceDomain);
		key1.setSourceSystem(sourceSystem);
		e1.setKey(key1);
        
    	// given
        Engagement e2 = new Engagement();
        Engagement.Key key2 = Engagement.createKey();
        keys.add(key2);       
        key2.setRegisteredResidentIdentification("191313131313");
        key2.setBusinessObjectInstanceIdentifier(businessObjectInstanceIdentifier);
		key2.setCategorization(categorization);
		key2.setLogicalAddress(logicalAddress);
		key2.setOwner(owner);
		key2.setServiceDomain(serviceDomain);
		key2.setSourceSystem(sourceSystem);
		e2.setKey(key2);
        
        LinkedList<Engagement> list = new LinkedList<Engagement>();
        list.add(e1);
        list.add(e2);
        
        engagementRepository.save(list);
        
        List<Engagement> result = engagementRepository.findByKeyIn(keys);
        
        assertEquals(2, result.size());
    }
    
    //
    static Engagement.Key genKey(long residentIdentification) {	
    	Engagement.Key key = Engagement.createKey();
    	key.setRegisteredResidentIdentification(String.valueOf("19" + residentIdentification));
    	key.setServiceDomain("urn:riv:scheduling:timebooking");
    	key.setBusinessObjectInstanceIdentifier(String.valueOf(residentIdentification));
    	key.setCategorization("booking");
    	key.setLogicalAddress("SE100200400-600");
    	key.setSourceSystem("SE300200-300");
    	return key;    	
    }
}
