/*
 * jFCPlib - GetConfig.java - Copyright © 2008 David Roden
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
 * The “GetConfig” command tells the node to send its configuration to the
 * client.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class GetConfig extends FcpMessage {

	/**
	 * Creates a new “GetConfig” command.
	 */
	public GetConfig() {
		super("GetConfig");
	}

	/**
	 * Sets whether the {@link ConfigData} result message shall include the
	 * current values.
	 *
	 * @param withCurrent
	 *            <code>true</code> to include current values in the result,
	 *            <code>false</code> otherwise
	 */
	public void setWithCurrent(boolean withCurrent) {
		setField("WithCurrent", String.valueOf(withCurrent));
	}

	/**
	 * Sets whether the {@link ConfigData} result message shall include the
	 * short descriptions.
	 *
	 * @param withShortDescription
	 *            <code>true</code> to include the short descriptions in the
	 *            result, <code>false</code> otherwise
	 */
	public void setWithShortDescription(boolean withShortDescription) {
		setField("WithShortDescription", String.valueOf(withShortDescription));
	}

	/**
	 * Sets whether the {@link ConfigData} result message shall include the
	 * long descriptions.
	 *
	 * @param withLongDescription
	 *            <code>true</code> to include the long descriptions in the
	 *            result, <code>false</code> otherwise
	 */
	public void setWithLongDescription(boolean withLongDescription) {
		setField("WithLongDescription", String.valueOf(withLongDescription));
	}

	/**
	 * Sets whether the {@link ConfigData} result message shall include the
	 * data types.
	 *
	 * @param withDataTypes
	 *            <code>true</code> to include the data types in the result,
	 *            <code>false</code> otherwise
	 */
	public void setWithDataTypes(boolean withDataTypes) {
		setField("WithDataTypes", String.valueOf(withDataTypes));
	}

	/**
	 * Sets whether the {@link ConfigData} result message shall include the
	 * defaults.
	 *
	 * @param setWithDefaults
	 *            <code>true</code> to include the defaults in the result,
	 *            <code>false</code> otherwise
	 */
	public void setWithDefaults(boolean setWithDefaults) {
		setField("WithDefaults", String.valueOf(setWithDefaults));
	}

	/**
	 * Sets whether the {@link ConfigData} result message shall include the
	 * sort order.
	 *
	 * @param withSortOrder
	 *            <code>true</code> to include the sort order in the result,
	 *            <code>false</code> otherwise
	 */
	public void setWithSortOrder(boolean withSortOrder) {
		setField("WithSortOrder", String.valueOf(withSortOrder));
	}

	/**
	 * Sets whether the {@link ConfigData} result message shall include the
	 * expert flag.
	 *
	 * @param withExpertFlag
	 *            <code>true</code> to include the expert flag in the result,
	 *            <code>false</code> otherwise
	 */
	public void setWithExpertFlag(boolean withExpertFlag) {
		setField("WithExpertFlag", String.valueOf(withExpertFlag));
	}

	/**
	 * Sets whether the {@link ConfigData} result message shall include the
	 * force-write flag.
	 *
	 * @param withForceWriteFlag
	 *            <code>true</code> to include the force-write flag in the
	 *            result, <code>false</code> otherwise
	 */
	public void setWithForceWriteFlag(boolean withForceWriteFlag) {
		setField("WithForceWriteFlag", String.valueOf(withForceWriteFlag));
	}

}
