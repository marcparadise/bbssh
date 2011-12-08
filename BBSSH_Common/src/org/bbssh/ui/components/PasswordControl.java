package org.bbssh.ui.components;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.PasswordEditField;
import net.rim.device.api.ui.container.VerticalFieldManager;

import org.bbssh.i18n.BBSSHResource;
import org.bbssh.model.SettingsManager;

/**
 * Simple UI control that uses global settings to determine whether or not to display an unmasked password field; or two
 * masked fields that are compared to each other to ensure they match. Add this to any screen that needs to collect
 * password data.
 * 
 * @author marc
 * 
 */
public class PasswordControl extends VerticalFieldManager implements BBSSHResource {
	boolean showPassword;
	ResourceBundle res = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

	BasicEditField password1;
	BasicEditField password2;
	boolean inputMode;
	boolean passwordRequired;

	/**
	 * Constructor for password control.
	 * 
	 * @param passwordRequired true if validation should include ensuring that a password has been entered.
	 * @param inputMode true if we're using this to input a password, but not assign a new one.
	 */
	public PasswordControl(boolean passwordRequired, boolean inputMode) {
		super(VerticalFieldManager.NO_VERTICAL_SCROLL);
		this.passwordRequired = passwordRequired;
		this.inputMode = inputMode;
		showPassword = SettingsManager.getSettings().getShowPlaintextPassword();
		if (showPassword) {
			password1 = new BasicEditField(res.getString(PASSWORD_LABEL_1), "", 64, PasswordEditField.NO_NEWLINE);
			add(password1);
		} else {
			password1 = new PasswordEditField(res.getString(PASSWORD_LABEL_1), "", 64, PasswordEditField.NO_NEWLINE);
			add(password1);
			if (!inputMode) {
				password2 = new PasswordEditField(res.getString(PASSWORD_LABEL_2), "", 64, PasswordEditField.NO_NEWLINE);
				add(password2);
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.ui.Manager#isDataValid()
	 */
	public boolean isDataValid() {
		if (passwordRequired && password1.getTextLength() == 0) {
			Dialog.ask(Dialog.D_OK, res.getString(PASSWORD_MSG_NO_PASSWORD));
			password1.setFocus();
			return false;
		}

		if (!inputMode) {
			if (!showPassword && password1.getText().compareTo(password2.getText()) != 0) {
				Dialog.ask(Dialog.D_OK, res.getString(PASSWORD_MSG_PASSWORD_MISMATCH));
				password2.setFocus();
				password2.setCursorPosition(password2.getTextLength());
				return false;
			}
		}
		return true;
	}

	/**
	 * Without validting, returns the entered password. If password is masked, then the password returned may not have
	 * been checked against the second 'confirm password' field. Only if isDataValid is first invoked successfully is
	 * caller guaranteed this is a good password.
	 * 
	 * @return entered, unconfirmed password or empty string if none.
	 */
	public String getPassword() {

		return password1.getText();
	}

	public void clearPassword() {
		password1.setText("");
		password2.setText("");

	}
}
