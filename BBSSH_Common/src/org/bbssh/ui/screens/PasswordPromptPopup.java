package org.bbssh.ui.screens;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import org.bbssh.i18n.BBSSHResource;
import org.bbssh.ui.components.OKCancelControl;
import org.bbssh.ui.components.PasswordControl;

public class PasswordPromptPopup extends PopupScreen implements FieldChangeListener {
	BasicEditField usernameField;
	PasswordControl control;
	OKCancelControl okCancel = new OKCancelControl();
	boolean okPressed = false;

	/**
	 * Prompt for both username and password, using the provided default username. This does not permit matching
	 * password validation, as it assumes that this is live input mode.
	 * 
	 * @param promptText message to display
	 * @param defaultUserName defautl user name to use if any.
	 */
	public PasswordPromptPopup(String promptText, String defaultUserName, boolean passwordRequired) {
		this(promptText, true, passwordRequired);
		String label = ResourceBundle.getBundle(BBSSHResource.BUNDLE_ID, BBSSHResource.BUNDLE_NAME).getString(
				BBSSHResource.SESSION_DTL_LBL_LOGIN_USERNAME);
		if (defaultUserName == null)
			defaultUserName = "";
		usernameField = new BasicEditField(label, defaultUserName);
		insert(usernameField, 1);
	}

	/**
	 * Constructor for password prompt dialog
	 * 
	 * @param promptText message to display when asking for password.
	 * @param inputMode true if using dialog to request a password, but not assign it. (We will not require matching
	 *            password verification if so)
	 */
	public PasswordPromptPopup(String promptText, boolean inputMode) {
		this(promptText, inputMode, true);
	}

	public PasswordPromptPopup(String promptText, boolean inputMode, boolean passwordRequired) {
		super(new VerticalFieldManager(VERTICAL_SCROLL | VERTICAL_SCROLLBAR));
		add(new LabelField(promptText));
		control = new PasswordControl(passwordRequired, inputMode);
		add(control);
		add(okCancel);
		okCancel.setChangeListener(this);
	}

	/**
	 * Display the password popup using the specified prompt. Returns true if the user entered a confirmed password, and
	 * false if they canceled.
	 * 
	 * @return true if OK is pressed and password is confirmed and present.
	 */
	public boolean show() {
		okPressed = false;
		UiApplication.getUiApplication().pushModalScreen(this);
		return okPressed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.ui.Field#keyChar(char, int, int)
	 */
	protected boolean keyChar(char character, int status, int time) {
		// @todo this enter/esc behavior is pretty redundant. Let's combine it with
		// okcancel control, and make a base DialogBox class - then refactor the
		// places where we need to use it.
		if (character == Keypad.KEY_ENTER) {
			fieldChanged(okCancel, OKCancelControl.CONTEXT_OK_PRESS);
			return true;
		}
		if (character == Keypad.KEY_ESCAPE) {
			fieldChanged(okCancel, OKCancelControl.CONTEXT_CANCEL_PRESS);
			return true;
		}
		return super.keyChar(character, status, time);
	}

	/**
	 * 
	 * @return the entered username if a username was requested. If none was requested it will return null.
	 */
	public String getUsername() {
		if (usernameField == null)
			return null;

		return usernameField.getText();
	}

	/**
	 * Returns the entered, validated password. Password is only to be used if promptForPassword returns true;
	 * 
	 * @return entered password
	 */
	public String getPassword() {
		return control.getPassword();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.ui.FieldChangeListener#fieldChanged(net.rim.device.api.ui.Field, int)
	 */
	public void fieldChanged(Field field, int context) {
		if (field == okCancel) {
			if (context == OKCancelControl.CONTEXT_CANCEL_PRESS) {
				close();
			} else if (context == OKCancelControl.CONTEXT_OK_PRESS) {
				if (usernameField != null) {
					if (usernameField.getTextLength() == 0) {
						Status.show(ResourceBundle.getBundle(BBSSHResource.BUNDLE_ID, BBSSHResource.BUNDLE_NAME)
								.getString(BBSSHResource.MSG_INVALID_USERNAME));
						usernameField.setFocus();
					}
				}
				if (control.isDataValid()) {
					okPressed = true;
					close();
				}
			}
		}

	}

}
