package org.bbssh.command;

import org.bbssh.keybinding.ExecutableCommand;
import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.session.SessionManager;

public class DisconnectSession extends ExecutableCommand {
	public int getId() {
		return CommandConstants.DISCONNECT_SESSION;
	}

	public boolean execute(RemoteSessionInstance rsi, Object parameter) {
		SessionManager.getInstance().disconnectSession(rsi);
		return true;

	}

	public int getDescriptionResId() {
		return CMD_DESC_DISCONNECT;
	}

	public int getNameResId() {
		return CMD_NAME_DISCONNECT;
	}

	public boolean isMacroAction() {
		return true;
	}

	public boolean isParameterRequired() {
		return false;
	}

	public boolean isKeyBindable() {
		return true;
	}

	public boolean isConnectionRequired() {
		return true;
	}

}
