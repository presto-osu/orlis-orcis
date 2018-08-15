/*
 * jFCPlib - SubscribedUSKUpdate.java - Copyright © 2008 David Roden
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
 * A “SubscribedUSKUpdate” message is sent each time a new edition of a USK
 * that was previously subscribed to with {@link SubscribeUSK} was found. Note
 * that if the new edition that was found is several editions ahead of the
 * currently last known edition, you will received a SubscribedUSKUpdate for
 * each edition inbetween as welL!
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class SubscribedUSKUpdate extends BaseMessage implements Identifiable {

	/**
	 * Creates a new “SubscribedUSKUpdate” message that wraps the received
	 * message.
	 *
	 * @param receivedMessage
	 *            The received message
	 */
	SubscribedUSKUpdate(FcpMessage receivedMessage) {
		super(receivedMessage);
	}

	/**
	 * Returns the identifier of the subscription.
	 *
	 * @return The identifier of the subscription
	 */
	@Override
	public String getIdentifier() {
		return getField("Identifier");
	}

	/**
	 * Returns the new edition that was found.
	 *
	 * @return The new edition
	 */
	public int getEdition() {
		return FcpUtils.safeParseInt(getField("Edition"));
	}

	/**
	 * Returns the complete URI, including the new edition.
	 *
	 * @return The complete URI
	 */
	public String getURI() {
		return getField("URI");
	}

}
