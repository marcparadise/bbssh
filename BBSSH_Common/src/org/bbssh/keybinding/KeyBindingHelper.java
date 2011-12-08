/**
 * Copyright (c) 2010 Marc A. Paradise
 *
 * This file is part of "BBSSH"
 *

 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */
package org.bbssh.keybinding;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.util.IntHashtable;

import org.bbssh.i18n.BBSSHResource;
import org.bbssh.platform.PlatformServicesProvider;
import org.bbssh.terminal.VT320;

/**
 * @todo this entire class is a mess- a much better refactor is required when time allows.
 */
public class KeyBindingHelper implements BBSSHResource {

	// Here we define many 'virtual key' mappings that either don't have corresponding
	// constants in the platform; or may not exist in a specific platform version.
	// @todo do we just want to add virt keys for all possible keys, instead of just
	// the ones missing or platform specific?

	// These are safely clear of the standard key mappings
	// though they DO conflict with key status - so they can't
	// be used in conjunction as int values (ie, packed long only).
	public static final int KEY_NAV_UP = 0x01 << 16;
	public static final int KEY_NAV_DOWN = 0x02 << 16;
	public static final int KEY_NAV_LEFT = 0x03 << 16;
	public static final int KEY_NAV_RIGHT = 0x04 << 16;
	public static final int KEY_NAV_CLICK = 0x05 << 16;
	public static final int KEY_CURRENCY = 0x06 << 16;
	public static final int KEY_ZERO = 0x07 << 16;
	public static final int KEY_CAMERA_FOCUS = 0x08 << 16;
	public static final int KEY_LOCK = 0x09 << 16;

	// We use our own constants so that we can include generic binding support, without having to
	// split the keybinding core/engine into OS-specific components.
	public static final int KEY_TOUCH_ID_START = 0x10 << 16;
	// 0x10 available - touch
	// 0x11 available - touch
	// 0x12 available - touch
	// 0x13 available - touch

	public static final int KEY_TOUCH_PINCH_IN = 0x15 << 16;
	public static final int KEY_TOUCH_PINCH_OUT = 0x16 << 16;

	public static final int KEY_TOUCH_SWIPE_NORTH = 0x20 << 16;
	public static final int KEY_TOUCH_SWIPE_SOUTH = 0x21 << 16;
	public static final int KEY_TOUCH_SWIPE_EAST = 0x22 << 16;
	public static final int KEY_TOUCH_SWIPE_WEST = 0x23 << 16;
	public static final int KEY_TOUCH_SWIPE_SOUTHEAST = 0x24 << 16;
	public static final int KEY_TOUCH_SWIPE_SOUTHWEST = 0x25 << 16;
	public static final int KEY_TOUCH_SWIPE_NORTHEAST = 0x26 << 16;
	public static final int KEY_TOUCH_SWIPE_NORTHWEST = 0x27 << 16;

	public static final int KEY_NAV_SWIPE_NORTH = 0x30 << 16;
	public static final int KEY_NAV_SWIPE_SOUTH = 0x31 << 16;
	public static final int KEY_NAV_SWIPE_EAST = 0x32 << 16;
	public static final int KEY_NAV_SWIPE_WEST = 0x33 << 16;

	public static final int KEY_TOUCH_TAP_CENTER = 0x40 << 16;
	public static final int KEY_TOUCH_TAP_NORTH = 0x41 << 16;
	public static final int KEY_TOUCH_TAP_SOUTH = 0x42 << 16;
	public static final int KEY_TOUCH_TAP_EAST = 0x43 << 16;
	public static final int KEY_TOUCH_TAP_WEST = 0x44 << 16;
	public static final int KEY_TOUCH_TAP_SOUTHEAST = 0x45 << 16;
	public static final int KEY_TOUCH_TAP_SOUTHWEST = 0x46 << 16;
	public static final int KEY_TOUCH_TAP_NORTHEAST = 0x47 << 16;
	public static final int KEY_TOUCH_TAP_NORTHWEST = 0x48 << 16;

	public static final int KEY_TOUCH_DBTAP_CENTER = 0x50 << 16;
	public static final int KEY_TOUCH_DBTAP_NORTH = 0x51 << 16;
	public static final int KEY_TOUCH_DBTAP_SOUTH = 0x52 << 16;
	public static final int KEY_TOUCH_DBTAP_EAST = 0x53 << 16;
	public static final int KEY_TOUCH_DBTAP_WEST = 0x54 << 16;
	public static final int KEY_TOUCH_DBTAP_SOUTHEAST = 0x55 << 16;
	public static final int KEY_TOUCH_DBTAP_SOUTHWEST = 0x56 << 16;
	public static final int KEY_TOUCH_DBTAP_NORTHEAST = 0x57 << 16;
	public static final int KEY_TOUCH_DBTAP_NORTHWEST = 0x58 << 16;

