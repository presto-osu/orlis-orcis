/*
 * jFCPlib - NodeData.java - Copyright © 2008 David Roden
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
 * The “NodeData” contains the noderef of the node, along with additional data.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class NodeData extends BaseMessage {

	/** The noderef of the node. */
	private final NodeRef nodeRef;

	/**
	 * Creates a new “NodeData” message that wraps the received message.
	 *
	 * @param receivedMessage
	 *            The received message
	 */
	NodeData(FcpMessage receivedMessage) {
		super(receivedMessage);
		nodeRef = new NodeRef(receivedMessage);
	}

	/**
	 * Returns the noderef of the node.
	 *
	 * @return The noderef of the node
	 */
	public NodeRef getNodeRef() {
		return nodeRef;
	}

	/**
	 * Returns the last good version, i.e. the oldest version the node will
	 * connect to.
	 *
	 * @return The last good version
	 */
	public Version getLastGoodVersion() {
		return nodeRef.getLastGoodVersion();
	}

	/**
	 * Returns the signature of the noderef.
	 *
	 * @return The signature of the noderef
	 */
	public String getSignature() {
		return nodeRef.getSignature();
	}

	/**
	 * Returns whether the noderef is the opennet noderef of the node
	 *
	 * @return <code>true</code> if the noderef is the opennet noderef of the
	 *         node, <code>false</code> otherwise
	 */
	public boolean isOpennet() {
		return nodeRef.isOpennet();
	}

	/**
	 * Returns the identity of the node
	 *
	 * @return The identity of the node
	 */
	public String getIdentity() {
		return nodeRef.getIdentity();
	}

	/**
	 * Returns the name of the node.
	 *
	 * @return The name of the node
	 */
	public String getMyName() {
		return nodeRef.getMyName();
	}

	/**
	 * Returns the version of the node.
	 *
	 * @return The version of the node
	 */
	public Version getVersion() {
		return nodeRef.getVersion();
	}

	/**
	 * Returns IP addresses and port number of the node.
	 *
	 * @return The IP addresses and port numbers of the node
	 */
	public String getPhysicalUDP() {
		return nodeRef.getPhysicalUDP();
	}

	/**
	 * Returns the ARK of the node.
	 *
	 * @return The ARK of the node
	 */
	public ARK getARK() {
		return nodeRef.getARK();
	}

	/**
	 * Returns the public key of the node.
	 *
	 * @return The public key of the node
	 */
	public String getDSAPublicKey() {
		return nodeRef.getDSAPublicKey();
	}

	/**
	 * Returns the private key of the node.
	 *
	 * @return The private key of the node
	 */
	public String getDSKPrivateKey() {
		return getField("dsaPrivKey.x");
	}

	/**
	 * Returns the DSA group of the node.
	 *
	 * @return The DSA group of the node
	 */
	public DSAGroup getDSAGroup() {
		return nodeRef.getDSAGroup();
	}

	/**
	 * Returns the negotiation types supported by the node.
	 *
	 * @return The node’s supported negotiation types
	 */
	public int[] getNegotiationTypes() {
		return nodeRef.getNegotiationTypes();
	}

	/**
	 * Returns one of the volatile fields from the message. The given field
	 * name is prepended with “volatile.” so if you want to get the value of
	 * the field with the name “volatile.freeJavaMemory” you only need to
	 * specify “freeJavaMemory”.
	 *
	 * @param field
	 *            The name of the field
	 * @return The value of the field, or <code>null</code> if there is no such
	 *         field
	 */
	public String getVolatile(String field) {
		return getField("volatile." + field);
	}

}
