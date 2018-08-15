/*
 * utils - ObjectWrapper.java - Copyright © 2009-2010 David Roden
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

package net.pterodactylus.util.thread;

/**
 * Wrapper around an object that can be set and retrieved. Its primary use is as
 * a container for return values from anonymous classes.
 *
 * <pre>
 * final ObjectWrapper&lt;Object&gt; objectWrapper = new ObjectWrapper&lt;Object&gt;();
 * new Runnable() {
 *     public void run() {
 *         ...
 *         objectWrapper.set(someResult);
 *     }
 * }.run();
 * Object result = objectWrapper.get();
 * </pre>
 *
 * @param <T>
 *            The type of the wrapped object
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ObjectWrapper<T> {

	/** Object used for synchronization. */
	private final Object syncObject = new Object();

	/** The wrapped object. */
	private T wrappedObject;

	/** Whether the wrapped object has been set. */
	private boolean set;

	/**
	 * Returns the wrapped object.
	 *
	 * @return The wrapped object
	 */
	public T get() {
		synchronized (syncObject) {
			while (!set) {
				try {
					syncObject.wait();
				} catch (InterruptedException ie1) {
					/* ignore. */
				}
			}
			return wrappedObject;
		}
	}

	/**
	 * Returns whether the value has been set.
	 *
	 * @return {@code true} if the value was set, {@code false} otherwise
	 */
	public boolean isSet() {
		synchronized (syncObject) {
			return set;
		}
	}

	/**
	 * Sets the wrapped object.
	 *
	 * @param wrappedObject
	 *            The wrapped object
	 */
	public void set(T wrappedObject) {
		synchronized (syncObject) {
			this.wrappedObject = wrappedObject;
			set = true;
			syncObject.notifyAll();
		}
	}

}
