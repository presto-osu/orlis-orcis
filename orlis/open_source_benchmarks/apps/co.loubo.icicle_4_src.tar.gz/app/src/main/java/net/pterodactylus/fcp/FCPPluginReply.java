/*
 * jFCPlib - FCPPluginReply.java - Copyright © 2008 David Roden
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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The “FCPPluginReply” is sent by a plugin as a response to a
 * {@link FCPPluginMessage} message.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class FCPPluginReply extends BaseMessage implements Identifiable {

	/** The payload input stream. */
	private final InputStream payloadInputStream;

	/**
	 * Creates a new “FCPPluginReply” message that wraps the received message.
	 *
	 * @param receivedMessage
	 *            The received message
	 * @param payloadInputStream
	 *            The optional input stream for the payload
	 */
	FCPPluginReply(FcpMessage receivedMessage, InputStream payloadInputStream) {
		super(receivedMessage);
		this.payloadInputStream = payloadInputStream;
	}

	/**
	 * Returns the name of the plugin.
	 *
	 * @return The name of the plugin
	 */
	public String getPluginName() {
		return getField("PluginName");
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
	 * Returns the length of the optional payload.
	 *
	 * @return The length of the payload, or <code>-1</code> if there is no
	 *         payload or the length could not be parsed
	 */
	public long getDataLength() {
		return FcpUtils.safeParseLong(getField("DataLength"));
	}

	/**
	 * Returns a reply from the plugin.
	 *
	 * @param key
	 *            The name of the reply
	 * @return The value of the reply
	 */
	public String getReply(String key) {
		return getField("Replies." + key);
	}

	/**
	 * Returns all replies from the plugin. The plugin sends replies as normal
	 * message fields prefixed by “Replies.”. The keys of the returned map do
	 * not contain this prefix!
	 *
	 * @return All replies from the plugin
	 */
	public Map<String, String> getReplies() {
		Map<String, String> fields = getFields();
		Map<String, String> replies = new HashMap<String, String>();
		for (Entry<String, String> field : fields.entrySet()) {
			if (field.getKey().startsWith("Replies.")) {
				replies.put(field.getKey().substring(8), field.getValue());
			}
		}
		return replies;
	}

	/**
	 * Returns the optional payload.
	 *
	 * @return The payload of the reply, or <code>null</code> if there is no
	 *         payload
	 */
	public InputStream getPayloadInputStream() {
		return payloadInputStream;
	}

}
