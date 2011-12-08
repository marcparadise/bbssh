package org.bbssh.keybinding.defaults;

import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Keypad;

import org.bbssh.command.CommandConstants;
import org.bbssh.keybinding.KeyBindingHelper;
import org.bbssh.model.KeyBindingManager;
import org.bbssh.platform.PlatformServicesProvider;
import org.bbssh.terminal.VT320;

public class PhysicalKeyboardBase implements DefaultKeybindingSet {

	public void bindKeys(KeyBindingManager mgr, PlatformServicesProvider psp) {
		mgr.bindKey(KeyBindingHelper.KEY_SYM, 0, CommandConstants.TOGGLE_CONTROL);
		mgr.bindKey(KeyBindingHelper.KEY_SYM, KeypadListener.STATUS_ALT, CommandConstants.TOGGLE_ALT);

		// Some cases we can't use symbols - touchscreen only devices can't
		// support them.
		if (psp.hasTouchscreen() && !psp.hasSlider()) {
			if (psp.hasRightShift()) {
				mgr.bindKey(KeyBindingHelper.KEY_SYM, KeypadListener.STATUS_SHIFT_RIGHT | KeypadListener.STATUS_SHIFT,
						CommandConstants.SHOW_SYMBOLS);
			} else if (psp.hasLeftShift()) {
				mgr.bindKey(KeyBindingHelper.KEY_SYM, KeypadListener.STATUS_SHIFT_LEFT | KeypadListener.STATUS_SHIFT,
						CommandConstants.SHOW_SYMBOLS);

			}
		}
		mgr.bindKey(Keypad.KEY_BACKSPACE, 0, CommandConstants.SEND_TERMINAL_KEY, VT320.VK_BACK_SPACE);
		mgr.bindKey(Keypad.KEY_BACKSPACE, KeypadListener.STATUS_ALT, CommandConstants.SEND_TERMINAL_KEY,
				VT320.VK_ESCAPE);

	}

}
