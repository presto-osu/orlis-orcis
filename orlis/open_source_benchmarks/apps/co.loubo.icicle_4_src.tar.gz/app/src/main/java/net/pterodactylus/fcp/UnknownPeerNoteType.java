/*
 * jFCPlib - UnknownPeerNoteType.java - Copyright © 2008 David Roden
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
 * The ”UnknownPeerNoteType” message signals the client that the type of peer
 * note used in a previous {@link ModifyPeerNote} is unknown.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class UnknownPeerNoteType extends BaseMessage {

	/**
	 * Creates a new ”UnknownPeerNoteType” message that wraps the received
	 * message.
	 *
	 * @param receivedMessage
	 *            The received message
	 */
	public UnknownPeerNoteType(FcpMessage receivedMessage) {
		super(receivedMessage);
	}

	/**
	 * Returns the type of peer note that is unkown.
	 *
	 * @return The unknown peer note type
	 */
	public int getPeerNoteType() {
		return FcpUtils.safeParseInt(getField("PeerNoteType"));
	}

}
