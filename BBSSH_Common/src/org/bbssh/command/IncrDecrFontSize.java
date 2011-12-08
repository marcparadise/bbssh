package org.bbssh.command;

import net.rim.device.api.ui.component.Status;

import org.bbssh.exceptions.FontNotFoundException;
import org.bbssh.keybinding.ExecutableCommand;
import org.bbssh.model.FontSettings;
import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.ui.screens.TerminalScreen;
import org.bbssh.util.Logger;

public class IncrDecrFontSize extends ExecutableCommand {
	public int getId() {
		return CommandConstants.INCDEC_FONT_SIZE;
	}

	public boolean execute(RemoteSessionInstance rsi, Object parameter) {
		if (rsi == null || parameter == null || !(parameter instanceof Integer)) {
			return false;
		}

		FontSettings s = TerminalScreen.getInstance().getFontSettings();
		FontSettings ns = new FontSettings(rsi.state.fs);
		int action = ((Integer) parameter).intValue();
		if (action == 0) {
			ns.setFontSize((byte) (s.getFontSize() + 1));
		} else {
			ns.setFontSize((byte) (s.getFontSize() - 1));
		}

		try {
			TerminalScreen.getInstance().updateFontSettings(ns);
		} catch (FontNotFoundException e) {
			Logger.error("Unexpected fontNotFoundException in IncrDecrFontSize.execute [ " + e.getMessage() + " ] ");

			// Because we validate sizes, this shouldn't occur... but we'll recover cleanly by simply not updating.
			Status.show(res.getString(TERMINAL_MSG_INVALID_FONT_SIZE));
		}
		return true;
	}

	public int getDescriptionResId() {
		return CMD_DESC_INCDEC_FONT_SIZE;
	}

	public int getNameResId() {
		return CMD_NAME_INCDEC_FONT_SIZE;
	}

	public boolean isParameterRequired() {
		return true;
	}

	public boolean isKeyBindable() {
		return true;
	}

	public boolean isMacroAction() {
		return false;
	}

	public String translateParameter(Object parameter) {
		// @todo string table...
		if (!(parameter instanceof Integer)) {
			return "";
		}
		int action = ((Integer) parameter).intValue();
		if (action == 0) {
			return "Increment";
		}
		return "Decrement";
	}

	public boolean isConnectionRequired() {
		return true;
	}

	public boolean isUILockRequired() {
		return true;
	}
	public int getNotifyBehavior() {
		return NOTIFY_BEHAVIOR_NAME_AND_VALUE;
	}
}
