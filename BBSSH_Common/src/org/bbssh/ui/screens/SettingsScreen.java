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
package org.bbssh.ui.screens;

import java.io.IOException;

import net.rim.blackberry.api.phone.Phone;
import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.applicationcontrol.ApplicationPermissionsManager;
import net.rim.device.api.i18n.ResourceBundleFamily;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.PasswordEditField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.MainScreen;

import org.bbssh.BBSSHApp;
import org.bbssh.i18n.BBSSHResource;
import org.bbssh.model.ConnectionProperties;
import org.bbssh.model.Settings;
import org.bbssh.model.SettingsManager;
import org.bbssh.platform.PlatformServicesProvider;
import org.bbssh.ui.components.ClickableButtonField;
import org.bbssh.util.Version;

public class SettingsScreen extends MainScreen implements BBSSHResource, FieldChangeListener {

	private ResourceBundleFamily res = ResourceBundleFamily.getBundle(BUNDLE_ID, BUNDLE_NAME);
	private CheckboxField autoCheckForUpdates;
	private CheckboxField sendAnonymousUsageData;
	private CheckboxField vibrateOnBell;
	private CheckboxField enableHomescreenNotificationIcon;
	private CheckboxField integrateWithMessages;
	private CheckboxField backgroundOnClose;
	private CheckboxField displayTitlebar;

	private CheckboxField showPasswordText;
	private CheckboxField disableKeybindOnCall;

	private LabelField updateHeader;
	private LabelField networkingHeader;
	private LabelField integrationHeader;
	private BasicEditField apnUser;
	private PasswordEditField apnPassword;
	private BasicEditField apn;
	private BasicEditField updateInterval;
	private LabelField securityHeader;

	private CheckboxField showKeyboardOnSliderClosed;
	private ClickableButtonField editConnectionDefaults;
	private ConnectionProperties defaultConnProp;
	private MenuItem updatePerms = new MenuItem(res, SETTINGS_MENU_UPDATE_PERMS, 50000, 10) {
		public void run() {
			PermissionsHelperScreen helper = new PermissionsHelperScreen(false);
			UiApplication.getUiApplication().pushScreen(helper);
		};
	};

	/**
	 * Instantiates a new settings screen.
	 */
	public SettingsScreen() {
		super();
		setTitle(res.getString(SETTINGS_TITLE));
		createFields();
		addFields();
	}

	private void createFields() {
		Settings s = SettingsManager.getSettings();
		Font bold = getFont().derive(Font.BOLD);
		// Copy the connection property defaults -as we don't know if we want to retain values until the user saves.
		// @todo is this necessary? 
		defaultConnProp = new ConnectionProperties(s.getDefaultConnectionProperties());
		// Updates
		updateHeader = new LabelField(res.getString(SETTINGS_LBL_UPDATE));
		updateHeader.setFont(bold);
		autoCheckForUpdates = new CheckboxField(res.getString(SETTINGS_LBL_AUTOCHECK_VERSION), s
				.isAutoCheckUpdateEnabled());
		updateInterval = new BasicEditField(res.getString(SETTINGS_LBL_UPDATE_INTERVAL),
				Integer.toString(s.getUpdateCheckInterval()), 2, BasicEditField.FILTER_INTEGER);
		sendAnonymousUsageData = new CheckboxField(res.getString(SETTINGS_LBL_SEND_ANON_USAGE), s
				.isAnonymousUsageStatsEnabled());

		// Integration
		integrationHeader = new LabelField(res.getString(SETTINGS_LBL_INTEGRATION));
		integrationHeader.setFont(bold);
		// @todo only with platform support 4.5
		integrateWithMessages = new CheckboxField(res.getString(SETTINGS_LBL_ENABLE_MESSAGE_LIST),
				s.isMessageIntegrationEnabled());
		vibrateOnBell = new CheckboxField(res.getString(SETTINGS_LBL_VIBRATE_ON_BELL),
				s.isVibrateOnAlertEnabled());
		// @todo only with platform supprot 4.6
		enableHomescreenNotificationIcon = new CheckboxField(res.getString(SETTINGS_LBL_HOMESCREEN_NOTIFY),
				s.isHomeScreenNotificationIconEnabled());
		disableKeybindOnCall = new CheckboxField(res.getString(SETTINGS_LBL_DISABLE_KEYBIND_ON_CALL),
				s.getDisableKeybindWhenOnCall());

		backgroundOnClose = new CheckboxField(res.getString(SETTINGS_LBL_BACKROUND_ON_CLOSE),
				s.isBackgroundOnCloseEnabled());
		displayTitlebar = new CheckboxField(res.getString(SETTINGS_LBL_ENABLE_TITLEBAR), s.isTitlebarDisplayEnabled());

		showKeyboardOnSliderClosed = new CheckboxField(res.getString(SETTINGS_LBL_SHOW_KBD_SLIDER_CLOSE), s
				.getShowKeyboardOnSliderClose());
		networkingHeader = new LabelField(res.getString(SETTINGS_LBL_NETWORKING));
		networkingHeader.setFont(bold);
		apn = new BasicEditField(res.getString(SETTINGS_LBL_APN), s.getAPN());
		apnUser = new BasicEditField(res.getString(SETTINGS_LBL_APN_USER), s.getAPNUserName());
		apnPassword = new PasswordEditField(res.getString(SETTINGS_LBL_APN_PASSWORD), s.getAPNPassword());

		// Security
		securityHeader = new LabelField(res.getString(SETTINGS_LBL_SECURITY));
		securityHeader.setFont(bold);
		showPasswordText = new CheckboxField(res.getString(SETTINGS_LBL_SHOW_PLAINTEXT), s.getShowPlaintextPassword());
		// Connection Defaults
		editConnectionDefaults = new ClickableButtonField(res.getString(SETTINGS_LBL_CONN_DEFAULTS), this);

	}

