/*
 * jFCPlib - ListPeers.java - Copyright © 2008 David Roden
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
 * The “ListPeer” requests asks the node for a list of all peers it has.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class ListPeers extends FcpMessage {

	/**
	 * Creates a new “ListPeers” request that only includes basic data of the
	 * peers.
	 *
	 * @param identifier
	 *            The identifier of the request
	 */
	public ListPeers(String identifier) {
		this(identifier, false, false);
	}

	/**
	 * Creates a new “ListPeers” request that includes wanted data of the
	 * peers.
	 *
	 * @param identifier
	 *            The identifier of the request
	 * @param withMetadata
	 *            If <code>true</code> metadata of the peers is included in the
	 *            reply
	 * @param withVolatile
	 *            if <code>true</code> volatile data of the peers is included
	 *            in the reply
	 */
	public ListPeers(String identifier, boolean withMetadata, boolean withVolatile) {
		super("ListPeers");
		setField("Identifier", identifier);
		setField("WithMetadata", String.valueOf(withMetadata));
		setField("WithVolatile", String.valueOf(withVolatile));
	}

}
