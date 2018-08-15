/*
 * utils - StoppableDelay.java - Copyright © 2012 David Roden
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
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
 * Runs a runnable after a certain time of a potentially longer-running
 * operation has elapsed, until said operation is finished.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class StoppableDelay implements Runnable {

	/** The start time of the operation. */
	private final long startTime = System.currentTimeMillis();

	/** The delay after which to show the dialog. */
	private final long delay;

	/** The runnable that is run after the delay. */
	private final Runnable startRunnable;

	/** The runnable that is run when the job finishes. */
	private final Runnable stopRunnable;

	/** If the operation is finished. */
	private volatile boolean finished;

	/**
	 * Creates a new delay that runs the given runnable after 500 milliseconds.
	 *
	 * @param startRunnable
	 *            The runnable that is run after the delay
	 * @param stopRunnable
	 *            The runnable that is run when the job finishes
	 */
	public StoppableDelay(Runnable startRunnable, Runnable stopRunnable) {
		this(startRunnable, stopRunnable, 500);
	}

	/**
	 * Creates a new delay that runs the given runnable after the given number
	 * of milliseconds.
	 *
	 * @param startRunnable
	 *            The runnable that is run after the delay
	 * @param stopRunnable
	 *            The runnable that is run when the job finishes
	 * @param delay
	 *            The delay after which to display the dialog (in milliseconds)
	 */
	public StoppableDelay(Runnable startRunnable, Runnable stopRunnable, long delay) {
		this.startRunnable = startRunnable;
		this.stopRunnable = stopRunnable;
		this.delay = delay;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns whether the long-running operation has finished.
	 *
	 * @return {@code true} if the operation is finished
	 */
	public boolean isFinished() {
		return finished;
	}

	//
	// ACTIONS
	//

	/**
	 * Marks the long-running operation as finished and runs the appropriate
	 * runnable. If the {@link #startRunnable} was not started yet, it will not
	 * be started at all.
	 */
	public void finish() {
		finished = true;
		stopRunnable.run();
	}

	//
	// RUNNABLE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		while (!finished && (System.currentTimeMillis() - startTime) < delay) {
			try {
				Thread.sleep((startTime + delay) - System.currentTimeMillis());
			} catch (InterruptedException ie1) {
				/* ignore, we’re looping. */
			}
		}
		if (finished) {
			return;
		}
		startRunnable.run();
	}

}
