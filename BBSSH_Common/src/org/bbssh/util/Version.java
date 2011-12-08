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

package org.bbssh.util;

import java.util.Calendar;
import java.util.Date;

import net.rim.blackberry.api.browser.Browser;
import net.rim.device.api.crypto.SHA1Digest;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.i18n.ResourceBundleFamily;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import org.bbssh.i18n.BBSSHResource;
import org.bbssh.model.Settings;
import org.bbssh.model.SettingsManager;
import org.bbssh.platform.PlatformServicesProvider;

/**
 * 
 */
public class Version {
	public static String getVersionSSHIDString() {
		return "SSH-2.0-BBSSH_" + getAppVersion() + "." + getBuildNumber() + " BBSSH\n";
	}

	public final static class VersionInfo {
		String versionString;

		public String toString() {
			return versionString;
		}

		public long getVersionNo() {
			return this.versionNo;
		}

		private long versionNo = 0;

		public VersionInfo(String version) throws IllegalArgumentException {
			version = version.trim();
			String[] values = Tools.splitString(version, '.');
			if (values.length == 4) {
				versionNo = Byte.parseByte(values[0]);
				versionNo = versionNo << 8;
				versionNo = versionNo | Byte.parseByte(values[1]);
				versionNo = versionNo << 16;
				versionNo = versionNo | Byte.parseByte(values[2]);
				//
				versionNo = versionNo << 32; // four bytes for build
				versionNo = versionNo | Integer.parseInt(values[3]);
				versionString = version + "(" + versionNo + ")";

			} else if (values.length == 3) {
				versionNo = Byte.parseByte(values[0]);
				versionNo = versionNo << 8;
				versionNo = versionNo | Byte.parseByte(values[1]);
				versionNo = versionNo << 16;
				versionNo = versionNo | Byte.parseByte(values[2]);
				versionNo = versionNo << 32;
				versionString = version + "(" + versionNo + ")";
				;
			} else {
				throw new IllegalArgumentException("Version in bad format, expected X.Y.Z.b, got " + version);
			}
		}

		// [major] [minor] [tiny] [build #]
		// 0x FF FF FF FF FF FF FF FF FF

		private static final long LOW_8_MASK = 0x0000FFFFL;
		private static final long LOW_16_MASK = 0x0000FFFFL;
		private static final long LOW_32_MASK = 0xFFFFFFFFL;

		public String getBuildNumber() {
			return "" + (versionNo & LOW_32_MASK);
		}

		public String getMajor() {
			return "" + ((versionNo >> 54) & LOW_8_MASK);
		}

		public String getMinor() {
			return "" + ((versionNo >> 48) & LOW_16_MASK);

		}

		public String getTiny() {

			return "" + ((versionNo >> 32) & LOW_16_MASK);
		}
	}

	private static String version;
	private static VersionInfo versionInfo;

	private static void loadVersion() {
		version = ApplicationDescriptor.currentApplicationDescriptor().getVersion();
		try {
			versionInfo = new VersionInfo(version);
		} catch (IllegalArgumentException e) {
			versionInfo = new VersionInfo("1.0.0.0");
		}

	}

	private static String getOSVersionString() {
		String softwareVersion = DeviceInfo.getSoftwareVersion();
		int pos = softwareVersion.indexOf('.');
		if (softwareVersion.length() == 0 || pos == -1)
			return "4.5.0"; // default to minimum supported version.
		return softwareVersion;

	}

	private static String[] SUPPORTED_OS_VERSIONS = new String[] {
			"71", "70", "60", "50", "47", "46", "45"
	};

