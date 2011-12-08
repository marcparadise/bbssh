package org.bbssh;

import net.rim.blackberry.api.homescreen.HomeScreen;
import net.rim.blackberry.api.homescreen.ShortcutEventListener;
import net.rim.device.api.i18n.ResourceBundleFamily;
import net.rim.device.api.ui.component.Dialog;

import org.bbssh.i18n.BBSSHResource;
import org.bbssh.model.ConnectionManager;
import org.bbssh.model.ConnectionProperties;
import org.bbssh.session.SessionManager;
import org.bbssh.ui.screens.PrimaryScreen;

/**
 * This extension of the main UiApplication class implements ShortcutEventListener, allowing us to handle placed
 * launch/edit shortcuts on the user's Home screen.
 * 
 * @author marc
 * 
 */
public class BBSSHApp_60 extends BBSSHApp implements ShortcutEventListener {
	ResourceBundleFamily res = ResourceBundleFamily.getBundle(BBSSHResource.BUNDLE_ID,
			BBSSHResource.BUNDLE_NAME);

	public BBSSHApp_60() {
		super();
	}

	/**
	 * Returns the connection properties -if they don't exist it will return null and prompt the user to delete the
	 * shortcut.
	 * 
	 * @param shortcutID
	 * @return
	 */
	private ConnectionProperties getConnectionProperties(String shortcutID) {
		ConnectionManager m = ConnectionManager.getInstance();
		ConnectionProperties p = m.getConnectionPropertiesById(shortcutID);
		// @todo see if we can safely delete - may be we don't haev a dialog? need to get sequence of events.
		if (p == null) {
			if (Dialog.ask(Dialog.D_YES_NO, res.getString(BBSSHResource.PRIMARY_DELETE_CONNECTION_PROMPT), 0) == Dialog.YES) {
				HomeScreen.removeShortcut(shortcutID);
			}
		}

		return p;
	}

	public void editShortcut(String shortcutID) {
		final String sid = shortcutID;
		ConnectionProperties prop = getConnectionProperties(sid);
		if (prop != null) {
			PrimaryScreen ps = super.getPrimaryScreen();
			// @todo what if primaryscreen is not focused here? or a modal dialog is up?
			if (ps != null) {
				ps.editConnectionProperties(prop);
			}
		}

	}

	public void launchShortcut(String shortcutID) {
		// Note that testing shows this should be occuring on the event thread, so
		// we're safe in doing it directly.
		ConnectionProperties prop = getConnectionProperties(shortcutID);
		if (prop != null) {
			SessionManager.getInstance().initiateOrResumeSession(prop);
		}
	}

	public void shortcutDeleted(String shortcutID) {
		// wE don't care if the shortcut was deleted from the home screen.
	}



}
