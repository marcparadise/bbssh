package org.bbssh.ui.screens;

import net.rim.blackberry.api.homescreen.HomeScreen;
import net.rim.blackberry.api.homescreen.Shortcut;
import net.rim.blackberry.api.homescreen.ShortcutProvider;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.StandardTitleBar;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.picker.HomeScreenLocationPicker;

import org.bbssh.i18n.BBSSHResource;
import org.bbssh.model.ConnectionProperties;
import org.bbssh.model.SettingsManager;
import org.bbssh.platform.PlatformServicesProvider;
import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.session.SessionManager;
import org.bbssh.ui.components.OKCancelControl;
import org.bbssh.util.Tools;

public class PrimaryScreen_60 extends PrimaryScreen_47 {
	/** Menu to add item as shortcut */

	private final MenuItem addShortcutMenu = new MenuItem(res, BBSSHResource.MENU_SHORTCUT_ADD, 0x00020000, 1) {
		public void run() {
			ConnectionProperties prop = getPropertiesForCurrentSelection();
			if (prop != null) {
				addShortcut(prop);
			}

		}
	};

	private final MenuItem removeShortcutMenu = new MenuItem(res, BBSSHResource.MENU_SHORTCUT_REMOVE, 0x00020000, 2) {
		public void run() {
			ConnectionProperties prop = getPropertiesForCurrentSelection();
			if (prop != null) {
				removeShortcut(prop);
			}
		}
	};

	public PrimaryScreen_60() {
		super();
		if (PlatformServicesProvider.getInstance().isEnhancedTitlebarSupported()
				&& SettingsManager.getSettings().isTitlebarDisplayEnabled()) {
			StandardTitleBar tb = new StandardTitleBar().addClock().addSignalIndicator().addNotifications();

			tb.setPropertyValue(StandardTitleBar.PROPERTY_BATTERY_VISIBILITY, StandardTitleBar.BATTERY_VISIBLE_ALWAYS);
			tb.setPropertyValue(StandardTitleBar.PROPERTY_WIFI_VISIBILITY, StandardTitleBar.PROPERTY_VALUE_ON);
			tb.setPropertyValue(StandardTitleBar.PROPERTY_CELLULAR_VISIBILITY, StandardTitleBar.PROPERTY_VALUE_OFF);

			setTitleBar(tb);
		}

	}

	protected void addAndRefresh(ConnectionProperties prop) {
		super.addAndRefresh(prop);
		Shortcut sc = HomeScreen.getShortcut(prop.getUIDAsString());
		if (sc != null) {
			sc.setDescription(prop.getName());
		}
	}

	protected void removeShortcut(ConnectionProperties prop) {
		super.removeShortcut(prop);
		String id = prop.getUIDAsString();
		if (HomeScreen.doesShortcutExist(id)) {
			HomeScreen.removeShortcut(id);
		}
	}

	private static class LocationPickerPopup extends PopupScreen implements FieldChangeListener {
		OKCancelControl okcancel = new OKCancelControl();
		HomeScreenLocationPicker pick = HomeScreenLocationPicker.create();
		private boolean canceled;

		public LocationPickerPopup() {
			super(new VerticalFieldManager(Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR));
			add(pick);
			add(okcancel);
			okcancel.setChangeListener(this);
		}

		public void fieldChanged(Field field, int context) {
			if (field == okcancel) {
				if (context == OKCancelControl.CONTEXT_OK_PRESS) {
					canceled = false;
					close();
				} else if (context == OKCancelControl.CONTEXT_CANCEL_PRESS) {
					canceled = true;
					close();
				}
			}
		}
	}

	private void addShortcut(ConnectionProperties prop) {
		String id = String.valueOf(prop.getUID());
		LocationPickerPopup pop = new LocationPickerPopup();
		UiApplication.getUiApplication().pushModalScreen(pop);
		if (pop.canceled) {
			return;
		}
		Shortcut sc = ShortcutProvider.createShortcut(prop.getName(), id, 0);
		sc.setIsEditable(true);
		sc.setIsFavourite(pop.pick.getIsFavourite());
		RemoteSessionInstance rsi = SessionManager.getInstance().getFirstConnectedSession(prop);
		if (rsi == null || !rsi.isConnected()) {
			sc.setIcon(Tools.loadEncodedImage("shortcut.png"));
		} else {
			sc.setIcon(Tools.loadEncodedImage("shortcut-connected.png"));
		}
		HomeScreen.addShortcut(sc, pop.pick.getLocation());

	}

	public void makeMenu(Menu menu, int instance) {
		super.makeMenu(menu, instance);
		ConnectionProperties prop = getPropertiesForCurrentSelection();
		if (prop == null)
			return;
		String id = String.valueOf(prop.getUID());
		if (HomeScreen.doesShortcutExist(id)) {
			menu.add(removeShortcutMenu);
		} else {
			menu.add(addShortcutMenu);
		}
	}

}
