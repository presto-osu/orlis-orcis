/*
 * jFCPlib - SentFeed.java - Copyright © 2009 David Roden
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
 * The “SentFeed” message signals that a feed was successfully sent to a peer.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class SentFeed extends BaseMessage implements Identifiable {

	/**
	 * Creates a new “SentFeed” message from the given FCP message.
	 *
	 * @param fcpMessage
	 *            The FCP message containing the “SentFeed” message
	 */
	public SentFeed(FcpMessage fcpMessage) {
		super(fcpMessage);
	}

	/**
	 * Returns the identifier of the sent feed. The identifier of this message
	 * matches the identifier that was given when a {@link SendBookmarkFeed},
	 * {@link SendDownloadFeed}, or {@link SendTextFeed} command was created.
	 *
	 * @return The send feed’s identifier
	 */
	@Override
	public String getIdentifier() {
		return getField("Identifier");
	}

	/**
	 * Returns the node status of the peer. The node status is definied in
	 * {@code freenet.node.PeerManager}.
	 * <p>
	 * <ol start="1">
	 * <li>Connected</li>
	 * <li>Backed off</li>
	 * <li>Version too new</li>
	 * <li>Version too old</li>
	 * <li>Disconnected</li>
	 * <li>Never connected</li>
	 * <li>Disabled</li>
	 * <li>Bursting</li>
	 * <li>Listening</li>
	 * <li>Listening only</li>
	 * <li>Clock problem</li>
	 * <li>Connection error</li>
	 * <li>Disconnecting</li>
	 * <li>Routing disabled</li>
	 * </ol>
	 *
	 * @return The node’s status
	 */
	public int getNodeStatus() {
		return FcpUtils.safeParseInt(getField("NodeStatus"));
	}

}
