package org.bbssh.keybinding.defaults;

import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Keypad;

import org.bbssh.command.CommandConstants;
import org.bbssh.keybinding.KeyBindingHelper;
import org.bbssh.model.KeyBindingManager;
import org.bbssh.platform.PlatformServicesProvider;
import org.bbssh.terminal.VT320;

public class FullKeyboardBase implements DefaultKeybindingSet {

	public void bindKeys(KeyBindingManager mgr, PlatformServicesProvider psp) {
		mgr.bindKey(Keypad.KEY_SPACE, KeypadListener.STATUS_ALT, CommandConstants.SEND_TEXT, "&");
		mgr.bindKey(Keypad.KEY_SPACE, KeypadListener.STATUS_SHIFT_LEFT | KeypadListener.STATUS_SHIFT,
				CommandConstants.SEND_TEXT, "<");
		mgr.bindKey(Keypad.KEY_SPACE, KeypadListener.STATUS_SHIFT_RIGHT | KeypadListener.STATUS_SHIFT,
				CommandConstants.SEND_TEXT, ">");
		mgr.bindKey(KeyBindingHelper.KEY_ZERO, KeypadListener.STATUS_ALT, CommandConstants.SEND_TEXT, "=");
		mgr.bindKey(KeyBindingHelper.KEY_ZERO, KeypadListener.STATUS_SHIFT_LEFT | KeypadListener.STATUS_SHIFT,
				CommandConstants.SEND_TEXT, "[");
		mgr.bindKey(KeyBindingHelper.KEY_ZERO, KeypadListener.STATUS_SHIFT_RIGHT | KeypadListener.STATUS_SHIFT,
				CommandConstants.SEND_TEXT, "]");

		mgr.bindKey(KeyBindingHelper.KEY_CURRENCY, KeypadListener.STATUS_ALT, CommandConstants.SEND_TERMINAL_KEY,
				new Integer(VT320.VK_TAB));
		mgr.bindKey(KeyBindingHelper.KEY_CURRENCY, KeypadListener.STATUS_SHIFT_LEFT | KeypadListener.STATUS_SHIFT,
				CommandConstants.SEND_TEXT, "{");
		mgr.bindKey(KeyBindingHelper.KEY_CURRENCY, KeypadListener.STATUS_SHIFT_RIGHT | KeypadListener.STATUS_SHIFT,
				CommandConstants.SEND_TEXT, "}");
	}

}
