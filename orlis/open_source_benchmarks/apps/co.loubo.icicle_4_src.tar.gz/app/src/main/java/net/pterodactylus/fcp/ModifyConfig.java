/*
 * jFCPlib - ModifyConfig.java - Copyright © 2008 David Roden
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
 * The “ModifyConfig” message is used to change the node’s configuration.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class ModifyConfig extends FcpMessage {

	/**
	 * Creates a new “ModifyConfig” message.
	 */
	public ModifyConfig() {
		super("ModifyConfig");
	}

	/**
	 * Sets the option with the given name to the given value.
	 *
	 * @param option
	 *            The name of the option
	 * @param value
	 *            The value of the option
	 */
	public void setOption(String option, String value) {
		if (option.indexOf('.') == -1) {
			throw new IllegalArgumentException("invalid option name");
		}
		setField(option, value);
	}

}
