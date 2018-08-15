/*
 * jFCPlib - ReceivedBookmarkFeed.java - Copyright © 2009 David Roden
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
 * Implementation of the “ReceivedBookmarkFeed” FCP message. This message
 * notifies an FCP client that an update for a bookmark has been found.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class Feed extends BaseMessage {


	/** The payload. */
	private InputStream payloadInputStream;
	/**
	 * Creates a new “ReceivedBookmarkFeed” message.
	 *
	 * @param fcpMessage
	 *            The FCP message to get the fields from
	 */
	public Feed(FcpMessage fcpMessage, InputStream payloadInputStream) {
		super(fcpMessage);
		this.payloadInputStream = payloadInputStream;
	}

	public long getDataLength() {
		return FcpUtils.safeParseLong(getField("DataLength"));
	}
	public long getTextLength() {
		return FcpUtils.safeParseLong(getField("TextLength"));
	}
	public long getUpdatedTime() {
		return FcpUtils.safeParseLong(getField("UpdatedTime"));
	}
	public String getSourceNodeName() {
		return getField("SourceNodeName");
	}
	public String getHeader() {
		return getField("Header");
	}
	public String getShortText(){
		return getField("ShortText");
	}
	public String getField(String field){
		return super.getField(field);
	}

	/**
	 * Returns the payload input stream. You <strong>have</strong> consume the
	 * input stream before returning from the
	 * {@link FcpListener#receivedFeed(FcpConnection, Feed)} method!
	 *
	 * @return The payload
	 */
	public InputStream getPayloadInputStream() {
		return payloadInputStream;
	}
}
