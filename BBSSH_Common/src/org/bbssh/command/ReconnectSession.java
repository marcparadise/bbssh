package org.bbssh.command;

import org.bbssh.keybinding.ExecutableCommand;
import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.session.SessionManager;

public class ReconnectSession extends ExecutableCommand {
	public int getId() {
		return CommandConstants.RECONNECT_SESSION;
	}

	public boolean execute(RemoteSessionInstance rsi, Object parameter) {
		if (rsi.isConnected())
			return false;
		SessionManager.getInstance().reconnectSession(rsi);
		return true;
	}

	public int getDescriptionResId() {
		return CMD_DESC_RECONNECT;

	}

	public int getNameResId() {
		return CMD_NAME_RECONNECT;

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

	public boolean isParameterRequired() {
		return false;
	}

}
