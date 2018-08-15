/*
 * utils - AbstractListenerManager.java - Copyright © 2009 David Roden
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

package net.pterodactylus.util.event;

import java.util.EventListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

import net.pterodactylus.util.thread.CurrentThreadExecutor;

/**
 * Abstract implementation of a listener support class. The listener support
 * takes care of adding and removing {@link EventListener} implementations, and
 * subclasses are responsible for firing appropriate events.
 *
 * @param <S>
 *            The type of the source
 * @param <L>
 *            The type of the event listeners
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public abstract class AbstractListenerManager<S, L extends EventListener> {

	/** The source that emits the events. */
	private final S source;

	/** The list of listeners. */
	private final List<L> listeners = new CopyOnWriteArrayList<L>();

	/** Service that executes event threads. */
	private final Executor executor;

	/**
	 * Creates a new listener support that emits events from the given source.
	 *
	 * @param source
	 *            The source of the events
	 */
	public AbstractListenerManager(S source) {
		this(source, new CurrentThreadExecutor());
	}

	/**
	 * Creates a new listener support that emits events from the given source.
	 *
	 * @param source
	 *            The source of the events
	 * @param executor
	 *            The executor used to fire events
	 */
	public AbstractListenerManager(S source, Executor executor) {
		this.source = source;
		this.executor = executor;
	}

	/**
	 * Adds the given listener to the list of reigstered listeners.
	 *
	 * @param listener
	 *            The listener to add
	 */
	public void addListener(L listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	/**
	 * Removes the given listener from the list of registered listeners.
	 *
	 * @param listener
	 *            The listener to remove
	 */
	public void removeListener(L listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	//
	// PROTECTED METHODS
	//

	/**
	 * Returns the source for the events.
	 *
	 * @return The event source
	 */
	protected S getSource() {
		return source;
	}

	/**
	 * Returns the executor for the event firing.
	 *
	 * @return The executor
	 */
	protected Executor getExecutor() {
		return executor;
	}

	/**
	 * Returns a list of all registered listeners. The returned list is a copy
	 * of the original list so structural modifications will never occur when
	 * using the returned list.
	 *
	 * @return The list of all registered listeners
	 */
	protected List<L> getListeners() {
		return listeners;
	}

}
