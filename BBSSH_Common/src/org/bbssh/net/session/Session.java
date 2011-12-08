/**
 * This file is part of "BBSSH" (c) 2010 Marc A. Paradise Portions of this file Copyright (C) 2004 Karl von Randow as
 * part of midpssh. --LICENSE NOTICE-- This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * --LICENSE NOTICE--
 */
package org.bbssh.net.session;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.StreamConnection;

import net.rim.device.api.crypto.RandomSource;
import net.rim.device.api.system.ControlledAccessException;

import org.bbssh.i18n.BBSSHResource;
import org.bbssh.model.ConnectionProperties;
import org.bbssh.model.Key;
import org.bbssh.model.KeyManager;
import org.bbssh.net.ConnectionHelper;
import org.bbssh.ssh.kex.KexAgreement;
import org.bbssh.terminal.VT320;
import org.bbssh.terminal.VT320Debug;
import org.bbssh.util.Logger;
import org.bbssh.util.Tools;

public abstract class Session {
	private Vector dataListeners = new Vector();
	protected VT320 emulator;
	private SessionListener listener;
	protected SessionIOHandler filter;
	// Indicates that this was a client-requested disconnect.
	private boolean forceDisconnect;
	private StreamConnection connection;
	private InputStream in;
	private OutputStream out;
	private ConnectionProperties properties;
	private Thread reader, writer;
	private byte[] outputBuffer = new byte[1024]; // this will grow if needed
	private final Object writerMutex = new Object();
	private static final int BUF_SIZE = 8192;
	private String userName, password;

	public static final int CONNSTATE_NONE = 0;
	public static final int CONNSTATE_CONNECTING = 1;
	public static final int CONNSTATE_CONNECTED = 2;
	public static final int CONNSTATE_DISCONNECTING = 3;
	public static final int CONNSTATE_DISCONNECTED = 4;

	private final byte[] empty = new byte[0];

	private boolean wifiOverride;

	public int getBytesRead() {
		return bytesRead;
	}

	public int getBytesWritten() {
		return bytesWritten;
	}

	/**
	 * Number of bytes to be written, from output array, because it has fixed lenght.
	 */
	private int outputCount = 0;
	private int bytesWritten = 0;
	private int bytesRead = 0;

	private int connState;

	private int sessionId;

	/**
	 * Use this if you
	 * 
	 * @param prop
	 */
	protected Session(ConnectionProperties prop, int sessId, SessionListener listenr, VT320 emulator) {
		if (listenr == null)
			throw new NullPointerException("SessionListener must be valid.");
		connState = CONNSTATE_NONE;
		this.listener = listenr;
		this.sessionId = sessId;
		this.properties = prop;
		this.emulator = emulator;
		emulator.setScrollbackBufferSize(prop.getScrollbackLines());
		emulator.setFunctionKeyMode(prop.getFunctionKeyMode());
		if (prop.getAltPrefixesMeta()) {
			emulator.enableAltSendsMeta();
		}

	}

	public Session(ConnectionProperties prop, int sessId, SessionListener listenr) {
		if (listenr == null)
			throw new NullPointerException("SessionListener must be valid.");
		connState = CONNSTATE_NONE;
		this.listener = listenr;
		this.sessionId = sessId;
		this.properties = prop;

		if (prop.isCaptureEnabled()) {
			// @todo - pluggable emulator type based on configuration
			emulator = new VT320Debug(properties.getName() + sessId, prop) {
				public void sendData(byte[] b, int offset, int length) throws IOException {
					filter.handleSendData(b, offset, length);
				}

				// @todo is this extra layer of indirection useful?
				public void beep() {
					listener.onSessionRemoteAlert(sessionId);
				}

				public void resize() {
					if (filter != null) {
						filter.handleResize();
					}
					listener.onDisplayInvalid(sessionId);
				}
			};
		} else {
			// @todo - pluggable emulator type based on configuration
			emulator = new VT320() {
				public void sendData(byte[] b, int offset, int length) throws IOException {
					filter.handleSendData(b, offset, length);
				}

				public void beep() {
					listener.onSessionRemoteAlert(sessionId);
				}

				public void resize() {
					if (filter != null) {
						filter.handleResize();
					}
					listener.onDisplayInvalid(sessionId);
				}
			};
		}
		emulator.setScrollbackBufferSize(prop.getScrollbackLines());
		emulator.setFunctionKeyMode(prop.getFunctionKeyMode());
		emulator.setTerminalID(prop.getTermType());

		if (prop.getAltPrefixesMeta()) {
			emulator.enableAltSendsMeta();
		}
		reader = new Reader();
		writer = new Writer();
	}

