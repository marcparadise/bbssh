package org.bbssh.command;

import org.bbssh.keybinding.ExecutableCommand;
import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.ui.screens.TerminalScreen;

public class ShowOverlayInput extends ExecutableCommand {
	public int getId() {

		return CommandConstants.SHOW_OVERLAY_INPUT;
	}

	public boolean execute(RemoteSessionInstance rsi, Object parameter) {
		TerminalScreen.getInstance().toggleInputOverlay();
		return true;
	}

	public int getDescriptionResId() {
		return CMD_DESC_SHOW_OVERLAY_INPUT;
	}

	public int getNameResId() {
		return CMD_NAME_SHOW_OVERLAY_INPUT;

	}

	public boolean isKeyBindable() {
		return true;
	}

	public boolean isParameterRequired() {
		return false;
	}

	public boolean isMacroAction() {
		return false;
	}

	public boolean isConnectionRequired() {
		return true;
	}

	public boolean isUILockRequired() {
		return true;
	}
}
