/*
 * jFCPlib - NodeRef.java - Copyright © 2008 David Roden
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
 * A reference for a node. The noderef contains all data that is necessary to
 * establish a trusted and secure connection to the node.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class NodeRef {

	/** The identity of the node. */
	private String identity;

	/** Whether the node is an opennet peer. */
	private boolean opennet;

	/** The name of the node. */
	private String name;

	/** The location of the node. */
	private double location;

	/** The IP addresses and ports of the node. */
	private String physicalUDP;

	/** The ARK of the node. */
	private ARK ark;

	/** The public DSA key of the node. */
	private String dsaPublicKey;

	/** The DSA group of the node. */
	private DSAGroup dsaGroup;

	/** The node’s supported negotiation types. */
	private int[] negotiationTypes;

	/** The version of the node. */
	private Version version;

	/** The oldest version the node will connect to. */
	private Version lastGoodVersion;

	/** Whether the node is a testnet node. */
	private boolean testnet;

	/** The signature of the reference. */
	private String signature;

	/**
	 * Creates a new, empty noderef.
	 */
	public NodeRef() {
		/* intentionally left blank. */
	}

	/**
	 * Creates a new noderef that is initialized with fields from the given
	 * message.
	 *
	 * @param fromMessage
	 *            The message to get initial values for the noderef from
	 */
	public NodeRef(FcpMessage fromMessage) {
		identity = fromMessage.getField("identity");
		opennet = Boolean.valueOf(fromMessage.getField("opennet"));
		name = fromMessage.getField("myName");
		if (fromMessage.hasField("location")) {
			location = Double.valueOf(fromMessage.getField("location"));
		}
		physicalUDP = fromMessage.getField("physical.udp");
		ark = new ARK(fromMessage.getField("ark.pubURI"), fromMessage.getField("ark.privURI"), fromMessage.getField("ark.number"));
		dsaPublicKey = fromMessage.getField("dsaPubKey.y");
		dsaGroup = new DSAGroup(fromMessage.getField("dsaGroup.b"), fromMessage.getField("dsaGroup.p"), fromMessage.getField("dsaGroup.q"));
		negotiationTypes = FcpUtils.decodeMultiIntegerField(fromMessage.getField("auth.negTypes"));
		version = new Version(fromMessage.getField("version"));
		lastGoodVersion = new Version(fromMessage.getField("lastGoodVersion"));
		testnet = Boolean.valueOf(fromMessage.getField("testnet"));
		signature = fromMessage.getField("sig");
	}

	/**
	 * Returns the identity of the node.
	 *
	 * @return The identity of the node
	 */
	public String getIdentity() {
		return identity;
	}

	/**
	 * Sets the identity of the node.
	 *
	 * @param identity
	 *            The identity of the node
	 */
	public void setIdentity(String identity) {
		this.identity = identity;
	}

	/**
	 * Returns whether the node is an opennet peer.
	 *
	 * @return <code>true</code> if the node is an opennet peer,
	 *         <code>false</code> otherwise
	 */
	public boolean isOpennet() {
		return opennet;
	}

	/**
	 * Sets whether the node is an opennet peer.
	 *
	 * @param opennet
	 *            <code>true</code> if the node is an opennet peer,
	 *            <code>false</code> otherwise
	 */
	public void setOpennet(boolean opennet) {
		this.opennet = opennet;
	}

	/**
	 * Returns the name of the node. If the node is an opennet peer, it will
	 * not have a name!
	 *
	 * @return The name of the node, or <code>null</code> if the node is an
	 *         opennet peer
	 */
	public String getMyName() {
		return name;
	}

	/**
	 * Sets the name of the peer.
	 *
	 * @param name
	 *            The name of the peer
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the location of the node.
	 *
	 * @return The location of the node
	 */
	public double getLocation() {
		return location;
	}

	/**
	 * Sets the location of the node
	 *
	 * @param location
	 *            The location of the node
	 */
	public void setLocation(double location) {
		this.location = location;
	}

	/**
	 * Returns the IP addresses and port numbers of the node.
	 *
	 * @return The IP addresses and port numbers of the node
	 */
	public String getPhysicalUDP() {
		return physicalUDP;
	}

	/**
	 * Sets the IP addresses and port numbers of the node.
	 *
	 * @param physicalUDP
	 *            The IP addresses and port numbers of the node
	 */
	public void setPhysicalUDP(String physicalUDP) {
		this.physicalUDP = physicalUDP;
	}

	/**
	 * Returns the ARK of the node.
	 *
	 * @return The ARK of the node
	 */
	public ARK getARK() {
		return ark;
	}

	/**
	 * Sets the ARK of the node.
	 *
	 * @param ark
	 *            The ARK of the node
	 */
	public void setARK(ARK ark) {
		this.ark = ark;
	}

	/**
	 * Returns the public DSA key of the node.
	 *
	 * @return The public DSA key of the node
	 */
	public String getDSAPublicKey() {
		return dsaPublicKey;
	}

	/**
	 * Sets the public DSA key of the node.
	 *
	 * @param dsaPublicKey
	 *            The public DSA key of the node
	 */
	public void setDSAPublicKey(String dsaPublicKey) {
		this.dsaPublicKey = dsaPublicKey;
	}

	/**
	 * Returns the DSA group of the node.
	 *
	 * @return The DSA group of the node
	 */
	public DSAGroup getDSAGroup() {
		return dsaGroup;
	}

	/**
	 * Sets the DSA group of the node.
	 *
	 * @param dsaGroup
	 *            The DSA group of the node
	 */
	public void setDSAGroup(DSAGroup dsaGroup) {
		this.dsaGroup = dsaGroup;
	}

	/**
	 * Returns the negotiation types supported by the node.
	 *
	 * @return The node’s supported negotiation types
	 */
	public int[] getNegotiationTypes() {
		return negotiationTypes;
	}

	/**
	 * Sets the negotiation types supported by the node.
	 *
	 * @param negotiationTypes
	 *            The node’s supported negotiation types
	 */
	public void setNegotiationTypes(int[] negotiationTypes) {
		this.negotiationTypes = negotiationTypes;
	}

	/**
	 * Returns the version of the node.
	 *
	 * @return The version of the node
	 */
	public Version getVersion() {
		return version;
	}

	/**
	 * Sets the version of the node.
	 *
	 * @param version
	 *            The version of the node
	 */
	public void setVersion(Version version) {
		this.version = version;
	}

	/**
	 * Returns the last good version of the node.
	 *
	 * @return The oldest version the node will connect to
	 */
	public Version getLastGoodVersion() {
		return lastGoodVersion;
	}

	/**
	 * Sets the last good version of the node.
	 *
	 * @param lastGoodVersion
	 *            The oldest version the node will connect to
	 */
	public void setLastGoodVersion(Version lastGoodVersion) {
		this.lastGoodVersion = lastGoodVersion;
	}

	/**
	 * Returns whether the node is a testnet node.
	 *
	 * @return <code>true</code> if the node is a testnet node,
	 *         <code>false</code> otherwise
	 */
	public boolean isTestnet() {
		return testnet;
	}

	/**
	 * Sets whether this node is a testnet node.
	 *
	 * @param testnet
	 *            <code>true</code> if the node is a testnet node,
	 *            <code>false</code> otherwise
	 */
	public void setTestnet(boolean testnet) {
		this.testnet = testnet;
	}

	/**
	 * Returns the signature of the noderef.
	 *
	 * @return The signature of the noderef
	 */
	public String getSignature() {
		return signature;
	}

	/**
	 * Sets the signature of the noderef.
	 *
	 * @param signature
	 *            The signature of the noderef
	 */
	public void setSignature(String signature) {
		this.signature = signature;
	}

}