	public SessionListener getListener() {
		return listener;
	}

	public ConnectionProperties getProperties() {
		return properties;
	}

	protected void connect(SessionIOHandler filter) {
		this.filter = filter;
		if (!writer.isAlive()) {
			if (connState >= CONNSTATE_CONNECTED) {
				writer = new Writer();
				reader = new Reader();
			}
			setConnectionState(CONNSTATE_CONNECTING);
			writer.start();

		}

	}

	protected abstract int getDefaultPort();

	/*
	 * (non-Javadoc)
	 */
	protected void receiveData(byte[] buffer, int offset, int length) throws IOException {
		if (buffer != null && length > 0) {
			try {
				String data = new String(buffer, offset, length);
				sendLocalTerminalOutput(data);
				for (int x = dataListeners.size() - 1; x > -1; x--) {
					((SessionDataListener) dataListeners.elementAt(x)).onDataReceived(sessionId, data);
				}

			} catch (Throwable e) {
				// We can't allow misuse of this data to mess with the
				// connection.

			}
		}
	}

	protected void sendData(byte[] b, int offset, int length) throws IOException {

		synchronized (writerMutex) {
			if (outputCount + length > outputBuffer.length) {
				byte[] newOutput = new byte[outputCount + length];
				System.arraycopy(outputBuffer, 0, newOutput, 0, outputCount);
				outputBuffer = newOutput;
			}
			System.arraycopy(b, offset, outputBuffer, outputCount, length);
			outputCount += length;
			writerMutex.notify();
		}

	}

	/**
	 * @return pending output count - note that this is not guaranteed completely accurate as it is unsynchronized.
	 */
	public int getOutputBufferLength() {
		return outputCount;
	}

	private void attemptConnect(byte connType, String host, int timeout) throws IOException {
		StringBuffer conn = new StringBuffer("socket://").append(host);
		ConnectionHelper.configureConnectionString(connType, conn, timeout);
		Logger.info("Attempting connection: " + conn.toString());
		connection = (StreamConnection) Connector.open(conn.toString(), Connector.READ_WRITE, false);
		Logger.info("Connection completed.");
	}

	private boolean setupConnection() throws IOException {
		try {
			return connectImpl();
		} catch (ControlledAccessException e) {
			Logger.error("Unable to connect: ControlledAccessException");
			throw new IOException(Tools.getStringResource(BBSSHResource.ERROR_NETWORK_PERM_MISSING));
		}

	}

	/**
	 * Implementations must handle connection setup after socet connection has been made by the base class.
	 */
	public abstract void connect();

	private void sendLocalTerminalOutput(String message) {
		emulator.putString(message);
		listener.onDisplayDirty(sessionId);
	}

	private boolean connectImpl() throws IOException {
		String host = properties.getHost();
		bytesRead = 0;
		bytesWritten = 0;
		sendLocalTerminalOutput("Connecting to " + host + "... ");
		if (host.indexOf(':') == -1) {
			host += ":" + getDefaultPort();
		}

		StringBuffer conn;

		String proxyHost = properties.getHttpProxyHost();
		int proxyMode = properties.getHttpProxyMode();
		String okMsg = "OK\r\n";
		if (proxyHost.length() == 0 || proxyMode == ConnectionHelper.PROXY_MODE_NONE) {

			if (properties.getUseWifiIfAvailable() && ConnectionHelper.isWifiAvailable()
					&& properties.getConnectionType() != ConnectionHelper.CONNECTION_TYPE_WIFI) {
				try {
					// First do a failable attempt.
					wifiOverride = true;
					attemptConnect(ConnectionHelper.CONNECTION_TYPE_WIFI, host, 0);
					okMsg = "WiFi OK\r\n";
				} catch (IOException e) {
					Logger.warn("Initial WIFI connection failed, now attempting to use original connection type.");
				}
			}
			if (connection == null) {
				wifiOverride = false;

				// If this one fails, the exception will be thrown and handled
				// normally.
				attemptConnect(properties.getConnectionType(), host, properties.getBESTimeout());
			}
			in = connection.openInputStream();
			out = connection.openOutputStream();
		} else {
			okMsg = "HTTP Proxy OK\r\n";
			int id = RandomSource.getInt();
			conn = new StringBuffer("http://").append(proxyHost).append('/').append(id).append('/').append(host);
			ConnectionHelper
					.configureConnectionString(properties.getConnectionType(), conn, properties.getBESTimeout());

			if (properties.getHttpProxyMode() == ConnectionHelper.PROXY_MODE_PERSISTENT) {
				HttpConnection outbound = (HttpConnection) Connector.open(conn.toString(), Connector.READ_WRITE, false);
				outbound.setRequestMethod(HttpConnection.POST);
				out = outbound.openOutputStream();
				HttpConnection inbound = (HttpConnection) Connector.open(conn.toString(), Connector.READ_WRITE, false);
				inbound.setRequestProperty("X-MidpSSH-Persistent", "true");
				in = inbound.openInputStream();
			} else {
				out = new HttpOutboundStream(conn.toString());
				in = new HttpInboundStream(conn.toString());
			}
		}
		sendLocalTerminalOutput(okMsg);
		Logger.warn("Connected to " + host);
		filter.handleConnection();
		// Don't notify of connected state until the implementation tells us to
		// as the impl knows when we're completed negotiations.
		// listener.onSessionConnected(sessionId);

		return true;
	}

