/*
 * jFCPlib - ClientHello.java - Copyright © 2008 David Roden
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
 * A “ClientHello” message that <i>must</i> be sent to the node first thing
 * after calling {@link FcpConnection#connect()}.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class ClientHello extends FcpMessage {

	/**
	 * Creates a new “ClientHello” message with the given client name. The
	 * client name has to be unique to the node otherwise you will get a
	 * {@link CloseConnectionDuplicateClientName} response from the node!
	 *
	 * @param clientName
	 *            The unique client name
	 */
	public ClientHello(String clientName) {
		this(clientName, "2.0");
	}

	/**
	 * Creates a new “ClientHello” message with the given client name. The
	 * client name has to be unique to the node otherwise you will get a
	 * {@link CloseConnectionDuplicateClientName} response from the node! The
	 * expected FCP version is currently ignored by the node.
	 *
	 * @param clientName
	 *            The unique client name
	 * @param expectedVersion
	 *            The FCP version that the node is expected to talk
	 */
	public ClientHello(String clientName, String expectedVersion) {
		super("ClientHello");
		setField("Name", clientName);
		setField("ExpectedVersion", expectedVersion);
	}

}
