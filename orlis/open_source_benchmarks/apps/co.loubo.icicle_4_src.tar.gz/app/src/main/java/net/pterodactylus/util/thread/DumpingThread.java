/*
 * utils - DumpingThread.java - Copyright © 2006-2009 David Roden
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.pterodactylus.util.thread;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.util.logging.Logging;

/**
 * Wrapper around {@link Thread} that catches throws exceptions and dumps them
 * to the logfile.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class DumpingThread extends Thread {

	/** Logger. */
	private static final Logger logger = Logging.getLogger(DumpingThread.class.getName());

	/** Thread counter. */
	private static int counter = 0;

	/** Whether to log thread start and end. */
	private boolean logThreadStartAndEnd;

	/**
	 * Creates a new dumping thread that executes the given {@link Runnable}.
	 *
	 * @param runnable
	 *            The {@link Runnable} to execute
	 */
	public DumpingThread(Runnable runnable) {
		this(runnable, "DumpingThread-" + counter++);
	}

	/**
	 * Creates a new dumping thread that executes the given {@link Runnable} in
	 * a thread with the given name.
	 *
	 * @param runnable
	 *            The {@link Runnable} to execute
	 * @param name
	 *            The name of the thread
	 */
	public DumpingThread(Runnable runnable, String name) {
		this(runnable, name, false);
	}

	/**
	 * Creates a new dumping thread that executes the given {@link Runnable} in
	 * a thread with the given name.
	 *
	 * @param runnable
	 *            The {@link Runnable} to execute
	 * @param name
	 *            The name of the thread
	 * @param logThreadStartAndEnd
	 *            <code>true</code> if the thread should log its start and end
	 */
	public DumpingThread(Runnable runnable, String name, boolean logThreadStartAndEnd) {
		super(runnable, name);
		this.logThreadStartAndEnd = logThreadStartAndEnd;
	}

	/**
	 * Sets whether the started thread should logs its start and end.
	 *
	 * @param logThreadStartAndEnd
	 *            <code>true</code> if the thread should log its start and end,
	 *            <code>false</code> otherwise
	 */
	public void setLogThreadStartAndEnd(boolean logThreadStartAndEnd) {
		this.logThreadStartAndEnd = logThreadStartAndEnd;
	}

	/**
	 * Executes the runnable in a try-catch block and dumps the thrown exception
	 * to the {@link #logger}.
	 *
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		if (logThreadStartAndEnd) {
			logger.log(Level.INFO, "thread starting");
		}
		try {
			super.run();
		} catch (Throwable t) {
			logger.log(Level.SEVERE, "***** THREAD EXITED UNEXPECTEDLY! *****", t);
			throw new RuntimeException(t);
		}
		if (logThreadStartAndEnd) {
			logger.log(Level.INFO, "thread exited.");
		}
	}

}
