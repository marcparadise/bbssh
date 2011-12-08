package org.bbssh.ui.screens;

import net.rim.device.api.i18n.ResourceBundleFamily;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.MainScreen;

import org.bbssh.BBSSHApp;
import org.bbssh.i18n.BBSSHResource;
import org.bbssh.model.DataStoreCleaner;
import org.bbssh.ui.components.ClickableButtonField;
import org.bbssh.util.Tools;

public class RecoveryScreen extends MainScreen implements BBSSHResource {
	ResourceBundleFamily res = ResourceBundleFamily.getBundle(BUNDLE_ID, BUNDLE_NAME);
	boolean cleanupPerformed = false;

	public RecoveryScreen(int instructionResId) {
		setTitle(res, RECOVERY_TITLE);
		setFont(Tools.deriveBBSSHDialogFont(getFont()));
		add(new LabelField(res, instructionResId));
		add(new SeparatorField());
		add(new CheckboxField(res.getString(RECOVERY_RESET_SAVED_CONNECTIONS), false));
		add(new CheckboxField(res.getString(RECOVERY_RESET_SAVED_KEYS), false));
		add(new CheckboxField(res.getString(RECOVERY_RESET_MACROS), false));
		add(new CheckboxField(res.getString(RECOVERY_RESET_SETTINGS), false));
		add(new CheckboxField(res.getString(RECOVERY_RESET_SHORTCUTS), false));
		add(new SeparatorField());
		add(new ClickableButtonField(res.getString(RECOVERY_RESET_CLEAR_ALL), new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				Manager m = field.getManager();
				if (m != null) {
					int count = m.getFieldCount();
					for (int x = 0; x < count; x++) {
						Field f = m.getField(x);
						if (f != null && f instanceof CheckboxField) {
							((CheckboxField) f).setChecked(true);
						}
					}

				}
			}
		}));
		add(new ClickableButtonField(res.getString(RECOVERY_LABEL_EXIT_NO_RESET), new FieldChangeListener() {

			public void fieldChanged(Field field, int context) {
				onClose();
			}
		}));
		add(new ClickableButtonField(res.getString(RECOVERY_LABEL_RESET_DATA), new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				Manager m = field.getManager();
				if (m == null) {
					return;
				}
				int count = m.getFieldCount();
				byte repos = 0;
				for (int x = 0; x < count; x++) {
					Field f = m.getField(x);
					if (f != null && f instanceof CheckboxField) {
						if (((CheckboxField) f).getChecked()) {
							DataStoreCleaner.cleanData(repos);
							cleanupPerformed = true;
						}
						repos++;
					}
				}

				m.deleteAll();
				LabelField l = new LabelField(res, RECOVERY_LABEL_EXIT) {
					protected void paint(Graphics graphics) {
						graphics.setColor(Color.RED);
						super.paint(graphics);
					};

				};
				l.setFont(getFont().derive(Font.BOLD));
				m.add(l);
			}
		}));

	}

	public void close() {
		BBSSHApp.inst().saveAllSettings();
		super.close();
	}

}
