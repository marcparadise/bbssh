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
 * 
 * Configures the current state (for active terminal) such that the next key sent (either ordinary or special) will be
 * sent as if the user was holding down the CTRL key.
 */
public class ToggleControlKey extends ExecutableCommand {
	public int getId() {
		return CommandConstants.TOGGLE_CONTROL;
	}

	public boolean execute(RemoteSessionInstance rsi, Object parameter) {
		rsi.state.ctrlPressed = !rsi.state.ctrlPressed;
		return true;
	}

	public int getNameResId() {
		return CMD_NAME_TOGGLE_CTRL;
	}

	public int getDescriptionResId() {
		return CMD_DESC_TOGGLE_CTRL;
	}

	public boolean isKeyBindable() {
		return true;
	}

	public boolean isParameterRequired() {
		return false;
	}

	public boolean isMacroAction() {
		return true;
	}

	// @todo modify to support on-screen indicator (CTRL key)

	public boolean isConnectionRequired() {
		return true;
	}

}
