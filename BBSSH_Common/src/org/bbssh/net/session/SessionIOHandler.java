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

/**
 * @author Karl von Randow
 * 
 */
public interface SessionIOHandler {
	public void handleSendData(byte[] data, int offset, int length) throws IOException;

	public void handleReceiveData(byte[] data, int offset, int length) throws IOException;

	// A temporary kludge - invoke this to notify the server that the
	// terminal has resized.
	public void handleResize();

	/**
	 * Invoked when innitial connection is established to the remote host. No data has been exchanged at this point.
	 */
	public void handleConnection() throws IOException;

	public void disconnect() ; 
}