	/**
	 * Continuously read from remote host and display the data on screen.
	 */
	private void read() throws IOException {
		byte[] buf;
		buf = new byte[BUF_SIZE];
		int available = 0;
		int inputSize = 0;

		try {
			// May need to read 1 to make available check accurate.
			// Logger.debug("read: Reading one byte.");
			if (in.read(buf, 0, 1) == -1) {
				Logger.fatal("read: initial 1b read failed - aborting");
				return;
			}
			bytesRead++;
			filter.handleReceiveData(buf, 0, 1);
			// Logger.debug("read: processed 1 byte.");

			// @todo refactor - "connState < CONNSTATE_DISCONNECTING" is far
			// from threadsafe (or particularly smmart).

			while (connState < CONNSTATE_DISCONNECTING && (available = in.available()) != -1) {
				// Logger.debug("read: attempting to read bytes: " + available);
				while (connState < CONNSTATE_DISCONNECTING && available > 0) {
					inputSize = in.read(buf, 0, Math.max(1, Math.min(available, BUF_SIZE)));
					// Logger.debug("read: obtained bytes: " + inputSize);
					if (inputSize > 0) {
						bytesRead += inputSize;
						filter.handleReceiveData(buf, 0, inputSize);
					} else if (inputSize == -1) {
						throw new IOException("Connection Terminated [eof]");
					}
					available -= inputSize;
				}
				inputSize = 0;
				// May need to read 1 to make available check accurate.
				// Logger.debug("read: Reading one byte.");
				if (in.read(buf, 0, 1) > 0) {
					bytesRead++;
					filter.handleReceiveData(buf, 0, 1);
					// Logger.debug("read: processed one byte.");

				} else {
					throw new IOException("Connection Terminated [eof]");
				}

			}
			// }

		} catch (IOException e) {
			if (connState < CONNSTATE_DISCONNECTING) {
				Logger.fatal("read: Exception in Session.read", e);
				throw e;
			} else {
				Logger.warn("Received exception in Session.read, but connection shutting down: " + e.getMessage());
			}
		}

	}

	private void write() throws IOException {
		// @todo - again the redundant conditions - MUST be a cleaner
		// implementation possible
		synchronized (writerMutex) {
			if (outputCount > 0) {
				// Logger.debug("write: sending data -> socket : " +
				// outputCount);
				out.write(outputBuffer, 0, outputCount);
				bytesWritten += outputCount;
				out.flush();
				// Logger.debug("write: end flush");
				// Logger.debug("write: output count to be reset is " +
				// outputCount);
				outputCount = 0;
			}
			try {
				// Wait until our timeout expires, or we have
				// data to send. Timeout values of 0 will
				// wait indefinitely.
				// Logger.debug("write: waiting");
				writerMutex.wait(properties.getKeepAliveTime() * 1000);
				// writerMutex.wait();
				// Logger.debug("write: mutex reacquired, resuming");
				if (connState < CONNSTATE_DISCONNECTING && outputCount == 0) {
					// No data to send after timeout so send an empty array
					// through the filter which will trigger
					// the sending of a NOOP (see TelnetSession and SshSession)
					// -
					// this has the effect of a keepalive

					// Logger.debug("write: queueing NOOP");
					filter.handleSendData(empty, 0, 0);
					// Logger.debug("write: sent NOOP");
				}

			} catch (InterruptedException e) {
				//
				Logger.error("write: interrupted");
			}

		}

	}

