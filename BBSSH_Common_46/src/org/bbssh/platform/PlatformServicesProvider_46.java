package org.bbssh.platform;

public class PlatformServicesProvider_46 extends PlatformServicesProvider {
	public PlatformServicesProvider_46() {
		super();
	}

	/*
	 * This support was introduced with 4.6
	 * (non-Javadoc)
	 * @see org.bbssh.platform.PlatformServicesProvider#isNotificationSupportAvailable()
	 */
	public boolean isNotificationSupportAvailable() {
		return true;

	}
	public String getOSVersion() { 
		return "4.6"; 
	}
}
