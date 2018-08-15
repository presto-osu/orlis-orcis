/*
 * jFCPlib - FinishedCompression.java - Copyright © 2008 David Roden
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
 * A “FinishedCompression” message signals the client that the compression of
 * the request data has been finished.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class FinishedCompression extends BaseMessage implements Identifiable {

	/**
	 * Creates a new “FinishedCompression” message that wraps the received
	 * message.
	 *
	 * @param receivedMessage
	 *            The message that was recevied
	 */
	FinishedCompression(FcpMessage receivedMessage) {
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
	 * Returns the ID of the codec that was used for compression.
	 *
	 * @return The ID of the codec that was used for compression
	 */
	public int getCodec() {
		return FcpUtils.safeParseInt(getField("Codec"));
	}

	/**
	 * Returns the original size of the data (i.e. before compression).
	 *
	 * @return The original size of the data
	 */
	public long getOriginalSize() {
		return FcpUtils.safeParseLong(getField("OriginalSize"));
	}

	/**
	 * Returns the compressed size of the data (i.e. after compression).
	 *
	 * @return The compressed size of the data
	 */
	public long getCompressedSize() {
		return FcpUtils.safeParseLong(getField("CompressedSize"));
	}

}
