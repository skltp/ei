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


/**
 * Contains a number of collected messages in payload, the metadata map can be
 * used to return metadata from the (content aware) collection mechanism like
 * the number of records in a collected message.
 * <p>
 * The intention with the metadata is to be used for logging and analytics.
 * 
 * @author hakan
 */
public class CollectedMessage {
	private String payload;
	/**
	 * The number of records in the payload.
	 */
	private int statisticsNrRecords;
	/**
	 * The total number of messages collected into this message.
	 */
	private int statisticsCollectedNrMessages;
	/**
	 * The total numer of records collected into this message.
	 */
	private int statisticsCollectedNrRecords;
	/**
	 * The buffer age in milliseconds for this message.
	 */
	private long statisticsBufferAgeMs;

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public int getStatisticsNrRecords() {
		return statisticsNrRecords;
	}

	public void setStatisticsNrRecords(int statisticsNrRecords) {
		this.statisticsNrRecords = statisticsNrRecords;
	}

	public int getStatisticsCollectedNrMessages() {
		return statisticsCollectedNrMessages;
	}

	public void setStatisticsCollectedNrMessages(
			int statisticsCollectedNrMessages) {
		this.statisticsCollectedNrMessages = statisticsCollectedNrMessages;
	}

	public int getStatisticsCollectedNrRecords() {
		return statisticsCollectedNrRecords;
	}

	public void setStatisticsCollectedNrRecords(int statisticsCollectedNrRecords) {
		this.statisticsCollectedNrRecords = statisticsCollectedNrRecords;
	}

	public long getStatisticsBufferAgeMs() {
		return statisticsBufferAgeMs;
	}

	public void setStatisticsBufferAgeMs(long statisticsBufferAgeMs) {
		this.statisticsBufferAgeMs = statisticsBufferAgeMs;
	}

}
