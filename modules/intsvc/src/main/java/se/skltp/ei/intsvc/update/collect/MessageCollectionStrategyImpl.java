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

import static se.skltp.ei.svc.service.impl.util.EntityTransformer.toEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.EngagementType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.svc.entity.model.Engagement;
import se.skltp.ei.svc.service.impl.util.EntityTransformer;

public class MessageCollectionStrategyImpl implements MessageCollectionStrategy {
	private static Logger log = LoggerFactory
			.getLogger(MessageCollectionStrategyImpl.class);

	private static JaxbUtil jabxUtil = new JaxbUtil(UpdateType.class);
	private static riv.itintegration.engagementindex.updateresponder._1.ObjectFactory objectFactoryUpdate = new riv.itintegration.engagementindex.updateresponder._1.ObjectFactory();

	private long bufferAgeMillis;
	/**
	 * How long we are allowed to buffer records from messages before we must
	 * transmit them.
	 */
	private long maxBufferAgeMillis = 30000; //4 * 60 * 1000;
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
	/**
	 * How many messages we read and process to buffer before transmit (to limit
	 * memory consumption for ongoing JMS transaction).
	 */
	private int maxCollectedMessages = maxRecordsInCollectedMessage * 10;

	private HashMap<String, EngagementTransactionType> buffer = new HashMap<String, EngagementTransactionType>(
			maxBufferedRecords);
	private int totalNrAddedMessages;
	private int totalNrAddedRecords;

	public void setMaxBufferAgeMillis(long maxBufferAgeMillis) {
		this.maxBufferAgeMillis = maxBufferAgeMillis;
	}

	public void setMaxRecordsInCollectedMessage(int maxRecordsInCollectedMessage) {
		this.maxRecordsInCollectedMessage = maxRecordsInCollectedMessage;
	}

	public void setMaxBufferedRecords(int maxBufferedRecords) {
		this.maxBufferedRecords = maxBufferedRecords;
	}

	public void setMaxCollectedMessages(int maxCollectedMessages) {
		this.maxCollectedMessages = maxCollectedMessages;
	}

	public MessageCollectionStrategyImpl() {
		super();
	}

	@Override
	public void collectMessage(String message)
			throws MessageCollectionException {
		if (buffer.isEmpty()) {
			// start counting buffer age
			bufferAgeMillis = System.currentTimeMillis();
		}
		// Unmarshal message
		UpdateType updateRecord = (UpdateType)jabxUtil.unmarshal(message);

		int preAddBufferSize = buffer.size();
		// Update counter of processed messages
		totalNrAddedMessages++;
		totalNrAddedRecords += updateRecord.getEngagementTransaction().size();

		// Find all records in message
		List<EngagementTransactionType> engagementTransactions = updateRecord.getEngagementTransaction();
		for (final EngagementTransactionType newEngagementTransaction : engagementTransactions) {
			// Read one engagement and create key for this engagement
			onEachNewEngagementTransaction(newEngagementTransaction);
		}

		if (log.isDebugEnabled()) {
			log.debug(
					"added record: duplicate: {}, buffer size: {}, total added msgs: {}, total added records: {}",
					new Object[] { preAddBufferSize == buffer.size(),
							buffer.size(), totalNrAddedMessages,
							totalNrAddedRecords });
		}
	}

