package org.bbssh.help;

import net.rim.blackberry.api.browser.Browser;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;

import org.bbssh.i18n.BBSSHResource;
import org.bbssh.util.Version;

public class HelpManager {
	private final static ResourceBundle res = ResourceBundle.getBundle(BBSSHResource.BUNDLE_ID,
			BBSSHResource.BUNDLE_NAME);
	private static final String BASE = res.getString(Version.isReleaseMode()? BBSSHResource.URL_HELP : BBSSHResource.URL_HELP_DEV);
	private static final MenuItem menu = new MenuItem(res, BBSSHResource.MENU_HELP, 0x00500999, 100) {
		/**
		 * Shared menu handler
		 */
		public void run() {
			Field target = menu.getTarget();
			Screen s = target == null ? UiApplication.getUiApplication().getActiveScreen() : target.getScreen();
			String url = getContextualHelpURL(s);
			// @todo in 5.0 and later we can use a POpupScreen with an embedded BrowserField
			// that willr equire making this class use non-static methods, so we can extend it in HelpManger_50
			if (url != null) {
				Browser.getDefaultSession().displayPage(url);
			}

		};

	};

	public static MenuItem getHelpMenu() {
		return menu;

	}

	private static String getContextualHelpURL(Screen screen) {
		return getContextualHelpURL(screen, null);
	}

	/**
	 * For a given screen and field get the contextual help URL. If field is null, it will return the URL for only the
	 * screen. If the screeen is null, it will return the URL for an error page.
	 * 
	 * @param screen
	 * @param field
	 * @return
	 */
	private static String getContextualHelpURL(Screen screen, Field field) {
		if (screen == null) {
			return BASE + "sorry";
		}
		if (screen instanceof ScreenFieldRemoteHelp && field != null) {

			return BASE + screen.getClass().getName() + "&f="
					+ ((ScreenFieldRemoteHelp) screen).getScreenFieldHelpId(field);
		}

		return BASE + screen.getClass().getName();
	}
}
