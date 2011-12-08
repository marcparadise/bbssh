package org.bbssh.ui.screens;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.i18n.ResourceBundleFamily;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import org.bbssh.i18n.BBSSHResource;
import org.bbssh.model.Settings;
import org.bbssh.model.SettingsManager;
import org.bbssh.ui.components.ClickableButtonField;
import org.bbssh.util.Tools;

public class StartupPopup extends PopupScreen implements BBSSHResource {
	ResourceBundleFamily res = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);
	private CheckboxField option1 = new CheckboxField();
	private ClickableButtonField button1 = new ClickableButtonField();
	private ClickableButtonField button2 = new ClickableButtonField();
	private HorizontalFieldManager buttons = new HorizontalFieldManager(Manager.FIELD_HCENTER);
	private RichTextField text = new RichTextField(RichTextField.READONLY);
	private VerticalFieldManager textMgr = new VerticalFieldManager();

	public StartupPopup() {
		super(new VerticalFieldManager(Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR));
		// Add this to start focus at the top.
		setFont(Tools.deriveBBSSHDialogFont(getFont()));
		showNextPage();

	}

	private void showNextPage() {
		Settings s = SettingsManager.getSettings();
		boolean show = true;
		deleteAll();
		buttons.deleteAll();

		if (!s.getRememberOption(Settings.REMEMBER_LICENSE_AGREEMENT_COMPLETE)) {
			showLicensePage();
		} else if (!s.getRememberOption(Settings.REMEMBER_CHECKED_UPDATE_OK)) {
			// Don't need tihs anymore, and it seems to cause trouble with the checkbox field
			// on some displays - making it display blank/empty.
			showUpdateCheckPage();
		} else if (!s.getRememberOption(Settings.REMEMBER_CHECKED_SEND_USAGE_STATS_OK)) {
			showStatsCheckPage();
		} else {
			show = false;
			UiApplication.getUiApplication().popScreen(this);
		}

		if (show) {
			add(buttons);
		}
		// attempting this in an effort to correct the issue where the
		// checkbox we add does not show properly.
		updateLayout();
	}

	void showLicensePage() {
		textMgr = new VerticalFieldManager(Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR);
		textMgr.add(text);
		add(textMgr);

		addArrayResourceLabels(STARTER_WIZ_LBL_LICENSE);
		addButton(button1, STARTER_WIZ_LBL_AGREE, new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				SettingsManager.getSettings().setRememberOption(Settings.REMEMBER_LICENSE_AGREEMENT_COMPLETE, true);
				// Make sure they don't see this prompt again, even if they magically reboot the app without
				// completing the next pages.
				SettingsManager.getInstance().commitData();
				showNextPage();
			}

		});
		addButton(button2, STARTER_WIZ_LBL_DECLINE, new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				close();
			}
		});

	}

	private void showUpdateCheckPage() {

		addOption(option1, STARTER_WIZ_LBL_ENABLE_UPDATE, true);
		addButton(button1, GENERAL_LBL_OK, new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				Settings s = SettingsManager.getSettings();
				s.setAutoCheckUpdates(option1.getChecked());
				s.setRememberOption(Settings.REMEMBER_CHECKED_UPDATE_OK, true);
				showNextPage();
			}
		});
	}

	private void showStatsCheckPage() {
		addOption(option1, STARTER_WIZ_LBL_ENABLE_STATS, true);
		addButton(button1, GENERAL_LBL_OK, new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				Settings s = SettingsManager.getSettings();
				s.setAnonymousUsageStatsEnabled(option1.getChecked());
				s.setRememberOption(Settings.REMEMBER_CHECKED_SEND_USAGE_STATS_OK, true);
				showNextPage();
			}
		});
	}

	void addButton(ButtonField field, int labelRes, FieldChangeListener action) {
		field.setLabel(res.getString(labelRes));
		field.setChangeListener(null);
		field.setChangeListener(action);
		addFieldIfRequired(buttons, field);

	}

	void addOption(CheckboxField option, int labelRes, boolean checked) {
		option.setLabel(res.getString(labelRes));
		option.setChecked(checked);
		addFieldIfRequired(this, option);
	}

	void addFieldIfRequired(Manager owner, Field field) {
		if (field.getManager() == null) {
			owner.add(field);
		}
	}

	void addArrayResourceLabels(int id) {
		String[] data = res.getStringArray(id);
		for (int x = 0; x < data.length; x++) {
			addResourceLabel(data[x]);
		}
	}

	void addResourceLabel(int id) {
		addResourceLabel(res.getString(id));
	}

	void addResourceLabel(String label) {
		text.insert(label);
		text.insert("\r\n");

	}

}
