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

import org.bbssh.keybinding.ExecutableCommand;
import org.bbssh.platform.PlatformServicesProvider;
import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.terminal.TerminalStateData;
import org.bbssh.ui.screens.TerminalScreen;

/**
 * 
 */
public class ChangeInputMode extends ExecutableCommand {
	public int getId() {
		return CommandConstants.INPUT_MODE;
	}

	public boolean execute(RemoteSessionInstance rsi, Object parameter) {
		int mode = -1;
		if (parameter instanceof Integer) {
			mode = ((Integer) parameter).intValue();
		}
		boolean reduced = PlatformServicesProvider.getInstance().isReducedLayout();
		
		if (mode == -1) {
			if (reduced) { 
				// reduced only allows direct (which has no function in that case) and local scroll
				// hybrid is disallowed because tehre's no way to access it - all keys are hotkeys/shortcuts. 
				if (rsi.state.typingMode == TerminalStateData.TYPING_MODE_DIRECT) { 
					rsi.state.typingMode = TerminalStateData.TYPING_MODE_LOCAL_SCROLL;
				} else { 
					rsi.state.typingMode = TerminalStateData.TYPING_MODE_DIRECT;
				}
			} else {
				rsi.state.typingMode++; 
				if (rsi.state.typingMode > TerminalStateData.TYPING_MODE_LOCAL_SCROLL) {  
					rsi.state.typingMode = TerminalStateData.TYPING_MODE_DIRECT;
				}
			}
			
		} else {
			if (reduced && mode == TerminalStateData.TYPING_MODE_HYBRID)
				return false;
			 
			rsi.state.typingMode = mode;
		}

		// Just determine the correct descriptive string to use for telling the user of the new
		// input mode.
		// @todo localization
		String msg = rsi.state.typingMode == TerminalStateData.TYPING_MODE_DIRECT ? "Direct Input"
				: rsi.state.typingMode == TerminalStateData.TYPING_MODE_HYBRID ? "Hybrid" : "Scrolling";

		TerminalScreen.getInstance().showExpiringMessage( msg);
		return true;
	}

	public boolean isKeyBindable() {
		return true;
	}

	public boolean isMacroAction() {
		return false;
	}

	public int getNameResId() {
		return CMD_NAME_CHANGE_INPUT_MODE;
	}

	public int getDescriptionResId() {
		return CMD_DESC_CHANGE_INPUT_MODE;
	}

	public boolean isParameterRequired() {
		return false;
	}

	public boolean isParameterOptional() {
		return true;

	}

	public boolean isConnectionRequired() {
		return false;
	}
}
