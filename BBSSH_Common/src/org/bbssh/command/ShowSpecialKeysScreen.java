package org.bbssh.command;

import net.rim.device.api.ui.UiApplication;

import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.ui.screens.SpecialKeysPopup;

public class ShowSpecialKeysScreen extends ShowScreenCommand {

	public int getDescriptionResId() {
		return CMD_DESC_SHOW_SPECIAL_KEYS_SCREEN;
	}

	public int getId() {
		return CommandConstants.SHOW_SCREEN_SPECIAL_KEYS;
	}

	public int getNameResId() {
		return CMD_NAME_SHOW_SPECIAL_KEYS_SCREEN;
	}

	public boolean isConnectionRequired() {
		return true;
	}

	public boolean execute(RemoteSessionInstance rsi, Object parameter) {
		UiApplication.getUiApplication().pushModalScreen(new SpecialKeysPopup(rsi.emulator));
		return true;
	}
}
