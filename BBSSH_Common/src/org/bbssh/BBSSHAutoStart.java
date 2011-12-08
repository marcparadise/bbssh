package org.bbssh;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.notification.NotificationsManager;
import net.rim.device.api.synchronization.SyncManager;
import net.rim.device.api.system.EventLogger;

import org.bbssh.i18n.BBSSHResource;
import org.bbssh.model.ConnectionManager;
import org.bbssh.model.DefaultSyncCollection;
import org.bbssh.model.KeyBindingManager;
import org.bbssh.model.KeyManager;
import org.bbssh.model.MacroManager;
import org.bbssh.model.SettingsManager;
import org.bbssh.util.Logger;
import org.bbssh.util.Tools;
import org.bbssh.util.Version;

/**
 * BBSSHAutoStart handles registration for various BB services, and should be instantiated on startup.
 */
public class BBSSHAutoStart {
	public BBSSHAutoStart() {

	}

	/**
	 * Helper method to only register a collection for sync if it's not already registered. Primarily exists as a
	 * workaround for hot-swap issues - if hot swapping in the simulator, then these collections are already registered
	 * from the previous run and throw an exception.
	 * 
	 * @param dsc
	 */
	private void registerForSync(DefaultSyncCollection dsc) {
		SyncManager m = SyncManager.getInstance();
		if (!m.isCollectionRegistered(dsc, true, false)) {
			try {
				m.enableSynchronization(dsc, false);
			} catch (IllegalArgumentException e) {
				// seems to occur just in the simulator
			} catch (Throwable e) {
				// If a colleciton is already registered this exception is thrown.
				// The API to check and see if a collection is registered was only added in
				// 4.5 - so for backward compatibility we need to handle this exception.
				// Log it in case it turns out more serious than expected...
				Logger.error("Warning: registerForSync exception: " + e);
			}
		}
	}

	/**
	 * Registers the app for logging, notification icon, and as an event source.
	 */
	protected void init() {
		// Register as a source of events.
		NotificationsManager.registerSource(Tools.NOTIFICATION_GUID, this, NotificationsManager.DEFAULT_LEVEL);

		// Register for logging
		EventLogger.register(Tools.NOTIFICATION_GUID, "BBSSH", EventLogger.VIEWER_STRING);

		// Configure for sync
		registerForSync(ConnectionManager.getInstance());
		registerForSync(KeyManager.getInstance());
		registerForSync(MacroManager.getInstance());
		registerForSync(SettingsManager.getInstance());
		registerForSync(KeyBindingManager.getInstance());
	}

	/**
	 * Override of standard toString. This is used as the application name in the user's profile/notifications
	 * preferences page
	 * 
	 * @return application name
	 */
	public String toString() {
		return ResourceBundle.getBundle(BBSSHResource.BUNDLE_ID, BBSSHResource.BUNDLE_NAME).getString(
				BBSSHResource.APPLICATION_TITLE);
	}

	private static BBSSHAutoStart me = null;

	public static synchronized void initialize() {
		if (me == null) {

			// attempt to avoid verification errors in simulator 
			String name = BBSSHAutoStart.class.getName(); 
			me = (BBSSHAutoStart) Version.createOSObjectInstance(name);
			if (me == null) {
				// Shouldn't happen... but let's play it safe and go with minimum version.
				me = new BBSSHAutoStart();
			}

			me.init();
		}

	}
}
