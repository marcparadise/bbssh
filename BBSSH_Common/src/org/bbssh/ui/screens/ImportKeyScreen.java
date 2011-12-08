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

import java.io.IOException;

import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.crypto.CryptoTokenException;
import net.rim.device.api.crypto.CryptoUnsupportedOperationException;
import net.rim.device.api.crypto.DSACryptoSystem;
import net.rim.device.api.crypto.DSAKeyPair;
import net.rim.device.api.crypto.DSAPrivateKey;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BasicEditField;

import org.bbssh.BBSSHApp;
import org.bbssh.ui.components.ClickableButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.RadioButtonField;
import net.rim.device.api.ui.component.RadioButtonGroup;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import org.bbssh.crypto.TypesWriter;
import org.bbssh.i18n.BBSSHResource;
import org.bbssh.model.Key;
import org.bbssh.model.KeyManager;
import org.bbssh.patterns.UpdatingBackgroundTask;
import org.bbssh.platform.PlatformServicesProvider;
import org.bbssh.ui.components.FileSelectorPopupScreen;
import org.bbssh.ui.components.PasswordControl;
import org.bbssh.ui.components.PleaseWaitTaskMonitorScreen;
import org.bbssh.util.Logger;

import ch.ethz.ssh2.crypto.PEMDecoder;
import ch.ethz.ssh2.crypto.PEMStructure;

/**
 * Used for importing a key and configuring it, or creating a new key.
 */
public final class ImportKeyScreen extends PopupScreen implements BBSSHResource, FieldChangeListener {

