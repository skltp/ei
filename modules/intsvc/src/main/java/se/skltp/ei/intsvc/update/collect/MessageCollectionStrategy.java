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

import java.util.List;

/**
 * Collect messages to allow for defragmentation (constructing larger messages
 * out of small messages) and possibly de-duplication of messages during the
 * time for each collection.
 * <p>
 * Design notes:
 * </p>
 * <ol>
 * <li>An implementation does not need to be thread-safe (to simplify
 * implementation), single thread usage is the responsibility of the client</li>
 * <li>An implementation is expected to collect messages for a certain amount of
 * time before it responds yes to the question: is collected messages ready to
 * be transmitted?</li>
 * <li>For simplicity, there is no callback when the collected messages reach a
 * certain threshold (may it be a timebased or a message count threshold), it is
 * the clients responsibility to periodically ask if the buffer is ready to be
 * transmitted</li>
 * </ol>
 * 
 * @author hakan
 */
public interface MessageCollectionStrategy {

	/**
	 * Collect a new message.
	 * 
	 * @param message
	 * @throws MessageCollectionException
	 *             thrown if message can not be collected for some reason, for
	 *             example if the message does not pass a validation step
	 */
	void collectMessage(String message) throws MessageCollectionException;

	/**
	 * Must be called periodically, with a period shorter than the
	 * MessageCollectionStrategy's maxAge for collected messages if such a value
	 * exists, if true is returned, then all collected messages should be
	 * transmitted to the next processing step.
	 * <p>
	 * Note: for simplicity of implementation,
	 * 
	 * @return true if the strategy determines that enough messages have been
	 *         collected or that the maximum allowed time for a collection has
	 *         passed, false otherwise
	 */
	boolean isCollectedMessagesReadyToBeTransmitted();

	/**
	 * Get all collected messages and clears the internal buffer, preparing for
	 * a new collection.
	 * 
	 * @return returns an empty List if no messages have been collected, may
	 *         return a single or multiple messages depending on the collection
	 *         strategy
	 */
	List<CollectedMessage> getCollectedMessagesAndClearBuffer();
}