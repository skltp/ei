package se.skltp.ei.intsvc.subscriber.impl;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.skltp.ei.intsvc.integrationtests.notifyservice.util.FilterCreator;
import se.skltp.ei.intsvc.subscriber.api.Subscriber;

public class SubscriberCacheImplTest {
	
	public final String FILEPATH = System.getProperty("java.io.tmpdir") + File.separator + "ei_cache_test";
	
	private List<Subscriber> list;
	
	@After
	@Before
	public void removeFile() {
		File file = new File(FILEPATH);
		if (file.exists()) {
			file.delete();
		}
	}
	
	@Before
	public void createSubscribers() {
		list = FilterCreator.createOneSubscriber("HSA_ID_A", "SERVICEDOMAIN-A", "CATEGORY-A", "CATEGORY-B");
		list.get(0).getFilterList().add(FilterCreator.createFilter("SERVICEDOMAIN-B"));
		
		list.add(FilterCreator.createOneSubscriber("HSA_ID_B", "SERVICEDOMAIN_C").get(0));
		list.add(FilterCreator.createOneSubscriber("HSA_ID_C", null).get(0));
	}

	/**
	 * Verifies that we can write to file
	 */
	@Test
	public void test_write_to_file() {
		
		SubscriberCacheImpl subscriberCacheImpl = new SubscriberCacheImpl();
		subscriberCacheImpl.initialize(list);
		
		subscriberCacheImpl.setFilePath(FILEPATH);
		subscriberCacheImpl.saveToLocalCopy();
		
		
		File file = new File(FILEPATH);
		assertTrue(file.exists()); 
		
	}
	
	
	/**
	 * Verifies that we got the same content back when we read it from file
	 */
	@Test
	public void test_write_and_read_to_file() {
		
		// Write to cache
		SubscriberCacheImpl subscriberCacheImpl = new SubscriberCacheImpl();
		subscriberCacheImpl.initialize(list);
		subscriberCacheImpl.setFilePath(FILEPATH);
		subscriberCacheImpl.saveToLocalCopy();
		subscriberCacheImpl = null;
	
		// Read from cache
		SubscriberCacheImpl subscriberListFromFile = new SubscriberCacheImpl();
		subscriberListFromFile.setFilePath(FILEPATH);
		subscriberListFromFile.restoreFromLocalCopy();
		
		// Number of subscriber should be the same
		assertEquals(list.size(), subscriberListFromFile.getSubscribers().size());
		
		// LogicalAddress for the first subscriber should be same as before
		assertEquals(list.get(0).getLogicalAdress(), subscriberListFromFile.getSubscribers().get(0).getLogicalAdress());

		// Verify the the filter is the same
		assertEquals(list.get(0).getFilterList().size(), subscriberListFromFile.getSubscribers().get(0).getFilterList().size());
		assertEquals(list.get(0).getFilterList().get(0).getServiceDomain(), subscriberListFromFile.getSubscribers().get(0).getFilterList().get(0).getServiceDomain());
		assertEquals(list.get(0).getFilterList().get(0).getCategorization().get(0), subscriberListFromFile.getSubscribers().get(0).getFilterList().get(0).getCategorization().get(0));
		
	}
	
	
	/**
	 * Verifies that we got the same content back when we read it from file
	 */
	@Test
	public void test_reading_empty_cache_file() {
		
		// Write to cache (empty)
		SubscriberCacheImpl subscriberCacheImpl = new SubscriberCacheImpl();
		subscriberCacheImpl.initialize(new ArrayList<Subscriber>());
		subscriberCacheImpl.setFilePath(FILEPATH);
		subscriberCacheImpl.saveToLocalCopy();
		subscriberCacheImpl = null;
	
		// Read from cache
		SubscriberCacheImpl subscriberListFromFile = new SubscriberCacheImpl();
		subscriberListFromFile.setFilePath(FILEPATH);
		subscriberListFromFile.restoreFromLocalCopy();
		
		// There should not be any subscribers in the list
		assertTrue(subscriberListFromFile.getSubscribers().isEmpty());
	}
	
}