	private void onEachNewEngagementTransaction(EngagementTransactionType newEngagementTransaction) {
		EngagementType newEngagement = newEngagementTransaction.getEngagement();
		boolean newIsDeleteFlag = newEngagementTransaction.isDeleteFlag();
		Engagement newEngagementEntity = toEntity(newEngagement);

		// Check if we already have an engagement in our buffer with this key
		if (buffer.containsKey(newEngagementEntity.getId())) {
			// Engagement found in buffer
			if (newIsDeleteFlag) {
				// A delete overrides previously stored engagement
				buffer.put(newEngagementEntity.getId(), newEngagementTransaction);
			} else {
				// Check if incoming record contains a more recent time value
				EngagementTransactionType oldEngagementTransaction = buffer.get(newEngagementEntity.getId());
				Engagement oldEngagementEntity = toEntity(oldEngagementTransaction.getEngagement());
				boolean oldIsDeleteFlag = oldEngagementTransaction.isDeleteFlag();

				long newMostRecentContent = newEngagement.getMostRecentContent() == null ? 0L:Long.parseLong(EntityTransformer.formatDate(newEngagementEntity.getMostRecentContent()));
				long oldMostRecentContent = oldEngagementTransaction.getEngagement().getMostRecentContent() == null ? 0L:Long.parseLong(EntityTransformer.formatDate(oldEngagementEntity.getMostRecentContent()));

				if (oldIsDeleteFlag) {
					// Don't replace!
					return;
				} else {
					if (oldMostRecentContent == 0 && newMostRecentContent != 0) {
						// Replace with engagement that has a value
						buffer.put(newEngagementEntity.getId(), newEngagementTransaction);
					} else if (newMostRecentContent == 0) {
						// Dont replace anything!
						return;
					} else if (newMostRecentContent > oldMostRecentContent) {
						// Replace with newer content
						buffer.put(newEngagementEntity.getId(), newEngagementTransaction);
					}
				}
			}
		} else {
			// No engagement exist in buffer with this key, store it!
			buffer.put(newEngagementEntity.getId(), newEngagementTransaction);
		}
	}

	@Override
	public boolean isCollectedMessagesReadyToBeTransmitted() {
		return (System.currentTimeMillis() >= bufferAgeMillis
				+ maxBufferAgeMillis && !buffer.isEmpty())
				|| (buffer.size() >= maxBufferedRecords)
				|| (totalNrAddedMessages >= maxCollectedMessages );
	}

	@Override
	public List<CollectedMessage> getCollectedMessagesAndClearBuffer() {
		List<CollectedMessage> collMsgs = new ArrayList<CollectedMessage>();

		long currentBufferAgeMillis = System.currentTimeMillis() - bufferAgeMillis;
		List<EngagementTransactionType> records = new ArrayList<EngagementTransactionType>();
		int totalCount = 0;
		for (String key : buffer.keySet()) {
			totalCount++;
			records.add(buffer.get(key));
			if (records.size() % maxRecordsInCollectedMessage == 0
					|| totalCount == buffer.size()) {
				CollectedMessage cm = buildCollectedMessage(records);
				collMsgs.add(cm);
				// add statistics, only add stats for collected messages to
				// first collected message if there is more than one, we will
				// average stats over all collected messages so the result
				// will be correct
				cm.setStatisticsBufferAgeMs(currentBufferAgeMillis);
				cm.setStatisticsNrRecords(records.size());
				if (collMsgs.size() == 1) {
					cm.setStatisticsCollectedNrMessages(totalNrAddedMessages);
					cm.setStatisticsCollectedNrRecords(totalNrAddedRecords);
				}
				else {
					// all stats for the current buffer accounted for in the first collected message
					cm.setStatisticsCollectedNrMessages(0);
					cm.setStatisticsCollectedNrRecords(0);
				}

				records.clear();
			}
		}

		log.debug("built nr of messages: {}, total nr of records: {}",
				collMsgs.size(), buffer.size());

		clearBuffer();
		return collMsgs;
	}

	private CollectedMessage buildCollectedMessage(List<EngagementTransactionType> records) {
		CollectedMessage collMsg = new CollectedMessage();

		UpdateType updateRequest = new UpdateType();

		for (EngagementTransactionType engagementTransaction : records) {
			updateRequest.getEngagementTransaction().add(engagementTransaction);
		}

		collMsg.setPayload(jabxUtil.marshal(objectFactoryUpdate.createUpdate(updateRequest)));
		return collMsg;
	}

	private void clearBuffer() {
		buffer.clear();
		totalNrAddedMessages = 0;
		totalNrAddedRecords = 0;
	}

}
