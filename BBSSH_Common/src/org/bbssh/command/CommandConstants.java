/*
 *  Copyright (C) 2010 Marc A. Paradise
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.bbssh.command;

/**
 * contains a list of numeric IDs used to uniquely identify commands. These constants are used to dynamically map the
 * command objects to saved instances, without needing to serialize the objects themselves.
 * 
 * This allows us to extend the list of available commands or alter the underlying classes, without breaking persisted
 * data.
 * 
 * Important: the sequence defined here must match the initialization sequence in KeyBindingManager.
 * 
 */
public final class CommandConstants {
	public static final int NONE = 0; // special palceholder for display purposes only.
	public static final int COPY_CURRENT_BUFFER = 1;
	public static final int INPUT_MODE = 2;
	public static final int MOVEMENT_KEY = 3;
	public static final int PASTE_TEXT = 4;
	public static final int POP_TERMINAL_SCREEN = 5;
	public static final int RUN_MACRO = 6;
	public static final int SEND_TEXT = 7;
	public static final int TOGGLE_ORIENTATION_LOCK = 8;
	public static final int SEND_TERMINAL_KEY = 9;
	public static final int TOGGLE_ALT = 10;
	public static final int TOGGLE_CONTROL = 11;
	public static final int LIST_SESSIONS = 12;
	public static final int SHOW_SCREEN_SPECIAL_KEYS = 13;
	public static final int SHOW_SYMBOLS = 14;
	public static final int TAKE_SCREENSHOT = 15;
	public static final int INCDEC_FONT_SIZE = 16;
	public static final int WAIT_FOR_ACTIVITY = 17;
	public static final int SHOW_OVERLAY_COMMANDS = 18;
	public static final int SHOW_OVERLAY_INPUT = 19;
	public static final int DISCONNECT_SESSION = 20;
	public static final int RECONNECT_SESSION = 21;
	public static final int SHOW_HIDE_KEYBOARD = 22;
	public static final int WAIT = 23;
	public static final int SHOW_SCREEN_FONT = 24;
	public static final int SHOW_SCREEN_URL_SCRAPER = 25;
	public static final int SHOW_SCREEN_MACRO_LIST = 26;
	public static final int SHOW_SCREEN_SESSION_DETAIL = 27;
	public static final int SHOW_SCREEN_KEYBINDINGS = 28;
	public static final int SHOW_DEBUG_MESSAGE = 29;
	public static final int SCROLL_UP_LINES = 30;
	public static final int SCROLL_DOWN_LINES = 31;
	public static final int REFRESH_SCREEN = 32;
	public static final int TOGGLE_LOCAL_ALT = 33;
	public static final int TOGGLE_LOCAL_LSHIFT = 34;
	public static final int TOGGLE_LOCAL_RSHIFT = 35;
	
}
