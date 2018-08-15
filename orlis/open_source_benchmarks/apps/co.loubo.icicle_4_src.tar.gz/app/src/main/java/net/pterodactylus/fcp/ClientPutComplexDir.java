/*
 * jFCPlib - ClientPutComplexDir.java - Copyright © 2008 David Roden
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.pterodactylus.fcp.FileEntry.DirectFileEntry;

/**
 * The “ClientPutComplexDir” lets you upload a directory with different sources
 * for each file.
 *
 * @see FileEntry
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class ClientPutComplexDir extends FcpMessage {

	/** The index for added file entries. */
	private int fileIndex = 0;

	/** The input streams from {@link DirectFileEntry}s. */
	private final List<InputStream> directFileInputStreams = new ArrayList<InputStream>();

	/**
	 * Creates a new “ClientPutComplexDir” with the given identifier and URI.
	 *
	 * @param identifier
	 *            The identifier of the request
	 * @param uri
	 *            The URI to insert the directory to
	 */
	public ClientPutComplexDir(String identifier, String uri) {
		super("ClientPutComplexDir");
		setField("Identifier", identifier);
		setField("URI", uri);
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
	 * Sets the maximum number of retries for failed blocks.
	 *
	 * @param maxRetries
	 *            The maximum number of retries for failed blocks, or
	 *            <code>-1</code> to retry endlessly
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
	 * Sets whether to generate the final URI only.
	 *
	 * @param getCHKOnly
	 *            <code>true</code> to generate the final CHK only,
	 *            <code>false</code> to complete the insert
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
	 * Sets whether the request is on the global queue.
	 *
	 * @param global
	 *            <code>true</code> to put the request on the global queue,
	 *            <code>false</code> to put it on the client-local queue
	 */
	public void setGlobal(boolean global) {
		setField("Global", String.valueOf(global));
	}

	/**
	 * Sets whether the node should not try to compress the data.
	 *
	 * @param dontCompress
	 *            <code>true</code> to skip compression of the data,
	 *            <code>false</code> to try and compress the data
	 */
	public void setDontCompress(boolean dontCompress) {
		setField("DontCompress", String.valueOf(dontCompress));
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
	 * Sets the persistence of the request.
	 *
	 * @param persistence
	 *            The persistence of the request
	 */
	public void setPersistence(Persistence persistence) {
		setField("Persistence", String.valueOf(persistence));
	}

	/**
	 * Sets the target filename of the request. This is useful for inserts that
	 * go to “CHK@” only and creates a manifest with a single file.
	 *
	 * @param targetFilename
	 *            The target filename
	 */
	public void setTargetFilename(String targetFilename) {
		setField("TargetFilename", targetFilename);
	}

	/**
	 * Sets whether to encode the complete data early to generate the
	 * {@link URIGenerated} message early.
	 *
	 * @param earlyEncode
	 *            <code>true</code> to encode the complete data early,
	 *            <code>false</code> otherwise
	 */
	public void setEarlyEncode(boolean earlyEncode) {
		setField("EarlyEncode", String.valueOf(earlyEncode));
	}

	/**
	 * Sets the default name. This is the name of the file that should be shown
	 * if no file was specified.
	 *
	 * @param defaultName
	 *            The default name
	 */
	public void setDefaultName(String defaultName) {
		setField("DefaultName", defaultName);
	}

	/**
	 * Adds an entry for a file.
	 *
	 * @param fileEntry
	 *            The file entry to add
	 */
	public void addFileEntry(FileEntry fileEntry) {
		Map<String, String> fields = fileEntry.getFields();
		for (Entry<String, String> fieldEntry : fields.entrySet()) {
			setField("Files." + fileIndex + "." + fieldEntry.getKey(), fieldEntry.getValue());
		}
		fileIndex++;
		if (fileEntry instanceof FileEntry.DirectFileEntry) {
			directFileInputStreams.add(((DirectFileEntry) fileEntry).getInputStream());
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Do not call this method to add input streams! The input streams, if any,
	 * will be taken directly from the {@link FileEntry}s and the stream you
	 * set here will be overridden!
	 */
	@Override
	public void setPayloadInputStream(InputStream payloadInputStream) {
		/* do nothing. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(OutputStream outputStream) throws IOException {
		/* create payload stream. */
		setPayloadInputStream(new SequenceInputStream(Collections.enumeration(directFileInputStreams)));
		/* write out all the fields. */
		super.write(outputStream);
	}

}
