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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * {@link ThreadFactory} implementation that creates threads with a configurable name.
 * <br>
 * The {@code nameFormat} parameter handed in to the
 * {@link #NamedThreadFactory(String) constructor} will be used as a pattern
 * for {@link String#format(String, Object...)}. The number of the thread
 * factory and the number of the created thread are handed in as parameters to
 * {@link String#format(String, Object...)}; the number of the thread factory
 * is an {@code int}, the number of the thread is a {@link long}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class NamedThreadFactory implements ThreadFactory {

    /** The counter for all named thread factories. */
    private static final AtomicInteger threadFactoryCounter = new AtomicInteger();

    /** The number of the current thread factory. */
    private final int threadFactoryNumber;

    /** The counter for created threads. */
    private final AtomicLong threadCounter = new AtomicLong();

    /** The name format. */
    private final String nameFormat;

    /**
     * Creates a new named thread factory with the given name format.
     *
     * @param nameFormat
     *            The name format
     */
    public NamedThreadFactory(String nameFormat) {
        threadFactoryNumber = threadFactoryCounter.getAndIncrement();
        this.nameFormat = nameFormat;
    }

    //
    // THREADFACTORY METHODS
    //

    /**
     * {@inheritDoc}
     */
    @Override
    public Thread newThread(Runnable runnable) {
        return new Thread(runnable, String.format(nameFormat, threadFactoryNumber, threadCounter.getAndIncrement()));
    }

}
