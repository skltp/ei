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
package se.skltp.ei.intsvc.update.collect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import riv.itintegration.engagementindex.updateresponder._1.ObjectFactory;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.svc.service.GenServiceTestDataUtil;

public class MessageCollectionStrategyImplTest{
	private static final JaxbUtil jabxUtil = new JaxbUtil(UpdateType.class, ProcessNotificationType.class);
	private static final ObjectFactory update_of = new ObjectFactory();

	private MessageCollectionStrategyImpl impl;

	@Before
	public void setUp() {
		impl = new MessageCollectionStrategyImpl();
	}

	@Test
	public void testCollectMessage1Records() {
		impl.setMaxRecordsInCollectedMessage(2);
		assertEquals("buffer should be empty at start", 0, impl
				.getCollectedMessagesAndClearBuffer().size());
		impl.collectMessage(createUpdateTextMessage("", false, 1212121212));
		impl.collectMessage(createUpdateTextMessage("", false, 1313131313));
		impl.collectMessage(createUpdateTextMessage("", false, 1414141414));
		assertEquals(2, impl.getCollectedMessagesAndClearBuffer().size());
		impl.collectMessage(createUpdateTextMessage(null, false, 1515151515));
		assertEquals(1, impl.getCollectedMessagesAndClearBuffer().size());
	}

	@Test
	public void testCollectMessages2Records() {
		impl.setMaxRecordsInCollectedMessage(3);
		assertEquals("buffer should be empty at start", 0, impl
				.getCollectedMessagesAndClearBuffer().size());
		impl.collectMessage(createUpdateTextMessage("", false, 1212121201, 1212121202));
		impl.collectMessage(createUpdateTextMessage("", false, 1212121203, 1212121204));
		impl.collectMessage(createUpdateTextMessage("", false, 1212121205, 1212121206));
		assertEquals(2, impl.getCollectedMessagesAndClearBuffer().size());
		impl.collectMessage(createUpdateTextMessage("", false, 1212121207, 1212121208));
		assertEquals(1, impl.getCollectedMessagesAndClearBuffer().size());
	}

	@Test
	public void testCollect5Messages1Records() {
		impl.setMaxCollectedMessages(3);
		assertEquals("buffer should be empty at start", 0, impl
				.getCollectedMessagesAndClearBuffer().size());
		impl.collectMessage(createUpdateTextMessage("", false, 1212121201, 1212121202));
		impl.collectMessage(createUpdateTextMessage("", false, 1212121201, 1212121202));
		impl.collectMessage(createUpdateTextMessage("", false, 1212121201, 1212121202));
		impl.collectMessage(createUpdateTextMessage("", false, 1212121201, 1212121202));
		assert(impl.isCollectedMessagesReadyToBeTransmitted());
		assertEquals(1, impl.getCollectedMessagesAndClearBuffer().size());
	}

	@Test
	public void testCollectMessagesWithDuplicateRecords() {
		impl.setMaxRecordsInCollectedMessage(1);
		assertEquals("buffer should be empty at start", 0, impl
				.getCollectedMessagesAndClearBuffer().size());
		impl.collectMessage(createUpdateTextMessage("", false, 1212121212));
		impl.collectMessage(createUpdateTextMessage("", false, 1212121212));
		assertEquals(1, impl.getCollectedMessagesAndClearBuffer().size());
	}	
	
	@Test
	public void testCollectMessagesWithDifferentMostRecentTimes_1() {
		impl.setMaxRecordsInCollectedMessage(1);
		assertEquals("buffer should be empty at start", 0, impl
				.getCollectedMessagesAndClearBuffer().size());
		impl.collectMessage(createUpdateTextMessage("20150610120001", false, 1212121212));
		impl.collectMessage(createUpdateTextMessage("", false, 1212121212));
		assertEquals(1, impl.getCollectedMessagesAndClearBuffer().size());
	}

	@Test
	public void testCollectMessagesWithDifferentMostRecentTimes_2() {
		impl.setMaxRecordsInCollectedMessage(1);
		assertEquals("buffer should be empty at start", 0, impl
				.getCollectedMessagesAndClearBuffer().size());
		impl.collectMessage(createUpdateTextMessage("20150610120001", false, 1212121212));
		impl.collectMessage(createUpdateTextMessage("20150610120002", false, 1212121212));
		assertEquals(1, impl.getCollectedMessagesAndClearBuffer().size());
	}