	/**
	 * This rather crude method will attempt to create the requested class by working backwards through the various
	 * version strings. As an example, let's say you wnated to create an instance of "Foo" which had a platform-specific
	 * version called Foo_45 for OS 4.5, and a minimal version Foo that was compatibile with 4.3
	 * 
	 * 
	 * Let's say you're running on an OS 4.3 device. This method would attempt to create classes as follows: Foo_60
	 * Foo_50- Foo_47 Foo_46 Foo_45 Foo and would succeed only on "Foo" -- because Foo_45 would not be present in the
	 * classpath of the 4.3 build. If you were runing on a 4.5 or later device, you'd get Foo_45 assuming that Foo_45
	 * was the latest platform-specific version you'd created.
	 * 
	 * If no match is found, it willr eturn a new instance of the base class as provided. In other words as long as the
	 * class name provided is valid and has a public no-args constructor, it will always succeed. No exception is throw
	 * because of this - so if you start seeing null pointer, you'll know where to look (hint: check to make sure you're
	 * using class.getName, and NOT class.toString()). .
	 * 
	 * This operation is based on the assumption that the highest available platform version is *always* the one we want
	 * to instantiate.
	 * 
	 * @param baseName fully qualified base name of the class instance to create.
	 * @return appropriate instance of the requested class
	 */
	public static Object createOSObjectInstance(String baseName) {

		Class newClass = null;
		String className = "";
		int count = SUPPORTED_OS_VERSIONS.length;
		for (int x = 0; x < count; x++) {
			className = baseName + "_" + SUPPORTED_OS_VERSIONS[x];
			try {
				newClass = Class.forName(className);
				break;
			} catch (ClassNotFoundException e) {
				// That's OK - an OS-specific class won't exist for
				// every OS version.
			}

		}
		// No platform specific class exists to override the base version - so we'll use that one.
		if (newClass == null) {
			try {
				className = baseName;
				newClass = Class.forName(baseName);
			} catch (ClassNotFoundException e) {
				Logger.error("ClassNotFoundException when trying to create class: " + className
						+ " -- did you use SomeClass.class.getName()?");
				return null;
			}
		}
		try {
			String name = newClass.getName();
			Logger.debug("Creating platform class instance: " + name);
			return newClass.newInstance();
		} catch (InstantiationException e) {
			Logger.error("InstantiationException when trying to create class: " + className
					+ " -- is there a no-args constructor?");
		} catch (IllegalAccessException e) {
			Logger.error("IllegalAccessException when trying to create class: " + className
					+ " -- is the no-args constructor public?");
		}
		return null;

	}

	public static int getOSMajorVersion() {
		String softwareVersion = getOSVersionString();
		int pos = softwareVersion.indexOf('.');
		return Integer.parseInt(softwareVersion.substring(0, pos));

	}

	public static int getOSMinorVersion() {
		String softwareVersion = getOSVersionString();
		int pos = softwareVersion.indexOf('.');
		int pos2 = softwareVersion.indexOf('.', pos + 1);
		return Integer.parseInt(softwareVersion.substring(pos + 1, pos2));

	}

	public static String getAppVersion() {
		if (version == null) {
			loadVersion();
		}
		return version;

	}

	public static long getVersionNumber() {
		if (versionInfo == null) {
			loadVersion();
		}
		return versionInfo.getVersionNo();
	}

	public static String getBuildNumber() {

		if (versionInfo == null) {
			loadVersion();
		}
		return versionInfo.getBuildNumber();

	}

	/**
	 * Send a request to the designated BBSSH server to determine if the latest available version is a later one than
	 * our current version.
	 * 
	 * This request will also send some basic information to ensure that BBSSH is looking for the correct build for your
	 * device:
	 * <ul>
	 * <li>A one-way hash of your pin which cannot be used to identify you as an individual, but statistically allows us
	 * to know how many individual users we have.</li>
	 * <li>Your hardware name (9000, 9630, 9700, etc)</li>
	 * <li>Your OS version</li>
	 * <li>Your platform version</li>
	 * <li>Current BBSSH version</li>
	 * </ul>
	 * 
	 * @return true if your instaled version is at least the same as the one the server provides.
	 */
	private static boolean isUpToDate() {
		ResourceBundle res = ResourceBundle.getBundle(BBSSHResource.BUNDLE_ID, BBSSHResource.BUNDLE_NAME);
		// Make a non-reversible hash of the data so that we aren't gathering any personally identifiable info
		// even inadvertantly.
		int deviceId = DeviceInfo.getDeviceId();
		StringBuffer toSend = new StringBuffer(res.getString(Version.isReleaseMode() ? BBSSHResource.URL_UPDATE
				: BBSSHResource.URL_UPDATE_DEV));

		if (SettingsManager.getSettings().isAnonymousUsageStatsEnabled()) {

			SHA1Digest digest = new SHA1Digest();
			digest.update(deviceId & 0xFF);
			deviceId = (deviceId >> 8);
			digest.update(deviceId & 0xFF);
			deviceId = (deviceId >> 8);
			digest.update(deviceId & 0xFF);
			deviceId = (deviceId >> 8);
			digest.update(deviceId & 0xFF);
			byte[] value = digest.getDigest();

			toSend.append("&bbkey=").
						append(Tools.getBytesAsUnpaddedHexString(value, value.length));
			if (DeviceInfo.isSimulator()) {
				toSend.append("-simulator");
			}

			// @todo - extended capture:
			// number of macros
			// number of connections defined
			// number of times conneccted
			// hours used
			// other?

		} else {
			// In addition to not sending the unique ID derived from the
			// user's PIN, we will also send notice that the request is "anonymous". The server-side script
			// will avoid recording IP address when this is present; and excludes them from any stats,
			// This also prevents anonymous users from skewing aggregate stats, since we have no way to
			// know how many times a single anonymous user is checking for updates.

			toSend.append("&anon=true");
		}
		toSend.append("&bbplatform=").append(DeviceInfo.getPlatformVersion()).
				append("&bbname=").append(DeviceInfo.getDeviceName()).
				append("&swversion=").append(DeviceInfo.getSoftwareVersion()).append("&bbssh=").
				append(getAppVersion()).append("&bbsshos=").
				append(PlatformServicesProvider.getInstance().getOSVersion());

		Logger.info("Update check - GET URL is " + toSend.toString());
		try {
			StringBuffer data = Tools.getHTTPFileContents(toSend.toString());
			if (data != null && data.length() > 0) {
				String[] values = Tools.splitString(data.toString(), '|');
				VersionInfo remote = new VersionInfo(values[0]);
				Logger.warn("Remote Version: " + remote + " Local Version: " + versionInfo);
				if (remote.versionNo > versionInfo.versionNo) {
					return false;
				}
			} else {
				Logger.error("Remote file load failed when checking for updates.");
			}
		} catch (Throwable ex) {
			Logger.error("Error checking for updates: " + ex.getMessage() + " url: " + toSend.toString());
		}
		return true;

	}

