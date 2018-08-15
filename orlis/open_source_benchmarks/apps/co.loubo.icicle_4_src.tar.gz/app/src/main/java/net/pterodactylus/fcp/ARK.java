/*
 * jFCPlib - ARK.java - Copyright © 2008 David Roden
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
 * Container for ARKs (address resolution keys).
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class ARK {

	/** The public URI of the ARK. */
	private final String publicURI;

	/** The private URI of the ARK. */
	private final String privateURI;

	/** The number of the ARK. */
	private final int number;

	/**
	 * Creates a new ARK with the given URI and number.
	 *
	 * @param publicURI
	 *            The public URI of the ARK
	 * @param number
	 *            The number of the ARK
	 */
	public ARK(String publicURI, String number) {
		this(publicURI, null, number);
	}

	/**
	 * Creates a new ARK with the given URIs and number.
	 *
	 * @param publicURI
	 *            The public URI of the ARK
	 * @param privateURI
	 *            The private URI of the ARK
	 * @param number
	 *            The number of the ARK
	 */
	public ARK(String publicURI, String privateURI, String number) {
		if ((publicURI == null) || (number == null)) {
			throw new NullPointerException(((publicURI == null) ? "publicURI" : "number") + " must not be null");
		}
		this.publicURI = publicURI;
		this.privateURI = privateURI;
		try {
			this.number = Integer.valueOf(number);
		} catch (NumberFormatException nfe1) {
			throw new IllegalArgumentException("number must be numeric", nfe1);
		}
	}

	/**
	 * Returns the public URI of the ARK.
	 *
	 * @return The public URI of the ARK
	 */
	public String getPublicURI() {
		return publicURI;
	}

	/**
	 * Returns the private URI of the ARK.
	 *
	 * @return The private URI of the ARK
	 */
	public String getPrivateURI() {
		return privateURI;
	}

	/**
	 * Returns the number of the ARK.
	 *
	 * @return The number of the ARK
	 */
	public int getNumber() {
		return number;
	}

}
