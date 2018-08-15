/*
 * jFCPlib - PluginMessage.java - Copyright © 2008 David Roden
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
 * An “CPPluginMessage” sends a message with custom parameters and (optional)
 * payload to a plugin.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class FCPPluginMessage extends FcpMessage {

	/**
	 * Creates a new “FCPPluginMessage” message for the given plugin.
	 *
	 * @param pluginClass
	 *            The name of the plugin class
	 */
	public FCPPluginMessage(String pluginClass) {
		super("FCPPluginMessage");
		setField("PluginName", pluginClass);
	}

	/**
	 * Sets the identifier of the request. Though this is still optional you
	 * are encouraged to include it because the plugin might reply in random
	 * order to requests.
	 *
	 * @param identifier
	 *            The identifier of the request
	 */
	public void setIdentifier(String identifier) {
		setField("Identifier", identifier);
	}

	/**
	 * Sets a custom parameter for the plugin.
	 *
	 * @param key
	 *            The key of the parameter
	 * @param value
	 *            The value of the parameter
	 */
	public void setParameter(String key, String value) {
		setField("Param." + key, value);
	}

	/**
	 * Sets the length of data of the optional payload. If you call this method
	 * you also have to call
	 * {@link #setPayloadInputStream(java.io.InputStream)} !
	 *
	 * @param dataLength
	 *            The length of data in the payload input stream
	 */
	public void setDataLength(long dataLength) {
		setField("DataLength", String.valueOf(dataLength));
	}

}
