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
import net.rim.device.api.applicationcontrol.ReasonProvider;
import net.rim.device.api.i18n.ResourceBundleFamily;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.DeviceInfo;

import org.bbssh.i18n.BBSSHResource;
import org.bbssh.model.SettingsManager;
import org.bbssh.session.SessionManager;
import org.bbssh.util.Logger;
import org.bbssh.util.Version;

/**
 * Main application class. Launches front screen, PrimaryScreen.
 * 
 * @author marc
 */
public class BBSSH {
	private static ReasonProvider provider = new ReasonProvider() {
		public String getMessage(int permissionID) {
			ResourceBundleFamily bundle =
					ResourceBundleFamily.getBundle(BBSSHResource.BUNDLE_ID, BBSSHResource.BUNDLE_NAME);
			switch (permissionID) {
				// To be implemented in future.
				case ApplicationPermissions.PERMISSION_HANDHELD_KEYSTORE:
					return bundle.getString(BBSSHResource.PERM_DESC_KEYSTORE);
				case ApplicationPermissions.PERMISSION_FILE_API:
					return bundle.getString(BBSSHResource.PERM_DESC_FILE);
				case ApplicationPermissions.PERMISSION_INTERNAL_CONNECTIONS:
					return bundle.getString(BBSSHResource.PERM_DESC_NET_INTERNAL);
				case ApplicationPermissions.PERMISSION_INTER_PROCESS_COMMUNICATION:
					return bundle.getString(BBSSHResource.PERM_DESC_INTERPROC);
				case ApplicationPermissions.PERMISSION_EXTERNAL_CONNECTIONS:
					return bundle.getString(BBSSHResource.PERM_DESC_NET_EXTERNAL);
				case ApplicationPermissions.PERMISSION_WIFI:
					return bundle.getString(BBSSHResource.PERM_DESC_WIFI);
				case ApplicationPermissions.PERMISSION_PHONE:
					return bundle.getString(BBSSHResource.PERM_DESC_PHONE);
				case ApplicationPermissions.PERMISSION_EMAIL:
					return bundle.getString(BBSSHResource.PERM_DESC_EMAIL);
				case ApplicationPermissions.PERMISSION_PIM:
					return bundle.getString(BBSSHResource.PERM_DESC_PIM);
				case ApplicationPermissions.PERMISSION_MEDIA:
					return bundle.getString(BBSSHResource.PERM_DESC_MEDIA);
			}
			return "Unknown Permission";
		}
	};

	public static void main(String[] args) {
		ApplicationPermissionsManager mgr = ApplicationPermissionsManager.getInstance();
		mgr.addReasonProvider(ApplicationDescriptor.currentApplicationDescriptor(), provider);
		int count = args.length;
		boolean error = false;
		if (count == 0) {
			error = true;
			Version.setReleaseMode(false);
		}

		for (int x = count - 1; x >= 0; x--) {
			if (args[x].equals("init")) {
				if (initSettings()) {
					BBSSHAutoStart.initialize();
					return;
				}
			} else if (args[x].equals("dev")) {
				Version.setReleaseMode(false);
			} else if (args[x].equals("release")) {
				Version.setReleaseMode(true);
			} else {

			}
		}
		// AVoid a verification error in 4.5 and 4.6 - don't direcftly call
		// .class temp.getClass().getName()
		BBSSHApp app = (BBSSHApp) Version.createOSObjectInstance("org.bbssh.BBSSHApp");

		if (app == null)
			return;
		if (!app.verifyMinimalPermissions()) {
			return;
		}

		if (!initSettings()) {
			app.attemptRecovery(BBSSHResource.RECOVERY_INSTRUCTION);
			return;
		}

	
		if (DeviceInfo.isSimulator() || !Version.isReleaseMode()) {
			try {
				Logger.enableFileLogging();
			} catch (Exception e) {
				Logger.warn("Failed to enable file logging.");
			}
		}
		if (error) {
			Logger.error("Received no argument, was expecting at minimum a run mode. Assuming dev mode.");
		}

		try {
			app.immediateInitTasks();
			app.deferInitTasks();
		} catch (Throwable t) {
			// This is a major error - the only known cause of it so far is
			// permissions..
			Logger.fatal("Unexpected throwable in main app - attempting recovery.", t);
			app.attemptRecovery(BBSSHResource.RECOVERY_INSTRUCTION);
			return;
		}
		// boolean startupSucceeded = false;
		try {
			SessionManager sm = SessionManager.getInstance();
			app.addSystemListener(sm);
			app.pushMainScreen();
			// startupSucceeded = true;
			app.enterEventDispatcher();

		} catch (Throwable e) {
			// We're going to cheat here - we'll set "last run version" to a
			// completely bogus version which will force us to run the recovery
			// screen on next startup.
			// Logger.fatal("Fatal error in main thread - forcing recovery screen. ", e);
			// s.setLastSaveVersion(new
			// if (!startupSucceeded) {
			// SettingsManager.getSettings().setLastSaveVersion(
			// new Version.VersionInfo("127.127.127.999").getVersionNo());
			// SettingsManager.getInstance().commitData();
			// }
		}

	}

	private static boolean initSettings() {
		SettingsManager m = SettingsManager.getInstance();
		try {
			m.initialize();
		} catch (Exception e) {
			Logger.fatal("Failed to initialize settings: ", e);
			return false;
		}
		return true;
	}

}
