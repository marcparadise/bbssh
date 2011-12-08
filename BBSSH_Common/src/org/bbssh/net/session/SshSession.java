/**
 * This file is part of "BBSSH" (c) 2010 Marc A. Paradise
 *
 * BBSSH is based upon MidpSSH by Karl von Randow. Portions
 * Copyright (C) 2004 Karl von Randow
 * MidpSSH was based upon Telnet Floyd and FloydSSH by Radek Polak.
 *
 * --LICENSE NOTICE--
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * --LICENSE NOTICE--
 *
 */
package org.bbssh.net.session;

import java.io.IOException;
import java.util.Vector;

import net.rim.device.api.ui.UiApplication;

import org.bbssh.model.ConnectionProperties;
import org.bbssh.ssh.SshIO;
import org.bbssh.ssh.SshIODebug;
import org.bbssh.ssh.kex.KexAgreement;
import org.bbssh.ui.components.SSHPromptScreen;
import org.bbssh.util.Logger;

// note commandlisterner only applicable if kybdinteractive...
public class SshSession extends Session implements SessionIOHandler {
	private SshIO sshIO;
	public int inputPacketCount = 0;
	public int outputPacketCount = 0;
	public long getAcceptedDataBytes() { 
		return sshIO.dataAcceptedBytes;
	}
	public long getReceivedDataBytes() { 
		return sshIO.dataReceivedLen;
	}


	public SshSession(ConnectionProperties prop, int sessId, SessionListener listener) {
		super(prop, sessId, listener);
	}

	public void connect() {
		String username = getUserName();
		String password = getPassword();

		if (Logger.isLevelEnabled(Logger.LOG_LEVEL_DEBUG)) {
			sshIO = new SshIODebug(this);
		} else {
			sshIO = new SshIO(this);
		}
		ConnectionProperties p = super.getProperties();
		sshIO.setUserName(username != null ? username : p.getUsername());
		sshIO.setPassword(password != null ? password : p.getPassword());
		super.connect(this);
	}

	public int getDefaultPort() {
		return 22;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see app.session.SessionIOListener#receiveData(byte[], int, int)
	 */

	public void handleReceiveData(byte[] data, int offset, int length) throws IOException {
		byte[] result = sshIO.handleSSH(data, offset, length, this);
		super.receiveData(result, 0, result.length);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see app.session.SessionIOListener#sendData(byte[], int, int)
	 */
	public void handleSendData(byte[] data, int offset, int length) throws IOException {
		if (length > 0) {
			sshIO.sendData(data, offset, length);
		} else {
			sshIO.Send_SSH_NOOP();
		}
	}

	/*
	 * Receive data send back by SshIO and send it out onto the network
	 */
	public void sendData(byte[] data) throws IOException {
		super.sendData(data, 0, data.length);
	}

	public String getTerminalID() {

		if (getProperties().getTermType().length() > 0) {
			return getProperties().getTermType();
		} else {
			return emulator.getTerminalID();
		}
	}

	public int getTerminalWidth() {
		return emulator.getTerminalWidth();
	}

	public int getTerminalHeight() {
		return emulator.getTerminalHeight();
	}

	/**
	 * Present the authentication prompt to the user, using the provided prompts and instructions. Returns the user's
	 * reply in the form of an array.
	 * 
	 * @param name User name being authenticated
	 * @param instruction Instruction to display.
	 * @param prompts array of SSHPrompts (questions) to display
	 * @param password current password as set for the user.
	 * @return array of values corresponding to the SHHPrompt array received as input.
	 */
	public String[] authPrompt(String name, String instruction, Vector prompts, String password) {
		// @todo SSHPromptScreen reference does NOT belong here, nor does this UI logic...
		final SSHPromptScreen form = new SSHPromptScreen(name, instruction, prompts, password);
		UiApplication.getUiApplication().invokeAndWait(new Runnable() {
			public void run() {
				form.doModal();
			}
		});
		return form.getResponses();
	}

	public void handleResize() {
		try {
			Logger.info("Sending terminal resize request.");
			sshIO.sendTerminalSizeUpdate();
		} catch (IOException e) {
			Logger.error("Resize packet failed: " + e.getMessage());
		}
	}

	public KexAgreement getAgreement() {
		return sshIO.getAgreement();

	}

	public void handleConnection() throws IOException {
		sshIO.onConnected();
	}

	public synchronized void disconnect() {
		try {

			sshIO.sendChannelClose();
			sshIO.sendDisconnect(11, "Finished");
		} catch (IOException e) {
			Logger.error("IOException in SshSession.disconnect [ " + e.getMessage() + " ] ");
		}

		super.disconnect();
	}

}
