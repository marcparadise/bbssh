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

package org.bbssh.keybinding;

import net.rim.device.api.ui.UiApplication;

import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.ui.screens.TerminalScreen;

/**
 * This decorator for ExecutableCommand provides for persisting relevant parameters required by the command, and
 * providing them to the command instance at execution time.
 * 
 * @author marc
 */
public class BoundCommand {
	private ExecutableCommand command;
	Object param;

	public BoundCommand(ExecutableCommand cmd) {
		this.command = cmd;
	}

	public BoundCommand(ExecutableCommand cmd, int data) {
		this(cmd, new Integer(data));
	}

	public BoundCommand(ExecutableCommand cmd, Object data) {
		this.command = cmd;
		this.param = data;
	}

	/**
	 * Executes the command associated with this instance, if the state is such that execution can occur and if the
	 * command is supported on the current platform.
	 * 
	 * @param inst the session
	 * @param enableNotify if true, the command will be permitted to update the status bar with descriptive info.
	 * @return true if command execuetd successfully.
	 */
	public boolean execute(RemoteSessionInstance inst, boolean enableNotify) {
		// @todo - fruther refine isConnectionRequired? 
		if (command.isAvailableOnCurrentPlatform() &&
				(inst.isConnected() || !command.isConnectionRequired())) {
			int notify = enableNotify ? command.getNotifyBehavior() : ExecutableCommand.NOTIFY_BEHAVIOR_NONE;
			StringBuffer b = new StringBuffer();
			switch (notify) {
				case ExecutableCommand.NOTIFY_BEHAVIOR_NAME_ONLY:
					b.append(command.toString());
					break;

				case ExecutableCommand.NOTIFY_BEHAVIOR_NAME_AND_VALUE:
					b.append(command.toString());
					b.append('-');
					// no break;

				case ExecutableCommand.NOTIFY_BEHAVIOR_VALUE_ONLY:
					b.append(command.translateParameter(param));
					break;
			}
			if (b.length() > 0) {
				TerminalScreen.getInstance().showExpiringMessage(b.toString());
			}

			if (command.isUILockRequired()) {
				Object lock = UiApplication.getUiApplication().getAppEventLock();
				synchronized (lock) {
					return command.execute(inst, param);
				}

			}
			return command.execute(inst, param);
		}

		return false;

	}

	public ExecutableCommand getCommand() {
		return command;
	}

	public void setCommand(ExecutableCommand command) {
		this.command = command;
	}

	public Object getParam() {
		return param;
	}

	public void setParam(Object param) {
		this.param = param;
	}
	public String toString() {
		if (command == null)
			return "No Command";
		if (param == null)
			return command.toString();
		
		return command.toString() + " " + command.translateParameter(param); 
	}
	
}
