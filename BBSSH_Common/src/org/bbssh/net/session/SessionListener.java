/**
 * Copyright (c) 2010 Marc A. Paradise
 *
 * This file is part of "BBSSH"
 *
 * BBSSH is based upon MidpSSH by Karl von Randow.
 * MidpSSH was based upon Telnet Floyd and FloydSSH by Radek Polak.
 *
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
 *
 */

package org.bbssh.net.session;

import org.bbssh.model.Key;

/**
 * Implement this interface to receive notification of session-related events. Note that you should marshal any UI
 * operations to ensure that they are on the main event thread -- events coming in via SessionListener will <b>not</b>
 * be on the main event thread in most cases.
 */
public interface SessionListener {
	/**
	 * Invoked when session is connected. Note that authentication is not necessarily complete at this point.
	 * 
	 * @param sessionId the session which has been connected
	 */
	public void onSessionConnected(int sessionId);

	/**
	 * Invoked when the session is disconnected.
	 * 
	 * @param sessionId the session which has been disconnected
	 * @param bytesWritten Number of bytes written in this session
	 * @param bytesRead number of bytes read in this session.
	 */
	public void onSessionDisconnected(int sessionId, int bytesWritten, int bytesRead);

	/**
	 * Invoked when an error occurs in processing the session.
	 * 
	 * @param sessionId the session which has received an error 
	 * @param errorMessage error message text. Note that any error received will cause the connection to be terminated
	 */
	public void onSessionError(int sessionId, String errorMessage);

	/**
	 * Invoked when an alert is sent by the host, typically a bell character
	 * 
	 * @param sessionId the session which is receiving the alert
	 */
	public void onSessionRemoteAlert(int sessionId);

	/**
	 * If a key is not successfully decrypted, or if is encrytped but no password provided, this callback will be
	 * invoked to obtain the password.
	 * 
	 * @todo does this belong in SessionListener, or should we use an independent listener? 
	 * 
	 * @param key
	 * @return the password supplied for the given key.
	 */
	public String getKeyPassword(int sessionId, Key key);


	/**
	 * Invoked when the emulator for a session is dirty, and portions of the emulator must be re-rendered.    
	 * @param sessionId session that has the dirty emulator 
	 */
	public void onDisplayDirty(int sessionId);
	
	/**
	 * Invoked when the emulator for a session is dirty, and the entirety of the emulator must be re-rendered.     
	 * @param sessionId session that has the dirty emulator 
	 */
	public void onDisplayInvalid(int sessionId); 

}
