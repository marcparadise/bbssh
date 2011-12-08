package org.bbssh.command;

import org.bbssh.keybinding.ExecutableCommand;
import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.ui.screens.TerminalScreen;

public class RefreshScreen extends ExecutableCommand {

	public boolean execute(RemoteSessionInstance inst, Object parameter) {
		inst.emulator.setLineDirty(0, inst.emulator.getBufferSize() - 1);
		TerminalScreen.getInstance().forceRefresh();
		return true;
	}

	public int getDescriptionResId() {
		return CMD_DESC_REFRESH_SCREEN;
	}

	public int getId() {
		return CommandConstants.REFRESH_SCREEN;
	}

	public int getNameResId() {
		return CMD_NAME_REFRESH_SCREEN;
	}

	public boolean isConnectionRequired() {
		return false;
	}

	public boolean isKeyBindable() {
		return true;
	}

	public boolean isMacroAction() {
		return true;
	}

	public boolean isParameterRequired() {
		return false;
	}

	public int getNotifyBehavior() {
		return NOTIFY_BEHAVIOR_NAME_ONLY;
	}
}
