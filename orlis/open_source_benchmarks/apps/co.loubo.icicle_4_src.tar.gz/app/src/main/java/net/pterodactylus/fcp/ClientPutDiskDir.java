/*
 * jFCPlib - ClientPutDiskDir.java - Copyright © 2008 David Roden
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
 * The “ClientPutDiskDir” message is used to insert a complete directory from
 * the disk to a single key.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class ClientPutDiskDir extends FcpMessage {

	/**
	 * Creates a new “ClientPutDiskDir” message.
	 *
	 * @param uri
	 *            The URI to insert the file to
	 * @param identifier
	 *            The identifier of the request
	 * @param directory
	 *            The name of the directory to insert
	 */
	public ClientPutDiskDir(String uri, String identifier, String directory) {
		super("ClientPutDiskDir");
		setField("URI", uri);
		setField("Identifier", identifier);
		setField("Filename", directory);
	}

	/**
	 * The verbosity of the request. Depending on this parameter you will
	 * received only the bare minimum of messages for the request (i.e. “it
	 * completed”) or a whole lot more.
	 *
	 * @see Verbosity
	 * @param verbosity
	 *            The verbosity of the request
	 */
	public void setVerbosity(Verbosity verbosity) {
		setField("Verbosity", String.valueOf(verbosity));
	}

	/**
	 * The number of retries for a request if the initial try failed.
	 *
	 * @param maxRetries
	 *            The maximum number of retries after failure, or
	 *            <code>-1</code> to retry forever.
	 */
	public void setMaxRetries(int maxRetries) {
		setField("MaxRetries", String.valueOf(maxRetries));
	}

	/**
	 * Sets the priority of the request.
	 *
	 * @param priority
	 *            The priority of the request
	 */
	public void setPriority(Priority priority) {
		setField("PriorityClass", String.valueOf(priority));
	}

	/**
	 * Determines whether the node should really insert the data or generate
	 * the final CHK only.
	 *
	 * @param getCHKOnly
	 *            <code>true</code> to generate the final CHK only,
	 *            <code>false</code> to really insert the data
	 */
	public void setGetCHKOnly(boolean getCHKOnly) {
		setField("GetCHKOnly", String.valueOf(getCHKOnly));
	}

	/**
	 * Sets whether an insert request should be forked when it is cached.
	 *
	 * @param forkOnCacheable
	 *            {@code true} to fork the insert when it is cached,
	 *            {@code false} otherwise
	 */
	public void setForkOnCacheable(boolean forkOnCacheable) {
		setField("ForkOnCacheable", String.valueOf(forkOnCacheable));
	}

	/**
	 * Sets the number of additional inserts of single blocks.
	 *
	 * @param extraInsertsSingleBlock
	 *            The number of additional inserts
	 */
	public void setExtraInsertsSingleBlock(int extraInsertsSingleBlock) {
		setField("ExtraInsertsSingleBlock", String.valueOf(extraInsertsSingleBlock));
	}

	/**
	 * Sets the number of additional inserts of splitfile header blocks.
	 *
	 * @param extraInsertsSplitfileHeaderBlock
	 *            The number of additional inserts
	 */
	public void setExtraInsertsSplitfileHeaderBlock(int extraInsertsSplitfileHeaderBlock) {
		setField("ExtraInsertsSplitfileHeaderBlock", String.valueOf(extraInsertsSplitfileHeaderBlock));
	}

	/**
	 * Determines whether this request appears on the global queue.
	 *
	 * @param global
	 *            <code>true</code> to put the request on the global queue,
	 *            <code>false</code> for the client-local queue.
	 */
	public void setGlobal(boolean global) {
		setField("Global", String.valueOf(global));
	}

	/**
	 * Determines whether the node should skip compression because the file has
	 * already been compressed.
	 *
	 * @param dontCompress
	 *            <code>true</code> to skip compression of the data in the
	 *            node, <code>false</code> to allow compression
	 */
	public void setDontCompress(boolean dontCompress) {
		setField("DontCompress", String.valueOf(dontCompress));
	}

	/**
	 * Sets an optional client token. This client token is mentioned in
	 * progress and other request-related messages and can be used to identify
	 * this request.
	 *
	 * @param clientToken
	 *            The client token
	 */
	public void setClientToken(String clientToken) {
		setField("ClientToken", clientToken);
	}

	/**
	 * Sets the persistence of this request.
	 *
	 * @param persistence
	 *            The persistence of this request
	 */
	public void setPersistence(Persistence persistence) {
		setField("Persistence", String.valueOf(persistence));
	}

	/**
	 * Sets the name of the default file. The default file is shown when the
	 * key is requested with an additional name.
	 *
	 * @param defaultName
	 *            The name of the default file
	 */
	public void setDefaultName(String defaultName) {
		setField("DefaultName", defaultName);
	}

	/**
	 * Sets whether unreadable files allow the insert to continue.
	 *
	 * @param allowUnreadableFiles
	 *            <code>true</code> to just ignore unreadable files,
	 *            <code>false</code> to let the insert fail when an unreadable
	 *            file is encountered
	 */
	public void setAllowUnreadableFiles(boolean allowUnreadableFiles) {
		setField("AllowUnreadableFiles", String.valueOf(allowUnreadableFiles));
	}

}
