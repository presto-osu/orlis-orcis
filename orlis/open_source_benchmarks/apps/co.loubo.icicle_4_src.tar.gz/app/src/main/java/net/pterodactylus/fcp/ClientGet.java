/*
 * jFCPlib - ClientGet.java - Copyright © 2008 David Roden
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
 * A “ClientGet” request is used for download files from the Freenet node.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class ClientGet extends FcpMessage {

	/**
	 * Creates a new “ClientGet” request.
	 *
	 * @param uri
	 *            The URI to get
	 * @param identifier
	 *            The identifier of the request
	 */
	public ClientGet(String uri, String identifier) {
		this(uri, identifier, ReturnType.direct);
	}

	/**
	 * Creates a new “ClientGet” request.
	 *
	 * @param uri
	 *            The URI to get
	 * @param identifier
	 *            The identifier of the request
	 * @param returnType
	 *            The return type of the request
	 */
	public ClientGet(String uri, String identifier, ReturnType returnType) {
		super("ClientGet");
		setField("URI", uri);
		setField("Identifier", identifier);
		setField("ReturnType", String.valueOf(returnType));
	}

	/**
	 * Sets whether the local data store should be ignored when searching for a
	 * key.
	 *
	 * @param ignoreDataStore
	 *            <code>true</code> to ignore the local data store,
	 *            <code>false</code> to include it
	 */
	public void setIgnoreDataStore(boolean ignoreDataStore) {
		setField("IgnoreDS", String.valueOf(ignoreDataStore));
	}

	/**
	 * Sets whether the search for the key should be restricted to the local
	 * data store only.
	 *
	 * @param dsOnly
	 *            <code>true</code> to restrict the search to the local data
	 *            store, <code>false</code> to search on other nodes, too
	 */
	public void setDataStoreOnly(boolean dsOnly) {
		setField("DSonly", String.valueOf(dsOnly));
	}

	/**
	 * Sets the verbosity of the request.
	 *
	 * @param verbosity
	 *            The verbosity of the request
	 */
	public void setVerbosity(Verbosity verbosity) {
		setField("Verbosity", String.valueOf(verbosity));
	}

	/**
	 * Sets the maximum size of the file to retrieve. If the file is larger
	 * than this size the request will fail!
	 *
	 * @param maxSize
	 *            The maximum size of the file to retrieve
	 */
	public void setMaxSize(long maxSize) {
		setField("MaxSize", String.valueOf(maxSize));
	}

	/**
	 * Sets the maximum size of temporary files created by the node. If a
	 * temporary file is larger than this size the request will fail!
	 *
	 * @param maxTempSize
	 *            The maximum size of temporary files
	 */
	public void setMaxTempSize(long maxTempSize) {
		setField("MaxTempSize", String.valueOf(maxTempSize));
	}

	/**
	 * The maximum number of retries in case a block can not be retrieved.
	 *
	 * @param maxRetries
	 *            The maximum number of retries for failed blocks,
	 *            <code>-1</code> to try forever
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
	 * Sets the persistence of the request.
	 *
	 * @param persistence
	 *            The persistence of the request
	 */
	public void setPersistence(Persistence persistence) {
		setField("Persistence", String.valueOf(persistence));
	}

	/**
	 * Sets the client token of the request.
	 *
	 * @param clientToken
	 *            The client token of the request
	 */
	public void setClientToken(String clientToken) {
		setField("ClientToken", clientToken);
	}

	/**
	 * Sets whether the request should be visible on the global queue.
	 *
	 * @param global
	 *            <code>true</code> to make the request visible on the global
	 *            queue, <code>false</code> for client-local queue only
	 */
	public void setGlobal(boolean global) {
		setField("Global", String.valueOf(global));
	}

	/**
	 * Sets whether to request the “binary blob” for a key.
	 *
	 * @param binaryBlob
	 *            <code>true</code> to request the binary blob,
	 *            <code>false</code> to get the “real thing”
	 */
	public void setBinaryBlob(boolean binaryBlob) {
		setField("BinaryBlob", String.valueOf(binaryBlob));
	}

	/**
	 * Sets whether to filter the fetched content.
	 *
	 * @param filterData
	 *            {@code true} to filter content, {@code false} otherwise
	 */
	public void setFilterData(boolean filterData) {
		setField("FilterData", String.valueOf(filterData));
	}

	/**
	 * Sets the allowed MIME types of the requested file. If the MIME type of
	 * the file does not match one of the given MIME types the request will
	 * fail!
	 *
	 * @param allowedMimeTypes
	 *            The allowed MIME types
	 */
	public void setAllowedMimeTypes(String... allowedMimeTypes) {
		setField("AllowedMIMETypes", FcpUtils.encodeMultiStringField(allowedMimeTypes));
	}

	/**
	 * Sets the filename to download the file to. You should only call this
	 * method if your return type is {@link ReturnType#disk}!
	 *
	 * @param filename
	 *            The filename to download the file to
	 */
	public void setFilename(String filename) {
		setField("Filename", filename);
	}

	/**
	 * Sets the name for the temporary file. You should only call this method
	 * if your return type is {@link ReturnType#disk}!
	 *
	 * @param tempFilename
	 *            The name of the temporary file
	 */
	public void setTempFilename(String tempFilename) {
		setField("TempFilename", tempFilename);
	}

}
