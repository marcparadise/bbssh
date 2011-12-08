package org.bbssh.ui.screens;

import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.TouchEvent;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.VirtualKeyboard;

import org.bbssh.util.Logger;

public class TerminalScreen_47 extends TerminalScreen {
	boolean ignore = false;
	boolean loggedKeyboardNotice = false;

	public TerminalScreen_47() {
		super(); 
	}

	/**
	 * Override of touchEvent that routes the event to either the terminal field or the overlay manager as appropriate.
	 */
	protected boolean touchEvent(TouchEvent message) {
		// if ! overlay active and if (in coords of field) then send touchevent to field

		// @todo - what about event that starts in overlay and ends outside of overlay?

		if (isOverlayActive()) {
			if (getOverlayManager().isCoordinateInOverlayFields(message.getX(1), message.getY(1))) {
				return super.touchEvent(message);
			}
			// Close the overlay if any touch occurs not in the overlay... then ignore everything until we get "UP"
			if (message.getEvent() == TouchEvent.DOWN) {
				ignore = true;
				hideOverlayManager();
			}

		}
		if (ignore) {
			if (message.getEvent() == TouchEvent.UP) {
				ignore = false;
			}
			return true;
		}
		//		
		// if (getFieldAtLocation(message.getX(1), message.getY(1)) == termField.getIndex()) {
		// return ((TerminalField_47)termField).touchEvent(message);
		// }

		return super.touchEvent(message);
	}

	protected void setVirtualKeyboardVisibility(boolean visible) {
		showKeyboard(visible);

	}

	public void showKeyboard(boolean show) {
		if (!isVisible()) {
			Logger.debug("TerminalScreen_47.showKeyboard - screen not visible.");
			return;
		}

		Screen s = UiApplication.getUiApplication().getActiveScreen();
		if (s == null) {
			Logger.debug("TerminalScreen_47.showKeyboard - no active screen found.");
			return;
		}

		VirtualKeyboard k = s.getVirtualKeyboard();
		if (k == null) {
			if (!loggedKeyboardNotice) {
				Logger.debug("TerminalScreen_47.showKeyboard - no virtual keyboard found.");
				loggedKeyboardNotice = true;
			}
			return;
		}

		// Using SHOW instead of SHOW_FORCE/HIDE_FORCE - "FORCE" means that you're
		// locking that owption in place -- that is the user can't hide a SHOW_FORCE
		// keyboard with down swipe, and nothing they can do will cause a HIDE_FORCE
		// keyboard to show.
		if (show) {
			k.setVisibility(VirtualKeyboard.SHOW);
		} else {
			k.setVisibility(VirtualKeyboard.HIDE);
		}
	}

	public boolean isVirtualKeyboardVisible() {
		VirtualKeyboard k = getVirtualKeyboard();
		if (k == null)
			return false;
		int vis = k.getVisibility();
		if (vis == VirtualKeyboard.SHOW || vis == VirtualKeyboard.SHOW_FORCE)
			return true;
		return false;

	}
}
