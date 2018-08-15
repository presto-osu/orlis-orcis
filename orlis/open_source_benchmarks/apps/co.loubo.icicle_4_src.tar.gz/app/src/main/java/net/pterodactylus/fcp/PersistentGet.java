/*
 * jFCPlib - PersistentGet.java - Copyright © 2008 David Roden
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
 * The “PersistentGet” message is sent to the client to inform it about a
 * persistent download, either in the client-local queue or in the global
 * queue.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class PersistentGet extends BaseMessage implements Identifiable {

	/**
	 * Creates a new “PersistentGet” message that wraps the received message.
	 *
	 * @param receivedMessage
	 *            The received message
	 */
	PersistentGet(FcpMessage receivedMessage) {
		super(receivedMessage);
	}

	/**
	 * Returns the identifier of the request.
	 *
	 * @return The identifier of the request
	 */
	@Override
	public String getIdentifier() {
		return getField("Identifier");
	}

	/**
	 * Returns the URI of the request.
	 *
	 * @return The URI of the request
	 */
	public String getURI() {
		return getField("URI");
	}

	/**
	 * Returns the verbosity of the request.
	 *
	 * @return The verbosity of the request
	 */
	public Verbosity getVerbosity() {
		return Verbosity.valueOf(getField("Verbosity"));
	}

	/**
	 * Returns the return type of the request.
	 *
	 * @return The return type of the request
	 */
	public ReturnType getReturnType() {
		try {
			return ReturnType.valueOf(getField("ReturnType"));
		} catch (IllegalArgumentException iae1) {
			return ReturnType.unknown;
		}
	}

	/**
	 * Returns the name of the file the data is downloaded to. This field will
	 * only be set if {@link #getReturnType()} is {@link ReturnType#disk}.
	 *
	 * @return The name of the file the data is downloaded to
	 */
	public String getFilename() {
		return getField("Filename");
	}

	/**
	 * Returns the name of the temporary file. This field will only be set if
	 * {@link #getReturnType()} is {@link ReturnType#disk}.
	 *
	 * @return The name of the temporary file
	 */
	public String getTempFilename() {
		return getField("TempFilename");
	}

	/**
	 * Returns the client token of the request.
	 *
	 * @return The client token of the request
	 */
	public String getClientToken() {
		return getField("ClientToken");
	}

	/**
	 * Returns the priority of the request.
	 *
	 * @return The priority of the request
	 */
	public Priority getPriority() {
		return Priority.values()[FcpUtils.safeParseInt(getField("PriorityClass"), Priority.unknown.ordinal())];
	}

	/**
	 * Returns the persistence of the request.
	 *
	 * @return The persistence of the request, or {@link Persistence#unknown}
	 *         if the persistence could not be parsed
	 */
	public Persistence getPersistence() {
		try {
			return Persistence.valueOf(getField("Persistence"));
		} catch (IllegalArgumentException iae1) {
			return Persistence.unknown;
		}
	}

	/**
	 * Returns whether this request is on the global queue or on the
	 * client-local queue.
	 *
	 * @return <code>true</code> if the request is on the global queue,
	 *         <code>false</code> if the request is on the client-local queue
	 */
	public boolean isGlobal() {
		return Boolean.valueOf(getField("Global"));
	}

	/**
	 * Returns the maximum number of retries for a failed block.
	 *
	 * @return The maximum number of retries for a failed block,
	 *         <code>-1</code> for endless retries, <code>-2</code> if the
	 *         number could not be parsed
	 */
	public int getMaxRetries() {
		return FcpUtils.safeParseInt(getField("MaxRetries"), -2);
	}

}
