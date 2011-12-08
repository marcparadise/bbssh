package org.bbssh.command;

import org.bbssh.keybinding.ExecutableCommand;
import org.bbssh.platform.PlatformServicesProvider;
import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.ui.screens.TerminalScreen;

public class ToggleKeyboardState extends ExecutableCommand {
	public int getId() {
		return CommandConstants.SHOW_HIDE_KEYBOARD;
	}

	public boolean execute(RemoteSessionInstance rsi, Object parameter) {
		TerminalScreen screen = TerminalScreen.getInstance();
		if (screen.isVirtualKeyboardVisible())
			screen.hideVirtualKeyboard();
		else
			screen.showVirtualKeyboard(true);

		return true;
	}

	public int getDescriptionResId() {
		return CMD_DESC_SHOW_HIDE_KEYBOARD;

	}

	public int getNameResId() {
		return CMD_NAME_SHOW_HIDE_KEYBOARD;

	}

	public boolean isKeyBindable() {
		return true;
	}

	public boolean isMacroAction() {
		return false;
	}

	public boolean isParameterRequired() {
		return false;
	}

	public boolean isAvailableOnCurrentPlatform() {
		if (PlatformServicesProvider.getInstance().hasVirtualKeyboard()) {
			return true;
		}
		return false;

	}

	public boolean isConnectionRequired() {
		return false;
	}

}
