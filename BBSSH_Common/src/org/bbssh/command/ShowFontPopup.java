package org.bbssh.command;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Status;

import org.bbssh.exceptions.FontNotFoundException;
import org.bbssh.model.FontSettings;
import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.ui.components.FontPicker;
import org.bbssh.ui.screens.TerminalScreen;
import org.bbssh.util.Logger;

public class ShowFontPopup extends ShowScreenCommand {

	public int getDescriptionResId() {
		return CMD_DESC_SHOW_SET_FONT_SCREEN;
	}

	public int getId() {
		return CommandConstants.SHOW_SCREEN_FONT;
	}

	public int getNameResId() {
		return CMD_NAME_SHOW_SET_FONT_SCREEN;
	}

	public boolean isConnectionRequired() {
		return true;
	}

	public boolean execute(RemoteSessionInstance rsi, Object parameter) {
		FontPicker p = new FontPicker(rsi.state.fs);
		UiApplication.getUiApplication().pushModalScreen(p);
		FontSettings fs = p.getUpdatedFontSettings();
		if (fs != null) {
			try {
				TerminalScreen.getInstance().updateFontSettings(fs);
			} catch (FontNotFoundException e) {
				Status.show(res.getString(TERMINAL_MSG_FONT_NOT_LOADED));
				Logger.error("FontNotFoundException in ShowFontPopup.execute [ " + e.getMessage() + " ] ");
			}
		}
		return true;
	}

	public boolean isUILockRequired() {
		return true;
	}
}