	public static final int KEY_TOUCH_CLICK_CENTER = 0x60 << 16;
	public static final int KEY_TOUCH_CLICK_NORTH = 0x61 << 16;
	public static final int KEY_TOUCH_CLICK_SOUTH = 0x62 << 16;
	public static final int KEY_TOUCH_CLICK_EAST = 0x63 << 16;
	public static final int KEY_TOUCH_CLICK_WEST = 0x64 << 16;
	public static final int KEY_TOUCH_CLICK_SOUTHEAST = 0x65 << 16;
	public static final int KEY_TOUCH_CLICK_SOUTHWEST = 0x66 << 16;
	public static final int KEY_TOUCH_CLICK_NORTHEAST = 0x67 << 16;
	public static final int KEY_TOUCH_CLICK_NORTHWEST = 0x68 << 16;

	public static final int KEY_TOUCH_DBCLICK_CENTER = 0x70 << 16;
	public static final int KEY_TOUCH_DBCLICK_NORTH = 0x71 << 16;
	public static final int KEY_TOUCH_DBCLICK_SOUTH = 0x72 << 16;
	public static final int KEY_TOUCH_DBCLICK_EAST = 0x73 << 16;
	public static final int KEY_TOUCH_DBCLICK_WEST = 0x74 << 16;
	public static final int KEY_TOUCH_DBCLICK_SOUTHEAST = 0x75 << 16;
	public static final int KEY_TOUCH_DBCLICK_SOUTHWEST = 0x76 << 16;
	public static final int KEY_TOUCH_DBCLICK_NORTHEAST = 0x77 << 16;
	public static final int KEY_TOUCH_DBCLICK_NORTHWEST = 0x78 << 16;

	public static final int KEY_TOUCH_HOVER_CENTER = 0x80 << 16;
	public static final int KEY_TOUCH_HOVER_NORTH = 0x81 << 16;
	public static final int KEY_TOUCH_HOVER_SOUTH = 0x82 << 16;
	public static final int KEY_TOUCH_HOVER_EAST = 0x83 << 16;
	public static final int KEY_TOUCH_HOVER_WEST = 0x84 << 16;
	public static final int KEY_TOUCH_HOVER_SOUTHEAST = 0x85 << 16;
	public static final int KEY_TOUCH_HOVER_SOUTHWEST = 0x86 << 16;
	public static final int KEY_TOUCH_HOVER_NORTHEAST = 0x87 << 16;
	public static final int KEY_TOUCH_HOVER_NORTHWEST = 0x88 << 16;

	/**
	 * Add this to the 'touch swipe' value to get 'nav swipe' mappings for the same directions; and add to TAP value to
	 * get DOUBLE_TAP value. Add *2 to get CLICK value, *3 for DBCLICK, and *4 for HOVER
	 */
	public static final int KEY_MODE_ADJUST = 0x10 << 16;
	public static final int KEY_MODE_MULTIPLIER_TAP = 0;
	public static final int KEY_MODE_MULTIPLIER_DOUBLE_TAP = 1;
	public static final int KEY_MODE_MULTIPLIER_CLICK = 2;
	public static final int KEY_MODE_MULTIPLIER_DOUBLE_CLICK = 3;

	public static final int KEY_MODE_MULTIPLIER_HOVER = 4;
	public static final int KEY_MODE_MULTIPLIER_CLICK_REPEAT = 5;
	public static final int KEY_TOUCH_ID_END = 0x9F << 16;

	// These are used in allowing HW_LAYOUT_REDUCED_24 keypads to use
	// each key as a binding.
	public static final int KEY_R24_QW = 0xA0 << 16; // 'Q'
	public static final int KEY_R24_ER = 0xA1 << 16; // 'E'
	public static final int KEY_R24_TY = 0xA2 << 16; // 'T'
	public static final int KEY_R24_UI = 0xA3 << 16; // 'U'
	public static final int KEY_R24_OP = 0xA4 << 16; // 'O'
	public static final int KEY_R24_AS = 0xA5 << 16; // 'A'
	public static final int KEY_R24_DF = 0xA6 << 16; // 'D'
	public static final int KEY_R24_GH = 0xA7 << 16; // 'G'
	public static final int KEY_R24_JK = 0xA8 << 16; // 'J'
	public static final int KEY_R24_L = 0xA9 << 16; // 'L'
	public static final int KEY_R24_ZX = 0xAA << 16; // 'Z'
	public static final int KEY_R24_CV = 0xAB << 16; // 'C'
	public static final int KEY_R24_BN = 0xAC << 16; // 'B'
	public static final int KEY_R24_0 = 0xAD << 16; // ' '
	public static final int KEY_R24_M = 0xAE << 16; // 'M'

