/*
 * jFCPlib - GenerateSSK.java - Copyright © 2008 David Roden
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
 * A “GenerateSSK” message. This message tells the node to generate a new SSK
 * key pair.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class GenerateSSK extends FcpMessage {

	/**
	 * Creates a new “GenerateSSK” message.
	 */
	public GenerateSSK() {
		this(FcpUtils.getUniqueIdentifier());
	}

	/**
	 * Creates a new “GenerateSSK” message with the given client identifier.
	 *
	 * @param clientIdentifier
	 *            The client identifier
	 */
	public GenerateSSK(String clientIdentifier) {
		super("GenerateSSK");
		setField("Identifier", clientIdentifier);
	}

}
