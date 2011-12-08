/**
 * Copyright (c) 2010 Marc A. Paradise This file is part of "BBSSH" BBSSH is based upon MidpSSH by Karl von Randow.
 * MidpSSH was based upon Telnet Floyd and FloydSSH by Radek Polak. This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU
 * General Public License along with this program; if not, write to the Free Software Foundation, Inc., 675 Mass Ave,
 * Cambridge, MA 02139, USA.
 */
package org.bbssh;

import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.applicationcontrol.ApplicationPermissionsManager;
import net.rim.device.api.io.FileNotFoundException;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import org.bbssh.model.ConnectionManager;
import org.bbssh.model.DataStoreCleaner;
import org.bbssh.model.KeyBindingManager;
import org.bbssh.model.KeyManager;
import org.bbssh.model.MacroManager;
import org.bbssh.model.SettingsManager;
import org.bbssh.notifications.NotificationManager;
import org.bbssh.session.SessionManager;
import org.bbssh.terminal.fonts.BBSSHFontManager;
import org.bbssh.ui.screens.PrimaryScreen;
import org.bbssh.ui.screens.RecoveryScreen;
import org.bbssh.util.Logger;
import org.bbssh.util.Tools;
import org.bbssh.util.Version;

/**
 * Main application class. Launches front screen, PrimaryScreen.
 * 
 * @author marc
 */
public class BBSSHApp extends UiApplication {
	PrimaryScreen screen;
	boolean initComplete = false;
	protected Object initMutex = new Object();;
	private boolean permissionGranted = false;

	public boolean isInTouchCompatibilityMode() {
		return false;
	}

	public BBSSHApp() {

	}

	public PrimaryScreen getPrimaryScreen() {
		return screen;

	}

	protected boolean verifyMinimalPermissions() {
		ApplicationPermissionsManager mgr = ApplicationPermissionsManager.getInstance();
		if (mgr.getPermission(ApplicationPermissions.PERMISSION_INTER_PROCESS_COMMUNICATION) == ApplicationPermissions.VALUE_DENY) {
			ApplicationPermissions perm = new ApplicationPermissions();
			perm.addPermission(ApplicationPermissions.PERMISSION_INTER_PROCESS_COMMUNICATION);
			// Note that in some versions of BBOS, this will ALWAYS prompt even
			// if the permissions is already granted - that's why the check
			// above is required.
			return mgr.invokePermissionsRequest(perm);
		}
		return true;
	}

	public void attemptRecovery(int reasonRes) {
		Logger.error("BBSSHApp.attemptRecovery");
		try {
			RecoveryScreen rec = new RecoveryScreen(reasonRes);
			pushScreen(rec);
			enterEventDispatcher();
		} catch (Throwable e) {
			Logger.error("BBSSHApp.attemptRecovery: exception reported " + e.getMessage() + " : " + e.toString());
			
		}

	}

	void pushMainScreen() {
		Logger.info("Creating and pushing main screen onto display stack.");
		if (screen == null) {
			Class cl = PrimaryScreen.class;
			screen = (PrimaryScreen) Version.createOSObjectInstance(cl.getName());
		}
		pushScreen(screen);

	}

	void immediateInitTasks() {
		// must do this FIRST - as the absence of data will cause subsequent
		// loadup/init
		// to automatically recreate their default data sets.
		DataStoreCleaner.cleanData();

		// Since we're displaying connection list as the main screen, we need to
		// have
		// this initialization complete before we can do anything else.
		// @todo move this and the other background tasks to use bg, but block
		// startup
		// until compelted - this will allow cancelation of long load.
		ConnectionManager.getInstance().initialize();

		// Force safe, non-threaded load of session manager.
		SessionManager.getInstance();

		initComplete = true;

	}

