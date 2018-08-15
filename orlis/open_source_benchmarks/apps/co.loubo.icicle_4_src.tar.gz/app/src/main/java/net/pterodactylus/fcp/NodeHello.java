/*
 * jFCPlib - NodeHello.java - Copyright © 2008 David Roden
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
 * Some convenience methods for parsing a “NodeHello” message from the node.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class NodeHello extends BaseMessage {

	/**
	 * Createa a new “NodeHello” message that wraps the received message.
	 *
	 * @param receivedMessage
	 *            The received FCP message
	 */
	NodeHello(FcpMessage receivedMessage) {
		super(receivedMessage);
	}

	/**
	 * Returns the build of the node. This may not be a number but also a
	 * string like “@custom@” in case you built the node yourself.
	 *
	 * @return The build of the node
	 */
	public String getBuild() {
		return getField("Build");
	}

	/**
	 * Returns the build number of the node. This may not be a number but also
	 * a string like “@custom@” in case you built the node yourself.
	 *
	 * @return The build number of the node, or <code>-1</code> if the build
	 *         number could not be determined
	 */
	public int getBuildNumber() {
		return FcpUtils.safeParseInt(getBuild());
	}

	/**
	 * Returns the number of compression codecs.
	 *
	 * @return The number of compression codecs
	 */
	public String getCompressionCodecs() {
		return getField("CompressionCodecs");
	}

	/**
	 * Returns the number of compression codecs.
	 *
	 * @return The number of compression codecs, or <code>-1</code> if the
	 *         number of compression codecs could not be determined
	 */
	public int getCompressionCodecsNumber() {
		return FcpUtils.safeParseInt(getCompressionCodecs());
	}

	/**
	 * Returns the unique connection identifier.
	 *
	 * @return The connection identifier
	 */
	public String getConnectionIdentifier() {
		return getField("ConnectionIdentifier");
	}

	/**
	 * Returns the build of the external library file.
	 *
	 * @return The build of the external library file
	 */
	public String getExtBuild() {
		return getField("ExtBuild");
	}

	/**
	 * Returns the build number of the external library file.
	 *
	 * @return The build number of the external library file, or
	 *         <code>-1</code> if the build number could not be determined
	 */
	public int getExtBuildNumber() {
		return FcpUtils.safeParseInt(getExtBuild());
	}

	/**
	 * Returns the revision of the external library file.
	 *
	 * @return The revision of the external library file
	 */
	public String getExtRevision() {
		return getField("ExtRevision");
	}

	/**
	 * Returns the revision number of the external library file.
	 *
	 * @return The revision number of the external library file, or
	 *         <code>-1</code> if the revision number could not be determined
	 */
	public int getExtRevisionNumber() {
		return FcpUtils.safeParseInt(getExtRevision());
	}

	/**
	 * Returns the FCP version the node speaks.
	 *
	 * @return The FCP version the node speaks
	 */
	public String getFCPVersion() {
		return getField("FCPVersion");
	}

	/**
	 * Returns the make of the node, e.g. “Fred” (freenet reference
	 * implementation).
	 *
	 * @return The make of the node
	 */
	public String getNode() {
		return getField("Node");
	}

	/**
	 * Returns the language of the node as 2-letter code, e.g. “en” or “de”.
	 *
	 * @return The language of the node
	 */
	public String getNodeLanguage() {
		return getField("NodeLanguage");
	}

	/**
	 * Returns the revision of the node.
	 *
	 * @return The revision of the node
	 */
	public String getRevision() {
		return getField("Revision");
	}

	/**
	 * Returns the revision number of the node.
	 *
	 * @return The revision number of the node, or <code>-1</code> if the
	 *         revision number coult not be determined
	 */
	public int getRevisionNumber() {
		return FcpUtils.safeParseInt(getRevision());
	}

	/**
	 * Returns whether the node is currently is testnet mode.
	 *
	 * @return <code>true</code> if the node is currently in testnet mode,
	 *         <code>false</code> otherwise
	 */
	public boolean getTestnet() {
		return Boolean.valueOf(getField("Testnet"));
	}

	/**
	 * Returns the version of the node.
	 *
	 * @return The version of the node
	 */
	public String getVersion() {
		return getField("Version");
	}

}