	public static final int KEY_ITUT_1 = 0xB0 << 16; // 1!?,. 'Q'
	public static final int KEY_ITUT_2 = 0xB1 << 16; // abc 'T'
	public static final int KEY_ITUT_3 = 0xB2 << 16; // def 'O'
	public static final int KEY_ITUT_4 = 0xB3 << 16; // ghi 'A'
	public static final int KEY_ITUT_5 = 0xB4 << 16; // jkl 'G'
	public static final int KEY_ITUT_6 = 0xB5 << 16; // mno 'L'
	public static final int KEY_ITUT_7 = 0xB6 << 16; // pqrs 'Z'
	public static final int KEY_ITUT_8 = 0xB7 << 16; // tuv 'B'
	public static final int KEY_ITUT_9 = 0xB8 << 16; // wxyz 'W'
	public static final int KEY_ITUT_0 = 0xB9 << 16; // ' ' ' '

	/** Identical to Keypad.HW_LAYOUT_ITUT introduced in 6.0 and value used in 5.0 */
	public static final int HW_LAYOUT_ITUT = 1230263636;

	/** Keypad 5.0: public static final int KEY_FORWARD = 4100; */
	public static final int KEY_FORWARD_SDK = 4100;
	/** Keypad 5.0: public static final int KEY_BACKWARD = 4101; */
	public static final int KEY_BACKWARD_SDK = 4101;
	/** Keypad 4.7: public static final int KEY_CAMERA_FOCUS = 211 */
	public static final int KEY_CAMERA_FOCUS_SDK = 211;
	/** Keypad 5.0: public static final int KEY_LOCK = 4099 */
	public static final int KEY_LOCK_SDK = 4099;

	/**
	 * Keypad 4.5: public static final int KEY_DELETE = 127; For clarity, as KEY_DELETE is actually "SYM" in all models
	 * that we are concerned with.
	 */
	public static final int KEY_SYM = Keypad.KEY_DELETE;

	// @todo Okay, this is sloppy too. ACtually... ALL of these strings belong in the RRC.
	public static String[] FONT_CHANGE_CHOICES = new String[] { "Increase", "Decrease" };
	
	// Categories 
	public static final int CAT_TERMINAL = 0;
	public static final int CAT_TOUCH_TAP = 1;
	public static final int CAT_TOUCH_DOUBLE_TAP = 2;
	public static final int CAT_TOUCH_CLICK = 3;
	public static final int CAT_TOUCH_DOUBLE_CLICK = 4;
	public static final int CAT_TOUCH_GESTURE = 5;
	public static final int CAT_TOUCH_HOVER = 6;
	public static final int CAT_MEDIA = 7;
	public static final int CAT_PHONE = 8;
	public static final int CAT_KEYBOARD = 9;
	public static final int CAT_NAV = 10;

	/**
	 * Non-directional terminal keys.
	 */
	public static TerminalKey[] specialTerminalKeys = new TerminalKey[] {
			new TerminalKey(VT320.VK_TAB, VTKEYNAME_TAB),
			new TerminalKey(VT320.VK_DELETE, VTKEYNAME_DELETE),
			new TerminalKey(VT320.VK_ENTER, VTKEYNAME_ENTER),
			new TerminalKey(VT320.VK_CLEAR, VTKEYNAME_CLEAR),
			new TerminalKey(VT320.VK_PAUSE, VTKEYNAME_PAUSE),
			new TerminalKey(VT320.VK_SPACE, VTKEYNAME_SPACE),
			new TerminalKey(VT320.VK_ESCAPE, VTKEYNAME_ESCAPE),
			new TerminalKey(VT320.VK_BACK_SPACE, VTKEYNAME_BACK_SPACE),
			new TerminalKey(VT320.VK_PAGE_UP, VTKEYNAME_PAGE_UP),
			new TerminalKey(VT320.VK_PAGE_DOWN, VTKEYNAME_PAGE_DOWN),
			new TerminalKey(VT320.VK_HOME, VTKEYNAME_HOME),
			new TerminalKey(VT320.VK_END, VTKEYNAME_END),
			new TerminalKey(VT320.VK_F1, VTKEYNAME_F1),
			new TerminalKey(VT320.VK_F2, VTKEYNAME_F2),
			new TerminalKey(VT320.VK_F3, VTKEYNAME_F3),
			new TerminalKey(VT320.VK_F4, VTKEYNAME_F4),
			new TerminalKey(VT320.VK_F5, VTKEYNAME_F5),
			new TerminalKey(VT320.VK_F6, VTKEYNAME_F6),
			new TerminalKey(VT320.VK_F7, VTKEYNAME_F7),
			new TerminalKey(VT320.VK_F8, VTKEYNAME_F8),
			new TerminalKey(VT320.VK_F9, VTKEYNAME_F9),
			new TerminalKey(VT320.VK_F10, VTKEYNAME_F10),
			new TerminalKey(VT320.VK_F11, VTKEYNAME_F11),
			new TerminalKey(VT320.VK_F12, VTKEYNAME_F12)
	};

