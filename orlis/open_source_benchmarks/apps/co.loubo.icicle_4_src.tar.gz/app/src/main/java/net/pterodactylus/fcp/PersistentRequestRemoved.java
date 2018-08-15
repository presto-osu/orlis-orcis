/*
 * jFCPlib - PersistentRequestRemoved.java - Copyright © 2008 David Roden
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
 * A “PersistentRequestRemoved” message signals that a persistent request was
 * removed from either the global or the client-local queue.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class PersistentRequestRemoved extends BaseMessage implements Identifiable {

	/**
	 * Creates a new “PersistentRequestRemoved” message that wraps the received
	 * message.
	 *
	 * @param receivedMessage
	 *            The received message
	 */
	PersistentRequestRemoved(FcpMessage receivedMessage) {
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
	 * Returns whether the request was removed from the global queue.
	 *
	 * @return <code>true</code> if the request was removed from the global
	 *         queue, <code>false</code> if it was removed from the
	 *         client-local queue
	 */
	public boolean isGlobal() {
		return Boolean.valueOf(getField("Global"));
	}

}
