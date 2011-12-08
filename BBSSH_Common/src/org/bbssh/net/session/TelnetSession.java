/**
 * This file is part of "BBSSH" (c) 2010 Marc A. Paradise BBSSH is based upon MidpSSH by Karl von Randow. Portions
 * Copyright (C) 2004 Karl von Randow, 2010 Marc A. Paradise --LICENSE NOTICE-- This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version. This program is distributed in
 * the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA. --LICENSE NOTICE--
 */
package org.bbssh.net.session;

import java.io.IOException;

import net.rim.device.api.ui.XYPoint;

import org.bbssh.model.ConnectionProperties;
import org.bbssh.telnet.TelnetProtocolHandler;
import org.bbssh.util.Logger;

/**
 * @author Karl von Randow
 */
public class TelnetSession extends Session implements SessionIOHandler {
	public TelnetSession(ConnectionProperties prop, int sessionId, SessionListener listener) {
		super(prop, sessionId, listener);
		// Default to true in telnet and then turn off when told to
		emulator.setLocalEcho(true);
	}

	public void connect() {
		telnet = new TelnetProtocolHandler() {
			/** get the current terminal type */
			public String getTerminalType() {
				String type = getProperties().getTermType();
				if (type != null && type.length() > 0) {
					return type;
				} else {
					return emulator.getTerminalID();
				}
			}

			/** get the current window size */
			public XYPoint getWindowSize() {
				return new XYPoint(emulator.getTerminalWidth(), emulator.getTerminalHeight());
			}

			/** notify about local echo */
			public void setLocalEcho(boolean echo) {
				emulator.localecho = echo;
			}

			/** notify about EOR end of record */
			public void notifyEndOfRecord() {
				// only used when EOR needed, like for line mode
			}

			/** write data to our back end */
			public void write(byte[] b) throws IOException {
				/*for ( int i = 0; i < b.length; i++ ) {
				System.out.println( "SEND " + b[i] + "=" + (char) b[i] );
				}*/
				TelnetSession.this.sendData(b, 0, b.length);
			}
		};

		super.connect(this);
		// Nothing special is required - if we have a socket (and we do, to get here)
		// then we're connected.
		setConnectionState(CONNSTATE_CONNECTED);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see telnet.Session#defaultPort()
	 */
	public int getDefaultPort() {
		return 23;
	}

	private TelnetProtocolHandler telnet;

	/*
	 * (non-Javadoc)
	 *
	 * @see terminal.TerminalIOListener#receiveData(byte[])
	 */
	public void handleReceiveData(byte[] data, int offset, int length) throws IOException {
		telnet.inputfeed(data, offset, length);
		int n;
		do {
			n = telnet.negotiate(data, offset, length);
			if (n > 0) {
				/*for ( int i = offset; i < offset + n; i++ ) {
				System.out.println( "RECV " + data[i] + "=" + (char) data[i] );
				}*/
				TelnetSession.this.receiveData(data, offset, n);
			}
		} while (n != -1);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see terminal.TerminalIOListener#sendData(byte[])
	 */
	public void handleSendData(byte[] data, int offset, int length) throws IOException {
		if (length > 0) {
			telnet.transpose(data, offset, length);
		} else {
			telnet.sendTelnetNOP();
		}
	}

	public void handleResize() {
		try {
			telnet.sendNAWS();
		} catch (IOException e) {
			Logger.error("IOException in TelnetSession.handleResize [ " + e.getMessage() + " ] ");
			emulator.putStringStartLine("Resize failed: " + e.getMessage());
			getListener().onDisplayInvalid(getSessionId());
		}
	}

	public void handleConnection() throws IOException {
		// Telnet sessions don't need to take special actions when connection is made.

	}

	public void disconnect() {
		super.disconnect();
		setConnectionState(CONNSTATE_DISCONNECTED);
	}
}
