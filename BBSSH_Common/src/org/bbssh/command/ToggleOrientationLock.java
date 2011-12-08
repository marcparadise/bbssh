package org.bbssh.command;

import org.bbssh.keybinding.ExecutableCommand;
import org.bbssh.platform.PlatformServicesProvider;
import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.terminal.TerminalStateData;
import org.bbssh.ui.screens.TerminalScreen;

public class ToggleOrientationLock extends ExecutableCommand {

	public boolean execute(RemoteSessionInstance rsi, Object parameter) {
		if (rsi.state == null)
			return false;
		PlatformServicesProvider p = PlatformServicesProvider.getInstance();
		int msg;
		if (rsi.state.orientationMode == TerminalStateData.DIRECTION_ALL) {
			rsi.state.orientationMode = p.lockOrientation(0);
			msg = MSG_ORIENTATION_LOCKED;
		} else {
			rsi.state.orientationMode = p.unlockOrientation();
			msg = MSG_ORIENTATION_UNLOCKED;
		}
		TerminalScreen.getInstance().showExpiringMessage(rsi, res.getString(msg));
		return true;

	}

	public int getDescriptionResId() {
		return CMD_DESC_TOGGLE_ORIENTATION_LOCK;
	}

	public int getId() {
		return CommandConstants.TOGGLE_ORIENTATION_LOCK;
	}

	public int getNameResId() {
		return CMD_NAME_TOGGLE_ORIENTATION_LOCK;
	}

	public boolean isConnectionRequired() {
		return false;
	}

	public boolean isKeyBindable() {
		return true;
	}

	public boolean isMacroAction() {
		return true;
	}

	public boolean isAvailableOnCurrentPlatform() {
		return PlatformServicesProvider.getInstance().hasTouchscreen();
	}

	public boolean isParameterRequired() {
		return false;
	}

}
