package org.bbssh.ui.screens;

import net.rim.device.api.ui.UiApplication;

import org.bbssh.command.CommandConstants;
import org.bbssh.command.ShowScreenCommand;
import org.bbssh.session.RemoteSessionInstance;

public class ShowSessionDetailScreen extends ShowScreenCommand {

	public int getDescriptionResId() {
		return CMD_DESC_SHOW_SESSION_DETAIL;
	}

	public int getId() {
		return CommandConstants.SHOW_SCREEN_SESSION_DETAIL;
	}

	public int getNameResId() {
		return CMD_NAME_SHOW_SESSION_DETAIL;
	}

	public boolean isConnectionRequired() {
		return true;
	}


	public boolean execute(RemoteSessionInstance rsi, Object parameter) {
		UiApplication.getUiApplication().pushScreen(new SessionDetailScreen()); 
		return true; 
	}

}
