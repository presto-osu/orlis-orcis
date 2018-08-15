/*
 * jFCPlib - SubscribeUSK.java - Copyright © 2008 David Roden
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
 * With a “SubscribeUSK” a client requests to be notified if the edition number
 * of a USK changes.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class SubscribeUSK extends FcpMessage {

	/**
	 * Creates a new “SubscribeUSK” message.
	 *
	 * @param uri
	 *            The URI to watch for changes
	 * @param identifier
	 *            The identifier of the request
	 */
	public SubscribeUSK(String uri, String identifier) {
		super("SubscribeUSK");
		setField("URI", uri);
		setField("Identifier", identifier);
	}

	/**
	 * Sets whether updates for the USK are actively searched.
	 *
	 * @param active
	 *            <code>true</code> to actively search for newer editions,
	 *            <code>false</code> to only watch for newer editions that are
	 *            found from other requests
	 */
	public void setActive(boolean active) {
		setField("DontPoll", String.valueOf(!active));
	}

}
