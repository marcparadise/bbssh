/*
 *  Copyright (COLUMNS) 2010 Marc A. Paradise
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

import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.terminal.TerminalStateData;

/**
 * Handler for directional keys (up/down/left/right); depending on input mode this will behavior differently. In input
 * mode, it will just send the appropriate key to the remote session (up/down/loeft/rigth). But in scrollback mode, it
 * will move the cursor in the local buffer.
 */
public class SendMovementKey extends SendTerminalKey {
	public int getId() {
		return CommandConstants.MOVEMENT_KEY;
	}

	public boolean execute(RemoteSessionInstance rsi, Object parameter) {
		// If we're not in scrollback mode, super has what it needs to process this command -- so we'll
		// let it take care of this.
		if (rsi.state.typingMode != TerminalStateData.TYPING_MODE_LOCAL_SCROLL) {
			return super.execute(rsi, parameter);
		}
		// int key = ((Integer) parameter).intValue();
		// // In scrollback mode, we need move the cursor.
		// XYPoint cursor = emulator.getCursorPositon();
		// XYPoint oldCursor = new XYPoint(cursor);
		// // @todo - this does cannot be part of the same bindable command.
		//
		//		
		// // If Shift is pressed we are moving and selecting. Otherwise we are
		// // just moving.
		// // If the cursor is at the screen bounary we must also scroll up/down.
		// boolean swapDir;
		//
		// switch (key) {
		// case KeyBindingHelper.KEY_NAV_UP:
		// --cursor.y;
		// if (cursor.y < 0) {
		// // Don't move the cursor
		// cursor.y = 0;
		// data.updateTopPosition(data.top - 1);
		// }
		// swapDir = true;
		// break;
		// case KeyBindingHelper.KEY_NAV_DOWN:
		// ++cursor.y;
		// if (cursor.y > data.numRows) {
		// cursor.y = data.numRows;
		// data.updateTopPosition(data.top + 1);
		// }
		// swapDir = false;
		// break;
		// case KeyBindingHelper.KEY_NAV_LEFT:
		// --cursor.x;
		// if (cursor.x < 0) {
		// cursor.x = 0;
		// data.updateLeftPosition(data.left - 1);
		// }
		// swapDir = true;
		// break;
		// case KeyBindingHelper.KEY_NAV_RIGHT:
		// ++cursor.x;
		// if (cursor.x > data.numCols) {
		// data.updateLeftPosition(data.left + 1);
		// cursor.x = data.numCols;
		// }
		// swapDir = false;
		//
		// break;
		// default:
		// return false;
		// }
		// if (cursor.y < 0 || cursor.x < 0 || cursor.y > data.numRows || cursor.x > data.numCols) {
		// return false;
		// }
		// // Invert
		// // If shifted, we are in select mode - "highlight" the selection.
		// if ((KeypadListener.STATUS_SHIFT & data.deviceStateFlags) > 0) {
		// if (swapDir) {
		// emulator.toggleAttributeStateForRange(oldCursor.y, oldCursor.x, cursor.y, cursor.x, VT320.INVERT);
		// } else {
		// emulator.toggleAttributeStateForRange(cursor.y, cursor.x, oldCursor.y, oldCursor.x, VT320.INVERT);
		// }
		// }
		// emulator.setCursorPosition(cursor.x, cursor.y);

		return true;
	}

	public boolean isKeyBindable() {
		return true;
	}

	public int getNameResId() {
		return CMD_NAME_DIRECTIONAL_KEY;
	}

	public int getDescriptionResId() {
		return CMD_DESC_DIRECTIONAL_KEY;
	}

	public boolean isParameterRequired() {
		return true;
	}

}
