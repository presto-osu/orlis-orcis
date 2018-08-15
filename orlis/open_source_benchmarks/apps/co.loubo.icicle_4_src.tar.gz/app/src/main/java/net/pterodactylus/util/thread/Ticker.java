/*
 * utils - Ticker.java - Copyright © 2009 David Roden
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.util.logging.Logging;

/**
 * Executes threads at specified times in the future.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Ticker implements Runnable {

	/** Logger. */
	private static final Logger logger = Logging.getLogger(Ticker.class.getName());

	/** A global instance. */
	private static final Ticker globalInstance = new Ticker();

	/** Counter for nameless events. */
	private static int counter = 0;

	/** Thread factory for new threads. */
	private ThreadFactory threadFactory;

	/** Synchronization object. */
	private final Object syncObject = new Object();

	/** Sorted list of execution times. */
	private final Queue<EventIdentifier> executionTimes = new PriorityBlockingQueue<EventIdentifier>();

	/** Mappings from execution time to runnables. */
	private final Map<EventIdentifier, Runnable> runnables = Collections.synchronizedMap(new HashMap<EventIdentifier, Runnable>());

	/** Whether the ticker thread is running. */
	private boolean running = false;

	/**
	 * Creates a new ticker with a default thread factory (which uses
	 * {@link DumpingThread}s).
	 */
	public Ticker() {
		this(new DumpingThreadFactory());
	}

	/**
	 * Creates a new ticker that uses the given thread factory to create new
	 * threads.
	 *
	 * @param threadFactory
	 *            The thread factory to use for new threads
	 */
	public Ticker(ThreadFactory threadFactory) {
		this.threadFactory = threadFactory;
	}

	/**
	 * Returns the global ticker instance.
	 *
	 * @return The global ticker instance
	 */
	public static Ticker getInstance() {
		return globalInstance;
	}

	/**
	 * Registers an unnamed thread that is executed at the specified time.
	 *
	 * @deprecated Use {@link #registerEvent(long, Runnable, String)} instead.
	 * @param executionTime
	 *            Time of execution (in milliseconds since the epoch)
	 * @param thread
	 *            The thread to execute
	 * @return An object that identifies the created ticker event
	 */
	@Deprecated
	public Object registerEvent(long executionTime, Runnable thread) {
		synchronized (syncObject) {
			return registerEvent(executionTime, thread, "Event-" + counter++);
		}
	}

	/**
	 * Registers a named thread that is executed at the specified time.
	 *
	 * @param executionTime
	 *            Time of execution (in milliseconds since the epoch)
	 * @param thread
	 *            The thread to execute
	 * @param eventName
	 *            The name of the event
	 * @return An object that identifies the created ticker event
	 */
	public Object registerEvent(long executionTime, Runnable thread, String eventName) {
		synchronized (syncObject) {
			logger.log(Level.INFO, "Ticker registered %s at %d.", new Object[] { eventName, executionTime });
			EventIdentifier identifierObject = new EventIdentifier(executionTime, eventName);
			runnables.put(identifierObject, thread);
			executionTimes.add(identifierObject);
			if (!running) {
				running = true;
				Thread tickerThread = threadFactory.newThread(this);
				tickerThread.setName("Ticker Thread");
				tickerThread.start();
			} else {
				syncObject.notify();
			}
			return identifierObject;
		}
	}

	/**
	 * Changes the execution time of the thread identified by the given object.
	 *
	 * @param identifierObject
	 *            The object that identifies the ticker object to change
	 * @param newExecutionTime
	 *            The new execution time for the thread
	 */
	public void changeExecutionTime(Object identifierObject, long newExecutionTime) {
		if (!(identifierObject instanceof EventIdentifier)) {
			return;
		}
		EventIdentifier eventIdentifier = (EventIdentifier) identifierObject;
		synchronized (syncObject) {
			executionTimes.remove(eventIdentifier);
			eventIdentifier.setExecutionTime(newExecutionTime);
			executionTimes.add(eventIdentifier);
			syncObject.notify();
		}
	}

	/**
	 * Removes the event identified by the given identifier. The
	 * <code>eventIdentifier</code> is an object that was returned by a previous
	 * call to {@link #registerEvent(long, Runnable)}.
	 *
	 * @param eventIdentifier
	 *            The identifier of the event to remove
	 */
	public void deregisterEvent(Object eventIdentifier) {
		if (!(eventIdentifier instanceof EventIdentifier)) {
			return;
		}
		synchronized (syncObject) {
			logger.log(Level.INFO, "Ticker removes event %s at %d.", new Object[] { ((EventIdentifier) eventIdentifier).getEventName(), ((EventIdentifier) eventIdentifier).getExecutionTime() });
			runnables.remove(eventIdentifier);
			removeEventIdentifier((EventIdentifier) eventIdentifier);
			syncObject.notify();
		}
	}

	/**
	 * Removes the given event identifier from the {@link #executionTimes}
	 * queue. This method had to be created because the {@link PriorityQueue}
	 * reimplements {@link Collection#remove(Object)} to search for objects that
	 * have the same natural order which might simply delete the wrong object if
	 * two event identifiers have the same execution time.
	 *
	 * @param eventIdentifier
	 *            The event identifier to remove
	 * @return <code>true</code> if the event identifier was removed,
	 *         <code>false</code> otherwise
	 */
	private boolean removeEventIdentifier(EventIdentifier eventIdentifier) {
		Iterator<EventIdentifier> iterator = executionTimes.iterator();
		while (iterator.hasNext()) {
			if (iterator.next().equals(eventIdentifier)) {
				iterator.remove();
				return true;
			}
		}
		return false;
	}

	/**
	 * Stops the ticker. No further threads will be run.
	 */
	public void stop() {
		synchronized (syncObject) {
			running = false;
			syncObject.notify();
		}
	}

	/**
	 * Main ticker thread.
	 */
	@Override
	public void run() {
		logger.log(Level.INFO, "Ticker started.");
		synchronized (syncObject) {
			while (running) {
				if (executionTimes.isEmpty()) {
					logger.log(Level.INFO, "Ticker is waiting for events.");
					try {
						syncObject.wait();
					} catch (InterruptedException ie1) {
						/*
						 * ignore, ticker will land here again if there's
						 * nothing to do.
						 */
					}
				} else {
					EventIdentifier eventIdentifier = executionTimes.peek();
					if (eventIdentifier == null) {
						continue;
					}
					long now = System.currentTimeMillis();
					long executionTime = eventIdentifier.getExecutionTime();
					if (executionTime > now) {
						logger.log(Level.INFO, "Ticker is waiting up to %d for %s to execute at %d.", new Object[] { executionTime - now, eventIdentifier.getEventName(), executionTime });
						try {
							syncObject.wait(executionTime - now);
						} catch (InterruptedException ie1) {
							/*
							 * ignore, ticker will land here again if there's
							 * nothing to do.
							 */
						}
					} else {
						removeEventIdentifier(eventIdentifier);
						Runnable runnable = runnables.remove(eventIdentifier);
						if (runnable != null) {
							logger.log(Level.INFO, "Ticker executes %s, %d ms late", new Object[] { eventIdentifier.getEventName(), now - executionTime });
							Thread eventThread = threadFactory.newThread(runnable);
							eventThread.setName("Event Thread for " + eventIdentifier.getEventName() + " @ " + executionTime);
							eventThread.start();
						}
					}
				}
			}
		}
	}

	/**
	 * Identifier objects for events to circumvent the
	 * <em>consistent with equals</em> logic used by the Java Collections
	 * architecture that gives problem when using normals Long objects as
	 * identifiers.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	private static class EventIdentifier implements Comparable<EventIdentifier> {

		/** The execution time. */
		private long executionTime;

		/** The name of the event. */
		private final String eventName;

		/**
		 * Constructs a new event identifier for an event at the given execution
		 * time.
		 *
		 * @param executionTime
		 *            The execution time of the event
		 * @param eventName
		 *            The name of the event
		 */
		public EventIdentifier(long executionTime, String eventName) {
			this.executionTime = executionTime;
			this.eventName = eventName;
		}

		/**
		 * Sets the new execution time of this event identifier.
		 *
		 * @param newExecutionTime
		 *            The new execution time
		 */
		public void setExecutionTime(long newExecutionTime) {
			executionTime = newExecutionTime;
		}

		/**
		 * Returns the execution time of the event.
		 *
		 * @return The execution time of the event
		 */
		public long getExecutionTime() {
			return executionTime;
		}

		/**
		 * Returns the name of the event.
		 *
		 * @return The name of the event.
		 */
		public String getEventName() {
			return eventName;
		}

		/**
		 * {@inheritDoc}
		 *
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(EventIdentifier o) {
			return (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, executionTime - o.executionTime));
		}

	}

}
