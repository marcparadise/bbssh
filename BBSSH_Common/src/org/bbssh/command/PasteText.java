/*
 *  Copyright (C) 2010 Marc A. Paradise
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.bbssh.command;

import net.rim.device.api.system.Clipboard;

import org.bbssh.keybinding.ExecutableCommand;
import org.bbssh.session.RemoteSessionInstance;

/**
 * Implements paste from clipboard. Must be in direct input mode.
 */
public class PasteText extends ExecutableCommand {
	public int getId() {
		return CommandConstants.PASTE_TEXT;
	}

	public boolean execute(RemoteSessionInstance rsi, Object parameter) {
		Clipboard c = Clipboard.getClipboard();
		Object o = c.get();
		if (o == null) {
			return false;
		}
		rsi.emulator.stringTyped(o.toString());
		return true;
	}

	public int getNameResId() {
		return CMD_NAME_PASTE_TEXT;
	}

	public boolean isKeyBindable() {
		return true;
	}

	public int getDescriptionResId() {
		return CMD_DESC_PASTE_TEXT;
	}

	public boolean isParameterRequired() {
		return false;
	}

	public boolean isMacroAction() {
		return true;
	}

	public boolean isConnectionRequired() {

		return true;
	}

}
