package org.bbssh.platform;

import org.bbssh.keybinding.KeyBindingHelper;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Touchscreen;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.VirtualKeyboard;

/**
 * A platform-version specific class that provides UI services.
 * 
 * @author marc
 * 
 */
public class PlatformServicesProvider_47 extends PlatformServicesProvider {
	public PlatformServicesProvider_47() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see org.bbssh.platform.PlatformServiceProvider#getGraphicsObjectForBitmap(net.rim.device.api.system.Bitmap)
	 */
	public Graphics getGraphicsObjectForBitmap(Bitmap bmp) {
		return Graphics.create(bmp);
	}

	/*
	 * (non-Javadoc)
	 * @see org.bbssh.platform.PlatformServiceProvider#hasHardwareKeyboard()
	 */
	public boolean hasHardwareKeyboard() {
		int layout = Keypad.getHardwareLayout();
		if (layout == Keypad.HW_LAYOUT_32 || layout == Keypad.HW_LAYOUT_39 ||
				layout == Keypad.HW_LAYOUT_LEGACY || layout == Keypad.HW_LAYOUT_PHONE ||
				layout == Keypad.HW_LAYOUT_REDUCED || layout == Keypad.HW_LAYOUT_REDUCED_24 ||
				layout == KeyBindingHelper.HW_LAYOUT_ITUT) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.bbssh.platform.PlatformServiceProvider#hasVirtualKeyboard()
	 */
	public boolean hasVirtualKeyboard() {
		return VirtualKeyboard.isSupported();
	}

	/*
	 * (non-Javadoc)
	 * @see org.bbssh.platform.PlatformServiceProvider#isTouchscreenSupported()
	 */
	public boolean hasTouchscreen() {
		if (super.hasTouchscreen())
			return true;

		// In 4.7 and later we CAN support touchscreen - but we aren't guaranteed to have a touchscreen device
		// eg a Bold with 5.0 installed. The OS supports touchscreen, but the device does not.
		return Touchscreen.isSupported();
	}

	public int unlockOrientation() {
		// int orientation = Display.getOrientation();
		// boolean mismatch = orientation != (Display.DIRECTION_EAST | Display.DIRECTION_NORTH |
		// Display.DIRECTION_WEST);

		Ui.getUiEngineInstance().
				setAcceptableDirections(Display.DIRECTION_EAST | Display.DIRECTION_NORTH | Display.DIRECTION_WEST);
		return super.unlockOrientation();

	}

	public int lockOrientation(int orientation) {
		if (orientation == 0)
			orientation = Display.getOrientation();
		// boolean mismatch = (orientation != Display.getOrientation());

		// @todo - may need to register as an accelerometer listener, since this will allow reversal of
		// landscape mode - getOrietnation doe snot return east/west., only port/land.
		Ui.getUiEngineInstance().setAcceptableDirections(orientation);
		// If our orientation has changed ... force a relayout to fix it?
		// if (mismatch) {
		// BBSSHApp.inst().repaint();
		// BBSSHApp.inst().updateDisplay();
		// }

		return orientation;

	}

	public boolean isTouchClickSupported() {
		// 4.7 and 5.0 support "Click" events if the touchscreen is present - because of that we don't need
		// to check for Surepress technology as it's guaranteed for all 4.7/5.0 touchscreen devices. In 6.0
		// (in which we don't know which devices will/won't have it)
		return hasTouchscreen();
	}
	public String getOSVersion() { 
		return "4.7"; 
	}

}
