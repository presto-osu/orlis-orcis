/*
 * jFCPlib - ConfigData.java - Copyright © 2008 David Roden
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
 * A “ConfigData” message contains various aspects of the node’s configuration.
 *
 * @see GetConfig
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class ConfigData extends BaseMessage {

	/**
	 * Creates a new “ConfigData” message that wraps the received message.
	 *
	 * @param receivedMessage
	 *            The received message
	 */
	ConfigData(FcpMessage receivedMessage) {
		super(receivedMessage);
	}

	/**
	 * Returns the current value of the given option.
	 *
	 * @param option
	 *            The name of the option
	 * @return The current value of the option
	 */
	public String getCurrent(String option) {
		return getField("current." + option);
	}

	/**
	 * Returns the short description of the given option.
	 *
	 * @param option
	 *            The name of the option
	 * @return The short description of the option
	 */
	public String getShortDescription(String option) {
		return getField("shortDescription." + option);
	}

	/**
	 * Returns the long description of the given option.
	 *
	 * @param option
	 *            The name of the option
	 * @return The long description of the option
	 */
	public String getLongDescription(String option) {
		return getField("longDescription." + option);
	}

	/**
	 * Returns the data type of the given option.
	 *
	 * @param option
	 *            The name of the option
	 * @return The data type of the option
	 */
	public String getDataType(String option) {
		return getField("dataType." + option);
	}

	/**
	 * Returns the default value of the given option.
	 *
	 * @param option
	 *            The name of the option
	 * @return The default value of the option
	 */
	public String getDefault(String option) {
		return getField("default." + option);
	}

	/**
	 * Returns the sort order of the given option.
	 *
	 * @param option
	 *            The name of the option
	 * @return The sort order of the option, or <code>-1</code> if the sort
	 *         order could not be parsed
	 */
	public int getSortOrder(String option) {
		return FcpUtils.safeParseInt(getField("sortOrder." + option));
	}

	/**
	 * Returns the expert flag of the given option.
	 *
	 * @param option
	 *            The name of the option
	 * @return The expert flag of the option
	 */
	public boolean getExpertFlag(String option) {
		return Boolean.valueOf(getField("expertFlag." + option));
	}

	/**
	 * Returns the force-write flag of the given option
	 *
	 * @param option
	 *            The name of the option
	 * @return The force-write flag of the given option
	 */
	public boolean getForceWriteFlag(String option) {
		return Boolean.valueOf(getField("forceWriteFlag." + option));
	}

}