	/**
	 * Direction terminal keys. These are also "special" keys in terms of how we send them, but we apply different
	 * handling in terms of scrollback so we separate them out for usage in a different command.
	 */
	public static TerminalKey[] directionalTerminalKeys = new TerminalKey[] {
			new TerminalKey(VT320.VK_UP, VTKEYNAME_UP),
			new TerminalKey(VT320.VK_DOWN, VTKEYNAME_DOWN),
			new TerminalKey(VT320.VK_LEFT, VTKEYNAME_LEFT),
			new TerminalKey(VT320.VK_RIGHT, VTKEYNAME_RIGHT) };

	static IntHashtable terminalKeys;

	public static TerminalKey[] getTerminalKeys() {
		return specialTerminalKeys;
	}

	public static TerminalKey[] getDirectionalTerminalKeys() {
		return directionalTerminalKeys;
	}

	// @todo we may also want to support Hold-and-scroll...
	public static int SUPPORTED_KEYPAD_MODIFIERS = KeypadListener.STATUS_ALT | KeypadListener.STATUS_SHIFT | KeypadListener.STATUS_SHIFT_LEFT
				| KeypadListener.STATUS_SHIFT_RIGHT;


	public static String getModifierFriendlyName(int status) {
		ResourceBundle res = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);
		if ((status & KeypadListener.STATUS_ALT) > 0) {
			return res.getString(MODNAME_ALT);
		}
		// ShiftX gets reported as "Left Shift" constnat - but we don't want to confuse
		// the user, who will not see a left shift on that keyboard; so we check for this first.
		if (PlatformServicesProvider.getInstance().hasShiftX()) {
			if ((status & KeypadListener.STATUS_SHIFT_LEFT) > 0 ||
					(status & KeypadListener.STATUS_SHIFT) > 0) {
				return res.getString(MODNAME_SHIFT);
			}
		}
		
		if ((status & KeypadListener.STATUS_SHIFT_LEFT) > 0) {
			return res.getString(MODNAME_LSHIFT);
		}
		if ((status & KeypadListener.STATUS_SHIFT_RIGHT) > 0) {
			return res.getString(MODNAME_RSHIFT);
		}
		if ((status & KeypadListener.STATUS_SHIFT) > 0) {
			return res.getString(MODNAME_SHIFT);
		}

		// We don't currently support this for binding modifier, I would like to in the future but it will require
		// shifting some of our processing to key-up...
		// if ((status & KeypadListener.STATUS_KEY_HELD_WHILE_ROLLING) > 0) {
		// return res.getString(MODNAME_KEY_ROLL);
		// }
		return "";

	}

	/**
	 * More of the same mess... serious cleanup needed in here.
	 * 
	 * @param map
	 * @param keys
	 */
	private static void populateLookupMap(TerminalKey[] keys) {
		if (terminalKeys == null)
			terminalKeys = new IntHashtable(100);
		for (int x = keys.length - 1; x >= 0; x--) {
			terminalKeys.put(keys[x].getIntValue(), keys[x]);
		}
	}

	public static String getTerminalKeyFriendlyName(int code) {
		if (terminalKeys == null) {
			populateLookupMap(getTerminalKeys());
			populateLookupMap(getDirectionalTerminalKeys());
		}
		Object o = terminalKeys.get(code);
		if (o == null) {
			return "Invalid Key";
		}
		return o.toString();
	}

	public static int getTerminalKeyIndex(int intValue) {
		for (int x = specialTerminalKeys.length - 1; x >= 0; x--) {
			if (specialTerminalKeys[x].getIntValue() == intValue) {
				return x;
			}
		}
		return -1;
	}

	public static int getMovementTerminalKeyIndex(int intValue) {

		for (int x = directionalTerminalKeys.length - 1; x >= 0; x--) {
			if (directionalTerminalKeys[x].getIntValue() == intValue) {
				return x;
			}
		}
		return -1;
	}

	public static Object[] getFontChangeChoices() {
		return FONT_CHANGE_CHOICES;

	}

}
