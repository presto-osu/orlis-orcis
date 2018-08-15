/*
 * jFCPlib - DSAGroup.java - Copyright © 2008 David Roden
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

import java.security.interfaces.DSAParams;

/**
 * Container for the DSA group of a peer. A DSA group consists of a base
 * (called “g”), a prime (called “p”) and a subprime (called “q”).
 *
 * @see DSAParams
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class DSAGroup {

	/** The base of the DSA group. */
	private final String base;

	/** The prime of the DSA group. */
	private final String prime;

	/** The subprime of the DSA group. */
	private final String subprime;

	/**
	 * Creates a new DSA group with the given base (“g”), prime (“p”), and
	 * subprime (“q”).
	 *
	 * @param base
	 *            The base of the DSA group
	 * @param prime
	 *            The prime of the DSA group
	 * @param subprime
	 *            The subprime of the DSA group
	 */
	public DSAGroup(String base, String prime, String subprime) {
		this.base = base;
		this.prime = prime;
		this.subprime = subprime;
	}

	/**
	 * Returns the base (“g”) of the DSA group.
	 *
	 * @return The base of the DSA group
	 */
	public String getBase() {
		return base;
	}

	/**
	 * Returns the prime (“p”) of the DSA group.
	 *
	 * @return The prime of the DSA group
	 */
	public String getPrime() {
		return prime;
	}

	/**
	 * Returns the subprime (“q”) of the DSA group.
	 *
	 * @return The subprime of the DSA group
	 */
	public String getSubprime() {
		return subprime;
	}

}
