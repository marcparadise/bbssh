package org.bbssh.notifications;

import java.util.Enumeration;

import net.rim.blackberry.api.homescreen.HomeScreen;
import net.rim.device.api.notification.NotificationsManager;
import net.rim.device.api.system.Alert;

import org.bbssh.model.ConnectionProperties;
import org.bbssh.model.SettingsManager;
import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.session.SessionManager;
import org.bbssh.util.Tools;
import org.bbssh.util.Version;

/**
 * Across versions, BBSSH supports many different notification indicators:
 * <ul>
 * <ul>
 * <li>HSI connected + new (6.0) / connected_splat (<) if 1+ connections exist and 1+ sessions are notified.</li>
 * <li>HSI disconnected + new (6.0) / disconnected_splat (<) if 0 connections exist and 1+ sessions are notified.</li>
 * <li>HSI connected if at least one connection exists and none are notified.</li>
 * <li>HSI disconnected if no n otifications exist and no connections exists.</li>
 * </ul>
 * </li> <li>Home screen: session shortcut icon state (6.0)</li>
 * <ul>
 * <li>Session Shortcut icon (SSI) is set to connected when session is connected</li>
 * <li>SSI is set to connected + new when a connection exists and the session is in a notified state.</li>
 * <li>SSI is set to default + new when a connection exists and the session is in a notified state.</li>
 * <li>SSI is set to connected when a connection exists and the session is not in a notified state.</li>
 * <li>SSI is set to disconnected when no connection exists and the session is not in a notified state.</li>
 * </ul>
 * </li> <li>Home screen: statusbar notification icon and count(4.6)
 * <ul>
 * <li>Icon is enabled when any session is in the notified state</li>
 * <li>Icon count is the total number of session in the notified state.</li>
 * <li>Icon is disabled when the number of sessions in the notified state is zero.</li>
 * </ul>
 * </li> <li>Home screen: statusbar notification icon new state (6.0)
 * <ul>
 * <li>State is new when message received while app does not have focus or is foreground but asleep.</li>
 * <li>New state is cleared</li> when the app receives focus.</li> </ul> </li> <li>Message list: n/a</li> <li>Message
 * list: statusbar integration: n/a</li> <li>Vibrate (ONLY if preference is on, and bell occurs in ACTIVE session)</li>
 * <li>Profile-based notification(all).</li> <li>Main screen: set alert indicator(all)</li> <li>Main screen: set error
 * indicator(all)</li> <li>Home screen: main app icon state (HSI)</li> </ul> This class (overridden appropriately or
 * platform versions) manages of the state of these various notification behaviors
 */
public class NotificationManager {
	private static NotificationManager me = null;

	public synchronized static NotificationManager inst() {
		if (me == null) {
			String name = NotificationManager.class.getName();
			me = (NotificationManager) Version.createOSObjectInstance(name);
		}
		return me;
	}

	public NotificationManager() {

	}

	/**
	 * Provides an icon filename usable for either the main app icon; or a shortcut icon. For OS 5 and earlie, the icon
	 * will reflect the notified and connected state; for 6.0+ it iwill reflect only the connected state; and the
	 * application must invoke the appropriate Homescreen method to toggle the notified state.
	 * 
	 * @param connected
	 *            for all versions this will provide a different icon based on its value.
	 * @param notified
	 *            for 5.0 and earlier this will ensure that the icon reflects a notified state by using a "splat"
	 *            indicator similar to the one BlackBerry uses on its messages app.
	 * @return bitmap icon meeting the required
	 */
	protected String getHomeScreenIconName(boolean connected, boolean notified) {
		StringBuffer buf = new StringBuffer(32);
		buf.append("shortcut");
		if (connected)
			buf.append("-connected");
		if (notified)
			buf.append("_splat");
		buf.append(".png");
		return buf.toString();
	}

	/**
	 * Resets the notified state of the overall application; and clears the new indicator from the current icon.
	 */
	public final synchronized void resetNotificationState() {
		NotificationsManager.cancelImmediateEvent(Tools.NOTIFICATION_GUID, 1, null, null);

	}

