package org.bbssh.ui.screens;

import net.rim.device.api.system.Sensor;
import net.rim.device.api.system.SensorListener;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.StandardTitleBar;
import net.rim.device.api.ui.input.InputSettings;
import net.rim.device.api.ui.input.NavigationDeviceSettings;
import net.rim.device.api.ui.input.TouchscreenSettings;

import org.bbssh.BBSSHApp;
import org.bbssh.exceptions.FontNotFoundException;
import org.bbssh.model.SettingsManager;
import org.bbssh.net.ConnectionHelper;
import org.bbssh.platform.PlatformServicesProvider;
import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.session.SessionManager;

public class TerminalScreen_60 extends TerminalScreen_47 implements SensorListener {
	public TerminalScreen_60() {
		// Now set up the title bar if the user wants it
		if (PlatformServicesProvider.getInstance().isEnhancedTitlebarSupported()
				&& SettingsManager.getSettings().isTitlebarDisplayEnabled()) {
			StandardTitleBar tb = new StandardTitleBar()
					.addClock()
					.addSignalIndicator() // seems this is required in order to do WIFI_VISIBILITY
					.addNotifications();

			tb.setPropertyValue(StandardTitleBar.PROPERTY_BATTERY_VISIBILITY, StandardTitleBar.BATTERY_VISIBLE_ALWAYS);

			// Display connectivity icon for the connection type we're using.
			getOverlayManager().setTitleBar(tb);

			InputSettings ts = TouchscreenSettings.createEmptySet();
			InputSettings ns = NavigationDeviceSettings.createEmptySet();
			ns.set(NavigationDeviceSettings.DETECT_SWIPE, 1);
			ts.set(TouchscreenSettings.DETECT_PINCH, 1);
			ts.set(TouchscreenSettings.DETECT_HOVER, 1);
			addInputSettings(ns);
			addInputSettings(ts);

			Sensor.addListener(UiApplication.getUiApplication(), this, Sensor.SLIDE);
		}

	}

	public void attachSession(RemoteSessionInstance rsi) throws FontNotFoundException {
		super.attachSession(rsi);
		Field tb = getOverlayManager().getTitleBar();
		if (tb instanceof StandardTitleBar) {
			StandardTitleBar st = (StandardTitleBar) tb;
			st.removeTitle();
			// @todo also add stats, and or conn state?
			st.addTitle(rsi.state.settings.getName());
			if (rsi.session.isWifiOverrideConnection()
					|| rsi.state.settings.getConnectionType() == ConnectionHelper.CONNECTION_TYPE_WIFI) {
				st.setPropertyValue(StandardTitleBar.PROPERTY_WIFI_VISIBILITY, StandardTitleBar.PROPERTY_VALUE_ON);
				st.setPropertyValue(StandardTitleBar.PROPERTY_CELLULAR_VISIBILITY, StandardTitleBar.PROPERTY_VALUE_OFF);
			} else {
				st.setPropertyValue(StandardTitleBar.PROPERTY_WIFI_VISIBILITY, StandardTitleBar.PROPERTY_VALUE_OFF);
				st.setPropertyValue(StandardTitleBar.PROPERTY_CELLULAR_VISIBILITY, StandardTitleBar.PROPERTY_VALUE_ON);
			}

		}
	}

	public void onSensorUpdate(int sensorId, int update) {
		if (!BBSSHApp.inst().isForeground())
			return;
		if (sensorId == Sensor.SLIDE) {
			SessionManager mgr = SessionManager.getInstance();
			if (!mgr.getTerminalScreen().isDisplayed())
				return;

			if (update == Sensor.STATE_SLIDE_CLOSED) {
				if (mgr.activeSession != null) {
					mgr.activeSession.state.setArtificialStatus(0);
					invalidateStatusIcons();
				}
				// Treat this as user-requested so that we properly resize the display.
				if (SettingsManager.getSettings().getShowKeyboardOnSliderClose())
					showVirtualKeyboard(true);
			} else {
				hideVirtualKeyboard();
			}
		}
	}

	public boolean onClose() {
		Sensor.removeListener(UiApplication.getUiApplication(), this);
		return super.onClose();
	}
}
