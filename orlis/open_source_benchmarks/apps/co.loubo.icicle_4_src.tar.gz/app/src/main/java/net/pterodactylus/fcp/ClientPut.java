/*
 * jFCPlib - ClientPut.java - Copyright © 2008 David Roden
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
 * A “ClientPut” requests inserts a single file into freenet, either uploading
 * it directly with this messge ({@link UploadFrom#direct}), uploading it from
 * disk ({@link UploadFrom#disk}) or by creating a redirect to another URI (
 * {@link UploadFrom#redirect}).
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class ClientPut extends FcpMessage {

	/**
	 * Creates a new “ClientPut” message that inserts a file to the given URI.
	 * The file data <em>has</em> to be supplied to this message using
	 * {@link #setPayloadInputStream(java.io.InputStream)}! Using this
	 * constructor is the same as using
	 * {@link #ClientPut(String, String, UploadFrom)} with
	 * {@link UploadFrom#direct} as third parameter.
	 *
	 * @param uri
	 *            The URI to insert the file to
	 * @param identifier
	 *            The identifier of the request
	 */
	public ClientPut(String uri, String identifier) {
		this(uri, identifier, UploadFrom.direct);
	}

	/**
	 * Creates a new “ClientPut” message that inserts a file to the given URI.
	 * Depending on <code>uploadFrom</code> the file data has to be supplied in
	 * different ways: If <code>uploadFrom</code> is {@link UploadFrom#direct},
	 * use {@link #setPayloadInputStream(java.io.InputStream)} to supply the
	 * input data. If <code>uploadFrom</code> is {@link UploadFrom#disk}, use
	 * {@link #setFilename(String)} to supply the file to upload. You have to
	 * test your direct-disk access (see {@link TestDDARequest},
	 * {@link TestDDAReply}, {@link TestDDAResponse}, {@link TestDDAComplete})
	 * before using this option! If <code>uploadFrom</code> is
	 * {@link UploadFrom#redirect}, use {@link #setTargetURI(String)} to set
	 * the target URI of the redirect.
	 *
	 * @param uri
	 *            The URI to insert to
	 * @param identifier
	 *            The identifier of the insert
	 * @param uploadFrom
	 *            The source of the upload
	 */
	public ClientPut(String uri, String identifier, UploadFrom uploadFrom) {
		super("ClientPut");
		setField("URI", uri);
		setField("Identifier", identifier);
		setField("UploadFrom", String.valueOf(uploadFrom));
	}

	/**
	 * The MIME type of the content.
	 *
	 * @param metadataContentType
	 *            The MIME type of the content
	 */
	public void setMetadataContentType(String metadataContentType) {
		setField("Metadata.ContentType", metadataContentType);
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
	 * Sets the target filename of the inserted file. This value is ignored for
	 * all inserts that do not have “CHK@” as a target.
	 *
	 * @param targetFilename
	 *            The filename of the target
	 */
	public void setTargetFilename(String targetFilename) {
		setField("TargetFilename", targetFilename);
	}

	/**
	 * Determines whether to encode the complete file early in the life of the
	 * request.
	 *
	 * @param earlyEncode
	 *            <code>true</code> to generate the final key long before the
	 *            file is completely fetchable
	 */
	public void setEarlyEncode(boolean earlyEncode) {
		setField("EarlyEncode", String.valueOf(earlyEncode));
	}

	/**
	 * Sets the length of the data that will be transferred after this message
	 * if <code>uploadFrom</code> is {@link UploadFrom#direct} is used.
	 *
	 * @param dataLength
	 *            The length of the data
	 */
	public void setDataLength(long dataLength) {
		setField("DataLength", String.valueOf(dataLength));
	}

	/**
	 * Sets the name of the file to upload the data from.
	 *
	 * @param filename
	 *            The filename to upload
	 */
	public void setFilename(String filename) {
		setField("Filename", filename);
	}

	/**
	 * If <code>uploadFrom</code> is {@link UploadFrom#redirect}, use this
	 * method to determine that target of the redirect.
	 *
	 * @param targetURI
	 *            The target URI to redirect to
	 */
	public void setTargetURI(String targetURI) {
		setField("TargetURI", targetURI);
	}

}
