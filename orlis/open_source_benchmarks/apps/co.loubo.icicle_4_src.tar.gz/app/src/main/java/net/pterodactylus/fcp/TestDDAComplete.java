/*
 * jFCPlib - TestDDAComplete.java - Copyright © 2008 David Roden
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
 * The “TestDDAComplete” message signals that the node has finished checking
 * your read and write access to a certain directory.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class TestDDAComplete extends BaseMessage {

	/**
	 * Creates a new “TestDDAComplete” message that wraps the received message.
	 *
	 * @param receivedMessage
	 *            The received message
	 */
	TestDDAComplete(FcpMessage receivedMessage) {
		super(receivedMessage);
	}

	/**
	 * Returns the directory the authorization is given for.
	 *
	 * @return The directory that was tested for read and/or write access
	 */
	public String getDirectory() {
		return getField("Directory");
	}

	/**
	 * Returns whether read access to the directory is allowed.
	 *
	 * @return <code>true</code> if the client is allowed to read from that
	 *         directory, <code>false</code> otherwise
	 */
	public boolean isReadDirectoryAllowed() {
		return Boolean.valueOf(getField("ReadDirectoryAllowed"));
	}

	/**
	 * Returns whether write access to the directory is allowed.
	 *
	 * @return <code>true</code> if the client is allowed to write into that
	 *         directory, <code>false</code> otherwise
	 */
	public boolean isWriteDirectoryAllowed() {
		return Boolean.valueOf(getField("WriteDirectoryAllowed"));
	}

}
