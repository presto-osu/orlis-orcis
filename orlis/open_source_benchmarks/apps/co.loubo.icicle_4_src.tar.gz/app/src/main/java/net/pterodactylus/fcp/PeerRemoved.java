/*
 * jFCPlib - PeerRemoved.java - Copyright © 2008 David Roden
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
 * A “PeerRemoved” message is sent by the node when a peer has been removed.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class PeerRemoved extends BaseMessage {

	/**
	 * Creates a new “PeerRemoved” message that wraps the received message.
	 *
	 * @param receivedMessage
	 *            The received message
	 */
	PeerRemoved(FcpMessage receivedMessage) {
		super(receivedMessage);
	}

	/**
	 * Returns the identity of the removed peer.
	 *
	 * @return The identity of the removed peer
	 */
	public String getIdentity() {
		return getField("Identity");
	}

	/**
	 * Returns the node identifier of the removed peer.
	 *
	 * @return The node identifier of the removed peer
	 */
	public String getNodeIdentifier() {
		return getField("NodeIdentifier");
	}

}
