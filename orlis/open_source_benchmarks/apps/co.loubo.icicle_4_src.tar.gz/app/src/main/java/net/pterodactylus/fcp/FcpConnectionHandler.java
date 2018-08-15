/*
 * jFCPlib - FcpConnectionHandler.java - Copyright © 2008 David Roden
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
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.util.logging.Logging;

/**
 * Handles an FCP connection to a node.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
class FcpConnectionHandler implements Runnable {

	/** The logger. */
	private static final Logger logger = Logging.getLogger(FcpConnectionHandler.class.getName());

	/** The underlying connection. */
	private final FcpConnection fcpConnection;

	/** The input stream from the node. */
	private final InputStream remoteInputStream;

	/** Whether to stop the connection handler. */
	private boolean shouldStop;

	/** Whether the next read line feed should be ignored. */
	private boolean ignoreNextLinefeed;

	/**
	 * Creates a new connection handler that operates on the given connection
	 * and input stream.
	 *
	 * @param fcpConnection
	 *            The underlying FCP connection
	 * @param remoteInputStream
	 *            The input stream from the node
	 */
	public FcpConnectionHandler(FcpConnection fcpConnection, InputStream remoteInputStream) {
		this.fcpConnection = fcpConnection;
		this.remoteInputStream = remoteInputStream;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		FcpMessage fcpMessage = null;
		Throwable throwable = null;
		while (true) {
			synchronized (this) {
				if (shouldStop) {
					break;
				}
			}
			try {
				String line = readLine();
				logger.log(Level.FINEST, "read line: %1$s", line);
				if (line == null) {
					break;
				}
				if (line.length() == 0) {
					continue;
				}
				line = line.trim();
				if (fcpMessage == null) {
					fcpMessage = new FcpMessage(line);
					continue;
				}
				if ("EndMessage".equalsIgnoreCase(line) || "Data".equalsIgnoreCase(line)) {
					fcpConnection.handleMessage(fcpMessage);
					fcpMessage = null;
				}
				int equalSign = line.indexOf('=');
				if (equalSign == -1) {
					/* something's fishy! */
					continue;
				}
				String field = line.substring(0, equalSign);
				String value = line.substring(equalSign + 1);
				assert fcpMessage != null: "fcp message is null";
				fcpMessage.setField(field, value);
			} catch (IOException ioe1) {
				throwable = ioe1;
				break;
			}
		}
		fcpConnection.handleDisconnect(throwable);
	}

	/**
	 * Stops the connection handler.
	 */
	public void stop() {
		synchronized (this) {
			shouldStop = true;
		}
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Reads bytes from {@link #remoteInputStream} until ‘\r’ or ‘\n’ are
	 * encountered and decodes the read bytes using UTF-8.
	 *
	 * @return The decoded line
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	private String readLine() throws IOException {
		byte[] readBytes = new byte[512];
		int readIndex = 0;
		while (true) {
			int nextByte = remoteInputStream.read();
			if (nextByte == -1) {
				if (readIndex == 0) {
					return null;
				}
				break;
			}
			if (nextByte == 10) {
				if (!ignoreNextLinefeed) {
					break;
				}
			}
			ignoreNextLinefeed = false;
			if (nextByte == 13) {
				ignoreNextLinefeed = true;
				break;
			}
			if (readIndex == readBytes.length) {
				/* recopy & enlarge array */
				byte[] newReadBytes = new byte[readBytes.length * 2];
				System.arraycopy(readBytes, 0, newReadBytes, 0, readBytes.length);
				readBytes = newReadBytes;
			}
			readBytes[readIndex++] = (byte) nextByte;
		}
		ByteBuffer byteBuffer = ByteBuffer.wrap(readBytes, 0, readIndex);
		return Charset.forName("UTF-8").decode(byteBuffer).toString();
	}

}
