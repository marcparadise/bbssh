package org.bbssh.ui.components.keybinding;

import org.bbssh.keybinding.ExecutableCommand;

public class KeybindState {

	/** The bitshifted keycode + status that this is bound to */
	public long combinedKey;
	/** Description that's displayed for the associated keyCode */
	String bindingDescription;
	/** the command that is CURRENTLY configured, which may have changed from the original */
	public ExecutableCommand command;
	/** the parameter that is CURRENTLY set, which may have changed from the original */
	public Object parameter;
	/** true if the user wants these changes committed */
	public boolean changed;

	/* Convenience constructor to represent the state of a single key binding that has been previously bound. */
	public KeybindState(long keyCode, String description, ExecutableCommand command, Object parameter) {
		this.combinedKey = keyCode;
		this.command = command;
		this.parameter = parameter;
		this.bindingDescription = description;
	}

	public KeybindState(String description, ExecutableCommand command, Object parameter) {
		this(-1, description, command, parameter);
	}
//
//	/** Convenience constructor to represent the state of a single key binding that is not previously bound. */
//	public KeybindState(long keyCode, String description, ExecutableCommand command, Object parameter) {
//		this(keyCode, description, command, parameter, null);
//	}

}
