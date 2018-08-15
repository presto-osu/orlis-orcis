/*
 * jFCPlib - EndListPeers.java - Copyright © 2008 David Roden
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
 * This message marks the end of a list of “Peer” replies.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class EndListPeers extends BaseMessage implements Identifiable {

	/**
	 * Creates a new “EndListPeers” message that wraps the received message.
	 *
	 * @param receivedMessage
	 *            The message that was received
	 */
	EndListPeers(FcpMessage receivedMessage) {
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

}
