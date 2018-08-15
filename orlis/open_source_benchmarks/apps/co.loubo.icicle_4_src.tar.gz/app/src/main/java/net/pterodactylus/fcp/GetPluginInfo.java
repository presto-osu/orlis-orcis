/*
 * jFCPlib - GetPluginInfo.java - Copyright © 2008 David Roden
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
 * The “GetPluginInfo” message requests information about a plugin from the
 * node, which will response with a {@link PluginInfo} message.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class GetPluginInfo extends FcpMessage {

	/**
	 * Creates a new “GetPluginInfo” message.
	 *
	 * @param pluginName
	 *            The name of the plugin
	 * @param identifier
	 *            The identifier of the request
	 */
	public GetPluginInfo(String pluginName, String identifier) {
		super("GetPluginInfo");
		setField("PluginName", pluginName);
		setField("Identifier", identifier);
	}

	/**
	 * Sets whether detailed information about the plugin is wanted.
	 *
	 * @param detailed
	 *            <code>true</code> to request detailed information about the
	 *            plugin, <code>false</code> otherwise
	 */
	public void setDetailed(boolean detailed) {
		setField("Detailed", String.valueOf(detailed));
	}

}