	@Test
	public void testCollectMessagesWithDifferentMostRecentTimes_3() {
		impl.setMaxRecordsInCollectedMessage(1);
		assertEquals("buffer should be empty at start", 0, impl
				.getCollectedMessagesAndClearBuffer().size());
		impl.collectMessage(createUpdateTextMessage("20150610120001", false, 1212121212));
		impl.collectMessage(createUpdateTextMessage("20150610110001", false, 1212121212));
		assertEquals(1, impl.getCollectedMessagesAndClearBuffer().size());
	}

	@Test
	public void testCollectMessagesWithDifferentMostRecentTimes_4() {
		impl.setMaxRecordsInCollectedMessage(1);
		assertEquals("buffer should be empty at start", 0, impl
				.getCollectedMessagesAndClearBuffer().size());
		impl.collectMessage(createUpdateTextMessage("", false, 1212121212));
		impl.collectMessage(createUpdateTextMessage("20150610110001", false, 1212121212));
		assertEquals(1, impl.getCollectedMessagesAndClearBuffer().size());
	}

	@Test
	public void testCollectMessagesWithDifferentMostRecentTimes_5() {
		impl.setMaxRecordsInCollectedMessage(1);
		assertEquals("buffer should be empty at start", 0, impl
				.getCollectedMessagesAndClearBuffer().size());
		impl.collectMessage(createUpdateTextMessage("", false, 1212121212));
		impl.collectMessage(createUpdateTextMessage("", false, 1212121212));
		assertEquals(1, impl.getCollectedMessagesAndClearBuffer().size());
	}
	
	@Test
	public void testCollectMessagesWithDeleteFlagFirst() {
		impl.setMaxRecordsInCollectedMessage(1);
		assertEquals("buffer should be empty at start", 0, impl
				.getCollectedMessagesAndClearBuffer().size());
		impl.collectMessage(createUpdateTextMessage("", true, 1212121212));
		impl.collectMessage(createUpdateTextMessage("20150610120001", false, 1212121212));
		assertEquals(1, impl.getCollectedMessagesAndClearBuffer().size());
	}

	@Test
	public void testCollectMessagesWithDeleteFlagLast() {
		impl.setMaxRecordsInCollectedMessage(1);
		assertEquals("buffer should be empty at start", 0, impl
				.getCollectedMessagesAndClearBuffer().size());
		impl.collectMessage(createUpdateTextMessage("20150610120001", false, 1212121212));
		impl.collectMessage(createUpdateTextMessage("", true, 1212121212));
		assertEquals(1, impl.getCollectedMessagesAndClearBuffer().size());
	}

	@Test
	public void testIsCollectedMessagesReadyToBeTransmitted() {
		impl.setMaxBufferedRecords(2);

		impl.setMaxBufferAgeMillis(0);
		assertFalse("buffer with records should NOT be transmitted if empty",
				impl.isCollectedMessagesReadyToBeTransmitted());

		impl.collectMessage(createUpdateTextMessage(null, false, 1212121212));
		assertTrue("buffer with records should be transmitted if timeout",
				impl.isCollectedMessagesReadyToBeTransmitted());

		impl.setMaxBufferAgeMillis(100000);
		assertFalse(
				"buffer with records should NOT be transmitted until full OR timeout",
				impl.isCollectedMessagesReadyToBeTransmitted());

		impl.collectMessage(createUpdateTextMessage(null, false, 1313131313));
		assertTrue("buffer should be transmitted if full",
				impl.isCollectedMessagesReadyToBeTransmitted());

		impl.getCollectedMessagesAndClearBuffer();
		impl.setMaxBufferAgeMillis(0);
		assertFalse("buffer should NOT be transmitted if empty",
				impl.isCollectedMessagesReadyToBeTransmitted());
	}

	/*
	 * Create message as a String for Update request. Set attributes deletFlag and most_recent_time for the request
	 * Most recent time has the following format YYYYMMDDhhmmss
	 */
	private String createUpdateTextMessage(String mostRecentTime, boolean deleteFlag, long... residentIds) {
		UpdateType request = new UpdateType();
		for (int i = 0; i < residentIds.length; i++) {
			EngagementTransactionType et = GenServiceTestDataUtil.genEngagementTransaction(residentIds[i]);
			et.setDeleteFlag(deleteFlag);
			if (mostRecentTime != null && mostRecentTime.length() > 0 ) {
				// Set most recent time in data!
				et.getEngagement().setMostRecentContent(mostRecentTime);
			}		
			request.getEngagementTransaction().add(et);
		}
		return jabxUtil.marshal(update_of.createUpdate(request));
    }
	
}
