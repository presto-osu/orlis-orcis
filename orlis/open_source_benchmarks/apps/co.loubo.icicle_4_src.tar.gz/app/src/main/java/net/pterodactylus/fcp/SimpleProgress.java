/*
 * jFCPlib - SimpleProgress.java - Copyright © 2008 David Roden
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package net.pterodactylus.fcp;

/**
 * A “SimpleProgress” message tells the client about the progress of a
 * {@link ClientGet} or {@link ClientPut} operation.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class SimpleProgress extends BaseMessage implements Identifiable {

	/**
	 * Creates a new “SimpleProgress” message that wraps the received message.
	 *
	 * @param receivedMessage
	 *            The received message
	 */
	SimpleProgress(FcpMessage receivedMessage) {
		super(receivedMessage);
	}

	/**
	 * Returns the total number of blocks. This number may increase as long as
	 * {@link #isFinalizedTotal()} returns <code>false</code>.
	 *
	 * @return The total number of blocks
	 */
	public int getTotal() {
		return FcpUtils.safeParseInt(getField("Total"));
	}

	/**
	 * Returns the number of blocks that are required to completet the request.
	 * This number might actually be lower than {@link #getTotal} because of
	 * redundancy information. This number may also increase as long as
	 * {@link #isFinalizedTotal()} returns <code>false</code>.
	 *
	 * @return The number of required blocks
	 */
	public int getRequired() {
		return FcpUtils.safeParseInt(getField("Required"));
	}

	/**
	 * Returns the number of blocks that have failed and run out of retries.
	 *
	 * @return The number of failed blocks
	 */
	public int getFailed() {
		return FcpUtils.safeParseInt(getField("Failed"));
	}

	/**
	 * Returns the number of fatally failed blocks. A block that failed fatally
	 * can never be completed, even with infinite retries.
	 *
	 * @return The number of fatally failed blocks
	 */
	public int getFatallyFailed() {
		return FcpUtils.safeParseInt(getField("FatallyFailed"));
	}

	/**
	 * Returns the number of blocks that have been successfully processed.
	 *
	 * @return The number of succeeded blocks
	 */
	public int getSucceeded() {
		return FcpUtils.safeParseInt(getField("Succeeded"));
	}

	/**
	 * Returns whether the total number of blocks (see {@link #getTotal()} has
	 * been finalized. Once the total number of blocks has been finalized for a
	 * request it will not change any more, and this method of every further
	 * SimpleProgress message will always return <code>true</code>.
	 *
	 * @return <code>true</code> if the number of total blocks has been
	 *         finalized, <code>false</code> otherwise
	 */
	public boolean isFinalizedTotal() {
		return Boolean.valueOf(getField("FinalizedTotal"));
	}

	/**
	 * Returns the identifier of the request.
	 *
	 * @return The identifier of the request
	 */
	@Override
	public String getIdentifier() {
		return getField("Identifier");
	}

}
