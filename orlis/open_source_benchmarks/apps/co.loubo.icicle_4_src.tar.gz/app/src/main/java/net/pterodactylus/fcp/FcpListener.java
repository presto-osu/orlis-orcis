/*
 * jFCPlib - FpcListener.java - Copyright © 2008 David Roden
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

import java.util.EventListener;

/**
 * Interface for objects that want to be notified on certain FCP events.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public interface FcpListener extends EventListener {

	/**
	 * Notifies a listener that a “NodeHello” message was received.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param nodeHello
	 *            The “NodeHello” message
	 */
	public void receivedNodeHello(FcpConnection fcpConnection, NodeHello nodeHello);

	/**
	 * Notifies a listener that a “CloseConnectionDuplicateClientName” message
	 * was received.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param closeConnectionDuplicateClientName
	 *            The “CloseConnectionDuplicateClientName” message
	 */
	public void receivedCloseConnectionDuplicateClientName(FcpConnection fcpConnection, CloseConnectionDuplicateClientName closeConnectionDuplicateClientName);

	/**
	 * Notifies a listener that a “SSKKeypair” message was received.
	 *
	 * @param fcpConnection
	 *            The connection that received themessage
	 * @param sskKeypair
	 *            The “SSKKeypair” message
	 */
	public void receivedSSKKeypair(FcpConnection fcpConnection, SSKKeypair sskKeypair);

	/**
	 * Notifies a listener that a “Peer” message was received.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param peer
	 *            The “Peer” message
	 */
	public void receivedPeer(FcpConnection fcpConnection, Peer peer);

	/**
	 * Notifies a listener that an “EndListPeers” message was received.
	 *
	 * @param fcpConnection
	 *            The connection that recevied the message
	 * @param endListPeers
	 *            The “EndListPeers” message
	 */
	public void receivedEndListPeers(FcpConnection fcpConnection, EndListPeers endListPeers);

	/**
	 * Notifies a listener that a “PeerNote” message was received.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param peerNote
	 *            The “PeerNote” message
	 */
	public void receivedPeerNote(FcpConnection fcpConnection, PeerNote peerNote);

	/**
	 * Notifies a listener that an “EndListPeerNotes” message was received.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param endListPeerNotes
	 *            The “EndListPeerNotes” message
	 */
	public void receivedEndListPeerNotes(FcpConnection fcpConnection, EndListPeerNotes endListPeerNotes);

	/**
	 * Notifies a listener that a “PeerRemoved” message was received.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param peerRemoved
	 *            The “PeerRemoved” message
	 */
	public void receivedPeerRemoved(FcpConnection fcpConnection, PeerRemoved peerRemoved);

	/**
	 * Notifies a listener that a “NodeData” message was received.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param nodeData
	 *            The “NodeData” message
	 */
	public void receivedNodeData(FcpConnection fcpConnection, NodeData nodeData);

	/**
	 * Notifies a listener that a “TestDDAReply” message was received.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param testDDAReply
	 *            The “TestDDAReply” message
	 */
	public void receivedTestDDAReply(FcpConnection fcpConnection, TestDDAReply testDDAReply);

	/**
	 * Notifies a listener that a “TestDDAComplete” was received.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param testDDAComplete
	 *            The “TestDDAComplete” message
	 */
	public void receivedTestDDAComplete(FcpConnection fcpConnection, TestDDAComplete testDDAComplete);

	/**
	 * Notifies a listener that a “PersistentGet” was received.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param persistentGet
	 *            The “PersistentGet” message
	 */
	public void receivedPersistentGet(FcpConnection fcpConnection, PersistentGet persistentGet);

	/**
	 * Notifies a listener that a “PersistentPut” was received.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param persistentPut
	 *            The “PersistentPut” message
	 */
	public void receivedPersistentPut(FcpConnection fcpConnection, PersistentPut persistentPut);

	/**
	 * Notifies a listener that a “EndListPersistentRequests” was received.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param endListPersistentRequests
	 *            The “EndListPersistentRequests” message
	 */
	public void receivedEndListPersistentRequests(FcpConnection fcpConnection, EndListPersistentRequests endListPersistentRequests);

	/**
	 * Notifies a listener that a “URIGenerated” was received.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param uriGenerated
	 *            The “URIGenerated” message
	 */
	public void receivedURIGenerated(FcpConnection fcpConnection, URIGenerated uriGenerated);

	/**
	 * Notifies a listener that a “DataFound” was received.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param dataFound
	 *            The “DataFound” message
	 */
	public void receivedDataFound(FcpConnection fcpConnection, DataFound dataFound);

	/**
	 * Notifies a listener that an “AllData” was received.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param allData
	 *            The “AllData” message
	 */
	public void receivedAllData(FcpConnection fcpConnection, AllData allData);

	/**
	 * Notifies a listener that a “SimpleProgress” was received.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param simpleProgress
	 *            The “SimpleProgress” message
	 */
	public void receivedSimpleProgress(FcpConnection fcpConnection, SimpleProgress simpleProgress);

	/**
	 * Notifies a listener that a “StartedCompression” was received.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param startedCompression
	 *            The “StartedCompression” message
	 */
	public void receivedStartedCompression(FcpConnection fcpConnection, StartedCompression startedCompression);

	/**
	 * Notifies a listener that a “FinishedCompression” was received.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param finishedCompression
	 *            The “FinishedCompression” message
	 */
	public void receivedFinishedCompression(FcpConnection fcpConnection, FinishedCompression finishedCompression);

	/**
	 * Notifies a listener that an “UnknownPeerNoteType” was received.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param unknownPeerNoteType
	 *            The “UnknownPeerNoteType” message
	 */
	public void receivedUnknownPeerNoteType(FcpConnection fcpConnection, UnknownPeerNoteType unknownPeerNoteType);

	/**
	 * Notifies a listener that a “UnknownNodeIdentifier” message was received.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param unknownNodeIdentifier
	 *            The “UnknownNodeIdentifier” message
	 */
	public void receivedUnknownNodeIdentifier(FcpConnection fcpConnection, UnknownNodeIdentifier unknownNodeIdentifier);

	/**
	 * Notifies a listener that a “ConfigData” message was received.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param configData
	 *            The “ConfigData” message
	 */
	public void receivedConfigData(FcpConnection fcpConnection, ConfigData configData);

	/**
	 * Notifies a listener that a “GetFailed” message was recevied.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param getFailed
	 *            The “GetFailed” message
	 */
	public void receivedGetFailed(FcpConnection fcpConnection, GetFailed getFailed);

	/**
	 * Notifies a listener that a “PutFailed” message was received.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param putFailed
	 *            The “PutFailed” message
	 */
	public void receivedPutFailed(FcpConnection fcpConnection, PutFailed putFailed);

	/**
	 * Notifies a listener that an “IdentifierCollision” message was receied.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param identifierCollision
	 *            The “IdentifierCollision” message
	 */
	public void receivedIdentifierCollision(FcpConnection fcpConnection, IdentifierCollision identifierCollision);

	/**
	 * Notifies a listener that a “PersistentPutDir” message was received.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param persistentPutDir
	 *            The “PersistentPutDir” message
	 */
	public void receivedPersistentPutDir(FcpConnection fcpConnection, PersistentPutDir persistentPutDir);

	/**
	 * Notifies a listener that a “PersistentRequestRemoved” message was
	 * received.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param persistentRequestRemoved
	 *            The “PersistentRequestRemoved” message
	 */
	public void receivedPersistentRequestRemoved(FcpConnection fcpConnection, PersistentRequestRemoved persistentRequestRemoved);

	/**
	 * Notifies a listener that a “SubscribedUSKUpdate” message was received.
	 *
	 * @param fcpConnection
	 *            The connection that recevied the message
	 * @param subscribedUSKUpdate
	 *            The “SubscribedUSKUpdate” message
	 */
	public void receivedSubscribedUSKUpdate(FcpConnection fcpConnection, SubscribedUSKUpdate subscribedUSKUpdate);

	/**
	 * Notifies a listener that a “PluginInfo” message was received.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param pluginInfo
	 *            The “PluginInfo” message
	 */
	public void receivedPluginInfo(FcpConnection fcpConnection, PluginInfo pluginInfo);

	/**
	 * Notifies a listener that an “FCPPluginReply“ message was received.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param fcpPluginReply
	 *            The “FCPPluginReply” message
	 */
	public void receivedFCPPluginReply(FcpConnection fcpConnection, FCPPluginReply fcpPluginReply);

	/**
	 * Notifies a listener that a “PersistentRequestModified” message was
	 * received.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param persistentRequestModified
	 *            The “PersistentRequestModified” message
	 */
	public void receivedPersistentRequestModified(FcpConnection fcpConnection, PersistentRequestModified persistentRequestModified);

	/**
	 * Notifies a listener that a “PutSuccessful” message was received.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param putSuccessful
	 *            The “PutSuccessful” message
	 */
	public void receivedPutSuccessful(FcpConnection fcpConnection, PutSuccessful putSuccessful);

	/**
	 * Notifies a listener that a “PutFetchable” message was received.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param putFetchable
	 *            The “PutFetchable” message
	 */
	public void receivedPutFetchable(FcpConnection fcpConnection, PutFetchable putFetchable);

	/**
	 * Notifies a listener that a feed was sent to a peer.
	 *
	 * @param source
	 *            The connection that received the message
	 * @param sentFeed
	 *            The “SentFeed” message
	 */
	public void receivedSentFeed(FcpConnection source, SentFeed sentFeed);

	/**
	 * Notifies a listener that a Text Message was received.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param receivedTextFeed
	 *            The “TextFeed” message
	 */
	public void receivedTextFeed(FcpConnection fcpConnection, TextFeed receivedTextFeed);

	/**
	 * Notifies a listener that a bookmark was updated.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param receivedBookmarkFeed
	 *            The “BookmarkFeed” message
	 */
	public void receivedBookmarkFeed(FcpConnection fcpConnection, BookmarkFeed receivedBookmarkFeed);

	/**
	 * Notifies a listener that a link was received.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param receivedURIFeed
	 *            The “URIFeed” message
	 */
	public void receivedURIFeed(FcpConnection fcpConnection, URIFeed receivedURIFeed);

	/**
	 * Notifies a listener that a message was received from the node.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param receivedFeed
	 *            The “Feed” message
	 */
	public void receivedFeed(FcpConnection fcpConnection, Feed receivedFeed);

	/**
	 * Notifies a listener that a “ProtocolError” was received.
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param protocolError
	 *            The “ProtocolError” message
	 */
	public void receivedProtocolError(FcpConnection fcpConnection, ProtocolError protocolError);

	/**
	 * Notifies a listener that a message has been received. This method is
	 * only called if {@link FcpConnection#handleMessage(FcpMessage)} does not
	 * recognize the message. Should that ever happen, please file a bug
	 * report!
	 *
	 * @param fcpConnection
	 *            The connection that received the message
	 * @param fcpMessage
	 *            The message that was received
	 */
	public void receivedMessage(FcpConnection fcpConnection, FcpMessage fcpMessage);

	/**
	 * Notifies a listener that a connection was closed. A closed connection
	 * can be reestablished by calling {@link FcpConnection#connect()} on the
	 * same object again.
	 *
	 * @param fcpConnection
	 *            The connection that was closed.
	 * @param throwable
	 *            The exception that caused the disconnect, or
	 *            <code>null</code> if there was no exception
	 */
	public void connectionClosed(FcpConnection fcpConnection, Throwable throwable);

}
