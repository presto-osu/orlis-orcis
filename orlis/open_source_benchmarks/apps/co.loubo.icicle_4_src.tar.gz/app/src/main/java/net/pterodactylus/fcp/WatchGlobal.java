/*
 * jFCPlib - WatchGlobal.java - Copyright © 2008 David Roden
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
 * The “WatchGlobal” messages enables clients to watch the global queue in
 * addition to the client-local queue.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class WatchGlobal extends FcpMessage {

	/**
	 * Enables or disables watching the global queue.
	 *
	 * @param enabled
	 *            <code>true</code> to watch the global queue and the
	 *            client-local queue, <code>false</code> to watch only the
	 *            client-local queue
	 */
	public WatchGlobal(boolean enabled) {
		this(enabled, Verbosity.ALL);
	}

	/**
	 * Enables or disables watching the global queue, optionally masking out
	 * certain events.
	 *
	 * @param enabled
	 *            <code>true</code> to watch the global queue and the
	 *            client-local queue, <code>false</code> to watch only the
	 *            client-local queue
	 * @param verbosityMask
	 *            A verbosity mask that determines which events are received
	 */
	public WatchGlobal(boolean enabled, Verbosity verbosityMask) {
		super("WatchGlobal");
		setField("Enabled", String.valueOf(enabled));
		setField("VerbosityMask", String.valueOf(verbosityMask));
	}

}
