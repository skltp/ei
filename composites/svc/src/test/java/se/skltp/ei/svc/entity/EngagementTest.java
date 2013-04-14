package se.skltp.ei.svc.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import se.skltp.ei.svc.entity.model.Engagement;

/**
 * Unit test for Engagement.
 * 
 * @author Peter
 */
public class EngagementTest {

    @Test
    public void keyComparisonTest() {
    	Engagement e1 = new Engagement();
    	Engagement e2 = new Engagement();
    	Engagement e3 = new Engagement();
    	BenchmarkTest.genKey(e1, 1);
    	BenchmarkTest.genKey(e2, 1);
    	BenchmarkTest.genKey(e3, 2);
    	
    	Engagement.BusinessKey key1 = e1.getBusinessKey();
    	Engagement.BusinessKey key2 = e2.getBusinessKey();
    	Engagement.BusinessKey key3 = e3.getBusinessKey();

    	assertEquals(e1.getId(), e2.getId());
    	assertFalse(e1.getId().equals(e3.getId()));
    	assertTrue(key1.equals(key2));
    	assertEquals(key1.hashCode(), key2.hashCode());
    	assertTrue(key1.hashCode() != key3.hashCode());
    	assertFalse(key1.equals(key3));
    }	
	
}
