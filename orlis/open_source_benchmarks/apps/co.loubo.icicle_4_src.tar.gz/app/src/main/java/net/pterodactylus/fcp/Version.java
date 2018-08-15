/*
 * jFCPlib - Version.java - Copyright © 2008 David Roden
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

import java.util.StringTokenizer;

/**
 * Container for the “lastGoodVersion” field.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class Version {

	/** The name of the node implementation. */
	private final String nodeName;

	/** The tree version of the node. */
	private final String treeVersion;

	/** The protocol version of the node. */
	private final String protocolVersion;

	/** The build number of the node. */
	private final int buildNumber;

	/**
	 * Creates a new Version from the given string. The string consists of the
	 * four required fields node name, tree version, protocol version, and
	 * build number, separated by a comma.
	 *
	 * @param version
	 *            The version string
	 * @throws NullPointerException
	 *             if <code>version</code> is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if <code>version</code> is not in the right format
	 */
	public Version(String version) {
		if (version == null) {
			throw new NullPointerException("version must not be null");
		}
		StringTokenizer versionTokens = new StringTokenizer(version, ",");
		if (versionTokens.countTokens() != 4) {
			throw new IllegalArgumentException("version must consist of four fields");
		}
		this.nodeName = versionTokens.nextToken();
		this.treeVersion = versionTokens.nextToken();
		this.protocolVersion = versionTokens.nextToken();
		try {
			this.buildNumber = Integer.valueOf(versionTokens.nextToken());
		} catch (NumberFormatException nfe1) {
			throw new IllegalArgumentException("last part of version must be numeric", nfe1);
		}
	}

	/**
	 * Creates a new Version from the given parts.
	 *
	 * @param nodeName
	 *            The name of the node implementation
	 * @param treeVersion
	 *            The tree version
	 * @param protocolVersion
	 *            The protocol version
	 * @param buildNumber
	 *            The build number of the node
	 */
	public Version(String nodeName, String treeVersion, String protocolVersion, int buildNumber) {
		this.nodeName = nodeName;
		this.treeVersion = treeVersion;
		this.protocolVersion = protocolVersion;
		this.buildNumber = buildNumber;
	}

	/**
	 * Returns the name of the node implementation.
	 *
	 * @return The node name
	 */
	public String getNodeName() {
		return nodeName;
	}

	/**
	 * The tree version of the node.
	 *
	 * @return The tree version of the node
	 */
	public String getTreeVersion() {
		return treeVersion;
	}

	/**
	 * The protocol version of the node
	 *
	 * @return The protocol version of the node
	 */
	public String getProtocolVersion() {
		return protocolVersion;
	}

	/**
	 * The build number of the node.
	 *
	 * @return The build number of the node
	 */
	public int getBuildNumber() {
		return buildNumber;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return nodeName + "," + treeVersion + "," + protocolVersion + "," + buildNumber;
	}

}
