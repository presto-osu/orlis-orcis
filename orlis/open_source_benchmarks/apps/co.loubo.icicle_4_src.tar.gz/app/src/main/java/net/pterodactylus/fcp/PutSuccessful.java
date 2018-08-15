/*
 * jFCPlib - PutSuccessful.java - Copyright © 2008 David Roden
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
 * The “PutSuccessful” message informs a client about a successfully finished
 * {@link ClientPut} (or similar) request.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class PutSuccessful extends BaseMessage implements Identifiable {

	/**
	 * Creates a new “PutSuccessful” message that wraps the received message.
	 *
	 * @param receivedMessage
	 *            The received message
	 */
	PutSuccessful(FcpMessage receivedMessage) {
		super(receivedMessage);
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

	/**
	 * Returns whether the request is on the global queue.
	 *
	 * @return <code>true</code> if the request is on the global queue,
	 *         <code>false</code> if it is on the client-local queue
	 */
	public boolean isGlobal() {
		return Boolean.valueOf(getField("Global"));
	}

	/**
	 * Returns the final URI of the {@link ClientPut} request.
	 *
	 * @return The final URI of the request
	 */
	public String getURI() {
		return getField("URI");
	}

	/**
	 * Returns the time the insert started.
	 *
	 * @return The time the insert started (in milliseconds since Jan 1, 1970
	 *         UTC), or <code>-1</code> if the time could not be parsed
	 */
	public long getStartupTime() {
		return FcpUtils.safeParseLong(getField("StartupTime"));
	}

	/**
	 * Returns the time the insert completed.
	 *
	 * @return The time the insert completed (in milliseconds since Jan 1, 1970
	 *         UTC), or <code>-1</code> if the time could not be parsed
	 */
	public long getCompletionTime() {
		return FcpUtils.safeParseLong(getField("CompletionTime"));
	}

}
