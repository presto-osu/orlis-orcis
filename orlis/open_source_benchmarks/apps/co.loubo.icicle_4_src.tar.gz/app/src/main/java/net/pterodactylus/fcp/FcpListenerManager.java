/*
 * jFCPlib - FcpListenerManager.java - Copyright © 2009 David Roden
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

import net.pterodactylus.util.event.AbstractListenerManager;

/**
 * Manages FCP listeners and event firing.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@pterodactylus.net&gt;
 */
public class FcpListenerManager extends AbstractListenerManager<FcpConnection, FcpListener> {

	/**
	 * Creates a new listener manager.
	 *
	 * @param fcpConnection
	 *            The source FCP connection
	 */
	public FcpListenerManager(FcpConnection fcpConnection) {
		super(fcpConnection);
	}

	/**
	 * Notifies listeners that a “NodeHello” message was received.
	 *
	 * @see FcpListener#receivedNodeHello(FcpConnection, NodeHello)
	 * @param nodeHello
	 *            The “NodeHello” message
	 */
	public void fireReceivedNodeHello(NodeHello nodeHello) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedNodeHello(getSource(), nodeHello);
		}
	}

	/**
	 * Notifies listeners that a “CloseConnectionDuplicateClientName” message
	 * was received.
	 *
	 * @see FcpListener#receivedCloseConnectionDuplicateClientName(FcpConnection,
	 *      CloseConnectionDuplicateClientName)
	 * @param closeConnectionDuplicateClientName
	 *            The “CloseConnectionDuplicateClientName” message
	 */
	public void fireReceivedCloseConnectionDuplicateClientName(CloseConnectionDuplicateClientName closeConnectionDuplicateClientName) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedCloseConnectionDuplicateClientName(getSource(), closeConnectionDuplicateClientName);
		}
	}

	/**
	 * Notifies listeners that a “SSKKeypair” message was received.
	 *
	 * @see FcpListener#receivedSSKKeypair(FcpConnection, SSKKeypair)
	 * @param sskKeypair
	 *            The “SSKKeypair” message
	 */
	public void fireReceivedSSKKeypair(SSKKeypair sskKeypair) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedSSKKeypair(getSource(), sskKeypair);
		}
	}

	/**
	 * Notifies listeners that a “Peer” message was received.
	 *
	 * @see FcpListener#receivedPeer(FcpConnection, Peer)
	 * @param peer
	 *            The “Peer” message
	 */
	public void fireReceivedPeer(Peer peer) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedPeer(getSource(), peer);
		}
	}

	/**
	 * Notifies all listeners that an “EndListPeers” message was received.
	 *
	 * @see FcpListener#receivedEndListPeers(FcpConnection, EndListPeers)
	 * @param endListPeers
	 *            The “EndListPeers” message
	 */
	public void fireReceivedEndListPeers(EndListPeers endListPeers) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedEndListPeers(getSource(), endListPeers);
		}
	}

	/**
	 * Notifies all listeners that a “PeerNote” message was received.
	 *
	 * @see FcpListener#receivedPeerNote(FcpConnection, PeerNote)
	 * @param peerNote
	 *            The “PeerNote” message
	 */
	public void fireReceivedPeerNote(PeerNote peerNote) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedPeerNote(getSource(), peerNote);
		}
	}

	/**
	 * Notifies all listeners that an “EndListPeerNotes” message was received.
	 *
	 * @see FcpListener#receivedEndListPeerNotes(FcpConnection,
	 *      EndListPeerNotes)
	 * @param endListPeerNotes
	 *            The “EndListPeerNotes” message
	 */
	public void fireReceivedEndListPeerNotes(EndListPeerNotes endListPeerNotes) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedEndListPeerNotes(getSource(), endListPeerNotes);
		}
	}

	/**
	 * Notifies all listeners that a “PeerRemoved” message was received.
	 *
	 * @see FcpListener#receivedPeerRemoved(FcpConnection, PeerRemoved)
	 * @param peerRemoved
	 *            The “PeerRemoved” message
	 */
	public void fireReceivedPeerRemoved(PeerRemoved peerRemoved) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedPeerRemoved(getSource(), peerRemoved);
		}
	}

	/**
	 * Notifies all listeners that a “NodeData” message was received.
	 *
	 * @see FcpListener#receivedNodeData(FcpConnection, NodeData)
	 * @param nodeData
	 *            The “NodeData” message
	 */
	public void fireReceivedNodeData(NodeData nodeData) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedNodeData(getSource(), nodeData);
		}
	}

	/**
	 * Notifies all listeners that a “TestDDAReply” message was received.
	 *
	 * @see FcpListener#receivedTestDDAReply(FcpConnection, TestDDAReply)
	 * @param testDDAReply
	 *            The “TestDDAReply” message
	 */
	public void fireReceivedTestDDAReply(TestDDAReply testDDAReply) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedTestDDAReply(getSource(), testDDAReply);
		}
	}

	/**
	 * Notifies all listeners that a “TestDDAComplete” message was received.
	 *
	 * @see FcpListener#receivedTestDDAComplete(FcpConnection, TestDDAComplete)
	 * @param testDDAComplete
	 *            The “TestDDAComplete” message
	 */
	public void fireReceivedTestDDAComplete(TestDDAComplete testDDAComplete) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedTestDDAComplete(getSource(), testDDAComplete);
		}
	}

	/**
	 * Notifies all listeners that a “PersistentGet” message was received.
	 *
	 * @see FcpListener#receivedPersistentGet(FcpConnection, PersistentGet)
	 * @param persistentGet
	 *            The “PersistentGet” message
	 */
	public void fireReceivedPersistentGet(PersistentGet persistentGet) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedPersistentGet(getSource(), persistentGet);
		}
	}

	/**
	 * Notifies all listeners that a “PersistentPut” message was received.
	 *
	 * @see FcpListener#receivedPersistentPut(FcpConnection, PersistentPut)
	 * @param persistentPut
	 *            The “PersistentPut” message
	 */
	public void fireReceivedPersistentPut(PersistentPut persistentPut) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedPersistentPut(getSource(), persistentPut);
		}
	}

	/**
	 * Notifies all listeners that a “EndListPersistentRequests” message was
	 * received.
	 *
	 * @see FcpListener#receivedEndListPersistentRequests(FcpConnection,
	 *      EndListPersistentRequests)
	 * @param endListPersistentRequests
	 *            The “EndListPersistentRequests” message
	 */
	public void fireReceivedEndListPersistentRequests(EndListPersistentRequests endListPersistentRequests) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedEndListPersistentRequests(getSource(), endListPersistentRequests);
		}
	}

	/**
	 * Notifies all listeners that a “URIGenerated” message was received.
	 *
	 * @see FcpListener#receivedURIGenerated(FcpConnection, URIGenerated)
	 * @param uriGenerated
	 *            The “URIGenerated” message
	 */
	public void fireReceivedURIGenerated(URIGenerated uriGenerated) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedURIGenerated(getSource(), uriGenerated);
		}
	}

	/**
	 * Notifies all listeners that a “DataFound” message was received.
	 *
	 * @see FcpListener#receivedDataFound(FcpConnection, DataFound)
	 * @param dataFound
	 *            The “DataFound” message
	 */
	public void fireReceivedDataFound(DataFound dataFound) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedDataFound(getSource(), dataFound);
		}
	}

	/**
	 * Notifies all listeners that an “AllData” message was received.
	 *
	 * @see FcpListener#receivedAllData(FcpConnection, AllData)
	 * @param allData
	 *            The “AllData” message
	 */
	public void fireReceivedAllData(AllData allData) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedAllData(getSource(), allData);
		}
	}

	/**
	 * Notifies all listeners that a “SimpleProgress” message was received.
	 *
	 * @see FcpListener#receivedSimpleProgress(FcpConnection, SimpleProgress)
	 * @param simpleProgress
	 *            The “SimpleProgress” message
	 */
	public void fireReceivedSimpleProgress(SimpleProgress simpleProgress) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedSimpleProgress(getSource(), simpleProgress);
		}
	}

	/**
	 * Notifies all listeners that a “StartedCompression” message was received.
	 *
	 * @see FcpListener#receivedStartedCompression(FcpConnection,
	 *      StartedCompression)
	 * @param startedCompression
	 *            The “StartedCompression” message
	 */
	public void fireReceivedStartedCompression(StartedCompression startedCompression) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedStartedCompression(getSource(), startedCompression);
		}
	}

	/**
	 * Notifies all listeners that a “FinishedCompression” message was
	 * received.
	 *
	 * @see FcpListener#receivedFinishedCompression(FcpConnection,
	 *      FinishedCompression)
	 * @param finishedCompression
	 *            The “FinishedCompression” message
	 */
	public void fireReceivedFinishedCompression(FinishedCompression finishedCompression) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedFinishedCompression(getSource(), finishedCompression);
		}
	}

	/**
	 * Notifies all listeners that an “UnknownPeerNoteType” message was
	 * received.
	 *
	 * @see FcpListener#receivedUnknownPeerNoteType(FcpConnection,
	 *      UnknownPeerNoteType)
	 * @param unknownPeerNoteType
	 *            The “UnknownPeerNoteType” message
	 */
	public void fireReceivedUnknownPeerNoteType(UnknownPeerNoteType unknownPeerNoteType) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedUnknownPeerNoteType(getSource(), unknownPeerNoteType);
		}
	}

	/**
	 * Notifies all listeners that an “UnknownNodeIdentifier” message was
	 * received.
	 *
	 * @see FcpListener#receivedUnknownNodeIdentifier(FcpConnection,
	 *      UnknownNodeIdentifier)
	 * @param unknownNodeIdentifier
	 *            The “UnknownNodeIdentifier” message
	 */
	public void fireReceivedUnknownNodeIdentifier(UnknownNodeIdentifier unknownNodeIdentifier) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedUnknownNodeIdentifier(getSource(), unknownNodeIdentifier);
		}
	}

	/**
	 * Notifies all listeners that a “ConfigData” message was received.
	 *
	 * @see FcpListener#receivedConfigData(FcpConnection, ConfigData)
	 * @param configData
	 *            The “ConfigData” message
	 */
	public void fireReceivedConfigData(ConfigData configData) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedConfigData(getSource(), configData);
		}
	}

	/**
	 * Notifies all listeners that a “GetFailed” message was received.
	 *
	 * @see FcpListener#receivedGetFailed(FcpConnection, GetFailed)
	 * @param getFailed
	 *            The “GetFailed” message
	 */
	public void fireReceivedGetFailed(GetFailed getFailed) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedGetFailed(getSource(), getFailed);
		}
	}

	/**
	 * Notifies all listeners that a “PutFailed” message was received.
	 *
	 * @see FcpListener#receivedPutFailed(FcpConnection, PutFailed)
	 * @param putFailed
	 *            The “PutFailed” message
	 */
	public void fireReceivedPutFailed(PutFailed putFailed) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedPutFailed(getSource(), putFailed);
		}
	}

	/**
	 * Notifies all listeners that an “IdentifierCollision” message was
	 * received.
	 *
	 * @see FcpListener#receivedIdentifierCollision(FcpConnection,
	 *      IdentifierCollision)
	 * @param identifierCollision
	 *            The “IdentifierCollision” message
	 */
	public void fireReceivedIdentifierCollision(IdentifierCollision identifierCollision) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedIdentifierCollision(getSource(), identifierCollision);
		}
	}

	/**
	 * Notifies all listeners that an “PersistentPutDir” message was received.
	 *
	 * @see FcpListener#receivedPersistentPutDir(FcpConnection,
	 *      PersistentPutDir)
	 * @param persistentPutDir
	 *            The “PersistentPutDir” message
	 */
	public void fireReceivedPersistentPutDir(PersistentPutDir persistentPutDir) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedPersistentPutDir(getSource(), persistentPutDir);
		}
	}

	/**
	 * Notifies all listeners that a “PersistentRequestRemoved” message was
	 * received.
	 *
	 * @see FcpListener#receivedPersistentRequestRemoved(FcpConnection,
	 *      PersistentRequestRemoved)
	 * @param persistentRequestRemoved
	 *            The “PersistentRequestRemoved” message
	 */
	public void fireReceivedPersistentRequestRemoved(PersistentRequestRemoved persistentRequestRemoved) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedPersistentRequestRemoved(getSource(), persistentRequestRemoved);
		}
	}

	/**
	 * Notifies all listeners that a “SubscribedUSKUpdate” message was
	 * received.
	 *
	 * @see FcpListener#receivedSubscribedUSKUpdate(FcpConnection,
	 *      SubscribedUSKUpdate)
	 * @param subscribedUSKUpdate
	 *            The “SubscribedUSKUpdate” message
	 */
	public void fireReceivedSubscribedUSKUpdate(SubscribedUSKUpdate subscribedUSKUpdate) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedSubscribedUSKUpdate(getSource(), subscribedUSKUpdate);
		}
	}

	/**
	 * Notifies all listeners that a “PluginInfo” message was received.
	 *
	 * @see FcpListener#receivedPluginInfo(FcpConnection, PluginInfo)
	 * @param pluginInfo
	 *            The “PluginInfo” message
	 */
	public void fireReceivedPluginInfo(PluginInfo pluginInfo) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedPluginInfo(getSource(), pluginInfo);
		}
	}

	/**
	 * Notifies all listeners that an “FCPPluginReply” message was received.
	 *
	 * @see FcpListener#receivedFCPPluginReply(FcpConnection, FCPPluginReply)
	 * @param fcpPluginReply
	 *            The “FCPPluginReply” message
	 */
	public void fireReceivedFCPPluginReply(FCPPluginReply fcpPluginReply) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedFCPPluginReply(getSource(), fcpPluginReply);
		}
	}

	/**
	 * Notifies all listeners that a “PersistentRequestModified” message was
	 * received.
	 *
	 * @see FcpListener#receivedPersistentRequestModified(FcpConnection,
	 *      PersistentRequestModified)
	 * @param persistentRequestModified
	 *            The “PersistentRequestModified” message
	 */
	public void fireReceivedPersistentRequestModified(PersistentRequestModified persistentRequestModified) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedPersistentRequestModified(getSource(), persistentRequestModified);
		}
	}

	/**
	 * Notifies all listeners that a “PutSuccessful” message was received.
	 *
	 * @see FcpListener#receivedPutSuccessful(FcpConnection, PutSuccessful)
	 * @param putSuccessful
	 *            The “PutSuccessful” message
	 */
	public void fireReceivedPutSuccessful(PutSuccessful putSuccessful) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedPutSuccessful(getSource(), putSuccessful);
		}
	}

	/**
	 * Notifies all listeners that a “PutFetchable” message was received.
	 *
	 * @see FcpListener#receivedPutFetchable(FcpConnection, PutFetchable)
	 * @param putFetchable
	 *            The “PutFetchable” message
	 */
	public void fireReceivedPutFetchable(PutFetchable putFetchable) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedPutFetchable(getSource(), putFetchable);
		}
	}

	/**
	 * Notifies all listeners that a “ProtocolError” message was received.
	 *
	 * @see FcpListener#receivedProtocolError(FcpConnection, ProtocolError)
	 * @param protocolError
	 *            The “ProtocolError” message
	 */
	public void fireReceivedProtocolError(ProtocolError protocolError) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedProtocolError(getSource(), protocolError);
		}
	}

	/**
	 * Notifies all listeners that a “SentFeed” message was received.
	 *
	 * @see FcpListener#receivedSentFeed(FcpConnection, SentFeed)
	 * @param sentFeed
	 *            The “SentFeed” message.
	 */
	public void fireSentFeed(SentFeed sentFeed) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedSentFeed(getSource(), sentFeed);
		}
	}

	/**
	 * Notifies all listeners that a “TextFeed” message was
	 * received.
	 *
	 * @see FcpListener#receivedBookmarkFeed(FcpConnection,
	 *      BookmarkFeed)
	 * @param receivedTextFeed
	 *            The “TextFeed” message
	 */
	public void fireReceivedTextFeed(TextFeed receivedTextFeed) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedTextFeed(getSource(), receivedTextFeed);
		}
	}

	/**
	 * Notifies all listeners that a “BookmarkFeed” message was
	 * received.
	 *
	 * @see FcpListener#receivedBookmarkFeed(FcpConnection,
	 *      BookmarkFeed)
	 * @param receivedBookmarkFeed
	 *            The “ReceivedBookmarkFeed” message
	 */
	public void fireReceivedBookmarkFeed(BookmarkFeed receivedBookmarkFeed) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedBookmarkFeed(getSource(), receivedBookmarkFeed);
		}
	}

	/**
	 * Notifies all listeners that a “URIFeed” message was
	 * received.
	 *
	 * @see FcpListener#receivedBookmarkFeed(FcpConnection,
	 *      BookmarkFeed)
	 * @param receivedURIFeed
	 *            The “URIFeed” message
	 */
	public void fireReceivedURIFeed(URIFeed receivedURIFeed) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedURIFeed(getSource(), receivedURIFeed);
		}
	}

	/**
	 * Notifies all listeners that a “Feed” message was
	 * received.
	 *
	 * @see FcpListener#receivedBookmarkFeed(FcpConnection,
	 *      BookmarkFeed)
	 * @param receivedFeed
	 *            The “RFeed” message
	 */
	public void fireReceivedFeed(Feed receivedFeed) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedFeed(getSource(), receivedFeed);
		}
	}

	/**
	 * Notifies all registered listeners that a message has been received.
	 *
	 * @see FcpListener#receivedMessage(FcpConnection, FcpMessage)
	 * @param fcpMessage
	 *            The message that was received
	 */
	public void fireMessageReceived(FcpMessage fcpMessage) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.receivedMessage(getSource(), fcpMessage);
		}
	}

	/**
	 * Notifies all listeners that the connection to the node was closed.
	 *
	 * @param throwable
	 *            The exception that caused the disconnect, or
	 *            <code>null</code> if there was no exception
	 * @see FcpListener#connectionClosed(FcpConnection, Throwable)
	 */
	public void fireConnectionClosed(Throwable throwable) {
		for (FcpListener fcpListener : getListeners()) {
			fcpListener.connectionClosed(getSource(), throwable);
		}
	}

}
