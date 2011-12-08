/**
 * Copyright (c) 2010 Marc A. Paradise
 *
 * This file is part of "BBSSH"
 *
 * BBSSH is based upon MidpSSH by Karl von Randow.
 * MidpSSH was based upon Telnet Floyd and FloydSSH by Radek Polak.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */
package org.bbssh.ui.screens;

import java.io.IOException;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.HorizontalFieldManager;

import org.bbssh.model.ConnectionProperties;
import org.bbssh.model.FontSettings;
import org.bbssh.model.Key;
import org.bbssh.model.KeyManager;
import org.bbssh.ui.components.ClickableButtonField;
import org.bbssh.util.Tools;

/**
 * Screen for displaying and editing connection/session instance properties. Extends ConnectionProperties, which
 * provides for editing of properties that are not specific to any one session.
 * 
 */
public final class ConnectionInstancePropertiesScreen extends ConnectionPropertiesScreen {

	protected BasicEditField nameField;
	protected BasicEditField hostField;
	protected BasicEditField portField;
	protected BasicEditField userNameField;
	protected ClickableButtonField setPasswordButton;
	protected ClickableButtonField clearPasswordButton;
	protected HorizontalFieldManager passwordFieldManager;
	protected ObjectChoiceField sessionTypeField;
	protected ObjectChoiceField keyListField;
	protected ObjectChoiceField proxyModeField;
	protected BasicEditField proxyHostField;

	protected boolean clearPassword = false;
	protected boolean changePassword = false;
	protected String password = null;
	LabelField passwordNoteLabel;

	/**
	 * Instantiates a new session detail screen.
	 * 
	 * @param prop the prop
	 */
	ConnectionInstancePropertiesScreen(ConnectionProperties prop) {
		super(prop);
		if (prop.isNew()) {
			setTitle(res.getString(SESSION_DTL_TITLE_1));
		} else {
			setTitle(res.getString(SESSION_DTL_TITLE_2));
		}
	}

	/**
	 * Creates the fields.
	 */
	protected void createFields() {
		super.createFields();

		// Extract port from host
		String host = prop.getHost();
		String port = "";
		if (host != null && host.indexOf(':') >= 0) {
			int delimiterIndex = host.indexOf(':');
			port = host.substring(delimiterIndex + 1);
			host = host.substring(0, delimiterIndex);
		}
		// Basic
		sessionTypeField = new ObjectChoiceField(res.getString(SESSION_DTL_LBL_SESSION_TYPE), res
				.getStringArray(SESSION_DTL_LIST_SESSION_TYPES));
		sessionTypeField.setSelectedIndex(prop.getSessionType());

		nameField = new BasicEditField(res.getString(SESSION_DTL_LBL_CONN_NAME), prop.getName(), 64, Field.EDITABLE
				| BasicEditField.NO_NEWLINE | BasicEditField.NON_SPELLCHECKABLE);
		hostField = new BasicEditField(res.getString(SESSION_DTL_LBL_CONN_HOST), host, 255, Field.EDITABLE
				| BasicEditField.NO_NEWLINE | BasicEditField.NON_SPELLCHECKABLE | BasicEditField.FILTER_URL);
		portField = new BasicEditField(res.getString(SESSION_DTL_LBL_CONN_PORT), port, 5, Field.EDITABLE
				| BasicEditField.NO_NEWLINE | BasicEditField.NON_SPELLCHECKABLE | BasicEditField.FILTER_NUMERIC);
		userNameField = new BasicEditField(res.getString(SESSION_DTL_LBL_LOGIN_USERNAME), prop.getUsername(), 64,
				Field.EDITABLE | BasicEditField.NO_NEWLINE | BasicEditField.NON_SPELLCHECKABLE);
		setPasswordButton = new ClickableButtonField(res.getString(SESSION_DTL_LBL_SET_PASSWORD));
		passwordNoteLabel = new LabelField();
		passwordNoteLabel.setFont(Tools.deriveBBSSHDialogFont(passwordNoteLabel.getFont()));
		clearPasswordButton = new ClickableButtonField(res.getString(SESSION_DTL_LBL_CLEAR_PASSWORD));
		passwordFieldManager = new HorizontalFieldManager();
		passwordFieldManager.add(new LabelField(res.getString(SESSION_DTL_LBL_LOGIN_PASSWORD)));
		passwordFieldManager.add(setPasswordButton);
		passwordFieldManager.add(clearPasswordButton);
		

		setPasswordButton.setChangeListener(this);
		clearPasswordButton.setChangeListener(this);
		updatePasswordNoteText(prop.getPassword());
		KeyManager mgr = KeyManager.getInstance();
		Object[] keys = Tools.vectorToArray(mgr.getKeys(), 1);
		keys[0] = res.getString(SESSION_DTL_NO_KEY);
		int index = mgr.findKeyIndexById(prop.getKeyId());
		keyListField = new ObjectChoiceField(res.getString(SESSION_DTL_LBL_KEY), keys);
		if (index > -1) {
			keyListField.setSelectedIndex(index + 1);
		} else {
			keyListField.setSelectedIndex(0);
		}
		int mode = prop.getHttpProxyMode();
		if (mode == -1)
			mode = 0;
		proxyModeField = new ObjectChoiceField(res.getString(SESSION_DTL_LBL_HTTP_PROXY_MODE),
				res.getStringArray(SESSION_DTL_LIST_PROXY_CHOICES), mode);
		// Note: disable if proxy none.
		proxyHostField = new BasicEditField(res.getString(SESSION_DTL_LBL_HTTP_PROXY_HOST), prop.getHttpProxyHost(),
				255, Field.EDITABLE | BasicEditField.NO_NEWLINE | BasicEditField.NON_SPELLCHECKABLE);
	}

