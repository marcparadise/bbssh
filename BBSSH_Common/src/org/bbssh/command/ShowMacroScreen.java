package org.bbssh.command;

import net.rim.device.api.ui.UiApplication;

import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.ui.screens.macros.MacroList;

public class ShowMacroScreen extends ShowScreenCommand {

	public int getDescriptionResId() {
		return CMD_DESC_SHOW_MACRO_SCREEN;
	}

	public int getId() {
		return CommandConstants.SHOW_SCREEN_MACRO_LIST;
	}

	public int getNameResId() {
		return CMD_NAME_SHOW_MACRO_SCREEN;
	}

	public boolean isConnectionRequired() {
		return true;
	}

	public boolean isUILockRequired() {
		return true;
	}

	public boolean execute(RemoteSessionInstance rsi, Object parameter) {
		UiApplication.getUiApplication().pushModalScreen(new MacroList());
		return true;

	}

}