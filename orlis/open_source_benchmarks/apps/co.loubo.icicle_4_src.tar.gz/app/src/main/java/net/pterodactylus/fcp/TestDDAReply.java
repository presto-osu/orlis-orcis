/*
 * jFCPlib - TestDDAReply.java - Copyright © 2008 David Roden
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
 * The “TestDDAReply” is sent as a response to {@link TestDDARequest}. If you
 * specified that you wanted to read files from that directory
 * {@link #getReadFilename()} will give you a filename. Similarly, if you
 * specified that you want to write in the directory
 * {@link #getWriteFilename()} will give you a filename to write
 * {@link #getContentToWrite()} to.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class TestDDAReply extends BaseMessage {

	/**
	 * Creates a “TestDDAReply” message that wraps the received message.
	 *
	 * @param receivedMessage
	 *            The received message
	 */
	TestDDAReply(FcpMessage receivedMessage) {
		super(receivedMessage);
	}

	/**
	 * Returns the directory the TestDDRequest was made for.
	 *
	 * @return The directory to test
	 */
	public String getDirectory() {
		return getField("Directory");
	}

	/**
	 * Returns the filename you have to read to proof your ability to read that
	 * specific directory.
	 *
	 * @return The name of the file to read
	 */
	public String getReadFilename() {
		return getField("ReadFilename");
	}

	/**
	 * Returns the filename you have to write to to proof your ability to write
	 * to that specific directory.
	 *
	 * @return The name of the file write to
	 */
	public String getWriteFilename() {
		return getField("WriteFilename");
	}

	/**
	 * If you requested a test for writing permissions you have to write the
	 * return value of this method to the file given by
	 * {@link #getWriteFilename()}.
	 *
	 * @return The content to write to the file
	 */
	public String getContentToWrite() {
		return getField("ContentToWrite");
	}

}
