package org.bbssh.notifications;

import net.rim.blackberry.api.homescreen.HomeScreen;
import net.rim.blackberry.api.homescreen.Shortcut;
import net.rim.blackberry.api.messagelist.ApplicationIndicator;
import net.rim.blackberry.api.messagelist.ApplicationIndicatorRegistry;
import net.rim.device.api.system.EncodedImage;

import org.bbssh.util.Tools;

public class NotificationManager_60 extends NotificationManager_46 {

	/**
	 * in addition to the functions of the base class, sets the "new" state on the specified icon
	 */
	public void updateHomeScreenNotifications(int count, boolean newNotification) {
		super.updateHomeScreenNotifications(count, newNotification);
		ApplicationIndicator a = ApplicationIndicatorRegistry.getInstance().getApplicationIndicator();
		if (a != null)
			a.setNotificationState(newNotification);

	}

	/**
	 * Implementation override for the base class, this will update the specified homescreen shortcut appearance in new
	 * indicator
	 * 
	 * @param shortcutID
	 *            the shortcut to update
	 * @param connected
	 *            indicates shortcut represents at least one connected session.
	 * @param notified
	 *            indicates shortcut represent a session in a notified state.
	 */
	protected void updateShortcutIcon(String shortcutID, boolean connected, boolean notified) {
		super.updateShortcutIcon(shortcutID, connected, notified);
		Shortcut sc = HomeScreen.getShortcut(shortcutID);
		if (sc == null)
			return;
		sc.setIcon(Tools.loadEncodedImage(getHomeScreenIconName(connected, notified)));
		sc.setNewState(notified);

	}

	/**
	 * Replaces the base implementation, behaving the same but ignoring the notified flag.
	 * 
	 * @param connected
	 *            indicates whether icon should reflect a connected state.
	 * @param notified
	 *            ignored in this version
	 * @return name of a matching bitmap file
	 */
	protected String getHomeScreenIconName(boolean connected, boolean notified) {
		StringBuffer buf = new StringBuffer(32);
		buf.append("shortcut");
		if (connected)
			buf.append("-connected");
		buf.append(".png");
		return buf.toString();
	}

	/**
	 * In addition to the main homecsreen icon (updated by the base class) this will also update the
	 */
	protected void resetHomeScreenIcons() {
		super.resetHomeScreenIcons();
		EncodedImage icon = Tools.loadEncodedImage("shortcut.png");
		String[] shortcuts = HomeScreen.getShortcutIDs();
		Shortcut sc;
		for (int x = 0; x < shortcuts.length; x++) {
			sc = HomeScreen.getShortcut(shortcuts[x]);
			sc.setNewState(false);
			sc.setUnreadCount(0);
			sc.setIcon(icon);
		}
	}

	public void setHomeScreenShortcutState(int sessionId, String imageName, boolean newState) {
		// ConnectionProperties prop = SessionManager.getInstance().getPropertiesForInstance(sessionId);
	}

}
