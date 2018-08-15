/*
 * jFCPlib - TestDDARequest.java - Copyright © 2008 David Roden
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
 * The “TestDDARequest” initiates a DDA test sequence.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class TestDDARequest extends FcpMessage {

	/**
	 * Creates a new “TestDDARequest” command that initiates a DDA test.
	 *
	 * @param directory
	 *            The directory you want to access files in
	 * @param wantReadDirectory
	 *            <code>true</code> if you want to read files from the
	 *            directory
	 * @param wantWriteDirectory
	 *            <code>true</code> if you want to write files to the directory
	 */
	public TestDDARequest(String directory, boolean wantReadDirectory, boolean wantWriteDirectory) {
		super("TestDDARequest");
		setField("Directory", directory);
		setField("WantReadDirectory", String.valueOf(wantReadDirectory));
		setField("WantWriteDirectory", String.valueOf(wantWriteDirectory));
	}

}
