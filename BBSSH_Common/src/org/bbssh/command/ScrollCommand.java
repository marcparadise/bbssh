package org.bbssh.command;

import org.bbssh.keybinding.ExecutableCommand;

public abstract class ScrollCommand extends ExecutableCommand {
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
		return true;
	}

	public String translateParameter(Object parameter) {
		if (parameter instanceof Integer) {
			int x = ((Integer) parameter).intValue();
			if (x == 0) {
				return " one screen";
			}
			return parameter.toString() + " lines";
		}
		return super.translateParameter(parameter);
	}

}
