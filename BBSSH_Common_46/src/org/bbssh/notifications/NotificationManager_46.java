package org.bbssh.notifications;

import net.rim.blackberry.api.messagelist.ApplicationIndicator;
import net.rim.blackberry.api.messagelist.ApplicationIndicatorRegistry;

import org.bbssh.model.SettingsManager;

public class NotificationManager_46 extends NotificationManager {
	protected void updateHomeScreenNotifications(int count, boolean newNotification) {
		super.updateHomeScreenNotifications(count, newNotification);
		if (SettingsManager.getSettings().isHomeScreenNotificationIconEnabled()) {
			ApplicationIndicator a = ApplicationIndicatorRegistry.getInstance().getApplicationIndicator();
			if (a != null) {
				a.setValue(count);
				a.setVisible(count > 0);
			}
		}
	}
}
