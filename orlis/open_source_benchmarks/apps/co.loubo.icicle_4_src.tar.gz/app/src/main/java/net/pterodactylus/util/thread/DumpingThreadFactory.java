/*
 * utils - DumpingThreadFactory.java - Copyright © 2006-2009 David Roden
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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link ThreadFactory} implementation that creates {@link DumpingThread}s.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class DumpingThreadFactory implements ThreadFactory {

	/** The counter for thread factories. */
	private static final AtomicInteger poolNumber = new AtomicInteger(0);

	/** The counter for threads. */
	private final AtomicInteger threadNumber = new AtomicInteger(0);

	/** The name prefix. */
	private final String namePrefix;

	/** Whether to create daemon threads. */
	private final boolean createDaemonThreads;

	/**
	 * Creates a new DumpingThread factory.
	 */
	public DumpingThreadFactory() {
		this("DumpingThreadFactory-" + poolNumber.getAndIncrement() + "-Thread-");
	}

	/**
	 * Creates a new dumping thread factory that uses the given name prefix and
	 * a running counter appended to it as names for the created threads.
	 *
	 * @param namePrefix
	 *            The name prefix, preferrable ending in a hyphen (
	 *            <code>‘-’</code>) or space (<code>‘ ’</code>)
	 */
	public DumpingThreadFactory(String namePrefix) {
		this(namePrefix, true);
	}

	/**
	 * Creates a new DumpingThread factory.
	 *
	 * @param createDaemonThreads
	 *            <code>true</code> to create daemon threads
	 */
	public DumpingThreadFactory(boolean createDaemonThreads) {
		this("DumpingThreadFactory-" + poolNumber.getAndIncrement() + "-Thread-", createDaemonThreads);
	}

	/**
	 * Creates a new dumping thread factory that uses the given name prefix and
	 * a running counter appended to it as names for the created threads.
	 *
	 * @param namePrefix
	 *            The name prefix, preferrable ending in a hyphen (
	 *            <code>‘-’</code>) or space (<code>‘ ’</code>)
	 * @param createDaemonThreads
	 *            <code>true</code> to create daemon threads
	 */
	public DumpingThreadFactory(String namePrefix, boolean createDaemonThreads) {
		this.namePrefix = namePrefix;
		this.createDaemonThreads = createDaemonThreads;
	}

	/**
	 * Creates a new {@link DumpingThread} that will execute the given runnable.
	 *
	 * @param r
	 *            The runnable to execute in a new {@link DumpingThread}
	 * @return The constructed {@link DumpingThread} that will execute the given
	 *         runnable
	 * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
	 */
	@Override
	public Thread newThread(Runnable r) {
		Thread thread = new DumpingThread(r);
		thread.setDaemon(createDaemonThreads);
		thread.setName(namePrefix + threadNumber.getAndIncrement());
		return thread;
	}

}
