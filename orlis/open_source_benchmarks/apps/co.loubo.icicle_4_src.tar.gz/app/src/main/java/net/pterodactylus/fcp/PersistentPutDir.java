/*
 * jFCPlib - PersistentPutDir.java - Copyright © 2008 David Roden
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
 * A “PersistentPutDir” is the response to a {@link ClientPutDiskDir} message.
 * It is also sent as a possible response to a {@link ListPersistentRequests}
 * message.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class PersistentPutDir extends BaseMessage implements Identifiable {

	/**
	 * Creates a new “PersistentPutDir” message that wraps the received
	 * message.
	 *
	 * @param receivedMessage
	 *            The received message
	 */
	PersistentPutDir(FcpMessage receivedMessage) {
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
	 * Returns the priority of the request.
	 *
	 * @return The priority of the request
	 */
	public Priority getPriority() {
		return Priority.values()[FcpUtils.safeParseInt(getField("PriorityClass"), Priority.unknown.ordinal())];
	}

	/**
	 * Returns whether the request is on the global queue.
	 *
	 * @return <code>true</code> if the request is on the global queue,
	 *         <code>false</code> if it is on the client-local queue
	 */
	public boolean isGlobal() {
		return Boolean.valueOf(getField("Global"));
	}

	/**
	 * Returns the maximum number of retries for failed blocks.
	 *
	 * @return The maximum number of retries, or <code>-1</code> for endless
	 *         retries, or <code>-2</code> if the number could not be parsed
	 */
	public int getMaxRetries() {
		return FcpUtils.safeParseInt(getField("MaxRetries"), -2);
	}

	/**
	 * Returns the number of files in the request.
	 *
	 * @return The number of files in the request
	 */
	public int getFileCount() {
		int fileCount = -1;
		while (getField("Files." + ++fileCount + ".UploadFrom") != null) {
			/* do nothing. */
		}
		return fileCount;
	}

	/**
	 * Returns the name of the file at the given index. The index is counted
	 * from <code>0</code>.
	 *
	 * @param fileIndex
	 *            The index of the file
	 * @return The name of the file at the given index
	 */
	public String getFileName(int fileIndex) {
		return getField("Files." + fileIndex + ".Name");
	}

	/**
	 * Returns the length of the file at the given index. The index is counted
	 * from <code>0</code>.
	 *
	 * @param fileIndex
	 *            The index of the file
	 * @return The length of the file at the given index
	 */
	public long getFileDataLength(int fileIndex) {
		return FcpUtils.safeParseLong(getField("Files." + fileIndex + ".DataLength"));
	}

	/**
	 * Returns the upload source of the file at the given index. The index is
	 * counted from <code>0</code>.
	 *
	 * @param fileIndex
	 *            The index of the file
	 * @return The upload source of the file at the given index
	 */
	public UploadFrom getFileUploadFrom(int fileIndex) {
		return UploadFrom.valueOf(getField("Files." + fileIndex + ".UploadFrom"));
	}

	/**
	 * Returns the content type of the file at the given index. The index is
	 * counted from <code>0</code>.
	 *
	 * @param fileIndex
	 *            The index of the file
	 * @return The content type of the file at the given index
	 */
	public String getFileMetadataContentType(int fileIndex) {
		return getField("Files." + fileIndex + ".Metadata.ContentType");
	}

	/**
	 * Returns the filename of the file at the given index. This value is only
	 * returned if {@link #getFileUploadFrom(int)} is returning
	 * {@link UploadFrom#disk}. The index is counted from <code>0</code>.
	 *
	 * @param fileIndex
	 *            The index of the file
	 * @return The filename of the file at the given index
	 */
	public String getFileFilename(int fileIndex) {
		return getField("Files." + fileIndex + ".Filename");
	}

}
