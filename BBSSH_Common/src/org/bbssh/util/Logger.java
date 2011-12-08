package org.bbssh.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.applicationcontrol.ApplicationPermissionsManager;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.EventLogger;

public class Logger {
	public static final int LOG_LEVEL_FATAL = 0;
	public static final int LOG_LEVEL_ERROR = 10;
	public static final int LOG_LEVEL_WARN = 20;
	public static final int LOG_LEVEL_INFO = 50;
	public static final int LOG_LEVEL_DEBUG = 80;
	private static final String LOGGING_DISABLED_PREFIX = "disabled logging to file due to IOException - ";;

	private static int logLevel = LOG_LEVEL_WARN;

	private static OutputStreamWriter stream;

	private static SimpleDateFormat outputDateFormat = new SimpleDateFormat("HHmmss.SSS");
	private static SimpleDateFormat filenameFormat = new SimpleDateFormat("yyyyMMdd");
	private static String fileName;

	public static byte[] getFileContent() {
		if (!isFileLoggingEnabled())
			return null;
		String name = getFileName();
		byte[] data = null;
		disableFileLogging();
		try {
			FileConnection fconn = (FileConnection) Connector.open(name);
			data = new byte[(int) fconn.fileSize()];
			DataInputStream s = fconn.openDataInputStream();
			s.readFully(data);
			s.close();
			fconn.close();
		} catch (IOException e) {

		} finally {
			enableFileLogging();
		}
		return data;
	}

	public static String getFileName() {
		return fileName;
	}

	public static boolean isFileLoggingEnabled() {
		return (stream != null);
	}

	/**
	 * Enabled file logging using a default location, first attemping to use the
	 * SDCard then the user home directory in on-device memory.
	 * 
	 * @throws IllegalArgumentException
	 */
	public static synchronized void enableFileLogging() throws IllegalArgumentException {
		Logger.disableFileLogging();
		try {
			Logger.enableFileLogging("file:///SDCard/");
		} catch (Exception e) {
			try {
				enableFileLogging("file:///store/home/user/");
			} catch (IllegalArgumentException e1) {
				throw e1;
			} catch (Exception e2) {

			}

		}
	}

	public static synchronized void enableFileLogging(String outDirectory) throws IllegalArgumentException {
		if (outDirectory == null || outDirectory.length() == 0)
			throw new IllegalArgumentException("No output location provided.");
		if (ApplicationPermissionsManager.getInstance().getPermission(ApplicationPermissions.PERMISSION_FILE_API) == ApplicationPermissions.VALUE_DENY) {
			return;
		}

		closeOutput();
		String name = "";
		try {
			FileConnection fconn = (FileConnection) Connector.open(outDirectory);
			if (!fconn.isDirectory())
				throw new IllegalArgumentException("Valid directory must be specified.");

			fconn.close();
			name = fconn.getURL() + "BBSSH_" + filenameFormat.formatLocal(new Date().getTime()) + ".txt";
			fconn.close();

			fconn = (FileConnection) Connector.open(name);
			// Pretty darned unlikely but let's be certain they didn't happen to
			// make a file of the same name...
			if (!fconn.exists()) {
				fconn.create();
			}
			stream = new OutputStreamWriter(fconn.openOutputStream(fconn.totalSize()));
			fconn.close(); // the stream will remain open
			fileName = name;
			logToFile(" *** BEGIN LOGGING *** ");
		} catch (Exception e) {
			String message = "Failed to open stream " + name + ": " + e.getMessage() + " " + e.toString();
			logToEventLog(message);
			throw new IllegalArgumentException(message);

		}

	}

	public static void disableFileLogging() {
		closeOutput();
	};

	private synchronized static void closeOutput() {
		if (stream == null) {
			return;
		}
		logToFile(" *** END LOGGING *** ");
		try {
			stream.close();
		} catch (Throwable e) {
		}
		stream = null;
	}

