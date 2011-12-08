package org.bbssh.ui.components;

import net.rim.device.api.ui.TouchEvent;
import net.rim.device.api.ui.TouchGesture;

import org.bbssh.keybinding.KeyBindingHelper;

public class TerminalField_50 extends TerminalField_47 {
	
	public TerminalField_50() {
		super();
	}

	/**
	 * ADd support for double-tap gesture, introduced in 5.0 - we're esssentially jsut counting it 
	 * as another "tap" since we're managign tap-count for double-taps ourself in the parent class. 
	 */
	protected boolean touchEventImpl(TouchEvent message) {
		if (message.getEvent() == TouchEvent.GESTURE) {
			TouchGesture g = message.getGesture();
			if (g.getEvent() == TouchGesture.DOUBLE_TAP) {
				// we handle double tap management ourselves - just treat this as another tap. 
				// in both 5.0 and 6.0, this *replaces* the next TAP notification, so there's 
				// no chance of getting TAP, TAP, DOUBLE_TAP; instead it will be TAP, DOUBLE_TAP
				// which this handles correctly. 
				return handleRepeatingEvent(message, mapTouchEvent(message.getX(1), message.getY(1)),
						KeyBindingHelper.KEY_MODE_MULTIPLIER_TAP);  
			}
		}
		return super.touchEventImpl(message);
	}

}
