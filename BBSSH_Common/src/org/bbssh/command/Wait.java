package org.bbssh.command;

import org.bbssh.keybinding.ExecutableCommand;
import org.bbssh.session.RemoteSessionInstance;

/**
 * This command is intended to be used with macros. It inserts a delay of the specified number of milliseconds into the
 * execution flow.
 * 
 */
public class Wait extends ExecutableCommand {

	public boolean execute(RemoteSessionInstance rsi, Object parameter) {
		if (!(parameter instanceof Integer))
			return false;

		try {
			Thread.sleep(((Integer) parameter).intValue());
		} catch (InterruptedException e) {
		}
		return true;
	}

	public int getDescriptionResId() {
		return CMD_DESC_WAIT;
	}

	public int getId() {
		return CommandConstants.WAIT;
	}

	public int getNameResId() {
		return CMD_NAME_WAIT;
	}
	
	public boolean isConnectionRequired() {

		return false;
	}

	public boolean isKeyBindable() {
		return false;
	}

	public boolean isMacroAction() {
		return true;
	}

	public boolean isParameterRequired() {
		return true;
	}

	public String translateParameter(Object parameter) {
		if (!(parameter instanceof Integer))
			return "";
		return ((Integer) parameter).intValue() + " ms.";
	}

	public int getNotifyBehavior() {
		return NOTIFY_BEHAVIOR_NAME_AND_VALUE;
	}

}
