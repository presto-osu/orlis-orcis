/*
 * jFCPlib - AllData.java - Copyright © 2008 David Roden
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

/**
 * The “AllData” message carries the payload of a successful {@link ClientGet}
 * request. You will only received this message if the {@link ClientGet}
 * request was started with a return type of {@link ReturnType#direct}. If you
 * get this message and decide that the data is for you, call
 * {@link #getPayloadInputStream()} to get the data. If an AllData message
 * passes through all registered {@link FcpListener}s without the payload being
 * consumed, the payload is discarded!
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class AllData extends BaseMessage implements Identifiable {

	/** The payload. */
	private InputStream payloadInputStream;

	/**
	 * Creates an “AllData” message that wraps the received message.
	 *
	 * @param receivedMessage
	 *            The received message
	 * @param payloadInputStream
	 *            The payload
	 */
	AllData(FcpMessage receivedMessage, InputStream payloadInputStream) {
		super(receivedMessage);
		this.payloadInputStream = payloadInputStream;
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
	 * Returns the length of the data.
	 *
	 * @return The length of the data, or <code>-1</code> if the length could
	 *         not be parsed
	 */
	public long getDataLength() {
		return FcpUtils.safeParseLong(getField("DataLength"));
	}

	/**
	 * Returns the startup time of the request.
	 *
	 * @return The startup time of the request (in milliseconds since Jan 1,
	 *         1970 UTC), or <code>-1</code> if the time could not be parsed
	 */
	public long getStartupTime() {
		return FcpUtils.safeParseLong(getField("StartupTime"));
	}

	/**
	 * Returns the completion time of the request.
	 *
	 * @return The completion time of the request (in milliseconds since Jan 1,
	 *         1970 UTC), or <code>-1</code> if the time could not be parsed
	 */
	public long getCompletionTime() {
		return FcpUtils.safeParseLong(getField("CompletionTime"));
	}

	/**
	 * Returns the payload input stream. You <strong>have</strong> consume the
	 * input stream before returning from the
	 * {@link FcpListener#receivedAllData(FcpConnection, AllData)} method!
	 *
	 * @return The payload
	 */
	public InputStream getPayloadInputStream() {
		return payloadInputStream;
	}

	/**
	 * Returns the content type of the found file.
	 *
	 * @return The content type
	 */
	public String getContentType() {
		return getField("Metadata.ContentType");
	}

}
