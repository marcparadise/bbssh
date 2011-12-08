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

package org.bbssh.ui.screens;

import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.container.FlowFieldManager;
import net.rim.device.api.ui.container.PopupScreen;

import org.bbssh.keybinding.KeyBindingHelper;
import org.bbssh.keybinding.TerminalKey;
import org.bbssh.terminal.VT320;
import org.bbssh.ui.components.SimpleButtonField;

/**
 * This modal popup screen displays a list of non-standard keys which they user can choose, and submits any pressed
 * button to the emulator. If no emulator is present, it just captures the keystroke.
 */
public class SpecialKeysPopup extends PopupScreen {

	//
	int keyPressed = 0;
	private VT320 emulator;

	private class KeyButton extends SimpleButtonField {
		TerminalKey key; 

		KeyButton(TerminalKey key) {
			super(key.toString(), FIELD_HCENTER | FIELD_VCENTER | DEFAULT_CLOSE | DEFAULT_MENU);
			this.key = key; 
			super.setBackgroundColorUnfocused(Color.BLACK);
			super.setTextColorUnfocused(Color.WHITE);
			super.setBackgroundColorUnfocused(Color.LIGHTGREY);
			super.setTextColorUnfocused(Color.BLACK);
			

		}

		public void onClicked() {
			super.onClicked();
			SpecialKeysPopup.this.buttonPressed(key.getIntValue());

		}
	}

	/**
	 * Invoked when any of the special keys buttons is pressed. Stores the keypress and also sends it to the emulator if
	 * the emulator is available.
	 * 
	 * @param value
	 */
	public void buttonPressed(int value) {
		keyPressed = value;
		if (emulator != null) {
			emulator.dispatchKey(value, (char) (value & 0xFF), 0);
		}
		close();

	}

	public SpecialKeysPopup(VT320 emulator) {
		super(new FlowFieldManager(), DEFAULT_CLOSE);
		this.emulator = emulator;
		// @todo these also need to be localized? Also can we just use BindableKey and the array we already created?
		int c = KeyBindingHelper.specialTerminalKeys.length;
		for (int x = 0; x < c; x++) { 
			add(new KeyButton(KeyBindingHelper.specialTerminalKeys[x]));
		}
	}

	public int getKeyPressed() {
		return this.keyPressed;
	}
}
