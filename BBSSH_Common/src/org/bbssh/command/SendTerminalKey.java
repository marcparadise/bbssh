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

import net.rim.device.api.ui.Keypad;

import org.bbssh.keybinding.ExecutableCommand;
import org.bbssh.keybinding.KeyBindingHelper;
import org.bbssh.session.RemoteSessionInstance;

/**
 * This command sends a non-standard key (such as DEL, F10, etc)
 */
public class SendTerminalKey extends ExecutableCommand {
	public int getId() {
		return CommandConstants.SEND_TERMINAL_KEY;
	}

	public boolean execute(RemoteSessionInstance rsi, Object parameter) {
		if (parameter == null || !(parameter instanceof Integer)) {
			return false;
		}

		int code = ((Integer) parameter).intValue();

		// outbound status is not set in binding, but is based on
		// current state keyboard modifiers.
		int status = rsi.state.getModifierKeyState(0, true);
		char c = Keypad.map(code);
		if (c == 0) {
			// Some things don't map, like tab. Take those as-is.
			code = (code & 0xFF);
		}

		// @todo - make sure this will work with function keys and other special keys!
		rsi.emulator.dispatchKey(code, (char) code, status);

		return true;
	}

	public boolean isKeyBindable() {
		return true;
	}

	public int getNameResId() {
		return CMD_NAME_SEND_TERMINAL_KEY;
	}

	public int getDescriptionResId() {
		return CMD_DESC_SEND_TERMINAL_KEY;
	}

	public String translateParameter(Object parameter) {
		if (parameter == null || !(parameter instanceof Integer))
			return "";
		int code = ((Integer) parameter).intValue();
		return KeyBindingHelper.getTerminalKeyFriendlyName(code);
	}

	public boolean isParameterRequired() {
		return true;
	}

	public boolean isMacroAction() {
		return true;
	}

	public boolean isConnectionRequired() {
		return true;
	}
}