	public synchronized void resetNotificationState(ConnectionProperties prop) {

	}

	public final synchronized void terminateAllNotifications() {
		resetNotificationState();
		resetHomeScreenIcons();
		updateHomeScreenNotifications(0, false);
	}

	protected void resetHomeScreenIcons() {
		HomeScreen.updateIcon(Tools.loadBitmap(getHomeScreenIconName(false, false)));

	}

	/**
	 * Refreshes all notification indicators based on application and terminal focus; and where possible updates
	 * notification indicators for the provided session.
	 * 
	 * @param notificationOccurred
	 * @param rsi
	 */
	public final synchronized void updateNotificationIndicators(boolean notificationOccurred, RemoteSessionInstance rsi) {
		// @todo we will need to detemrine how to do notification count
		// differently once we implememnt message folders
		int count = 0;
		SessionManager mgr = SessionManager.getInstance();

		// Active notifications first - porfile and terminal vibration.
		if (notificationOccurred) {
			if (SettingsManager.getSettings().isVibrateOnAlertEnabled()) {
				if (mgr.isTerminalActive() && rsi == mgr.activeSession) {
					// @todo alert options: vibrate, tone, none.
					Alert.startVibrate(150);
				}
			}
			// We're going to base this behavior on the BB messages app.
			// BB messages will ALWAYS display this notification even if you're
			// already in the app message list or reading an email. OUr
			// difference in behavior will be only if you are actively in the
			// session itself - then tehre's no reason to show the notification,
			// as you've already got the vibrate and are looking at the screen.
			if (!mgr.isTerminalActive() || rsi != mgr.activeSession) {
				// The other thing the messenger app will do is NOT display a
				// new notification
				// if a notification has already occurred for the app.
				if ((rsi != null && !rsi.state.suppressNotify)
						&& NotificationsManager.isImmediateEventOccuring(Tools.NOTIFICATION_GUID)) {
					NotificationsManager.triggerImmediateEvent(Tools.NOTIFICATION_GUID, 1, null, null);
				}
			}
		}

		// Now - update application and shortcut icons; and update the notification
		// icon counters, visibility, an state.
		Enumeration sessions = mgr.getAvailableSessions();
		RemoteSessionInstance r;
		boolean connected = false;
		while (sessions.hasMoreElements()) {
			r = (RemoteSessionInstance) sessions.nextElement();
			connected = connected || r.isConnected();
			if (r.state.notified) {
				count++;
			}
		}
		if (rsi != null) {
			// a forced disconnect - don't send a notification that the user can't
			// ever see (because the session is gone).
			if (rsi.state.suppressNotify) {
				notificationOccurred = false;
			}
			updateShortcutIcon(rsi.session.getProperties().getUIDAsString(), rsi.isConnected(), notificationOccurred);
		}
		updateMainIcon(connected, notificationOccurred);
		updateHomeScreenNotifications(count, notificationOccurred);

	}

	/**
	 * Derived versions will perform updates to appearance and state of the shortcut icons on the home screen (6.0+)
	 * 
	 * @param shortcutId
	 * @param newIcon
	 * @param notificationOccurred
	 */
	protected void updateShortcutIcon(String shortcutId, boolean connected, boolean notificationOccurred) {

	}

	/**
	 * Set the main application icon state to indicate whether or not a notification icon occurred.
	 * 
	 * @param connected
	 *            true if icon should reflect a connected state.
	 * @param notificationOccurred
	 *            true if icon should be in "new" state.
	 */
	protected void updateMainIcon(boolean connected, boolean notificationOccurred) {
		HomeScreen.updateIcon(Tools.loadBitmap(getHomeScreenIconName(connected, notificationOccurred)));
	}

	/**
	 * Updates the displayed number of notifications on the home screen, for versions which support it. This is
	 * available in 4.6, and so the base version has no implementation.
	 * 
	 * @param newNotification
	 *            true if a new notification has occurred.
	 */
	protected void updateHomeScreenNotifications(int count, boolean newNotification) {

	}

}
