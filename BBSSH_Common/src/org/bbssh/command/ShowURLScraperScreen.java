package org.bbssh.command;

import net.rim.device.api.ui.UiApplication;

import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.ui.screens.URLListScreen;

public class ShowURLScraperScreen extends ShowScreenCommand {

	public int getDescriptionResId() {
		return CMD_DESC_SHOW_URL_SCREEN;
	}

	public int getId() {
		return CommandConstants.SHOW_SCREEN_URL_SCRAPER;
	}

	public int getNameResId() {
		return CMD_NAME_SHOW_URL_SCREEN;
	}

	public boolean isConnectionRequired() {
		return false;
	}

	public boolean execute(RemoteSessionInstance rsi, Object parameter) {
		UiApplication.getUiApplication().pushModalScreen(new URLListScreen(rsi.emulator.getBufferString()));
		return true; 

	}

}