	private static String getLogLevelName(int level) {
		switch (level) {
			case LOG_LEVEL_DEBUG:
				return " DBG";
			case LOG_LEVEL_ERROR:
				return " ERR";
			case LOG_LEVEL_FATAL:
				return " FTL";
			case LOG_LEVEL_INFO:
				return " INF";
			case LOG_LEVEL_WARN:
				return " WRN";

		}
		return "    ";
	}

	public static void setLogLevel(int level) {
		Logger.info("Log level now set to " + getLogLevelName(level));
		logLevel = level;
	}

	public static void error(String message) {
		// Saved the synchronzied call by pre-filtering
		if (LOG_LEVEL_ERROR <= logLevel)
			log(LOG_LEVEL_ERROR, message);
	}

	public static void debug(String message) {
		// Saved the synchronzied call by pre-filtering
		if (LOG_LEVEL_DEBUG <= logLevel)
			log(LOG_LEVEL_DEBUG, message);
	}

	public static void info(String message) {
		// Saved the synchronzied call by pre-filtering
		if (LOG_LEVEL_INFO <= logLevel)
			log(LOG_LEVEL_INFO, message);
	}

	public static void fatal(String message) {
		// Saved the synchronzied call by pre-filtering
		if (LOG_LEVEL_FATAL <= logLevel)
			log(LOG_LEVEL_FATAL, message);
	}

	public static void warn(String message) {
		// Saved the synchronzied call by pre-filtering
		if (LOG_LEVEL_WARN <= logLevel)
			log(LOG_LEVEL_WARN, message);
	}

	public static synchronized void logToEventLog(String message) {
		EventLogger.logEvent(Tools.NOTIFICATION_GUID, message.getBytes());

	}

	public static void logToFile(String message) {
		logToFile(-1, message);
	}

	public static synchronized void flushFileLog() {
		if (stream != null) {
			try {
				stream.flush();
			} catch (IOException e) {
				EventLogger.logEvent(Tools.NOTIFICATION_GUID, (LOGGING_DISABLED_PREFIX + e.getMessage()).getBytes());
				disableFileLogging();
			}
		}
	}

	/**
	 * Log directly to the output file (if file logging is enabled) without
	 * writing to system event log.
	 * 
	 * @param message
	 */
	public static synchronized void logToFile(int level, String message) {
		if (stream == null) {
			return;
		}
		try {
			stream.write(outputDateFormat.formatLocal(new Date().getTime()));
			if (level > -1) {
				stream.write(" ");
				stream.write(Thread.currentThread().getName());
				stream.write(" ");
				stream.write(getLogLevelName(level));
			}
			stream.write(" ");
			stream.write(message);
			stream.write("\n");
		} catch (IOException e) {//
			// nothing we can do about it if this happens; however for
			// performance sake let's turn
			// file logging back off.
			EventLogger.logEvent(Tools.NOTIFICATION_GUID, (LOGGING_DISABLED_PREFIX + e.getMessage()).getBytes());
			disableFileLogging();
		}
	}

	public static synchronized void log(int level, String message) {
		if (message == null)
			return;

		// This will go to the debugger "output" window.
		if (DeviceInfo.isSimulator() && level > LOG_LEVEL_DEBUG) {
			System.out.print(Thread.currentThread().getName());
			System.out.print(" ");
			System.out.print(outputDateFormat.formatLocal(new Date().getTime()));
			System.out.print(" [");
			System.out.print(getLogLevelName(level));
			System.out.print("] ");
			System.out.println(message);
		}

		// Log to file controlled by log level settings
		if (level <= logLevel) {
			logToFile(level, message);
		}

		// Event log is fixed-level
		if (level <= LOG_LEVEL_WARN) {
			logToEventLog(message);
		}

	}

	public boolean isEnabled() {
		return (logLevel > -1);
	}

	public static void fatal(String message, Throwable t) {
		fatal(message + " : " + t.getClass().toString() + " : " + t.getMessage());
	}

	public static boolean isLevelEnabled(int level) {
		return (logLevel >= level);
	}
}
