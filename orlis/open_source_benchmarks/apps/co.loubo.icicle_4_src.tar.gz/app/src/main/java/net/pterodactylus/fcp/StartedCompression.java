/*
 * jFCPlib - StartedCompression.java - Copyright © 2008 David Roden
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
 * The “StartedCompression” message signals the client the compressing for a
 * request has started.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class StartedCompression extends BaseMessage implements Identifiable {

	/**
	 * Creates a new “StartedCompression” message that wraps the received
	 * message.
	 *
	 * @param receivedMessage
	 *            The received message
	 */
	StartedCompression(FcpMessage receivedMessage) {
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
	 * Returns the number of the codec that is used for compression.
	 *
	 * @return The codec used for the compression, or <code>-1</code> if the
	 *         codec could not be parsed
	 */
	public int getCodec() {
		return FcpUtils.safeParseInt(getField("Codec"));
	}

}
