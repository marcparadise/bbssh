/**
 * This file is part of "BBSSH" (c) 2010 Marc A. Paradise --LICENSE NOTICE-- This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version. This program is distributed in
 * the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA. --LICENSE NOTICE--
 */
package org.bbssh.model;

import org.bbssh.util.Version;

import net.rim.device.api.synchronization.SyncObject;

public class Settings implements SyncObject, DataObject {

	private boolean rememberOptions[] = new boolean[10];

	public static final int REMEMBER_OPT_BACKGROUND_ON_CLOSE = 0;
	public static final int REMEMBER_DO_NOT_SHOW_DATA_WARN = 1;
	public static final int REMEMBER_LICENSE_AGREEMENT_COMPLETE = 2;
	public static final int REMEMBER_CHECKED_UPDATE_OK = 3;
	public static final int REMEMBER_CHECKED_SEND_USAGE_STATS_OK = 4;

	public static final int REMEMBER_PERM_SHOWN = 5;

	private boolean dirty = false;
	private boolean autoCheckUpdates = true;
	private String APN = ""; // additional connection properties that user can specify.
	private String APNUserName = "";
	private String APNPassword = "";
	private boolean showPlaintextPassword = false;
	// @todo Future support:
	private boolean vibrateOnAlertEnabled = true;
	private boolean homeScreenNotificationIconEnabled = true;
	private boolean anonymousUsageStatsEnabled = true;
	private boolean messageIntegrationEnabled = true;
	private boolean backgroundOnCloseEnabled = true;
	private boolean titlebarDisplayEnabled = true;
	private boolean showKeyboardOnSliderClose = false;
	private ConnectionProperties defaultConnectionProperties;

	// Internal fields used not for settings, but state/activity tracking.

	private int lastCleanupVersion = 0;
	private long lastSaveVersionNumber = Version.getVersionNumber();
	private int updateCheckInterval = 1;
	private boolean disableKeybindsWhenOnCall = true;
	private long lastUpdateCheckTime = 0;

	public boolean isAutoCheckUpdateEnabled() {
		return autoCheckUpdates;
	}

	public void setAutoCheckUpdates(boolean autoCheckUpdates) {
		this.autoCheckUpdates = autoCheckUpdates;
	}

	/**
	 * get APN to be used for TCP connections.
	 * 
	 * @return APN value
	 */
	public String getAPN() {
		return APN;
	}

	/**
	 * set APN to be used for TCP connections. Will override any APN set globally for the device.
	 * 
	 * @param APN
	 */
	public void setAPN(String APN) {
		if (APN == null) {
			this.APN = "";
		} else {
			this.APN = APN;
		}
	}

	/**
	 * get APN password used for TCP connections.
	 * 
	 * @return APN password
	 */
	public String getAPNPassword() {
		return APNPassword;
	}

	/**
	 * set APN password used for TCP connections. If APN and APN username is present this will override any value
	 * configured on the device itself.
	 * 
	 * @param APNPassword
	 *            the APN password
	 */
	public void setAPNPassword(String APNPassword) {
		if (APNPassword == null) {
			this.APNPassword = "";
		} else {
			this.APNPassword = APNPassword;
		}
	}

	/**
	 * get APN username used for TCP connections.
	 * 
	 * @return APN user name.
	 */
	public String getAPNUserName() {
		return APNUserName;
	}

	/**
	 * set APN username to be used for TCP connections. If APN and APN username are present this will override any value
	 * configured on the device itself.
	 * 
	 * @param APNUserName
	 */
	public void setAPNUserName(String APNUserName) {
		if (APNUserName == null) {
			this.APNUserName = "";
		} else {
			this.APNUserName = APNUserName;
		}
	}

	/**
	 * true if - when entering passwords - user wants them displayed in plaintext.
	 * 
	 * @return true if passwords should be displayed as normal text.
	 */
	public boolean getShowPlaintextPassword() {
		return this.showPlaintextPassword;
	}

	/**
	 * Sets indicator that determiens whether passwords (when being entered) are displayed in plaintext. Passwords
	 * previously entered are never displayed in plaintext
	 * 
	 * @param showPlaintextPassword
	 */
	public void setShowPlaintextPassword(boolean showPlaintextPassword) {
		this.showPlaintextPassword = showPlaintextPassword;
	}

	public boolean isVibrateOnAlertEnabled() {
		return vibrateOnAlertEnabled;
	}

	public void setVibrateOnAlertEnabled(boolean vibrateOnAlertEnabled) {
		this.vibrateOnAlertEnabled = vibrateOnAlertEnabled;
	}

	public int getUID() {

		return 0;
	}

	public boolean isSyncStateDirty() {
		return dirty;
	}

