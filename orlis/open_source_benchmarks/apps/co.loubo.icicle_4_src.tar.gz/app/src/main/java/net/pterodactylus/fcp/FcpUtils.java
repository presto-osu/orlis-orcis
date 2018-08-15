/*
 * jFCPlib - FcpUtils.java - Copyright © 2008 David Roden
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Helper class with utility methods for the FCP protocol.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class FcpUtils {

	/** Counter for unique identifiers. */
	private static AtomicLong counter = new AtomicLong();

	/**
	 * Returns a unique identifier.
	 *
	 * @return A unique identifier
	 */
	public static String getUniqueIdentifier() {
		return new StringBuilder().append(System.currentTimeMillis()).append('-').append(counter.getAndIncrement()).toString();
	}

	/**
	 * Parses an integer field, separated by ‘;’ and returns the parsed values.
	 *
	 * @param field
	 *            The field to parse
	 * @return An array with the parsed values
	 * @throws NumberFormatException
	 *             if a value can not be converted to a number
	 */
	public static int[] decodeMultiIntegerField(String field) throws NumberFormatException {
		StringTokenizer fieldTokens = new StringTokenizer(field, ";");
		int[] result = new int[fieldTokens.countTokens()];
		int counter = 0;
		while (fieldTokens.hasMoreTokens()) {
			String fieldToken = fieldTokens.nextToken();
			result[counter++] = Integer.valueOf(fieldToken);
		}
		return result;
	}

	/**
	 * Encodes the given integer array into a string, separating the values by
	 * ‘;’.
	 *
	 * @param values
	 *            The values to encode
	 * @return The encoded values
	 */
	public static String encodeMultiIntegerField(int[] values) {
		StringBuilder encodedField = new StringBuilder();
		for (int value : values) {
			if (encodedField.length() > 0) {
				encodedField.append(';');
			}
			encodedField.append(value);
		}
		return encodedField.toString();
	}

	/**
	 * Encodes the given string array into a string, separating the values by
	 * ‘;’.
	 *
	 * @param values
	 *            The values to encode
	 * @return The encoded values
	 */
	public static String encodeMultiStringField(String[] values) {
		StringBuilder encodedField = new StringBuilder();
		for (String value : values) {
			if (encodedField.length() > 0) {
				encodedField.append(';');
			}
			encodedField.append(value);
		}
		return encodedField.toString();
	}

	/**
	 * Tries to parse the given string into an int, returning <code>-1</code>
	 * if the string can not be parsed.
	 *
	 * @param value
	 *            The string to parse
	 * @return The parsed int, or <code>-1</code>
	 */
	public static int safeParseInt(String value) {
		return safeParseInt(value, -1);
	}

	/**
	 * Tries to parse the given string into an int, returning
	 * <code>defaultValue</code> if the string can not be parsed.
	 *
	 * @param value
	 *            The string to parse
	 * @param defaultValue
	 *            The value to return if the string can not be parsed.
	 * @return The parsed int, or <code>defaultValue</code>
	 */
	public static int safeParseInt(String value, int defaultValue) {
		try {
			return Integer.valueOf(value);
		} catch (NumberFormatException nfe1) {
			return defaultValue;
		}
	}

	/**
	 * Tries to parse the given string into an long, returning <code>-1</code>
	 * if the string can not be parsed.
	 *
	 * @param value
	 *            The string to parse
	 * @return The parsed long, or <code>-1</code>
	 */
	public static long safeParseLong(String value) {
		return safeParseLong(value, -1);
	}

	/**
	 * Tries to parse the given string into an long, returning
	 * <code>defaultValue</code> if the string can not be parsed.
	 *
	 * @param value
	 *            The string to parse
	 * @param defaultValue
	 *            The value to return if the string can not be parsed.
	 * @return The parsed long, or <code>defaultValue</code>
	 */
	public static long safeParseLong(String value, long defaultValue) {
		try {
			return Long.valueOf(value);
		} catch (NumberFormatException nfe1) {
			return defaultValue;
		}
	}

	/**
	 * Closes the given socket.
	 *
	 * @param socket
	 *            The socket to close
	 */
	public static void close(Socket socket) {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException ioe1) {
				/* ignore. */
			}
		}
	}

	/**
	 * Closes the given Closeable.
	 *
	 * @param closeable
	 *            The Closeable to close
	 */
	public static void close(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException ioe1) {
				/* ignore. */
			}
		}
	}

	/**
	 * Copies as many bytes as possible (i.e. until {@link InputStream#read()}
	 * returns <code>-1</code>) from the source input stream to the destination
	 * output stream.
	 *
	 * @param source
	 *            The input stream to read from
	 * @param destination
	 *            The output stream to write to
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public static void copy(InputStream source, OutputStream destination) throws IOException {
		copy(source, destination, -1);
	}

	/**
	 * Copies <code>length</code> bytes from the source input stream to the
	 * destination output stream. If <code>length</code> is <code>-1</code> as
	 * much bytes as possible will be copied (i.e. until
	 * {@link InputStream#read()} returns <code>-1</code> to signal the end of
	 * the stream).
	 *
	 * @param source
	 *            The input stream to read from
	 * @param destination
	 *            The output stream to write to
	 * @param length
	 *            The number of bytes to copy
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public static void copy(InputStream source, OutputStream destination, long length) throws IOException {
		copy(source, destination, length, 1 << 16);
	}

	/**
	 * Copies <code>length</code> bytes from the source input stream to the
	 * destination output stream. If <code>length</code> is <code>-1</code> as
	 * much bytes as possible will be copied (i.e. until
	 * {@link InputStream#read()} returns <code>-1</code> to signal the end of
	 * the stream).
	 *
	 * @param source
	 *            The input stream to read from
	 * @param destination
	 *            The output stream to write to
	 * @param length
	 *            The number of bytes to copy
	 * @param bufferSize
	 *            The buffer size
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public static void copy(InputStream source, OutputStream destination, long length, int bufferSize) throws IOException {
		long remaining = length;
		byte[] buffer = new byte[bufferSize];
		int read = 0;
		while ((remaining == -1) || (remaining > 0)) {
			read = source.read(buffer, 0, ((remaining > bufferSize) || (remaining == -1)) ? bufferSize : (int) remaining);
			if (read == -1) {
				if (length == -1) {
					return;
				}
				throw new EOFException("stream reached eof");
			}
			destination.write(buffer, 0, read);
			remaining -= read;
		}
	}

	/**
	 * This input stream stores the content of another input stream either in a
	 * file or in memory, depending on the length of the input stream.
	 *
	 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
	 */
	public static class TempInputStream extends InputStream {

		/** The default maximum lenght for in-memory storage. */
		public static final long MAX_LENGTH_MEMORY = 65536;

		/** The temporary file to read from. */
		private final File tempFile;

		/** The input stream that reads from the file. */
		private final InputStream fileInputStream;

		/** The input stream that reads from memory. */
		private final InputStream memoryInputStream;

		/**
		 * Creates a new temporary input stream that stores the given input
		 * stream in a temporary file.
		 *
		 * @param originalInputStream
		 *            The original input stream
		 * @throws IOException
		 *             if an I/O error occurs
		 */
		public TempInputStream(InputStream originalInputStream) throws IOException {
			this(originalInputStream, -1);
		}

		/**
		 * Creates a new temporary input stream that stores the given input
		 * stream in memory if it is shorter than {@link #MAX_LENGTH_MEMORY},
		 * otherwise it is stored in a file.
		 *
		 * @param originalInputStream
		 *            The original input stream
		 * @param length
		 *            The length of the input stream
		 * @throws IOException
		 *             if an I/O error occurs
		 */
		public TempInputStream(InputStream originalInputStream, long length) throws IOException {
			this(originalInputStream, length, MAX_LENGTH_MEMORY);
		}

		/**
		 * Creates a new temporary input stream that stores the given input
		 * stream in memory if it is shorter than <code>maxMemoryLength</code>,
		 * otherwise it is stored in a file.
		 *
		 * @param originalInputStream
		 *            The original input stream
		 * @param length
		 *            The length of the input stream
		 * @param maxMemoryLength
		 *            The maximum length to store in memory
		 * @throws IOException
		 *             if an I/O error occurs
		 */
		public TempInputStream(InputStream originalInputStream, long length, long maxMemoryLength) throws IOException {
			if ((length > -1) && (length <= maxMemoryLength)) {
				ByteArrayOutputStream memoryOutputStream = new ByteArrayOutputStream((int) length);
				try {
					FcpUtils.copy(originalInputStream, memoryOutputStream, length, (int) length);
				} finally {
					memoryOutputStream.close();
				}
				tempFile = null;
				fileInputStream = null;
				memoryInputStream = new ByteArrayInputStream(memoryOutputStream.toByteArray());
			} else {
				tempFile = File.createTempFile("temp-", ".bin");
				tempFile.deleteOnExit();
				FileOutputStream fileOutputStream = null;
				try {
					fileOutputStream = new FileOutputStream(tempFile);
					FcpUtils.copy(originalInputStream, fileOutputStream);
					fileInputStream = new FileInputStream(tempFile);
				} finally {
					FcpUtils.close(fileOutputStream);
				}
				memoryInputStream = null;
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int available() throws IOException {
			if (memoryInputStream != null) {
				return memoryInputStream.available();
			}
			return fileInputStream.available();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void close() throws IOException {
			if (memoryInputStream != null) {
				memoryInputStream.close();
				return;
			}
			tempFile.delete();
			fileInputStream.close();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public synchronized void mark(int readlimit) {
			if (memoryInputStream != null) {
				memoryInputStream.mark(readlimit);
				return;
			}
			fileInputStream.mark(readlimit);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean markSupported() {
			if (memoryInputStream != null) {
				return memoryInputStream.markSupported();
			}
			return fileInputStream.markSupported();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int read() throws IOException {
			if (memoryInputStream != null) {
				return memoryInputStream.read();
			}
			return fileInputStream.read();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int read(byte[] b) throws IOException {
			if (memoryInputStream != null) {
				return memoryInputStream.read(b);
			}
			return fileInputStream.read(b);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			if (memoryInputStream != null) {
				return memoryInputStream.read(b, off, len);
			}
			return fileInputStream.read(b, off, len);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public synchronized void reset() throws IOException {
			if (memoryInputStream != null) {
				memoryInputStream.reset();
				return;
			}
			fileInputStream.reset();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public long skip(long n) throws IOException {
			if (memoryInputStream != null) {
				return memoryInputStream.skip(n);
			}
			return fileInputStream.skip(n);
		}

	}

}
