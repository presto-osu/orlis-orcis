/*
 * jFCPlib - TestDDAResponse.java - Copyright © 2008 David Roden
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
 * A “TestDDAResponse” is sent to let the node know that either created a file
 * with the content from {@link TestDDAReply#getContentToWrite()} or that you
 * read the content of the file given by {@link TestDDAReply#getReadFilename()}
 * .
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class TestDDAResponse extends FcpMessage {

	/**
	 * Creates a new “TestDDAResponse” message that signals that you created
	 * the file given by {@link TestDDAReply#getWriteFilename()} and wrote the
	 * contents given by {@link TestDDAReply#getContentToWrite()} to it.
	 *
	 * @param directory
	 *            The directory from the {@link TestDDARequest} command
	 */
	public TestDDAResponse(String directory) {
		this(directory, null);
	}

	/**
	 * Creates a new “TestDDAResponse” message that signals that you created
	 * the file given by {@link TestDDAReply#getWriteFilename()} with the
	 * contents given by {@link TestDDAReply#getContentToWrite()} to it (when
	 * you specified that you want to write to the directory) and/or that you
	 * read the file given by {@link TestDDAReply#getReadFilename()} (when you
	 * specified you wanted to read the directory).
	 *
	 * @param directory
	 *            The directory from the {@link TestDDARequest} command
	 * @param readContent
	 *            The read content, or <code>null</code> if you did not request
	 *            read access
	 */
	public TestDDAResponse(String directory, String readContent) {
		super("TestDDAResponse");
		if (readContent != null) {
			setField("ReadContent", readContent);
		}
	}

}
