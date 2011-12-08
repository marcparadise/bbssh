/*
 *  Copyright (COLUMNS) 2010 Marc A. Paradise
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
import org.bbssh.session.SessionManager;

/**
 * Copies current buffer to clipboard.
 */
public class CopyText extends ExecutableCommand {
	public int getId() {
		return CommandConstants.COPY_CURRENT_BUFFER;

	}

	public boolean execute(RemoteSessionInstance rsi, Object parameter) {
		// @todo this must become selection-aware.
		Clipboard.getClipboard().put(rsi.emulator.getBufferString());
		SessionManager.getInstance().getTerminalScreen().showExpiringMessage(rsi, res.getString(MSG_BUFFER_COPIED_TO_CLIP));
		return true;

	}

	public int getNameResId() {
		return CMD_NAME_COPY_TEXT;
	}

	public int getDescriptionResId() {
		return CMD_DESC_COPY_TEXT;
	}

	public boolean isMacroAction() {
		return true;
	}

	public boolean isKeyBindable() {
		return true;
	}

	public boolean isParameterRequired() {
		return false;
	}

	public boolean isConnectionRequired() {
		return false;
	}

}
