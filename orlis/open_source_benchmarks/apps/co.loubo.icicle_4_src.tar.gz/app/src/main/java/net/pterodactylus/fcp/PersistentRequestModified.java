/*
 * jFCPlib - PersistentRequestModified.java - Copyright © 2008 David Roden
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
 * The “PersistentRequestModified” message is a reply to
 * {@link ModifyPersistentRequest}.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class PersistentRequestModified extends BaseMessage implements Identifiable {

	/**
	 * Creates a new “PersistentRequestModified” message that wraps the
	 * received message.
	 *
	 * @param receivedMessage
	 *            The received message
	 */
	PersistentRequestModified(FcpMessage receivedMessage) {
		super(receivedMessage);
	}

	/**
	 * Returns the identifier of the changed request.
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
	 *         <code>false</code> if it is on a client-local queue
	 */
	public boolean isGlobal() {
		return Boolean.valueOf(getField("Global"));
	}

	/**
	 * Returns the client token, if it was changed.
	 *
	 * @return The new client token, or <code>null</code> if the client token
	 *         was not changed
	 */
	public String getClientToken() {
		return getField("ClientToken");
	}

	/**
	 * Returns the priority of the request, if it was changed.
	 *
	 * @return The new priority of the request, or {@link Priority#unknown} if
	 *         the priority was not changed
	 */
	public Priority getPriority() {
		return Priority.values()[FcpUtils.safeParseInt(getField("PriorityClass"), Priority.unknown.ordinal())];
	}

}
