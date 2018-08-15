/*
 * jFCPlib - ListPeerNotes.java - Copyright © 2008 David Roden
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
 * A “ListPeerNodes” request tells the node to list all notes that have been
 * entered for a node. Note that notes are only supported for darknet nodes.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class ListPeerNotes extends FcpMessage {

	/**
	 * Creates a new “ListPeerNotes” request that lists all notes of the
	 * specified node.
	 *
	 * @param nodeIdentifier
	 *            The identifier of the node
	 */
	public ListPeerNotes(String nodeIdentifier) {
		super("ListPeerNotes");
		setField("NodeIdentifier", nodeIdentifier);
	}

}
