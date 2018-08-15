/*
 * jFCPlib - Verbosity.java - Copyright © 2008 David Roden
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
 * Convenicence class for verbosity handling. This might come in handy with the
 * {@link ClientPut} and {@link ClientGet} requests. The verbosity is a
 * bit-mask that can be composed of several bits. {@link #PROGRESS} and
 * {@link #COMPRESSION} are single bits in that mask and can be combined into a
 * new verbosity using {@link #add(Verbosity)}.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class Verbosity {

	/** Constant for no verbosity at all. */
	public static final Verbosity NONE = new Verbosity(0);

	/** Constant for progress message verbosity. */
	public static final Verbosity PROGRESS = new Verbosity(1);

	/** Constant for compression message verbosity. */
	public static final Verbosity COMPRESSION = new Verbosity(512);

	/** Constant for all events. */
	public static final Verbosity ALL = new Verbosity(-1);

	/** The verbosity level. */
	private final int level;

	/**
	 * Creates a new verbosity with the given level.
	 *
	 * @param level
	 *            The verbosity level
	 */
	private Verbosity(int level) {
		this.level = level;
	}

	/**
	 * Adds the given verbosity to this verbosity and returns a verbosity with
	 * the new value. The value of this verbosity is not changed.
	 *
	 * @param verbosity
	 *            The verbosity to add to this verbosity
	 * @return The verbosity with the new level.
	 */
	public Verbosity add(Verbosity verbosity) {
		return new Verbosity(level | verbosity.level);
	}

	/**
	 * Checks whether this Verbosity contains all bits of the given Verbosity.
	 *
	 * @param verbosity
	 *            The verbosity to check for in this Verbosity
	 * @return <code>true</code> if and only if all set bits in the given
	 *         Verbosity are also set in this Verbosity
	 */
	public boolean contains(Verbosity verbosity) {
		return (level & verbosity.level) == verbosity.level;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.valueOf(level);
	}

	/**
	 * Parses the given string and creates a Verbosity with the given level.
	 *
	 * @param s
	 *            The string to parse
	 * @return The parsed verbosity, or {@link #NONE} if the string could not
	 *         be parsed
	 */
	public static Verbosity valueOf(String s) {
		try {
			return new Verbosity(Integer.valueOf(s));
		} catch (NumberFormatException nfe1) {
			return NONE;
		}
	}

}
