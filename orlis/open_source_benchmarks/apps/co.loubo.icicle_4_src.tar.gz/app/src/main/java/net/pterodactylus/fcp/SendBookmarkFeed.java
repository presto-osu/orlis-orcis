/*
 * jFCPlib - SendBookmarkFeed.java - Copyright © 2009 David Roden
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
 * The “SendBookmarkFeed” command sends a bookmark to a peer.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class SendBookmarkFeed extends AbstractSendFeedMessage {

	/**
	 * Creates a new “SendBookmarkFeed” command.
	 *
	 * @param identifier
	 *            The identifier of the request
	 * @param nodeIdentifier
	 *            The identifier of the peer node
	 * @param name
	 *            The name of the bookmark
	 * @param uri
	 *            The URI of the bookmark
	 * @param description
	 *            The description of the bookmark (may be {@code null})
	 * @param hasActiveLink
	 *            {@code true} if the bookmark has an activelink image,
	 *            {@code false} otherwise
	 */
	public SendBookmarkFeed(String identifier, String nodeIdentifier, String name, String uri, String description, boolean hasActiveLink) {
		super("SendBookmark", identifier, nodeIdentifier);
		setField("Name", name);
		setField("URI", uri);
		setField("Description", description);
		setField("HasActiveLink", String.valueOf(hasActiveLink));
	}

}
