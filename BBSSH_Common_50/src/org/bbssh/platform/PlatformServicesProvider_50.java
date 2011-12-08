package org.bbssh.platform;

import net.rim.device.api.ui.Keypad;

import org.bbssh.platform.PlatformServicesProvider_47;

public class PlatformServicesProvider_50 extends PlatformServicesProvider_47 {
	public PlatformServicesProvider_50() {
		super();
	}

	public boolean hasForwardBackwardMediaKeys() {
		return Keypad.hasMediaKeys();
	}

	public boolean hasMuteKey() {

		return Keypad.hasMuteKey();
	}

	public String getOSVersion() { 
		return "5.0"; 
	}
}
