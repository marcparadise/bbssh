package org.bbssh.ui.components;

import net.rim.device.api.ui.TouchEvent;
import net.rim.device.api.ui.TouchGesture;

import org.bbssh.keybinding.KeyBindingHelper;

public class TerminalField_60 extends TerminalField_50 {
	float firstPinchMagnitude = 0.0f;

	/**
	 * Add Slider support to the base class initializer by adding an appropriate SensorListener -- the overlay edit
	 * field is not constructed when our constructor is invoked,
	 */
	public TerminalField_60() {
		super();
		
	}

	/**
	 * This override adds support for pinch gesture in 6.0
	 */
	protected boolean touchEventImpl(TouchEvent message) {
		int key = 0;
		if (message.getEvent() == TouchEvent.GESTURE) {
			TouchGesture g = message.getGesture();
			switch (g.getEvent()) {
				case TouchGesture.PINCH_BEGIN:
					// track this so at pinch end we can tell what direction the pinch moved.
					firstPinchMagnitude = g.getPinchMagnitude();
					return true;

				case TouchGesture.PINCH_END:
					float mag = g.getPinchMagnitude();
					// @todo apply multiple times based on magnitude?
					if (mag < firstPinchMagnitude) {
						key = KeyBindingHelper.KEY_TOUCH_PINCH_IN;
					} else if (mag > firstPinchMagnitude) {
						key = KeyBindingHelper.KEY_TOUCH_PINCH_OUT;
					}
					break;

				case TouchGesture.NAVIGATION_SWIPE: // getSwipeAngle/Magnitude/Direction
					key = resolveSwipeDirection(g.getSwipeDirection());
					if (key > 0) {
						// Since this was a nav swipe and not a screen swipe,
						// change the unique ID to reflect this.
						key += KeyBindingHelper.KEY_MODE_ADJUST;
					}
					break;
			}
		}
		if (key > 0 && keyEvent(key, 0, message.getTime(), false)) {
			return true;
		}
		return super.touchEventImpl(message);
	}

}
