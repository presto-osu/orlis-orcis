/*
 * jFCPlib - Priority.java - Copyright © 2008 David Roden
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
 * The priority classes used by the Freenet node.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public enum Priority {

	/** Maximum priority. */
	maximum,

	/** Priority for interactive request, i.e. FProxy. */
	interactive,

	/** Priority for splitfile manifests. */
	immediateSplitfile,

	/** Priority for USK searches. */
	update,

	/** Priority for splitfile blocks. */
	bulkSplitfile,

	/** Priority for prefetching blocks. */
	prefetch,

	/** Minimum priority. */
	minimum,

	/** Unknown priority. */
	unknown;

	/**
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		return String.valueOf(ordinal());
	}

}
