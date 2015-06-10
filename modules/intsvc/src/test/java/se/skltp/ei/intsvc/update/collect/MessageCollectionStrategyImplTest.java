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

import se.skltp.ei.intsvc.update.collect.MessageCollectionStrategyImpl;

public class MessageCollectionStrategyImplTest {
	private MessageCollectionStrategyImpl impl;

	@Before
	public void setUp() {
		impl = new MessageCollectionStrategyImpl();
	}

	@Test
	public void testCollectMessage() {
		testGetCollectedMessagesAndClearBuffer();
	}

	private void testGetCollectedMessagesAndClearBuffer() {
		impl.setMaxRecordsInCollectedMessage(2);
		assertEquals("buffer should be empty at start", 0, impl
				.getCollectedMessagesAndClearBuffer().size());
		impl.collectMessage("A");
		impl.collectMessage("B");
		impl.collectMessage("C");
		assertEquals(2, impl.getCollectedMessagesAndClearBuffer().size());
		impl.collectMessage("D");
		assertEquals(1, impl.getCollectedMessagesAndClearBuffer().size());
	}

	@Test
	public void testIsCollectedMessagesReadyToBeTransmitted() {
		impl.setMaxBufferedRecords(2);

		impl.setMaxBufferAgeMillis(0);
		assertFalse("buffer with records should NOT be transmitted if empty",
				impl.isCollectedMessagesReadyToBeTransmitted());

		impl.collectMessage("A");
		assertTrue("buffer with records should be transmitted if timeout",
				impl.isCollectedMessagesReadyToBeTransmitted());

		impl.setMaxBufferAgeMillis(100000);
		assertFalse(
				"buffer with records should NOT be transmitted until full OR timeout",
				impl.isCollectedMessagesReadyToBeTransmitted());

		impl.collectMessage("B");
		assertTrue("buffer should be transmitted if full",
				impl.isCollectedMessagesReadyToBeTransmitted());

		impl.getCollectedMessagesAndClearBuffer();
		impl.setMaxBufferAgeMillis(0);
		assertFalse("buffer should NOT be transmitted if empty",
				impl.isCollectedMessagesReadyToBeTransmitted());
	}

}
