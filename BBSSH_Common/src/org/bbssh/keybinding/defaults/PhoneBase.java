package org.bbssh.keybinding.defaults;

import net.rim.device.api.system.KeyListener;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Keypad;

import org.bbssh.command.CommandConstants;
import org.bbssh.keybinding.KeyBindingHelper;
import org.bbssh.model.KeyBindingManager;
import org.bbssh.platform.PlatformServicesProvider;
import org.bbssh.terminal.VT320;

public class PhoneBase implements DefaultKeybindingSet {

	public void bindKeys(KeyBindingManager mgr, PlatformServicesProvider psp) {
		mgr.bindKey(Keypad.KEY_ENTER, KeyListener.STATUS_ALT, CommandConstants.SHOW_OVERLAY_INPUT);
		mgr.bindKey(Keypad.KEY_ESCAPE, 0, CommandConstants.POP_TERMINAL_SCREEN);
		mgr.bindKey(Keypad.KEY_ENTER, 0, CommandConstants.SEND_TERMINAL_KEY, VT320.VK_ENTER);
		mgr.bindKey(Keypad.KEY_SEND, 0, CommandConstants.INPUT_MODE);
		mgr.bindKey(Keypad.KEY_END, KeyListener.STATUS_ALT, CommandConstants.DISCONNECT_SESSION);

		if (psp.hasVolumeControls()) {
			// Increment font size
			mgr.bindKey(Keypad.KEY_VOLUME_UP, KeyListener.STATUS_ALT, CommandConstants.INCDEC_FONT_SIZE,
							new Integer(0));

			// Decrement font size
			mgr.bindKey(Keypad.KEY_VOLUME_DOWN, KeyListener.STATUS_ALT, CommandConstants.INCDEC_FONT_SIZE,
					new Integer(1));

			mgr.bindKey(Keypad.KEY_VOLUME_UP, 0, CommandConstants.SEND_TERMINAL_KEY, VT320.VK_ESCAPE);

			mgr.bindKey(Keypad.KEY_VOLUME_DOWN, 0, CommandConstants.SHOW_SCREEN_SPECIAL_KEYS);
		}

		if (psp.hasAccelerometer()) {
			mgr.bindKey(Keypad.KEY_SPEAKERPHONE, KeyListener.STATUS_ALT, CommandConstants.TOGGLE_ORIENTATION_LOCK);
		}

		if (psp.hasMuteKey()) {
			mgr.bindKey(Keypad.KEY_SPEAKERPHONE, 0, CommandConstants.SHOW_SCREEN_MACRO_LIST);
		}

		if (psp.hasNavigationMethod()) {
			mgr.bindKey(KeyBindingHelper.KEY_NAV_CLICK, KeyListener.STATUS_ALT, CommandConstants.COPY_CURRENT_BUFFER);
			if (psp.hasLeftShift())
				mgr.bindKey(KeyBindingHelper.KEY_NAV_CLICK, KeyListener.STATUS_SHIFT_LEFT | KeyListener.STATUS_SHIFT,
						CommandConstants.PASTE_TEXT);
			if (psp.hasRightShift())
				mgr.bindKey(KeyBindingHelper.KEY_NAV_CLICK, KeyListener.STATUS_SHIFT_RIGHT | KeyListener.STATUS_SHIFT,
						CommandConstants.PASTE_TEXT);

			mgr.bindKey(KeyBindingHelper.KEY_NAV_UP, 0, CommandConstants.SEND_TERMINAL_KEY, VT320.VK_UP);
			mgr.bindKey(KeyBindingHelper.KEY_NAV_DOWN, 0, CommandConstants.SEND_TERMINAL_KEY, VT320.VK_DOWN);
			mgr.bindKey(KeyBindingHelper.KEY_NAV_LEFT, 0, CommandConstants.SEND_TERMINAL_KEY, VT320.VK_LEFT);
			mgr.bindKey(KeyBindingHelper.KEY_NAV_RIGHT, 0, CommandConstants.SEND_TERMINAL_KEY, VT320.VK_RIGHT);

			mgr.bindKey(KeyBindingHelper.KEY_NAV_UP, KeypadListener.STATUS_ALT, CommandConstants.SEND_TERMINAL_KEY,
					VT320.VK_PAGE_UP);
			mgr.bindKey(KeyBindingHelper.KEY_NAV_DOWN, KeypadListener.STATUS_ALT, CommandConstants.SEND_TERMINAL_KEY,
					VT320.VK_PAGE_DOWN);
			mgr.bindKey(KeyBindingHelper.KEY_NAV_LEFT, KeypadListener.STATUS_ALT, CommandConstants.SEND_TERMINAL_KEY,
					VT320.VK_HOME);
			mgr.bindKey(KeyBindingHelper.KEY_NAV_RIGHT, KeypadListener.STATUS_ALT, CommandConstants.SEND_TERMINAL_KEY,
					VT320.VK_END);

		}

		int key = -1;
		if (psp.hasCameraFocusKey()) {
			key = KeyBindingHelper.KEY_CAMERA_FOCUS;
		} else if (psp.hasConvKey1()) {
			key = Keypad.KEY_CONVENIENCE_1;
		} else if (psp.hasConvKey2()) {
			key = Keypad.KEY_CONVENIENCE_2;

		}
		if (key > -1) {
			mgr.bindKey(key, 0, CommandConstants.SHOW_SCREEN_URL_SCRAPER);
			mgr.bindKey(key, KeypadListener.STATUS_ALT, CommandConstants.LIST_SESSIONS);
		}

	}

}
