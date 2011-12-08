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
package org.bbssh.command;

import org.bbssh.keybinding.ExecutableCommand;
import org.bbssh.session.RemoteSessionInstance;

/**
 * Sends text representations of one or more objects to the active session via the emualator. Parameter can be either an
 * object, or an array of objects. toString is used to determine the text to display.
 * 
 */
public class SendText extends ExecutableCommand {

	public int getId() {
		return CommandConstants.SEND_TEXT;
	}

	public boolean execute(RemoteSessionInstance rsi, Object parameter) {
		if (parameter == null) {
			return false;
		}
		
		rsi.sendTwoPartString(parameter.toString());
		return true;
	}

	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.bbssh.keybinding.ExecutableCommand#getNameResId()
	 */
	public int getNameResId() {
		return CMD_NAME_SEND_KEYS;
	}

	/*
	 * (non-Javadoc)
	 * @see org.bbssh.keybinding.ExecutableCommand#isKeyBindable()
	 */
	public boolean isKeyBindable() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.bbssh.keybinding.ExecutableCommand#isMacroAction()
	 */
	public boolean isMacroAction() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.bbssh.keybinding.ExecutableCommand#getDescriptionResId()
	 */
	public int getDescriptionResId() {
		return CMD_DESC_SEND_KEYS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.bbssh.keybinding.ExecutableCommand#translateParameter(java.lang.Object)
	 */
	public String translateParameter(Object parameter) {
		if (parameter == null)
			return "";
		if (parameter instanceof Object[]) {
			Object[] p2 = (Object[]) parameter;
			switch (p2.length) {
				case 0:
					return "";
				case 1:
					return p2[0].toString();
				default:
					return p2[0].toString() + "...";
			}
		} else {
			return parameter.toString();
		}
	}

	public boolean isParameterRequired() {
		return true;
	}

	public boolean isConnectionRequired() {
		return true;
	}

	public int getNotifyBehavior() {
		return NOTIFY_BEHAVIOR_NAME_ONLY;
	}
}