	private void addFields() {

		add(integrationHeader);
		add(vibrateOnBell);
		if (PlatformServicesProvider.getInstance().isNotificationSupportAvailable()) {
			// @todo support for message integration: add this control back when ready
			// add(integrateWithMessages);
			add(enableHomescreenNotificationIcon);
		}
		add(disableKeybindOnCall);
		add(backgroundOnClose);
		add(displayTitlebar);
		if (PlatformServicesProvider.getInstance().hasSlider()) {
			add(showKeyboardOnSliderClosed);
		}
		add(new SeparatorField());
		add(securityHeader);
		add(showPasswordText);

		add(new SeparatorField());
		add(updateHeader);
		add(autoCheckForUpdates);
		add(sendAnonymousUsageData);
		add(updateInterval);
		// @todo we may not be using this APN when callign
		// @todo remember last successful check mode, and use it first
		// @todo use default check mode.
		add(new SeparatorField());
		add(networkingHeader);
		add(apn);
		add(apnUser);
		add(apnPassword);

		add(new SeparatorField());
		add(editConnectionDefaults);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.ui.Screen#isDataValid()
	 */
	public boolean isDataValid() {
		// Note that we have no rael validations around APN - as we have no way of knowing
		// what combinations are valid. Still - generally speaking if we have a username, we should bave
		// an APN value. If we have a password, we should have both username and apn.
		// The reverse is not true - it is possible to have an APN with no username and password.
		if (apnPassword.getTextLength() > 0) {
			if (apnUser.getTextLength() == 0) {
				Status.show(res.getString(MSG_SETTINGS_NO_APN_USERNAME));
				apnUser.setFocus();
				return false;
			}
			if (apn.getTextLength() == 0) {
				Status.show(res.getString(MSG_SETTINGS_NO_APN));
				apn.setFocus();
				return false;
			}
		}
		if (apnUser.getTextLength() > 0) {
			if (apn.getTextLength() == 0) {
				Status.show(res.getString(MSG_SETTINGS_NO_APN));
				apn.setFocus();
				return false;
			}
		}
		if (!Version.isReleaseMode()) {
			if (!autoCheckForUpdates.getChecked()) {
				Status.show(res.getString(MSG_SETTINGS_CANNOT_DISABLE_DEV_UPDATE));
				autoCheckForUpdates.setFocus();
				return false;
			}
		}
		// This one requires some extra permissions to do
		if (disableKeybindOnCall.getChecked()) {
			int p = ApplicationPermissionsManager.getInstance().getPermission(ApplicationPermissions.PERMISSION_PHONE);
			if (p == ApplicationPermissions.VALUE_PROMPT) {
				// force the prompt to occur now
				Phone.getActiveCall();
			} else {
				if (!BBSSHApp.inst().requestPermission(ApplicationPermissions.PERMISSION_PHONE,
						BBSSHResource.MSG_PERMISSIONS_MISSING_PHONE)) {
					disableKeybindOnCall.setChecked(false);
				}
			}
		}
		if (autoCheckForUpdates.getChecked() || sendAnonymousUsageData.getChecked()) {
			String data = updateInterval.getText();
			if (data.length() == 0)
				data = "0";
			int val = Integer.parseInt(data);
			if (val <= 0 || val > 30) {
				Status.show(res.getString(BBSSHResource.MSG_SETTINGS_INVALID_UPDATE_INTERVAL));
				updateInterval.setFocus();
				return false;
			}
		}

		return true;
	}

	public void save() throws IOException {
		SettingsManager mgr = SettingsManager.getInstance();
		Settings s = SettingsManager.getSettings();
		s.setDefaultConnectionProperties(defaultConnProp);
		s.setAutoCheckUpdates(autoCheckForUpdates.getChecked());
		s.setAnonymousUsageStatsEnabled(sendAnonymousUsageData.getChecked());
		s.setTitlebarDisplayEnabled(displayTitlebar.getChecked());

		s.setAPN(apn.getText().trim());
		s.setAPNUserName(apnUser.getText().trim());
		s.setAPNPassword(apnPassword.getText().trim());
		s.setShowPlaintextPassword(showPasswordText.getChecked());
		s.setHomeScreenNotificationIconEnabled(enableHomescreenNotificationIcon.getChecked());
		s.setMessageIntegrationEnabled(integrateWithMessages.getChecked());
		s.setVibrateOnAlertEnabled(vibrateOnBell.getChecked());
		s.setBackgroundOnClose(backgroundOnClose.getChecked());
		s.setShowKeyboardOnSliderClose(showKeyboardOnSliderClosed.getChecked());
		s.setDisableKeybindsWhenOnCall(disableKeybindOnCall.getChecked());
		s.setUpdateCheckInterval(Integer.parseInt(updateInterval.getText()));
		mgr.commitData();
		super.save();
	}

	public void fieldChanged(Field field, int context) {
		if (field == editConnectionDefaults) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					ConnectionPropertiesScreen screen = new ConnectionPropertiesScreen(defaultConnProp);
					UiApplication.getUiApplication().pushModalScreen(screen);
					if (!screen.isSaved()) {
						// revert any changes that have been done. 
						defaultConnProp = new ConnectionProperties(SettingsManager.getSettings()
								.getDefaultConnectionProperties(), false);
					}
				}
			});

		}
	}

	protected void makeMenu(Menu menu, int instance) {
		menu.add(updatePerms);
		super.makeMenu(menu, instance);
	}
}
