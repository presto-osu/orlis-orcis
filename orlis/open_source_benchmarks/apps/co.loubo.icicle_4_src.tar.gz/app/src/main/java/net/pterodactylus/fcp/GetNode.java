/*
 * jFCPlib - GetNode.java - Copyright © 2008 David Roden
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
 * The “GetNode” command returns the darknet or opennet noderef of the node,
 * optionally including private and volatile data.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class GetNode extends FcpMessage {

	/**
	 * Creates a “GetNode” command that returns the darknet noderef of the
	 * node.
	 */
	public GetNode() {
		this(null, null, null);
	}

	/**
	 * Creates a “GetNode” command that returns the request noderef of the
	 * node, including private and volatile data, if requested. If any of the
	 * Boolean parameters are <code>null</code> the parameter is ignored and
	 * the node’s default value is used.
	 *
	 * @param giveOpennetRef
	 *            <code>true</code> to request the opennet noderef,
	 *            <code>false</code> for darknet
	 * @param withPrivate
	 *            <code>true</code> to include private data in the noderef
	 * @param withVolatile
	 *            <code>true</code> to include volatile data in the noderef
	 */
	public GetNode(Boolean giveOpennetRef, Boolean withPrivate, Boolean withVolatile) {
		super("GetNode");
		if (giveOpennetRef != null) {
			setField("GiveOpennetRef", String.valueOf(giveOpennetRef));
		}
		if (withPrivate != null) {
			setField("WithPrivate", String.valueOf(withPrivate));
		}
		if (withVolatile != null) {
			setField("WithVolatile", String.valueOf(withVolatile));
		}
	}

}
