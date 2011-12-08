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
import org.bbssh.model.Macro;
import org.bbssh.model.MacroManager;
import org.bbssh.model.macros.MacroExecutor;
import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.util.Logger;

/**
 * Runs a macro. If appropriate it will execute it in a background thread.
 */
public class RunMacro extends ExecutableCommand {
	public int getId() {
		return CommandConstants.RUN_MACRO;
	}

	public boolean execute(RemoteSessionInstance rsi, Object parameter) {
		if (parameter == null || !(parameter instanceof String)) {
			return false;
		}
		Macro m = MacroManager.getInstance().getMacro((String) parameter);
		if (m == null) {
			Logger.error("Error executing macro: could not find macro " + parameter);
			return false;
		}
		MacroExecutor.getInstance().executeMacro(m, rsi);
		return true;
	}

	public int getNameResId() {
		return CMD_NAME_RUN_MACRO;
	}

	public int getDescriptionResId() {
		return CMD_DESC_RUN_MACRO;
	}

	public boolean isKeyBindable() {
		return true;
	}

	public boolean isParameterRequired() {
		return true;
	}

	public boolean isMacroAction() {
		return true;
	}

	public boolean isConnectionRequired() {
		// Not all commands possible via macros require a connection - eg copy terminal, reconnect
		return false;
	}

	public int getNotifyBehavior() {
		return NOTIFY_BEHAVIOR_VALUE_ONLY;
	}
}
