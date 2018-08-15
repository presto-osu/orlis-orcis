/*
 * jFCPlib - ModifyPersistentRequest.java - Copyright © 2008 David Roden
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
 * A “ModifyPersistentRequest” is used to modify certain properties of a
 * persistent request while it is running.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class ModifyPersistentRequest extends FcpMessage {

	/**
	 * Creates a new “ModifyPersistentRequest” that changes the specified
	 * request.
	 *
	 * @param requestIdentifier
	 *            The identifier of the request
	 * @param global
	 *            <code>true</code> if the request is on the global queue,
	 *            <code>false</code> if it is on the client-local queue
	 */
	public ModifyPersistentRequest(String requestIdentifier, boolean global) {
		super("ModifyPersistentRequest");
		setField("Identifier", requestIdentifier);
		setField("Global", String.valueOf(global));
	}

	/**
	 * Sets the new client token of the request.
	 *
	 * @param newClientToken
	 *            The new client token of the request
	 */
	public void setClientToken(String newClientToken) {
		setField("ClientToken", newClientToken);
	}

	/**
	 * Sets the new priority of the request.
	 *
	 * @param newPriority
	 *            The new priority of the request
	 */
	public void setPriority(Priority newPriority) {
		setField("PriorityClass", String.valueOf(newPriority));
	}

}
