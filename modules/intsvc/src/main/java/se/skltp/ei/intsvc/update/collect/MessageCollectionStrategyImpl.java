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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageCollectionStrategyImpl implements MessageCollectionStrategy {
	private static Logger log = LoggerFactory
			.getLogger(MessageCollectionStrategyImpl.class);

	private long bufferAgeMillis;
	/**
	 * How long we are allowed to buffer records from messages before we must
	 * transmit them.
	 */
	private long maxBufferAgeMillis = 4 * 60 * 1000;
	/**
	 * The max number of records to return in each CollectedMessage.
	 */
	private int maxRecordsInCollectedMessage = 1000;
	/**
	 * How many records (not messages) we buffer before transmit (to limit
	 * memory consumption for the buffer), larger value enables more efficient
	 * collection if we remove duplicates.
	 */
	private int maxBufferedRecords = maxRecordsInCollectedMessage * 3;
	private HashMap<String, String> buffer = new HashMap<String, String>(
			maxBufferedRecords);
	private int statisticsTotalNrAddedMessages;
	private int statisticsTotalNrAddedRecords;

	public void setMaxBufferAgeMillis(long maxBufferAgeMillis) {
		this.maxBufferAgeMillis = maxBufferAgeMillis;
	}

	public void setMaxRecordsInCollectedMessage(int maxRecordsInCollectedMessage) {
		this.maxRecordsInCollectedMessage = maxRecordsInCollectedMessage;
	}

	public void setMaxBufferedRecords(int maxBufferedRecords) {
		this.maxBufferedRecords = maxBufferedRecords;
	}

	@Override
	public void collectMessage(String message)
			throws MessageCollectionException {
		if (buffer.isEmpty()) {
			// start counting buffer age
			bufferAgeMillis = System.currentTimeMillis();
		}

		// TODO: parse XML and handle duplicates, currently only demo impl where
		// one message = one record
		int preAddBufferSize = buffer.size();
		statisticsTotalNrAddedMessages++;
		statisticsTotalNrAddedRecords++;
		buffer.put(message, message);

		if (log.isDebugEnabled()) {
			log.debug(
					"added record: duplicate: {}, buffer size: {}, total added msgs: {}, total added records: {}",
					new Object[] { preAddBufferSize == buffer.size(),
							buffer.size(), statisticsTotalNrAddedMessages,
							statisticsTotalNrAddedRecords });
		}
	}

	@Override
	public boolean isCollectedMessagesReadyToBeTransmitted() {
		return (System.currentTimeMillis() >= bufferAgeMillis
				+ maxBufferAgeMillis && !buffer.isEmpty())
				|| (buffer.size() >= maxBufferedRecords);
	}

	@Override
	public List<CollectedMessage> getCollectedMessagesAndClearBuffer() {
		List<CollectedMessage> collMsgs = new ArrayList<CollectedMessage>();

		List<String> records = new ArrayList<String>();
		int totalCount = 0;
		for (String key : buffer.keySet()) {
			totalCount++;
			records.add(buffer.get(key));
			if (records.size() % maxRecordsInCollectedMessage == 0
					|| totalCount == buffer.size()) {
				collMsgs.add(buildCollectedMessage(records));
				records.clear();
			}
		}

		log.debug("built nr of messages: {}, total nr of records: {}",
				collMsgs.size(), buffer.size());

		clearBuffer();
		return collMsgs;
	}

	private CollectedMessage buildCollectedMessage(List<String> records) {
		CollectedMessage collMsg = new CollectedMessage();
		StringBuilder sb = new StringBuilder();
		for (String rec : records) {
			sb.append(rec);
		}
		collMsg.setPayload(sb.toString());
		return collMsg;
	}

	private void clearBuffer() {
		buffer.clear();
		statisticsTotalNrAddedMessages = 0;
		statisticsTotalNrAddedRecords = 0;
	}

}
