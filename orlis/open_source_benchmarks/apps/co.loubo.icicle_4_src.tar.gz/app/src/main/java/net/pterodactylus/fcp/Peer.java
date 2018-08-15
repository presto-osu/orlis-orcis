/*
 * jFCPlib - Peer.java - Copyright © 2008 David Roden
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The “Peer” reply by the node contains information about a peer.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class Peer extends BaseMessage implements Identifiable {

	/**
	 * Creates a new “Peer” reply from the received message.
	 *
	 * @param receivedMessage
	 *            The received message
	 */
	Peer(FcpMessage receivedMessage) {
		super(receivedMessage);
	}

	/**
	 * Returns a collection of fields as a node reference.
	 *
	 * @return The node reference contained within this message
	 */
	public NodeRef getNodeRef() {
		NodeRef nodeRef = new NodeRef();
		nodeRef.setARK(getARK());
		nodeRef.setDSAGroup(getDSAGroup());
		nodeRef.setDSAPublicKey(getDSAPublicKey());
		nodeRef.setIdentity(getIdentity());
		nodeRef.setLastGoodVersion(getLastGoodVersion());
		nodeRef.setLocation(getLocation());
		nodeRef.setName(getMyName());
		nodeRef.setNegotiationTypes(getNegotiationTypes());
		nodeRef.setOpennet(isOpennet());
		nodeRef.setPhysicalUDP(getPhysicalUDP());
		nodeRef.setVersion(getVersion());
		return nodeRef;
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
	 * Returns the “physical.udp” line from the message. It contains all IP
	 * addresses and port numbers of the peer.
	 *
	 * @return The IP addresses and port numbers of the peer
	 */
	public String getPhysicalUDP() {
		return getField("physical.udp");
	}

	/**
	 * Returns whether the listed peer is an opennet peer.
	 *
	 * @return <code>true</code> if the peer is an opennet peer,
	 *         <code>false</code> if the peer is a darknet peer
	 */
	public boolean isOpennet() {
		return Boolean.valueOf(getField("opennet"));
	}

	/**
	 * Returns whether this peer is a seed.
	 *
	 * @return <code>true</code> if the peer is a seed, <code>false</code>
	 *         otherwise
	 */
	public boolean isSeed() {
		return Boolean.valueOf(getField("seed"));
	}

	/**
	 * Returns the “y” part of the peer’s public DSA key.
	 *
	 * @return The public DSA key
	 */
	public String getDSAPublicKey() {
		return getField("dsaPubKey.y");
	}

	/**
	 * Returns the DSA group of the peer.
	 *
	 * @return The DSA group of the peer
	 */
	public DSAGroup getDSAGroup() {
		return new DSAGroup(getField("dsaGroup.g"), getField("dsaGroup.p"), getField("dsaGroup.q"));
	}

	/**
	 * Returns the last good version of the peer, i.e. the oldest version the
	 * peer will connect to.
	 *
	 * @return The last good version of the peer
	 */
	public Version getLastGoodVersion() {
		return new Version(getField("lastGoodVersion"));
	}

	/**
	 * Returns the ARK of the peer.
	 *
	 * @return The ARK of the peer
	 */
	public ARK getARK() {
		return new ARK(getField("ark.pubURI"), getField("ark.number"));
	}

	/**
	 * Returns the identity of the peer.
	 *
	 * @return The identity of the peer
	 */
	public String getIdentity() {
		return getField("identity");
	}

	/**
	 * Returns the name of the peer. If the peer is not a darknet peer it will
	 * have no name.
	 *
	 * @return The name of the peer, or <code>null</code> if the peer is an
	 *         opennet peer
	 */
	public String getMyName() {
		return getField("myName");
	}

	/**
	 * Returns the location of the peer.
	 *
	 * @return The location of the peer
	 * @throws NumberFormatException
	 *             if the field can not be parsed
	 */
	public double getLocation() throws NumberFormatException {
		return Double.valueOf(getField("location"));
	}

	/**
	 * Returns whether the peer is a testnet node.
	 *
	 * @return <code>true</code> if the peer is a testnet node,
	 *         <code>false</code> otherwise
	 */
	public boolean isTestnet() {
		return Boolean.valueOf("testnet");
	}

	/**
	 * Returns the version of the peer.
	 *
	 * @return The version of the peer
	 */
	public Version getVersion() {
		return new Version(getField("version"));
	}

	/**
	 * Returns the negotiation types the peer supports.
	 *
	 * @return The supported negotiation types
	 */
	public int[] getNegotiationTypes() {
		return FcpUtils.decodeMultiIntegerField(getField("auth.negTypes"));
	}

	/**
	 * Returns all volatile fields from the message.
	 *
	 * @return All volatile files
	 */
	public Map<String, String> getVolatileFields() {
		Map<String, String> volatileFields = new HashMap<String, String>();
		for (Entry<String, String> field : getFields().entrySet()) {
			if (field.getKey().startsWith("volatile.")) {
				volatileFields.put(field.getKey(), field.getValue());
			}
		}
		return Collections.unmodifiableMap(volatileFields);
	}

	/**
	 * Returns one of the volatile fields from the message. The given field
	 * name is prepended with “volatile.” so if you want to get the value of
	 * the field with the name “volatile.status” you only need to specify
	 * “status”.
	 *
	 * @param field
	 *            The name of the field
	 * @return The value of the field, or <code>null</code> if there is no such
	 *         field
	 */
	public String getVolatile(String field) {
		return getField("volatile." + field);
	}

	/**
	 * Returns all metadata fields from the message.
	 *
	 * @return All volatile files
	 */
	public Map<String, String> getMetadataFields() {
		Map<String, String> metadataFields = new HashMap<String, String>();
		for (Entry<String, String> field : getFields().entrySet()) {
			if (field.getKey().startsWith("metadata.")) {
				metadataFields.put(field.getKey(), field.getValue());
			}
		}
		return Collections.unmodifiableMap(metadataFields);
	}

	/**
	 * Returns one of the metadata fields from the message. The given field
	 * name is prepended with “metadata.” so if you want to get the value of
	 * the field with the name “metadata.timeLastRoutable” you only need to
	 * specify “timeLastRoutable”.
	 *
	 * @param field
	 *            The name of the field
	 * @return The value of the field, or <code>null</code> if there is no such
	 *         field
	 */
	public String getMetadata(String field) {
		return getField("metadata." + field);
	}

}
