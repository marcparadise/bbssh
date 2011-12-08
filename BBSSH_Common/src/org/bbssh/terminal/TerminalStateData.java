/**
 * Copyright (c) 2010 Marc A. Paradise
 *
 * This file is part of "BBSSH"
 *
 * BBSSH is based upon MidpSSH by Karl von Randow.
 * MidpSSH was based upon Telnet Floyd and FloydSSH by Radek Polak.
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

package org.bbssh.terminal;

import net.rim.device.api.system.KeypadListener;

import org.bbssh.model.ConnectionProperties;
import org.bbssh.model.FontSettings;
import org.bbssh.util.Logger;

/**
 * SImple data object for tracking terminal state.
 * 
 * @author marc
 */
public class TerminalStateData {
	public static final byte TYPING_MODE_SELECT = 3;
	public static final byte TYPING_MODE_LOCAL_SCROLL = 2;
	public static final byte TYPING_MODE_HYBRID = 1;
	public static final byte TYPING_MODE_DIRECT = 0;
	// accelerometer orientation info - reference 4.7+ Display.ORIENTATION_*
	// constants
	public static final int DIRECTION_NORTH = 1;
	public static final int DIRECTION_EAST = 2;
	public static final int DIRECTION_WEST = 8;
	public static final int DIRECTION_ALL = DIRECTION_EAST | DIRECTION_NORTH
			| DIRECTION_WEST;
	public static final int DIRECTION_PORTRAIT = 32;

	public FontSettings fs = null;

	/**
	 * Session this terminal represents.
	 */
	public ConnectionProperties settings;
	/**
	 * What's the orientation mode for this terminal? (accelerometer support)
	 */
	public int orientationMode = DIRECTION_ALL;
	/**
	 * State flags for device, set temporarily upon processing keystroke event.
	 * Used by mapped handlers.
	 */
	public int deviceStateFlags;
	/**
	 * Update this to indicate if the next keypress should be submitted masked
	 * with the "ALT" keypress.
	 */
	public boolean altPressed;
	/**
	 * Update this to indicate if the next keypress shoudl be submitted masked
	 * with the "CTRL" keypress
	 */
	public boolean ctrlPressed;
	/**
	 * Update this to change the typing mode using the appropriate TYPING_MODE_*
	 * const
	 */
	public int typingMode;
	/**
	 * Top-most row current visible.
	 */
	public int topTermRow;
	/**
	 * Left-most position currently visible
	 */
	public int left;
	/**
	 * If true, a change has been made which requires the terminal screen to
	 * refresh/repaint.
	 */
	public boolean refreshRequired;
	/**
	 * Number of rows we can display.
	 */
	public int numRows;
	/**
	 * Number of columns we can display.
	 */
	public int numColsVisible;
	// Maxmimum terminal width
	public int maxWidth;
	// Maximum terminal height
	public int maxHeight;

	private int artificialStatus;
	public boolean fullRefreshRequired;
	public int debugPartialRefreshCount;
	public int debugFullRefreshCount;
	public int debugPaintCount;
	public int debugPaintBackStoreCount;
	public int debugLinePaintCount;
	public int debugLineEvalCount;
	public long debugRedrawRequestWaitTime;
	public long debugRedrawStartWaitTime;
	public int selectionCursorX;
	public int selectionCursorY;
	boolean externalUpdate = false;
	public boolean error = false;
	public boolean notified = false;
	public boolean suppressNotify = false;
	private int actualStatus;

	public TerminalStateData(ConnectionProperties prop) {
		typingMode = prop.getDefaultInputMode();
		settings = prop;
		fs = new FontSettings(prop.getFontSettings());

	}

	/**
	 * Retrieves a mask of VT320 codes indicating correct key modifiers based on
	 * this state instances "deviceState" field and alt/ctrl flags.
	 * 
	 * @param reset
	 *            indicates wehther to reset local state (ctrl and alt) to "off"
	 *            after calculating the correct flag. Note that device state is
	 *            always cleared.
	 * @return VT320 modifier flags based on state data
	 */
	public int getModifierKeyState(boolean reset) {
		int ret = getModifierKeyState(deviceStateFlags, reset);
		deviceStateFlags = 0;
		return ret;
	}

	/**
	 * Retrieves a mask of VT320 codes indicating correct key modifiers to send
	 * with a keystroke based on provided keypad state.
	 * 
	 * @param status
	 *            device keypad status mask
	 * @param reset
	 *            indicates wehther to reset local state (ctrl and alt) to "off"
	 *            after calculating the correct flag.
	 * @return VT320 modifier flags based on state data
	 */
	public int getModifierKeyState(int status, boolean reset) {
		int mode = 0;
		if ((status & KeypadListener.STATUS_SHIFT) > 0
				|| (status & KeypadListener.STATUS_SHIFT_LEFT) > 0
				|| (status & KeypadListener.STATUS_SHIFT_RIGHT) > 0) {
			mode = VT320.KEY_SHIFT;
		}
		if (ctrlPressed) {
			mode |= VT320.KEY_CONTROL;
			if (reset) {
				ctrlPressed = false;
			}
		}
		if (altPressed) {
			mode |= VT320.KEY_ALT;
			if (reset) {
				altPressed = false;
			}
		}
		return mode;
	}

	public int getArtificialStatus(boolean reset) {
		int temp = artificialStatus;
		if (reset) {
			artificialStatus = 0;
			externalUpdate = false;
		}
		return temp;
	}

	public void toggleArtificialStatus(int status, boolean externalUpdate) {
		if (!externalUpdate) { 
		//	actualStatus = ((actualStatus & status) > 0) ? actualStatus & ~status : actualStatus ^ status;
			// tracking the physical status state 
			if ((actualStatus & status) > 0) { 
				actualStatus ^= status;
			} else { 
				
				 
			}
			Logger.debug("Actual status: " + actualStatus);
			status = actualStatus; 
		}
		artificialStatus = (artificialStatus == 0) ? status : 0;
		this.externalUpdate = externalUpdate;
	}

	public void setArtificialStatus(int status) {
		actualStatus  = 0; // assuming we don't get proper notification when stauts is toggled due to leaving window focus. 
		artificialStatus = status;
		externalUpdate = false;
	}

	public boolean isExternalArtificialStatusUpdate() {
		return externalUpdate;
	}
}
