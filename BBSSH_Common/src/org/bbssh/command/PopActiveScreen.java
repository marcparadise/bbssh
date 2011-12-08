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

import net.rim.device.api.ui.UiApplication;

import org.bbssh.keybinding.ExecutableCommand;
import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.ui.screens.TerminalScreen;

/**
 * 
 */
public class PopActiveScreen extends ExecutableCommand {
	public int getId() {
		return CommandConstants.POP_TERMINAL_SCREEN;
	}

	public boolean execute(RemoteSessionInstance rsi, Object parameter) {
		if (TerminalScreen.getInstance().isDisplayed()) {
			// what happens if a popup/modal is on top of it?
			// answer - then this can't execute until it's removed, because
			// we require a UI lock.
			// @todo confirm that modal dialogs DO obtain and hold the UI lock.
			UiApplication.getUiApplication().popScreen(TerminalScreen.getInstance());
		}
		return true;
	}

	public boolean isKeyBindable() {
		return true;
	}

	public int getNameResId() {
		return CMD_NAME_POP_TERMINAL_SCREEN;
	}

	public int getDescriptionResId() {
		return CMD_DESC_POP_TERMINAL_SCREEN;
	}

	public boolean isParameterRequired() {
		return false;
	}

	public boolean isMacroAction() {
		return true;
	}

	public boolean isConnectionRequired() {
		return false;
	}

	public boolean isUILockRequired() {
		return true;
	}
}
