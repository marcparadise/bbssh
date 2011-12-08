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
package org.bbssh.ui.components;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import org.bbssh.i18n.BBSSHResource;

/**
 * Simple password prompt for a stored keyphrase
 * @todo refactor ok/cancel into a base component.
 */
public final class KeyPasswordPrompt extends PopupScreen implements BBSSHResource,
		FieldChangeListener {
	ResourceBundle res = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);
	private PasswordControl passwordField;
	private OKCancelControl okCancel;
	private CheckboxField savePassword;
	private LabelField titleLabel;
	private boolean okPressed;

	public KeyPasswordPrompt() {
		super(new VerticalFieldManager(VERTICAL_SCROLL | VERTICAL_SCROLLBAR), DEFAULT_CLOSE);
		titleLabel = new LabelField();
		add(titleLabel);
		passwordField = new PasswordControl(true, true);
		add(passwordField);
		savePassword = new CheckboxField(res.getString(KEY_PASS_LBL_UPDATE_KEY), false);
		add(savePassword);
		okCancel = new OKCancelControl();
		okCancel.setChangeListener(this);
		add(okCancel);
	

	}

	public String getPassword() {
		return passwordField.getPassword();
	}

	public boolean getSavePassword() {
		return savePassword.getChecked();
	}

	public boolean doModal(String keyName) {
		titleLabel.setText(res.getString(KEY_PASS_LBL_TITLE) + keyName);
		okPressed = false;
		UiApplication.getUiApplication().pushModalScreen(this);
		return okPressed;

	}

	public void fieldChanged(Field field, int context) {
		if (field == okCancel) {
			if (context == OKCancelControl.CONTEXT_OK_PRESS) { 
				okPressed = true;
			}
			close();
		}
	}
}
