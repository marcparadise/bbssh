package org.bbssh.command;

import net.rim.device.api.ui.UiApplication;

import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.ui.screens.KeybindingScreen;

public class ShowKeybindingScreen extends ShowScreenCommand {

	public int getDescriptionResId() {
		return CMD_DESC_SHOW_KEYBINDING_SCREEN;
	}

	public int getId() {
		return CommandConstants.SHOW_SCREEN_KEYBINDINGS;
	}

	public int getNameResId() {
		return CMD_NAME_SHOW_KEYBINDING_SCREEN;
	}

	public boolean isConnectionRequired() {
		return false;
	}

	public boolean execute(RemoteSessionInstance rsi, Object parameter) {
		UiApplication.getUiApplication().pushScreen(new KeybindingScreen());
		return true;
	}

}