	void updatePasswordNoteText(String password) {
		if (password == null) {
			passwordNoteLabel.setText(res.getString(SESSION_DTL_LBL_NOPASS));
		} else {
			passwordNoteLabel.setText(res.getString(SESSION_DTL_LBL_HASPASS));
		}

	}

	void updateFontButtonText(FontSettings fs) {
		String s = fs.toString();
		if (s == null || s.length() == 0) {
			s = res.getString(GEN_LBL_CLICK_TO_CHOOSE);
		}
		chooseFont.setLabel(s);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.rim.device.api.ui.Screen#isDataValid()
	 */
	public boolean isDataValid() {
		if (nameField.getTextLength() < 1) {
			Status.show(res.getString(MSG_INVALID_SESSION_NAME));
			nameField.setFocus();
			return false;
		}
		if (!validateHostAndPort()) {
			return false;
		}
		// telnet has no authentication guaranteed (as you can connect via telnet to
		// any socket) so username and password are not supported
		if (sessionTypeField.getSelectedIndex() == ConnectionProperties.SESSION_TYPE_TELNET) {
			if ((password != null && password.length() > 0) || userNameField.getTextLength() > 0) {
				Status.show(res.getString(MSG_NO_USER_NAME_PASS_ALLOWED));
				userNameField.setFocus();
				return false;
			}
			// -1 = 0 no selection (good) , 0 = "make a choice" indicator.
			if (keyListField.getSelectedIndex() > 0) {
				Status.show(res.getString(MSG_NO_KEY_ALLOWED));
				keyListField.setFocus();
				return false;
			}

		} else {
			int val;
			if (keepAliveDurationField.getTextLength() == 0) {
				val = -1;
			} else {
				val = Integer.parseInt(keepAliveDurationField.getText());
			}
			// If the user entered no value, or if value < 0, stop them.
			if (val < 0) {
				Status.show(res.getString(MSG_INVALID_CLIENT_KEEPALIVE));
				keepAliveDurationField.setFocus();
				return false;
			}
			// Values of less than 60 can drain the battery. Warn the user.
			if (val > 0 && val < 60) {
				if (Dialog.ask(Dialog.D_YES_NO, res.getString(MSG_CLIENT_KEEPALIVE_LOW_VAL_WARN), 0) == Dialog.NO) {
					keepAliveDurationField.setFocus();
					return false;
				}
			}

		}

		// If proxy is enabled, a host must be provided.
		if (proxyModeField.getSelectedIndex() > 0) {
			if (proxyHostField.getTextLength() < 1) {
				Status.show(res.getString(MSG_PROXY_HOST_INVALID));
				proxyHostField.setFocus();
				return false;
			}
		}

		return super.isDataValid();
		// .
		// Note: we used to have checking of WAP2 service book availability,
		// but because th service book name will vary regionally, I haven't found a
		// reliable way to do that.

	}

	private boolean validateHostAndPort() {
		String port;
		String host = hostField.getText().trim();
		if (host.length() == 0) {
			Status.show(res.getString(MSG_INVALID_HOST_NAME));
			hostField.setFocus();
			return false;
		}
		int pos = host.indexOf(':');
		if (pos == -1) {
			port = portField.getText().trim();
			if (port.length() == 0) {
				if (sessionTypeField.getSelectedIndex() == ConnectionProperties.SESSION_TYPE_SSH)
					port = "22";
				else
					port = "23";
			}
		} else {
			if (pos == host.length() - 1) { // eg host.com: w/ nothing after colon
				port = "0";
			} else {
				port = host.substring(pos + 1);
			}
			host = host.substring(0, pos - 1);
		}
		hostField.setText(host);
		portField.setText(port);
		try {
			int value = Integer.parseInt(port);
			if (value <= 0 || value > 65535) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException e) {
			Status.show(res.getString(MSG_INVALID_PORT));
			portField.setFocus();
			return false;

		}
		return true;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.rim.device.api.ui.Screen#save()
	 */
	public void save() throws IOException {
		super.save();
		int keyId = keyListField.getSelectedIndex();
		if (keyId > 0) {
			Key k = (Key) keyListField.getChoice(keyId);
			keyId = k.getId();
		} else {
			keyId = -1; // keyId == 0 is "no selection" row.
		}
		prop.setName(nameField.getText());

		prop.setHost(hostField.getText() + ':' + portField.getText());
		prop.setUsername(userNameField.getText());
		if (changePassword && password != null) {
			prop.setPassword(password);
		}

		if (clearPassword) {
			prop.setPassword(null);
		}
		prop.setSessionType((byte) sessionTypeField.getSelectedIndex());
		prop.setKeyId(keyId);
		prop.setConnectionType((byte) connectionTypeField.getSelectedIndex());
		prop.setHttpProxyMode((byte) proxyModeField.getSelectedIndex());
		prop.setHttpProxyHost(proxyHostField.getText());
		super.save();
	}

	/**
	 * Adds fields to the screen
	 * 
	 * @todo split into ssh vs telnet -for validation too. Provide a ConnectionPropertiesValidator?
	 */
	protected void addFields() {
		super.addFields();

		baseFields.add(nameField);
		baseFields.add(hostField);
		baseFields.add(portField);
		baseFields.add(sessionTypeField);
		baseFields.add(userNameField);
		baseFields.add(passwordFieldManager);
		baseFields.add(passwordNoteLabel);
		baseFields.add(keyListField);

		networkingFields.add(proxyModeField);
		networkingFields.add(proxyHostField);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.rim.device.api.ui.FieldChangeListener#fieldChanged(net.rim.device.api.ui.Field, int)
	 */
	public void fieldChanged(Field field, int context) {
		if (field == setPasswordButton) {
			if (sessionTypeField.getSelectedIndex() == ConnectionProperties.SESSION_TYPE_TELNET) {
				Status.show(res.getString(MSG_NO_USER_NAME_PASS_ALLOWED));
				return;
			}
			PasswordPromptPopup prompt = new PasswordPromptPopup(res.getString(SESSION_DTL_LBL_CONNECTION_PASSWORD),
					false);
			if (prompt.show()) {
				password = prompt.getPassword();
				changePassword = true;
				clearPassword = false;
				updatePasswordNoteText(password);
			} 
		} else if (field == clearPasswordButton) {
			clearPassword = true;
			changePassword = false;
			updatePasswordNoteText(null);
			Status.show(res.getString(SESSION_DTL_MSG_PASSWORD_CLEARED));
		} else {
			super.fieldChanged(field, context);
		}
	}

}
