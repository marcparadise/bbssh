package org.bbssh.command;

import org.bbssh.keybinding.ExecutableCommand;
import org.bbssh.platform.PlatformServicesProvider;

public abstract class ToggleHardStatusKey extends ExecutableCommand {
	public boolean isAvailableOnCurrentPlatform() {
		// Hard status key toggle is only available on Touchscreens
		PlatformServicesProvider psp = PlatformServicesProvider.getInstance();
		return psp.hasTouchscreen() || psp.isReducedLayout() ;
	}

	public boolean isMacroAction() {
		return false;
	}

	public boolean isConnectionRequired() {
		return true;
	}

	public boolean isUILockRequired() {
		return false;
	}

	public boolean isKeyBindable() {
		return true;
	}

	public boolean isParameterRequired() {
		return false;
	}
}
