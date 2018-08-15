/*
 * jFCPlib - GetRequestStatus.java - Copyright © 2008 David Roden
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
 * The “GetRequestStatus” message is used request status information about a
 * running request. It is also the only way to trigger a download of a
 * persisted completed {@link ClientGet} with a return type of
 * {@link ReturnType#direct}.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class GetRequestStatus extends FcpMessage {

	/**
	 * Creates a new “GetRequestStatus” message.
	 *
	 * @param identifier
	 *            The identifier of the request
	 */
	public GetRequestStatus(String identifier) {
		super("GetRequestStatus");
		setField("Identifier", identifier);
	}

	/**
	 * Sets whether the request is on the global queue.
	 *
	 * @param global
	 *            <code>true</code> if the request is on the global queue,
	 *            <code>false</code> if it is on the client-local queue
	 */
	public void setGlobal(boolean global) {
		setField("Global", String.valueOf(global));
	}

	/**
	 * Sets whether the omit the transmission of the request data in a
	 * {@link AllData} message.
	 *
	 * @param onlyData
	 *            <code>true</code> to skip transmission of data,
	 *            <code>false</code> to download data
	 */
	public void setOnlyData(boolean onlyData) {
		setField("OnlyData", String.valueOf(onlyData));
	}

}
