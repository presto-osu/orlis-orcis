/*
 * utils - Logging.java - Copyright © 2009 David Roden
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

package net.pterodactylus.util.logging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Sets up logging.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Logging {

	/** The log handler. */
	private static final LogHandler logHandler = new LogHandler();

	/** The console handler. */
	private static ConsoleHandler consoleHandler = new ConsoleHandler();

	/** Cache for logger’s classes. */
	private static final Map<String, Class<?>> classCache = new HashMap<String, Class<?>>();

	static {
		logHandler.setLevel(Level.ALL);
	}

	/** The root name of the hierarchy. */
	private static String hierarchyRootName = "default";

	/**
	 * Adds a listener to the log handler.
	 *
	 * @param loggingListener
	 *            The listener to add
	 */
	public static void addLoggingListener(LoggingListener loggingListener) {
		logHandler.addLoggingListener(loggingListener);
	}

	/**
	 * Removes a listener from the log handler.
	 *
	 * @param loggingListener
	 *            The listener to remove
	 */
	public static void removeLoggingListener(LoggingListener loggingListener) {
		logHandler.removeLoggingListener(loggingListener);
	}

	/**
	 * Sets up logging and installs the log handler.
	 *
	 * @param hierarchyName
	 *            The name of the hierarchy root logger
	 */
	public static void setup(String hierarchyName) {
		hierarchyRootName = hierarchyName;
		Logger rootLogger = getRootLogger();
		rootLogger.addHandler(logHandler);
		rootLogger.setUseParentHandlers(false);
		if (rootLogger.getLevel() == null) {
			rootLogger.setLevel(Level.ALL);
		}
	}

	/**
	 * Initializes console logging.
	 */
	public static void setupConsoleLogging() {
		Logger rootLogger = getRootLogger();
		consoleHandler.setLevel(Level.ALL);
		consoleHandler.setFormatter(new Formatter() {

			private StringBuffer recordBuffer = new StringBuffer();
			private DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z");

			/**
			 * {@inheritDoc}
			 */
			@Override
			public synchronized String format(LogRecord record) {
				recordBuffer.setLength(0);
				String linePrefix = dateFormatter.format(new Date(record.getMillis())) + " [" + record.getLevel() + "] [" + Thread.currentThread().getName() + "] [" + record.getSourceClassName() + "." + record.getSourceMethodName() + "] ";
				recordBuffer.append(linePrefix).append(String.format(record.getMessage(), record.getParameters())).append('\n');
				if (record.getThrown() != null) {
					Throwable throwable = record.getThrown();
					boolean causedBy = false;
					while (throwable != null) {
						recordBuffer.append(linePrefix);
						if (causedBy) {
							recordBuffer.append("caused by: ");
						}
						recordBuffer.append(throwable.getClass().getName());
						if (throwable.getMessage() != null) {
							recordBuffer.append(": ").append(throwable.getMessage());
						}
						recordBuffer.append("\n");
						StackTraceElement[] stackTraceElements = throwable.getStackTrace();
						for (StackTraceElement stackTraceElement : stackTraceElements) {
							recordBuffer.append(linePrefix).append("  at ").append(stackTraceElement.getClassName()).append('.').append(stackTraceElement.getMethodName()).append("(").append(stackTraceElement.getFileName()).append(':').append(stackTraceElement.getLineNumber()).append(')').append("\n");
						}
						throwable = throwable.getCause();
						causedBy = true;
					}
				}
				return recordBuffer.toString();
			}
		});
		rootLogger.addHandler(consoleHandler);
	}

	/**
	 * Shuts this logging hierarchy down by removing all {@link Handler}s from
	 * the root logger.
	 */
	public static void shutdown() {
		getRootLogger().removeHandler(logHandler);
		getRootLogger().removeHandler(consoleHandler);
	}

	/**
	 * Returns a named logger from the logger hierarchy.
	 *
	 * @param name
	 *            The name of the logger
	 * @return The logger
	 */
	public static Logger getLogger(String name) {
		Logger logger = Logger.getLogger(hierarchyRootName + "." + name);
		return logger;
	}

	/**
	 * Returns a named logger from the logger hierarchy.
	 *
	 * @param loggerClass
	 *            The class of the logger
	 * @return The logger
	 */
	public static Logger getLogger(Class<?> loggerClass) {
		classCache.put(hierarchyRootName + "." + loggerClass.getName(), loggerClass);
		return getLogger(loggerClass.getName());
	}

	/**
	 * Sets the log level of the hierarchy’s root logger.
	 *
	 * @param rootLevel
	 *            The log level for the root logger
	 */
	public static void setRootLevel(Level rootLevel) {
		getRootLogger().setLevel(rootLevel);
	}

	/**
	 * Returns the root logger of this logging hierarchy.
	 *
	 * @return The hierarchy’s root logger
	 */
	private static Logger getRootLogger() {
		return Logger.getLogger(hierarchyRootName);
	}

	/**
	 * Returns the class that created the logger with the given name.
	 *
	 * @param loggerName
	 *            The name of the logger
	 * @return The class of the creating class, if available
	 */
	public static Class<?> getLoggerClass(String loggerName) {
		return classCache.get(loggerName);
	}

	/**
	 * The log handler simply forwards every log message it receives to all
	 * registered listeners.
	 *
	 * @see LoggingListener
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	private static class LogHandler extends Handler {

		/**
		 * Package-private constructor.
		 */
		LogHandler() {
			/* do nothing. */
		}

		/** The list of the listeners. */
		private final List<LoggingListener> loggingListeners = Collections.synchronizedList(new ArrayList<LoggingListener>());

		//
		// EVENT MANAGEMENT
		//

		/**
		 * Adds a listener to the log handler.
		 *
		 * @param loggingListener
		 *            The listener to add
		 */
		public void addLoggingListener(LoggingListener loggingListener) {
			loggingListeners.add(loggingListener);
		}

		/**
		 * Removes a listener from the log handler.
		 *
		 * @param loggingListener
		 *            The listener to remove
		 */
		public void removeLoggingListener(LoggingListener loggingListener) {
			loggingListeners.remove(loggingListener);
		}

		/**
		 * Notifies all listeners that a log record was received.
		 *
		 * @param logRecord
		 *            The received log record
		 */
		private void fireLogged(LogRecord logRecord) {
			for (LoggingListener loggingListener : loggingListeners) {
				loggingListener.logged(logRecord);
			}
		}

		//
		// INTERFACE Handler
		//

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void close() throws SecurityException {
			/* do nothing. */
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void flush() {
			/* do nothing. */
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void publish(LogRecord logRecord) {
			fireLogged(logRecord);
		}

	}

}
