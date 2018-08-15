/*
 * jFCPlib - FileEntry.java - Copyright © 2008 David Roden
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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Container class for file entry data.
 *
 * @see ClientPutComplexDir#addFileEntry(FileEntry)
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public abstract class FileEntry {

	/** The name of the file. */
	protected final String name;

	/** The upload source of the file. */
	protected final UploadFrom uploadFrom;

	/**
	 * Creates a new file entry with the given name and upload source.
	 *
	 * @param name
	 *            The name of the file
	 * @param uploadFrom
	 *            The upload source of the file
	 */
	protected FileEntry(String name, UploadFrom uploadFrom) {
		this.name = name;
		this.uploadFrom = uploadFrom;
	}

	/**
	 * Creates a new file entry for a file that should be transmitted to the
	 * node in the payload of the message.
	 *
	 * @param name
	 *            The name of the file
	 * @param contentType
	 *            The content type of the file, or <code>null</code> to let the
	 *            node auto-detect it
	 * @param length
	 *            The length of the file
	 * @param dataInputStream
	 *            The input stream of the file
	 * @return A file entry
	 */
	public static FileEntry createDirectFileEntry(String name, String contentType, long length, InputStream dataInputStream) {
		return new DirectFileEntry(name, contentType, length, dataInputStream);
	}

	/**
	 * Creates a new file entry for a file that should be uploaded from disk.
	 *
	 * @param name
	 *            The name of the file
	 * @param filename
	 *            The name of the file on disk
	 * @param contentType
	 *            The content type of the file, or <code>null</code> to let the
	 *            node auto-detect it
	 * @param length
	 *            The length of the file, or <code>-1</code> to not specify a
	 *            size
	 * @return A file entry
	 */
	public static FileEntry createDiskFileEntry(String name, String filename, String contentType, long length) {
		return new DiskFileEntry(name, filename, contentType, length);
	}

	/**
	 * Creates a new file entry for a file that redirects to another URI.
	 *
	 * @param name
	 *            The name of the file
	 * @param targetURI
	 *            The target URI of the redirect
	 * @return A file entry
	 */
	public static FileEntry createRedirectFileEntry(String name, String targetURI) {
		return new RedirectFileEntry(name, targetURI);
	}

	/**
	 * Returns the fields for this file entry.
	 *
	 * @return The fields for this file entry
	 */
	abstract Map<String, String> getFields();

	/**
	 * A file entry for a file that should be transmitted in the payload of the
	 * {@link ClientPutComplexDir} message.
	 *
	 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
	 */
	static class DirectFileEntry extends FileEntry {

		/** The content type of the data. */
		private final String contentType;

		/** The length of the data. */
		private final long length;

		/** The input stream of the data. */
		private final InputStream inputStream;

		/**
		 * Creates a new direct file entry with content type auto-detection.
		 *
		 * @param name
		 *            The name of the file
		 * @param length
		 *            The length of the file
		 * @param inputStream
		 *            The input stream of the file
		 */
		public DirectFileEntry(String name, long length, InputStream inputStream) {
			this(name, null, length, inputStream);
		}

		/**
		 * Creates a new direct file entry.
		 *
		 * @param name
		 *            The name of the file
		 * @param contentType
		 *            The content type of the file, or <code>null</code> to let
		 *            the node auto-detect it
		 * @param length
		 *            The length of the file
		 * @param inputStream
		 *            The input stream of the file
		 */
		public DirectFileEntry(String name, String contentType, long length, InputStream inputStream) {
			super(name, UploadFrom.direct);
			this.contentType = contentType;
			this.length = length;
			this.inputStream = inputStream;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		Map<String, String> getFields() {
			Map<String, String> fields = new HashMap<String, String>();
			fields.put("Name", name);
			fields.put("UploadFrom", String.valueOf(uploadFrom));
			fields.put("DataLength", String.valueOf(length));
			if (contentType != null) {
				fields.put("Metadata.ContentType", contentType);
			}
			return fields;
		}

		/**
		 * Returns the input stream of the file.
		 *
		 * @return The input stream of the file
		 */
		InputStream getInputStream() {
			return inputStream;
		}

	}

	/**
	 * A file entry for a file that should be uploaded from the disk.
	 *
	 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
	 */
	static class DiskFileEntry extends FileEntry {

		/** The name of the on-disk file. */
		private final String filename;

		/** The content type of the file. */
		private final String contentType;

		/** The length of the file. */
		private final long length;

		/**
		 * Creates a new disk file entry.
		 *
		 * @param name
		 *            The name of the file
		 * @param filename
		 *            The name of the on-disk file
		 * @param length
		 *            The length of the file
		 */
		public DiskFileEntry(String name, String filename, long length) {
			this(name, filename, null, length);
		}

		/**
		 * Creates a new disk file entry.
		 *
		 * @param name
		 *            The name of the file
		 * @param filename
		 *            The name of the on-disk file
		 * @param contentType
		 *            The content type of the file, or <code>null</code> to let
		 *            the node auto-detect it
		 * @param length
		 *            The length of the file
		 */
		public DiskFileEntry(String name, String filename, String contentType, long length) {
			super(name, UploadFrom.disk);
			this.filename = filename;
			this.contentType = contentType;
			this.length = length;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		Map<String, String> getFields() {
			Map<String, String> fields = new HashMap<String, String>();
			fields.put("Name", name);
			fields.put("UploadFrom", String.valueOf(uploadFrom));
			fields.put("Filename", filename);
			if (length > -1) {
				fields.put("DataSize", String.valueOf(length));
			}
			if (contentType != null) {
				fields.put("Metadata.ContentType", contentType);
			}
			return fields;
		}

	}

	/**
	 * A file entry for a file that redirects to another URI.
	 *
	 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
	 */
	static class RedirectFileEntry extends FileEntry {

		/** The target URI of the redirect. */
		private String targetURI;

		/**
		 * Creates a new redirect file entry.
		 *
		 * @param name
		 *            The name of the file
		 * @param targetURI
		 *            The target URI of the redirect
		 */
		public RedirectFileEntry(String name, String targetURI) {
			super(name, UploadFrom.redirect);
			this.targetURI = targetURI;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		Map<String, String> getFields() {
			Map<String, String> fields = new HashMap<String, String>();
			fields.put("Name", name);
			fields.put("UploadFrom", String.valueOf(uploadFrom));
			fields.put("TargetURI", targetURI);
			return fields;
		}

	}

}
