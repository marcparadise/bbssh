package org.bbssh.command;

import org.bbssh.keybinding.ExecutableCommand;

public abstract class ShowScreenCommand extends ExecutableCommand {
	
	public boolean isKeyBindable() {
		return true;
	}

	public boolean isMacroAction() {
		return false;
	}

	public boolean isUILockRequired() {
		return true;
	}
	public final boolean isParameterRequired() {
		return false;
	}
}
