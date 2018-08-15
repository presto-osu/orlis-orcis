/*
 * jFCPlib - ProtocolError.java - Copyright © 2008 David Roden
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
 * The “ProtocolError” message signals that something has gone really wrong.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class ProtocolError extends BaseMessage {

	/**
	 * Creates a new “ProtocolError” message that wraps the received message.
	 *
	 * @param receivedMessage
	 *            The received message
	 */
	ProtocolError(FcpMessage receivedMessage) {
		super(receivedMessage);
	}

	/**
	 * Returns whether the causing message had the “Global” flag set.
	 *
	 * @return <code>true</code> if the causing message had the “Global” flag
	 *         set
	 */
	public boolean isGlobal() {
		return Boolean.valueOf(getField("Global"));
	}

	/**
	 * Returns the error code.
	 *
	 * @return The error code, or <code>-1</code> if the error code could not
	 *         be parsed
	 */
	public int getCode() {
		return FcpUtils.safeParseInt(getField("Code"));
	}

	/**
	 * Returns the description of the error.
	 *
	 * @return The description of the error
	 */
	public String getCodeDescription() {
		return getField("CodeDescription");
	}

	/**
	 * Returns some extra description of the error.
	 *
	 * @return Extra description of the error, or <code>null</code> if there is
	 *         none
	 */
	public String getExtraDescription() {
		return getField("ExtraDescription");
	}

	/**
	 * Returns whether the connection to the node can stay open.
	 *
	 * @return <code>true</code> when the connection has to be closed,
	 *         <code>false</code> otherwise
	 */
	public boolean isFatal() {
		return Boolean.valueOf(getField("Fatal"));
	}

	/**
	 * The identifier of the causing request, if any.
	 *
	 * @return The identifier of the causing request
	 */
	public String getIdentifier() {
		return getField("Identifier");
	}

}
