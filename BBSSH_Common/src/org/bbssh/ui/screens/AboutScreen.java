/*
 *  Copyright (C) 2010 Marc A. Paradise
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.bbssh.ui.screens;

import net.rim.blackberry.api.browser.Browser;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.i18n.ResourceBundleFamily;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;

import org.bbssh.i18n.BBSSHResource;
import org.bbssh.patterns.UpdatingBackgroundTask;
import org.bbssh.platform.PlatformServicesProvider;
import org.bbssh.ui.components.HyperlinkField;
import org.bbssh.ui.components.PleaseWaitTaskMonitorScreen;
import org.bbssh.util.Version;

/**
 * The Class AboutScreen.
 */
public class AboutScreen extends MainScreen implements BBSSHResource {
	private ResourceBundleFamily res = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);
	Font italics;
	Font bold;
	LabelField versionLabel;
	private LabelField versionLabel2;
	int page = 0;
	private MenuItem donate = new MenuItem(res.getString(ABOUT_MENU_DONATE), 0x300000, 10) {
		public void run() {
			Browser.getDefaultSession().displayPage(res.getString(URL_DONATE));
		}
	};

	private MenuItem updateCheck = new MenuItem(res.getString(ABOUT_MENU_CHECK_UPDATES_NOW), 0x400000, 10) {
		public void run() {
			PleaseWaitTaskMonitorScreen wait = new PleaseWaitTaskMonitorScreen(new UpdatingBackgroundTask() {
				public void execute() {
					updateListener(res.getString(SETTINGS_LBL_CHECKING));
					if (!Version.checkAndPromptForUpdates(true)) {
						UiApplication.getUiApplication().invokeLater(new Runnable() {
							public void run() {
								Dialog.ask(Dialog.D_OK, res.getString(ABOUT_MSG_NO_UPDATES));
							}
						});
					}
				}
			}, false);
			wait.launch();
		}
	};
	private MenuItem next = new MenuItem(res.getString(GEN_LBL_NEXT), 1, 10) {
		public void run() {
			page++;
			showPage();
		}
	};

	/** The prev. */
	private MenuItem prev = new MenuItem(res.getString(GEN_LBL_PREV), 1, 10) {
		public void run() {
			page--;
			showPage();
		}
	};

	LabelField blank = new LabelField("");
	SeparatorField sep = new SeparatorField();

	public AboutScreen() {
		super(DEFAULT_CLOSE | DEFAULT_MENU);
		italics = getFont().derive(Font.ITALIC);
		bold = getFont().derive(Font.BOLD);
		versionLabel = new LabelField("BBSSH Version: " + Version.getAppVersion() +
				" Build: " + Version.getBuildNumber());
		versionLabel2 = new LabelField("Optimized for BB OS " + PlatformServicesProvider.getInstance().getOSVersion());
		versionLabel.setFont(bold);

		showPage();
		// 

	}

	protected boolean keyChar(char c, int status, int time) {
		if (c == ' ') {
			if ((status & (KeypadListener.STATUS_SHIFT | KeypadListener.STATUS_SHIFT_LEFT | KeypadListener.STATUS_SHIFT_RIGHT)) > 0) {
				page--;

			} else {
				page++;
			}
			showPage();
		}
		return super.keyChar(c, status, time);
	}

	/* (non-Javadoc)
	 * @see net.rim.device.api.ui.container.MainScreen#makeMenu(net.rim.device.api.ui.component.Menu, int)
	 */
	protected void makeMenu(Menu menu, int instance) {
		super.makeMenu(menu, instance);
		menu.add(updateCheck);
		menu.add(donate);
		if (page < 4) {
			menu.add(next);
			menu.setDefault(next);
		}
		if (page > 1) {
			menu.add(prev);
		}
	}

	void addTitle() {
		setTitle(res, MENU_ABOUT);
		add(versionLabel);
		add(versionLabel2);
		add(sep);
	}

	void showProjects() {
		setTitle(res, HELP_ABOUT_TITLE_PROJECTS);

		addURLAndDescription(HELP_ABOUT_LBL_PROJECTS_0, HELP_ABOUT_URL_PROJECTS_0);
		addURLAndDescription(HELP_ABOUT_LBL_PROJECTS_1, HELP_ABOUT_URL_PROJECTS_1);
		addURLAndDescription(HELP_ABOUT_LBL_PROJECTS_2, HELP_ABOUT_URL_PROJECTS_2);

	}

	void showAd() {
		setTitle(res, HELP_ABOUT_TITLE_FOR_HIRE);
		add(new LabelField(res, HELP_ABOUT_LBL_FOR_HIRE));
		add(blank);
		add(new HyperlinkField(HELP_ABOUT_URL_NOETISYS, 0, HELP_ABOUT_URL_NOETISYS));

	}

	private void addURLAndDescription(int descId, int urlId) {
		addURLAndDescription(descId, urlId, true);
	}

	private void addURLAndDescription(int descId, int urlId, boolean sameLine) {
		LabelField lf = new LabelField(res.getString(descId));
		HyperlinkField hf = new HyperlinkField(urlId, 0, urlId);

		if (sameLine) {
			HorizontalFieldManager hfm = new HorizontalFieldManager();
			hfm.add(lf);
			hfm.add(hf);
			add(hfm);
		} else {
			add(lf);
			add(hf);

		}
		add(new LabelField(" "));
	}

	void showSupport() {
		setTitle(res, HELP_ABOUT_TITLE_SUPPORT);
		addURLAndDescription(HELP_ABOUT_LBL_SUPPORT_0, HELP_ABOUT_URL_SUPPORT_0, false);
		addURLAndDescription(HELP_ABOUT_LBL_SUPPORT_1, HELP_ABOUT_URL_SUPPORT_1, false);
		addURLAndDescription(HELP_ABOUT_LBL_SUPPORT_2, HELP_ABOUT_URL_SUPPORT_2, false);
		addURLAndDescription(HELP_ABOUT_LBL_SUPPORT_3, HELP_ABOUT_URL_SUPPORT_3, false);

	}

	void showAbout() {
		add(new LabelField(res, HELP_ABOUT_LBL_ABOUT_1));
		add(blank);
		add(new LabelField(res, HELP_ABOUT_LBL_ABOUT_2));
	}

	void showFeatures() {
		setTitle(res, HELP_ABOUT_TITLE_FEATURES);

		LabelField l = new LabelField(res, HELP_ABOUT_LBL_FEATURES);
		l.setFont(italics);
		add(l);

		String[] data = res.getStringArray(HELP_ABOUT_LST_FEATURES);
		for (int x = 0; x < data.length; x++) {
			add(new LabelField(data[x], Field.USE_ALL_WIDTH | LabelField.FIELD_LEFT));
		}
	}

	void showPage() {

		deleteAll();
		addTitle();
		switch (page) {
			case 0:
				showAbout();
				break;
			case 1:
				showFeatures();
				break;
			case 2:
				showProjects();
				break;
			case 3:
				showAd();
				break;
			case 4:
				showSupport();
				break;
		}

	}

	protected boolean keyDown(int keycode, int time) {
		if (Keypad.key(keycode) == Keypad.KEY_ESCAPE) {
			if (page < 4) {
				page++;
				showPage();
				return true; 
			}

		}
		return super.keyDown(keycode, time);
	}
}
