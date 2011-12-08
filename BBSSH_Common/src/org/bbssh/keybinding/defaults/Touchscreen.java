package org.bbssh.keybinding.defaults;

import net.rim.device.api.system.KeyListener;

import org.bbssh.command.CommandConstants;
import org.bbssh.keybinding.KeyBindingHelper;
import org.bbssh.model.KeyBindingManager;
import org.bbssh.platform.PlatformServicesProvider;
import org.bbssh.terminal.VT320;

public class Touchscreen implements DefaultKeybindingSet {
	public void bindKeys(KeyBindingManager mgr, PlatformServicesProvider psp) {
		mgr.bindKey(KeyBindingHelper.KEY_TOUCH_TAP_SOUTHWEST, 0, CommandConstants.TOGGLE_ALT);
		mgr.bindKey(KeyBindingHelper.KEY_TOUCH_TAP_SOUTHEAST, 0, CommandConstants.TOGGLE_CONTROL);

		mgr.bindKey(KeyBindingHelper.KEY_TOUCH_TAP_NORTHEAST, 0, CommandConstants.TOGGLE_LOCAL_ALT);
		mgr.bindKey(KeyBindingHelper.KEY_TOUCH_TAP_NORTH, 0, CommandConstants.TOGGLE_LOCAL_LSHIFT);
		mgr.bindKey(KeyBindingHelper.KEY_TOUCH_TAP_NORTHWEST, 0, CommandConstants.TOGGLE_LOCAL_RSHIFT);

		mgr.bindKey(KeyBindingHelper.KEY_TOUCH_TAP_SOUTH, 0, CommandConstants.SEND_TERMINAL_KEY, VT320.VK_DOWN);

		mgr.bindKey(KeyBindingHelper.KEY_TOUCH_HOVER_NORTH, 0, CommandConstants.LIST_SESSIONS);
		mgr.bindKey(KeyBindingHelper.KEY_TOUCH_TAP_CENTER, 0, CommandConstants.SHOW_OVERLAY_INPUT);
		mgr.bindKey(KeyBindingHelper.KEY_TOUCH_TAP_NORTH, 0, CommandConstants.SEND_TERMINAL_KEY, VT320.VK_UP);
		mgr.bindKey(KeyBindingHelper.KEY_TOUCH_TAP_SOUTH, 0, CommandConstants.SEND_TERMINAL_KEY, VT320.VK_DOWN);
		mgr.bindKey(KeyBindingHelper.KEY_TOUCH_TAP_WEST, 0, CommandConstants.SEND_TERMINAL_KEY, VT320.VK_LEFT);
		mgr.bindKey(KeyBindingHelper.KEY_TOUCH_TAP_EAST, 0, CommandConstants.SEND_TERMINAL_KEY, VT320.VK_RIGHT);

		mgr.bindKey(KeyBindingHelper.KEY_TOUCH_SWIPE_WEST, 0, CommandConstants.SEND_TERMINAL_KEY, VT320.VK_BACK_SPACE);
		mgr.bindKey(KeyBindingHelper.KEY_TOUCH_SWIPE_SOUTHEAST, 0, CommandConstants.SEND_TERMINAL_KEY, VT320.VK_ENTER);
		mgr.bindKey(KeyBindingHelper.KEY_TOUCH_SWIPE_NORTH, 0, CommandConstants.SEND_TERMINAL_KEY, VT320.VK_ESCAPE);
		mgr.bindKey(KeyBindingHelper.KEY_TOUCH_SWIPE_EAST, 0, CommandConstants.SEND_TERMINAL_KEY, VT320.VK_SPACE);
		mgr.bindKey(KeyBindingHelper.KEY_TOUCH_SWIPE_NORTHEAST, 0, CommandConstants.SEND_TERMINAL_KEY, VT320.VK_TAB);

		mgr.bindKey(KeyBindingHelper.KEY_TOUCH_SWIPE_SOUTHWEST, 0, CommandConstants.SHOW_SCREEN_URL_SCRAPER);
		mgr.bindKey(KeyBindingHelper.KEY_TOUCH_SWIPE_NORTHWEST, 0, CommandConstants.SHOW_SCREEN_MACRO_LIST);

		if (psp.isTouchClickSupported()) {
			mgr.bindKey(KeyBindingHelper.KEY_TOUCH_CLICK_SOUTHWEST, 0, CommandConstants.SHOW_SCREEN_SPECIAL_KEYS);
			mgr.bindKey(KeyBindingHelper.KEY_TOUCH_CLICK_NORTHWEST, 0, CommandConstants.COPY_CURRENT_BUFFER);
			mgr.bindKey(KeyBindingHelper.KEY_TOUCH_CLICK_NORTHEAST, 0, CommandConstants.PASTE_TEXT);
			mgr.bindKey(KeyBindingHelper.KEY_TOUCH_CLICK_CENTER, 0, CommandConstants.SHOW_HIDE_KEYBOARD);
			mgr.bindKey(KeyBindingHelper.KEY_TOUCH_CLICK_NORTH, 0, CommandConstants.SEND_TERMINAL_KEY, VT320.VK_UP);
			mgr.bindKey(KeyBindingHelper.KEY_TOUCH_CLICK_SOUTH, 0, CommandConstants.SEND_TERMINAL_KEY, VT320.VK_DOWN);
			mgr.bindKey(KeyBindingHelper.KEY_TOUCH_CLICK_WEST, 0, CommandConstants.SEND_TERMINAL_KEY, VT320.VK_LEFT);
			mgr.bindKey(KeyBindingHelper.KEY_TOUCH_CLICK_EAST, 0, CommandConstants.SEND_TERMINAL_KEY, VT320.VK_RIGHT);

		} else {
			mgr.bindKey(KeyBindingHelper.KEY_TOUCH_DBCLICK_SOUTHWEST, 0, CommandConstants.SHOW_SCREEN_SPECIAL_KEYS);
			mgr.bindKey(KeyBindingHelper.KEY_TOUCH_DBTAP_NORTHWEST, 0, CommandConstants.COPY_CURRENT_BUFFER);
			mgr.bindKey(KeyBindingHelper.KEY_TOUCH_DBTAP_NORTHEAST, 0, CommandConstants.PASTE_TEXT);
			mgr.bindKey(KeyBindingHelper.KEY_TOUCH_DBTAP_CENTER, 0, CommandConstants.SHOW_HIDE_KEYBOARD);
			mgr.bindKey(KeyBindingHelper.KEY_TOUCH_DBTAP_NORTH, 0, CommandConstants.SEND_TERMINAL_KEY,
					VT320.VK_PAGE_UP);
			mgr.bindKey(KeyBindingHelper.KEY_TOUCH_DBTAP_SOUTH, 0, CommandConstants.SEND_TERMINAL_KEY,
					VT320.VK_PAGE_DOWN);
			mgr.bindKey(KeyBindingHelper.KEY_TOUCH_DBTAP_WEST, 0, CommandConstants.SEND_TERMINAL_KEY, VT320.VK_HOME);
			mgr.bindKey(KeyBindingHelper.KEY_TOUCH_DBTAP_EAST, 0, CommandConstants.SEND_TERMINAL_KEY, VT320.VK_END);
		}
		if (psp.isTouchPinchSupported()) {
			// Increment font size
			mgr.bindKey(KeyBindingHelper.KEY_TOUCH_PINCH_OUT, KeyListener.STATUS_ALT,
					CommandConstants.INCDEC_FONT_SIZE,
					new Integer(0));

			// Decrement font size
			mgr.bindKey(KeyBindingHelper.KEY_TOUCH_PINCH_IN, KeyListener.STATUS_ALT, CommandConstants.INCDEC_FONT_SIZE,
					new Integer(1));

		}

	}
}