	ResourceBundle res = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);
	BasicEditField friendlyName;
	BasicEditField location;
	RadioButtonGroup importType = new RadioButtonGroup();
	RadioButtonField importFromFile;
	RadioButtonField importFromURL;
	PasswordControl passwordControl;
	ClickableButtonField loadFile;

	ClickableButtonField importButton;
	Key key;
	boolean generateNew = false;
	private ClickableButtonField cancelButton;
	private String fileName;

	/**
	 * Instantiates a new import key screen.
	 * 
	 * @param generateNew the generate new
	 */
	public ImportKeyScreen(boolean generateNew) {
		super(new VerticalFieldManager(VERTICAL_SCROLL | VERTICAL_SCROLLBAR), DEFAULT_CLOSE);

		int actionLabel;
		this.generateNew = generateNew;
		// @todo this same form shoudl support editing?
		friendlyName = new BasicEditField(res.getString(IMPORT_KEY_LBL_FRIENDLY_NAME), "", 32,
				BasicEditField.NO_NEWLINE | BasicEditField.NON_SPELLCHECKABLE);
		add(friendlyName);
		if (generateNew) {
			actionLabel = KEY_MGR_LABEL_GENERATE;
			// nothing else to add here.
		} else {
			actionLabel = MENU_IMPORT;
			// we'll default to displaying the URL import, but radio button will allow
			// user to choose "from file".
			location = new BasicEditField(res.getString(IMPORT_KEY_LBL_LOCATION), "http://", 1024,
					BasicEditField.NO_NEWLINE | BasicEditField.NON_SPELLCHECKABLE | BasicEditField.FILTER_URL);
			importFromFile = new RadioButtonField(res.getString(IMPORT_KEY_LBL_AS_FILE), importType, false);
			importFromURL = new RadioButtonField(res.getString(IMPORT_KEY_LBL_AS_URL), importType, true);
			loadFile = new ClickableButtonField(res.getString(IMPORT_KEY_LBL_CHOOSE_FILE));
			loadFile.setChangeListener(this);
			add(importFromFile);
			add(importFromURL);
			add(location);
			importType.setChangeListener(this);
			importType.setNotifyReselected(false);
			passwordControl = new PasswordControl(false, true);
			add(passwordControl);
			add(new SeparatorField());
		}

		HorizontalFieldManager hfm = new HorizontalFieldManager(HorizontalFieldManager.FIELD_HCENTER);
		importButton = new ClickableButtonField(res.getString(actionLabel));
		importButton.setChangeListener(this);
		hfm.add(importButton);

		cancelButton = new ClickableButtonField(res.getString(GENERAL_LBL_CANCEL));
		cancelButton.setChangeListener(this);
		hfm.add(cancelButton);
		add(hfm);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.ui.Screen#isDirty()
	 */
	public boolean isDirty() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.ui.Screen#onSavePrompt()
	 */
	protected boolean onSavePrompt() {
		return true;
	}

	/**
	 * Do shared validation.
	 * 
	 * @return true, if successful
	 */
	private boolean doSharedValidation() {
		if (friendlyName.getTextLength() == 0) {
			Status.show(res.getString(MSG_KEYPAIR_NAME_REQUIRED));
			friendlyName.setFocus();
			return false;
		}

		if (!passwordControl.isDataValid()) {
			passwordControl.setFocus();
			return false;
		}
		return true;
	}

	private void doGenerate() {
		try {
			DSACryptoSystem dcs = new DSACryptoSystem();
			DSAKeyPair pair = dcs.createDSAKeyPair();
			DSAPrivateKey pk = pair.getDSAPrivateKey();
			TypesWriter writer = new TypesWriter();
			writer.writeString(dcs.getP());
			writer.writeString(dcs.getQ());
			writer.writeString(dcs.getG());
			writer.writeString(pk.getPublicKeyData());
			writer.writeString(pk.getPrivateKeyData());
			key = new Key(friendlyName.getText(), writer.getBytes());
			Status.show(res.getString(MSG_KEYPAIR_GENERATE_SUCCESS));
			close();

		} catch (CryptoTokenException e) {
			Dialog.ask(Dialog.OK, res.getString(MSG_KEYPAIR_ERROR_OCCURRED) + " (" + e.toString() + ")");
		} catch (CryptoUnsupportedOperationException e) {
			Dialog.ask(Dialog.OK, res.getString(MSG_KEYPAIR_ERROR_OCCURRED) + " (" + e.toString() + ")");
		}

	}

	private String url;
	private String failError;

	/**
	 * Import a key from a file (http or SD card)
	 */
	private void doImport() {
		if (!doSharedValidation()) {
			return;
		}

		if (!BBSSHApp.inst().requestPermission(ApplicationPermissions.PERMISSION_FILE_API,
				BBSSHResource.MSG_PERMISSIONS_MISSING_FILE_IMPORT)) {
			importType.setSelectedIndex(1);
			return;
		}

		if (importType.getSelectedIndex() == 1) {
			// @todo we need to use connection type full rnage...
			url = location.getText();
			if (url.length() == 0) {
				Status.show(res.getString(MSG_INVALID_URL_OR_LOC));
				return;
			}
		} else {
			if (fileName == null || fileName.length() == 0) {
				Status.show(res.getString(IMPORT_KEY_MSG_NO_FILE));
				return;
			}
			url = fileName;
		}

		try {
			PleaseWaitTaskMonitorScreen wait = new PleaseWaitTaskMonitorScreen(new UpdatingBackgroundTask() {
				public void execute() {
					try {
						updateListener(res.getString(SETTINGS_LBL_LOADING_KEY));
						key = KeyManager.loadKey(friendlyName.getText(), url);
					} catch (IOException ex) {
						failError = ex.getMessage();
						Logger.error("Error checking for updates: " + failError);
						key = null;
					}
				}
			}, true);

			wait.launch();
			if (failError != null) {
				Dialog.ask(Dialog.D_OK, failError);
				failError = null;
			}

			if (key == null)
				return;

			PEMStructure keyData = PEMDecoder.parsePEM(key.getData());
			if (PEMDecoder.isPEMEncrypted(keyData)) {
				// if it's encrypted and no passphrase is supplied, warn the user that we can't validate
				// until they go to use it.
				String passPhrase = passwordControl.getPassword();
				if (passPhrase.length() == 0) {
					if (Dialog.ask(Dialog.D_YES_NO, res.getString(MSG_PASSPHRASE_WARN)) == Dialog.NO) {
						passwordControl.setFocus();
						key = null;
						return;
					}
				} else {
					try {
						PEMDecoder.decode(key.getData(), passPhrase);
						key.setPassphrase(passPhrase);
					} catch (Throwable e) {
						if (e.getMessage() == null || e.getMessage().length() == 0) {
							Status.show(res.getString(MSG_KEYPAIR_PASSWORD_FAILED));
						} else {
							Status.show(res.getString(MSG_KEYPAIR_PASSWORD_FAILED) + " (" + e.getMessage() + ")");
						}
						key = null;
						passwordControl.setFocus();
						return;
					}
				}

			}
			Status.show(res.getString(MSG_KEYPAIR_IMPORT_SUCCESS));
			close();
		} catch (IllegalArgumentException e) {
			Dialog.ask(Dialog.OK, res.getString(MSG_KEYPAIR_ERROR_OCCURRED) + " (" + e.getMessage() + ")");
			location.setFocus();
		} catch (IOException e) {
			Dialog.ask(Dialog.OK, res.getString(MSG_KEYPAIR_ERROR_OCCURRED) + " (" + e.getMessage() + ")");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.ui.FieldChangeListener#fieldChanged(net.rim.device.api.ui.Field, int)
	 */
	public void fieldChanged(Field field, int context) {
		if (field == importButton) {
			if (generateNew) {
				doGenerate();
			} else {
				doImport();
			}
		} else if (field == cancelButton) {
			UiApplication.getUiApplication().popScreen(this);
		} else if (field == importFromURL) {
			// We receive two alerts : first is from selecting the new one
			// and the second is deselecting the old one.
			if (importType.getSelectedIndex() == 0) {
				replace(location, loadFile);
			}
		} else if (field == importFromFile) {
			if (importType.getSelectedIndex() == 1) {
				// changed selection
				replace(loadFile, location);
			}
		} else if (field == loadFile) {
			FileSelectorPopupScreen pop = PlatformServicesProvider.getFileSelectorPopup();
			pop.setType(FileSelectorPopupScreen.TYPE_OPEN);
			fileName = pop.pickFile();

		}
	}

	/**
	 * @return the imported key object
	 */
	public Key getKey() {
		return key;
	}

}