	public synchronized void disconnect() {
		forceDisconnect = true;
		doDisconnect();
	}

	private void doDisconnect() {
		if (connState >= CONNSTATE_DISCONNECTING) {
			return;
		}
		if (Logger.isFileLoggingEnabled() && Logger.isLevelEnabled(Logger.LOG_LEVEL_INFO)) {
			StringBuffer b = new StringBuffer(2048);
			b.append("\r\n");
			Tools.buildDiagnosticString(b);
			Logger.info(b.toString());
		}
		setConnectionState(CONNSTATE_DISCONNECTING);
		synchronized (writerMutex) {
			try {
				if (in != null) {

					in.close();
					in = null;
				}
			} catch (IOException e) {
			}

			writerMutex.notify();
		}

		try {
			if (out != null) {
				out.close();
				out = null;
			}
		} catch (IOException e) {
		}

		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
		} catch (IOException e) {
		}
		Logger.error("Disconnected from " + properties.getHost());
		setConnectionState(CONNSTATE_DISCONNECTED);
		emulator.terminate();

	}

	// @todo addChannelDataListener(listener)

	// @todo WHY Are we extending thread instead of Runnable?

	private class Reader extends Thread {

		public Reader() {
			super("Reader");
		}

		public void run() {
			try {
				read();
			} catch (Exception e) {
				if (!forceDisconnect) {
					String msg;
					if (e.getMessage() == null) {
						msg = "Internal Exception - " + e.toString();
					} else {
						msg = e.getMessage();
					}
					Logger.error(msg);
					listener.onSessionError(sessionId, msg);

				}
			} finally {
				Logger.info("Reader thread terminating.");

				doDisconnect();
			}
		}
	}

	// @todo create wrapper for listener notifications? Or just implement it
	// ourselvs
	// and pass it through if defined? Avoid null check everywhere (and chance
	// of forgetting null check)
	private class Writer extends Thread {
		public Writer() {
			super("Writer");
		}

		public void run() {
			try {
				setupConnection();
				reader.start();
				while (connState < CONNSTATE_DISCONNECTING) {
					write();
				}
				Logger.info("Writer thread complete w/ no errors.");
			} catch (Throwable e) {
				if (!forceDisconnect) {
					Logger.error(properties.getHost() + " reports: " + e.getMessage());
					listener.onSessionError(sessionId, e.getMessage());
				}
			} finally {
				Logger.info("Writer thread terminating.");
				doDisconnect();
			}
		}
	}

	public VT320 getEmulator() {
		return emulator;
	}

	public KexAgreement getAgreement() {
		return null;

	}

	public synchronized void registerDataListener(SessionDataListener listener) {
		// Assumption here that we won't have a huge number of listeners
		// registered...
		if (dataListeners.contains(listener)) {
			return;
		}
		dataListeners.addElement(listener);
	}

	public synchronized void unregisterDataListener(SessionDataListener listener) {
		dataListeners.removeElement(listener);
	}

	public boolean isWifiOverrideConnection() {
		return wifiOverride;
	}

	public int getConnectionState() {
		return connState;
	}

	public void setConnectionState(int state) {
		// This seems to be the best place to notify when we change the state to
		// connected.
		if (state == connState)
			return;
		connState = state;
		switch (state) {
			case CONNSTATE_CONNECTED:
				listener.onSessionConnected(sessionId);
				break;
			case CONNSTATE_CONNECTING:
				break;
			case CONNSTATE_DISCONNECTED:
				listener.onSessionDisconnected(sessionId, bytesWritten, bytesRead);
				break;
			case CONNSTATE_DISCONNECTING:
				break;

		}
	}

	/**
	 * @return true if this session has made a connection, and is not in the process of disconnecting.
	 */
	public boolean isConnected() {
		return connState == Session.CONNSTATE_CONNECTED || connState == Session.CONNSTATE_CONNECTING;
	}

	/**
	 * @return true if there is a live netowrk connection associated with this session
	 */
	public boolean isConnectionActive() {
		return connState != CONNSTATE_DISCONNECTED && connState != CONNSTATE_NONE;

	}

	public int getSessionId() {
		return sessionId;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserName() {
		return userName;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public Key getKey() {
		return KeyManager.getInstance().getKey(properties.getKeyId());
	}
}
