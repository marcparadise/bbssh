package org.bbssh.command;

import org.bbssh.keybinding.ExecutableCommand;
import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.ui.screens.TerminalScreen;

public class ShowDebugMessage extends ExecutableCommand {

	public boolean execute(RemoteSessionInstance rsi, Object parameter) {
		if (parameter == null) { 
			return false; 
		}
		
	//	rsi.emulator.putStringStartLine(parameter.toString() + "\n");
		TerminalScreen.getInstance().showExpiringMessage(parameter.toString());
		TerminalScreen.getInstance().forceRefresh(); 
		
		
		
		return true;

	}


	public int getDescriptionResId() {
		return CMD_DESC_SHOW_DEBUG_MESSAGE;
	}

	public int getId() {
		return CommandConstants.SHOW_DEBUG_MESSAGE;
	}

	public int getNameResId() {
		return CMD_NAME_SHOW_DEBUG_MESSAGE;
	}

	public boolean isConnectionRequired() {
		return false;
	}

	public boolean isKeyBindable() {
		return false;
	}

	public boolean isMacroAction() {
		return true;
	}

	public boolean isAvailableOnCurrentPlatform() {
		return true; 
	}

	public boolean isParameterRequired() {
		return true;
	}
}
