package org.bbssh.command;

import net.rim.device.api.system.KeypadListener;

import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.session.SessionManager;
import org.bbssh.ui.screens.TerminalScreen;

public class ToggleLocalAlt extends ToggleHardStatusKey {

	public boolean execute(RemoteSessionInstance inst, Object parameter) {
		inst.state.toggleArtificialStatus(KeypadListener.STATUS_ALT, true);
		if (SessionManager.getInstance().activeSession == inst) {
			TerminalScreen.getInstance().invalidateStatusIcons();
		}
		return true;
	}

	public int getId() {
		return CommandConstants.TOGGLE_LOCAL_ALT;
	}

	public int getDescriptionResId() {
		return CMD_DESC_TOGGLE_LOCAL_ALT;
	}

	public int getNameResId() {
		return CMD_NAME_TOGGLE_LOCAL_ALT;

	}

}
