package org.bbssh.command;

import org.bbssh.keybinding.ExecutableCommand;
import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.ui.screens.TerminalScreen;

public class ShowOverlayCommands extends ExecutableCommand {
	public int getId() {
		return CommandConstants.SHOW_OVERLAY_COMMANDS;
	}

	public boolean execute(RemoteSessionInstance rsi, Object parameter) {
		TerminalScreen.getInstance().showShortcutOverlay();
		return true;
	}

	public int getDescriptionResId() {
		return CMD_DESC_SHOW_OVERLAY_COMMANDS;
	}

	public int getNameResId() {
		return CMD_NAME_SHOW_OVERLAY_COMMANDS;
	}

	// @todo disabled for now, until we can make the buttons work on a touchscreen.

	public boolean isKeyBindable() {
		return false;
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
	}}
