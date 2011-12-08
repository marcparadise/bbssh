package org.bbssh.platform;

import net.rim.device.api.system.Sensor;
import net.rim.device.api.system.capability.DeviceCapability;
import net.rim.device.api.ui.Keypad;

public class PlatformServicesProvider_60 extends PlatformServicesProvider_50 {
	public PlatformServicesProvider_60() {
		super();
	}

	public boolean isTouchPinchSupported() {
		return hasTouchscreen();
	}

	public boolean hasNavSwipeSupport() {
		// For NOW, we are assuming that if a device has 6.0 it
		// also has a trackapd; and we know that 6.0 allows trackpad swipes.
		// if 6.0 support is extended to any devices with trackball,
		// we'll have to revisit this.
		return true;
	}

	public boolean isTouchClickSupported() {
		return DeviceCapability.isSupported(DeviceCapability.TYPE_TOUCH_CLICK);
	}

	public boolean isEnhancedTitlebarSupported() {
		return true;
	}

	public boolean hasSlider() {
		return Sensor.isSupported(Sensor.SLIDE);
	}

	public boolean hasHardwareKeyboard() {
		return DeviceCapability.isPhysicalKeyboardSupported();
	}

	public boolean isSliderExtended() {
		if (hasSlider()) {
			// Note - in OS7 this has started throwing illegalargumentexception 
			// if the device doesn't support the requested sensor. 
			return Sensor.getState(Sensor.SLIDE) == Sensor.STATE_SLIDE_OPEN;
		}
		return true; // make the app aware of keyboard...  

	}

	public boolean hasAccelerometer() {
		return DeviceCapability.isRotationSupported();
	}

	/**
	 * We need to supercede the official version of this method, because that mfunction will not return accurate
	 * information when a slider keyboard is retracted.
	 */
	public int getHardwareLayout() {
		int layout = super.getHardwareLayout();
		switch (layout) {
			case Keypad.HW_LAYOUT_32:
			case Keypad.HW_LAYOUT_39:
			case Keypad.HW_LAYOUT_HANDW_RECOGNITION:
			case Keypad.HW_LAYOUT_ITUT:
			case Keypad.HW_LAYOUT_PHONE:
			case Keypad.HW_LAYOUT_REDUCED:
			case Keypad.HW_LAYOUT_REDUCED_24:
			case Keypad.HW_LAYOUT_LEGACY:
				// shouldn't happen... but just in case..
				return layout;

		}
		// The remaining layout options are touch screen - so let's check to see
		// if it's *really* a touchscreen.
		if (DeviceCapability.isPhysicalKeyboardSupported()) {
			// We actually don't know this for future models, but it's the best
			// we can do.
			return Keypad.HW_LAYOUT_39;
		}
		return layout;
	}

	public String getOSVersion() {
		return "6.0";
	}
}
