/*
 * jFCPlib - PeerNote.java - Copyright © 2008 David Roden
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
 * The “PeerNote” message contains a private note that has been entered for a
 * darknet peer.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class PeerNote extends BaseMessage {

	/** The type for base64 encoded peer notes. */
	public static final int TYPE_PRIVATE_PEER_NOTE = 1;

	/**
	 * Creates a “PeerNote” message that wraps the recevied message.
	 *
	 * @param receivedMessage
	 *            The received message
	 */
	PeerNote(FcpMessage receivedMessage) {
		super(receivedMessage);
	}

	/**
	 * Returns the identifier of the node this note belongs to.
	 *
	 * @return The note’s node’s identifier
	 */
	public String getNodeIdentifier() {
		return getField("NodeIdentifier");
	}

	/**
	 * Returns the base64-encoded note text.
	 *
	 * @return The note text
	 */
	public String getNoteText() {
		return getField("NoteText");
	}

	/**
	 * Returns the type of the peer note.
	 *
	 * @return The type of the peer note, or <code>-1</code> if the type can
	 *         not be parsed
	 */
	public int getPeerNoteType() {
		return FcpUtils.safeParseInt(getField("PeerNoteType"));
	}

}