	private static boolean releaseMode;

	public static void setReleaseMode(boolean b) {
		releaseMode = b;

	}

	public static boolean isReleaseMode() {
		return releaseMode;
	}

	private static boolean updateFound = false;

	/**
	 * check for updates; if there are any, prompt the user to download them.
	 * 
	 * @param force indicates to perform the update check no matter what - bypass user preference
	 * 
	 * @return true if an update was available
	 */
	public static boolean checkAndPromptForUpdates(final boolean force) {
		Settings s = SettingsManager.getSettings();
		// If 'force' is true the user has specifically
		// requested the update; otherwise we must verify that we're allowed to check.
		if (!force) {
			// Create a target date object that's 24*3600 (number of seconds in a day)
			Calendar target = Calendar.getInstance();
			Calendar now = Calendar.getInstance();

			target.setTime(new Date(s.getLastUpdateCheckTime() + ((s.getUpdateCheckInterval() * (24 * 3600)) * 1000)));
			now.setTime(new Date());
			if (now.before(target)) {
				Logger.warn("Next update check : " + SimpleDateFormat.
						getInstance(SimpleDateFormat.DATE_DEFAULT).formatLocal(target.getTime().getTime()));
				return false;
			}
			if (!s.isAutoCheckUpdateEnabled() || !s.getRememberOption(Settings.REMEMBER_CHECKED_UPDATE_OK)) {
				Logger
						.warn("User has disabled auto-updates or they have not yet agreed to allow auto update checks.");
				return false;
			}
		}
		s.setLastUpdateCheckTime(new Date().getTime());
		SettingsManager.getInstance().commitData();
		if (Version.isUpToDate()) {
			return false;
		}

		updateFound = true;
		if (UiApplication.getUiApplication().isEventThread()) {
			promptForUpdate();
		} else {
			UiApplication.getUiApplication().invokeAndWait(new Runnable() {
				public void run() {
					promptForUpdate();
				}
			});
		}
		return updateFound;
	}

	public static void goToDownloadSite() {
		ResourceBundleFamily res = ResourceBundleFamily.getBundle(BBSSHResource.BUNDLE_ID,
				BBSSHResource.BUNDLE_NAME);
		int url = Version.isReleaseMode() ? BBSSHResource.URL_DOWNLOAD : BBSSHResource.URL_DOWNLOAD_DEV;
		Browser.getDefaultSession().displayPage(res.getString(url));
	}

	private static void promptForUpdate() {
		ResourceBundleFamily res = ResourceBundleFamily.getBundle(BBSSHResource.BUNDLE_ID,
						BBSSHResource.BUNDLE_NAME);
		int msg = Version.isReleaseMode() ? BBSSHResource.MSG_UPDATE_AVAILABLE : BBSSHResource.MSG_UPDATE_AVAILABLE_DEV;
		// Usage and update check are actually a single call. So if the
		// use enables usage reporting but does not include update check,
		// don't harass them with a popup. In addiotn - make sure that the user has
		// both been prompted for and approved for the auto update check before we actually run it.
		if (Dialog.ask(Dialog.D_YES_NO, res.getString(msg)) == Dialog.YES) {
			goToDownloadSite();
		}

	}

	public static boolean doesAppVersionMatchOSVersion() {
		String osVersion = getOSVersionString();
		String appVersion = PlatformServicesProvider.getInstance().getOSVersion();
		int stop =  osVersion.indexOf('.') ;
		if (stop  < 1 || osVersion.length() < stop + 2 || appVersion.length() < stop + 2)  
			return true; // we can't parse this version
		
		appVersion = appVersion.substring(0, stop + 2);
		osVersion = osVersion.substring(0, stop + 2);
		Logger.warn("OS Version: " + osVersion + " Compare to app version: " + appVersion);
		return osVersion.equals(appVersion);
	}

}