	void deferInitTasks() {
		new Thread("Initial") {

			public void run() {
				synchronized (initMutex) {
					KeyBindingManager.getInstance().initialize();
					MacroManager.getInstance().initialize();
					KeyManager.getInstance().initialize();
					try {
						BBSSHFontManager.initialize();
					} catch (FileNotFoundException e) {
						Logger.error(e.getMessage());
					} catch (IllegalStateException e) {
						Logger.error(e.getMessage());

					} catch (Throwable e) {
						Logger.error(e.getMessage());
					}

				}

			}

		}.start();

	}

	public void deactivate() {
		if (!initComplete)
			return;
		super.deactivate();
		SessionManager.getInstance().notifyAppDeactivate();
		
	}

	public void activate() {
		if (!initComplete)
			return;
		super.activate();

		SessionManager.getInstance().notifyAppActivate();

	}

	public boolean requestPermissions(ApplicationPermissions perm) {
		ApplicationPermissionsManager mgr = ApplicationPermissionsManager.getInstance();
		return mgr.invokePermissionsRequest(perm);

	}

	public Object getInitializationMutex() {
		return initMutex;
	}

	/**
	 * Saves all user configurable options.
	 */
	public void saveAllSettings() {
		Logger.debug("Begin saveAllSettings.");

		try {
			ConnectionManager.getInstance().commitData();
		} catch (Throwable t) {
			Logger.fatal("Connection save failed.", t);

		}
		try {
			MacroManager.getInstance().commitData();
		} catch (Throwable t) {
			Logger.fatal("Macro save failed.", t);

		}
		try {
			KeyBindingManager.getInstance().commitData();
		} catch (Throwable t) {
			Logger.fatal("Keybinding save failed.", t);

		}
		try {
			SettingsManager.getInstance().commitData();
		} catch (Throwable t) {
			Logger.fatal("Settings save failed.", t);

		}
		Logger.debug("End saveAllSettings.");
	}

	/**
	 * Checks if the requested permission exists. If it is set to "VALUE_DENY", prompts the user for that permission by
	 * displaying the message provided.
	 * 
	 * @param requiredPermType
	 *            PERMISSION_* constant
	 * @param msg
	 *            message resource to display if permission is configured to 'deny'
	 * @return true if permission is granted by the system or the user.
	 */
	public synchronized boolean requestPermission(final int requiredPermType, final int msg) {
		permissionGranted = false;
		invokeAndWait(new Runnable() {

			public void run() {

				if (ApplicationPermissionsManager.getInstance().getPermission(requiredPermType) == ApplicationPermissions.VALUE_DENY) {
					if (Dialog.ask(Dialog.D_YES_NO, Tools.getStringResource(msg)) == Dialog.YES) {
						ApplicationPermissions perm = new ApplicationPermissions();
						perm.addPermission(requiredPermType);
						permissionGranted = requestPermissions(perm);
					}
				} else {
					permissionGranted = true;
				}
			}
		});
		// Don't assemble the string if we're not logging this anyway.
		int level = permissionGranted ? Logger.LOG_LEVEL_INFO : Logger.LOG_LEVEL_ERROR;
		if (Logger.isLevelEnabled(level)) {
			Logger.log(level, "Permission requested: " + requiredPermType + " and result is : " + permissionGranted);
		}
		return permissionGranted;
	}

	/**
	 * Convenience method to quickly get the BBSSHApp instance without casting.
	 * 
	 * @return the single BBSSHApp instance
	 */
	public static BBSSHApp inst() {
		return ((BBSSHApp) UiApplication.getUiApplication());

	}
	public void shutdownTasks() { 
		SessionManager.getInstance().terminateAllSessions();
		BBSSHApp.inst().saveAllSettings();
		NotificationManager.inst().resetNotificationState();
		NotificationManager.inst().terminateAllNotifications();
		Logger.disableFileLogging();
		BBSSHApp.inst().removeSystemListener(SessionManager.getInstance());
		NotificationManager.inst().resetNotificationState();

	}

}
