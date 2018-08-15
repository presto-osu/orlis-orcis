/*
 * jFCPlib - FcpAdapter.java - Copyright © 2008 David Roden
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
 * Adapter for {@link FcpListener}.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class FcpAdapter implements FcpListener {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedNodeHello(FcpConnection fcpConnection, NodeHello nodeHello) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedCloseConnectionDuplicateClientName(FcpConnection fcpConnection, CloseConnectionDuplicateClientName closeConnectionDuplicateClientName) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedSSKKeypair(FcpConnection fcpConnection, SSKKeypair sskKeypair) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedPeer(FcpConnection fcpConnection, Peer peer) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedEndListPeers(FcpConnection fcpConnection, EndListPeers endListPeers) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedPeerNote(FcpConnection fcpConnection, PeerNote peerNote) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedEndListPeerNotes(FcpConnection fcpConnection, EndListPeerNotes endListPeerNotes) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedPeerRemoved(FcpConnection fcpConnection, PeerRemoved peerRemoved) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see FcpListener#receivedNodeData(FcpConnection, NodeData)
	 */
	@Override
	public void receivedNodeData(FcpConnection fcpConnection, NodeData nodeData) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see FcpListener#receivedTestDDAReply(FcpConnection, TestDDAReply)
	 */
	@Override
	public void receivedTestDDAReply(FcpConnection fcpConnection, TestDDAReply testDDAReply) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedTestDDAComplete(FcpConnection fcpConnection, TestDDAComplete testDDAComplete) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedPersistentGet(FcpConnection fcpConnection, PersistentGet persistentGet) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedPersistentPut(FcpConnection fcpConnection, PersistentPut persistentPut) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedEndListPersistentRequests(FcpConnection fcpConnection, EndListPersistentRequests endListPersistentRequests) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedURIGenerated(FcpConnection fcpConnection, URIGenerated uriGenerated) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedDataFound(FcpConnection fcpConnection, DataFound dataFound) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedAllData(FcpConnection fcpConnection, AllData allData) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedSimpleProgress(FcpConnection fcpConnection, SimpleProgress simpleProgress) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedStartedCompression(FcpConnection fcpConnection, StartedCompression startedCompression) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedFinishedCompression(FcpConnection fcpConnection, FinishedCompression finishedCompression) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedUnknownPeerNoteType(FcpConnection fcpConnection, UnknownPeerNoteType unknownPeerNoteType) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedUnknownNodeIdentifier(FcpConnection fcpConnection, UnknownNodeIdentifier unknownNodeIdentifier) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedConfigData(FcpConnection fcpConnection, ConfigData configData) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedGetFailed(FcpConnection fcpConnection, GetFailed getFailed) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedPutFailed(FcpConnection fcpConnection, PutFailed putFailed) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedIdentifierCollision(FcpConnection fcpConnection, IdentifierCollision identifierCollision) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedPersistentPutDir(FcpConnection fcpConnection, PersistentPutDir persistentPutDir) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedPersistentRequestRemoved(FcpConnection fcpConnection, PersistentRequestRemoved persistentRequestRemoved) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedSubscribedUSKUpdate(FcpConnection fcpConnection, SubscribedUSKUpdate subscribedUSKUpdate) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedPluginInfo(FcpConnection fcpConnection, PluginInfo pluginInfo) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedFCPPluginReply(FcpConnection fcpConnection, FCPPluginReply fcpPluginReply) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedPersistentRequestModified(FcpConnection fcpConnection, PersistentRequestModified persistentRequestModified) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedPutSuccessful(FcpConnection fcpConnection, PutSuccessful putSuccessful) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedPutFetchable(FcpConnection fcpConnection, PutFetchable putFetchable) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedProtocolError(FcpConnection fcpConnection, ProtocolError protocolError) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedSentFeed(FcpConnection source, SentFeed sentFeed) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedTextFeed(FcpConnection fcpConnection, TextFeed receivedTextFeed) {
		/* empty. */
	}

	@Override
	public void receivedBookmarkFeed(FcpConnection fcpConnection, BookmarkFeed receivedBookmarkFeed) {
		/* empty. */
	}

	@Override
	public void receivedURIFeed(FcpConnection fcpConnection, URIFeed receivedUriFeed) {
		/* empty. */
	}

	@Override
	public void receivedFeed(FcpConnection fcpConnection, Feed receivedFeed) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedMessage(FcpConnection fcpConnection, FcpMessage fcpMessage) {
		/* empty. */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connectionClosed(FcpConnection fcpConnection, Throwable throwable) {
		/* empty. */
	}

}