	public void setSyncStateDirty(boolean dirty) {
		this.dirty = dirty;

	}

	public boolean isHomeScreenNotificationIconEnabled() {
		return this.homeScreenNotificationIconEnabled;
	}

	public void setHomeScreenNotificationIconEnabled(boolean enabled) {
		this.homeScreenNotificationIconEnabled = enabled;
	}

	/**
	 * Set last internal version that persistent store cleanup was performed against.
	 * 
	 * @param lastCleanupVersion
	 */
	public void setLastCleanupVersion(int lastCleanupVersion) {
		this.lastCleanupVersion = lastCleanupVersion;

	}

	/**
	 * @return the last internal version that persisten store cleanup was performed against.
	 */
	public int getLastCleanupVersion() {
		return this.lastCleanupVersion;
	}

	/**
	 * @return true if user wishes to send anon usage stats.
	 */
	public boolean isAnonymousUsageStatsEnabled() {
		return this.anonymousUsageStatsEnabled;
	}

	/**
	 * @param anonymousUsageStatsEnabled
	 *            true to enable anon usage stat collection
	 */
	public void setAnonymousUsageStatsEnabled(boolean anonymousUsageStatsEnabled) {
		this.anonymousUsageStatsEnabled = anonymousUsageStatsEnabled;
	}

	/**
	 * @return true if messagebox integration is enabled
	 */
	public boolean isMessageIntegrationEnabled() {
		return this.messageIntegrationEnabled;
	}

	/**
	 * @param messageIntegrationEnabled
	 *            true to enable messagebox integration
	 */
	public void setMessageIntegrationEnabled(boolean messageIntegrationEnabled) {
		this.messageIntegrationEnabled = messageIntegrationEnabled;
	}

	public boolean getRememberOption(int optionId) {
		if (optionId > -1 && optionId < rememberOptions.length)
			return rememberOptions[optionId];
		return false;
	}

	public boolean setRememberOption(int optionId, boolean remember) {
		if (optionId > -1 && optionId < rememberOptions.length)
			rememberOptions[optionId] = remember;
		return false;
	}

	public int getRememberOptionCount() {
		return rememberOptions.length;

	}

	public boolean isBackgroundOnCloseEnabled() {
		return this.backgroundOnCloseEnabled;
	}

	public void setBackgroundOnClose(boolean backgroundOnCloseEnabled) {
		this.backgroundOnCloseEnabled = backgroundOnCloseEnabled;
	}

	public boolean isTitlebarDisplayEnabled() {
		return this.titlebarDisplayEnabled;
	}

	public void setTitlebarDisplayEnabled(boolean enabled) {
		this.titlebarDisplayEnabled = enabled;
	}

	public boolean getShowKeyboardOnSliderClose() {
		return showKeyboardOnSliderClose;
	}

	public void setShowKeyboardOnSliderClose(boolean show) {
		showKeyboardOnSliderClose = show;

	}

	public long getLastSaveVersion() {
		return lastSaveVersionNumber;
	}

	public void setLastSaveVersion(long versionNumber) {
		this.lastSaveVersionNumber = versionNumber;
	}

	public ConnectionProperties getDefaultConnectionProperties() {
		if (defaultConnectionProperties == null) {
			defaultConnectionProperties = new ConnectionProperties(true);
		}
		return this.defaultConnectionProperties;
	}

	public void setDefaultConnectionProperties(ConnectionProperties defaultConnectionProperties) {
		this.defaultConnectionProperties = defaultConnectionProperties;
	}

	public void resetRememberOption() {
		for (int x = 0; x < rememberOptions.length; x++)
			rememberOptions[x] = false;
	}

	public boolean areInitialOptionsSet() {
		return getRememberOption(REMEMBER_CHECKED_UPDATE_OK) && getRememberOption(REMEMBER_CHECKED_SEND_USAGE_STATS_OK)
				&& getRememberOption(REMEMBER_LICENSE_AGREEMENT_COMPLETE);
	}

	public boolean getDisableKeybindWhenOnCall() {
		return disableKeybindsWhenOnCall;
	}

	public void setDisableKeybindsWhenOnCall(boolean disableKeybindsWhenOnCall) {
		this.disableKeybindsWhenOnCall = disableKeybindsWhenOnCall;
	}

	public void setUpdateCheckInterval(int updateCheckInterval) {
		this.updateCheckInterval = updateCheckInterval;
	}

	public int getUpdateCheckInterval() {
		return updateCheckInterval;
	}

	public void setLastUpdateCheckTime(long lastUpdateCheckTime) {
		this.lastUpdateCheckTime = lastUpdateCheckTime;
	}

	public long getLastUpdateCheckTime() {
		return lastUpdateCheckTime;
	}
}
