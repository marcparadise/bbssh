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
import java.util.Vector;

import net.rim.device.api.crypto.CryptoTokenException;
import net.rim.device.api.crypto.CryptoUnsupportedOperationException;
import net.rim.device.api.crypto.DSAKeyPair;
import net.rim.device.api.crypto.DSAPublicKey;
import net.rim.device.api.crypto.InvalidKeyException;
import net.rim.device.api.crypto.KeyPair;
import net.rim.device.api.crypto.RSAKeyPair;
import net.rim.device.api.i18n.ResourceBundleFamily;
import net.rim.device.api.system.Clipboard;
import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.MainScreen;

import org.bbssh.crypto.SignatureTools;
import org.bbssh.exceptions.KeyInUseException;
import org.bbssh.help.HelpManager;
import org.bbssh.i18n.BBSSHResource;
import org.bbssh.model.Key;
import org.bbssh.model.KeyManager;
import org.bbssh.ui.components.VectorListFieldCallback;
import org.bbssh.util.Tools;

import ch.ethz.ssh2.crypto.PEMDecoder;


/**
 * This screen allows the user to import keys from local storage, media card, http, or https locations.
 * 
 * It will currently support
 * 
 */
public final class KeyManagerScreen extends MainScreen implements BBSSHResource {

	ResourceBundleFamily res = ResourceBundleFamily.getBundle(BUNDLE_ID, BUNDLE_NAME);
	ListField keyListField = new ListField();
	VectorListFieldCallback keyVector;
	private MenuItem importKey = new MenuItem(res, KEY_MGR_LABEL_IMPORT, 0x0010000, 10) {
		public void run() {
			handleAddKey(false);
		}
	};
	private MenuItem generateKey = new MenuItem(res, KEY_MGR_LABEL_GENERATE, 0x0010000, 10) {
		public void run() {
			handleAddKey(true);
		}
	};

	private MenuItem copyKeyToClipboard = new MenuItem(res, KEY_MGR_MENU_COPY_KEY, 0x0010000, 10) {
		public void run() {
			try {
				Key sel = getSelection();
				if (sel == null) {
					return;
				}
				if (sel.isNativeKey()) {
					DSAPublicKey pk = sel.getKeyPair().getDSAPublicKey();
					Clipboard.getClipboard().put(SignatureTools.exportPublicKey(pk));

				} else {
					KeyPair kp = PEMDecoder.decode(sel.getData(), sel.getPassphrase());
					if (kp instanceof DSAKeyPair) { 
						Clipboard.getClipboard().put(SignatureTools.exportPublicKey(((DSAKeyPair)kp).getDSAPublicKey()));
					} else if (kp instanceof RSAKeyPair) { 
						Clipboard.getClipboard().put(SignatureTools.exportPublicKey(((RSAKeyPair)kp).getRSAPublicKey()));
					}
				}
				Status.show(res.getString(MSG_KEY_COPIED_TO_CLIP));
			} catch (InvalidKeyException ex) {
				Status.show("Provided key is a not a valid key."); 
			} catch (IOException ex) {
				Status.show(ex.getMessage() + " : " + ex.toString());
				// @todo some error handlign here would be nice.
			} catch (IllegalArgumentException ex) {
				Status.show(ex.getMessage() + " : " + ex.toString());
				// @todo prompt for correct password - this one was blank
				// but none is provided.
			} catch (CryptoTokenException e) {
				Status.show(e.getMessage() + " : " + e.toString());
				// @todo some error handlign here would be nice.
			} catch (CryptoUnsupportedOperationException e) {
				Status.show(e.getMessage() + " : " + e.toString());
				
				// @todo some error handlign here would be nice.
			}
		}
	};

	private MenuItem deleteKey = new MenuItem(res, KEY_MGR_MENU_DEL_KEY, 10000, 10) {
		public void run() {
			if (Dialog.ask(Dialog.D_DELETE, res.getString(MSG_CONFIRM_DELETE), Dialog.DELETE) != Dialog.DELETE) {
				return;
			}
			KeyManager m = KeyManager.getInstance();
			Key key = getSelection();
			if (key == null) {
				return;
			}
			try {
				m.deleteKey(key);
				keyListField.setSize(m.getKeys().size());
				keyListField.invalidate();
			} catch (KeyInUseException ex) {
				Status.show(res.getString(MSG_KEY_IN_USE));
			}

		}
	};

	// private MenuItem refreshKey = new MenuItem(res, KEY_MGR_MENU_REFRESH,
	// 10000, 10) {
	// public void run() {
	// // @todo unimplemented
	// // keyListField.setSize(m.getKeys().size());
	// // keyListField.invalidate();
	// }
	// };
	// private MenuItem editKey = new MenuItem(res, KEY_MGR_MENU_EDIT, 10000, 10) {
	// public void run() {
	// // @todo unimplemented
	// // keyListField.setSize(m.getKeys().size());
	// // keyListField.invalidate();
	// }
	// };

	/**
	 * Instantiates a new key manager screen.
	 */
	public KeyManagerScreen() {
		super(0);
		setTitle(res.getString(KEY_MGR_TITLE));
		Vector v = KeyManager.getInstance().getKeys();
		keyVector = new VectorListFieldCallback(v);
		// Old versions are unsorted - fix that here. 
		Tools.sortVector(v);
		keyListField.setCallback(keyVector);
		keyListField.setEmptyString(res, KEY_MGR_EMPTY_LIST_TEXT, 0);
		keyListField.setSize(v.size());

		setTitle(res, KEY_MGR_TITLE);
		add(keyListField);
	}

	/**
	 * Gets the selection.
	 * 
	 * @return the selection
	 */
	private Key getSelection() {
		int x = keyListField.getSelectedIndex();
		if (x == -1) {
			return null;
		}
		Object o = keyVector.get(keyListField, x);
		if (!(o instanceof Key)) {
			return null;
		}
		Key k = (Key) o;
		if (k.getId() == Key.INVALID_ID) {
			k = null;
		}
		return k;
	}

	/**
	 * Create context menu.
	 * 
	 * @param contextMenu the context menu
	 */
	protected void makeContextMenu(ContextMenu contextMenu) {
		super.makeContextMenu(contextMenu);
		Field focusField = contextMenu.getTarget();
		// We only want to provide a special context menu for our
		// list of keys
		if (focusField != keyListField) {
			return;
		}
		Key key = getSelection();
		if (key == null) {
			return;
		}
		// contextMenu.addItem(this.editKey);
		contextMenu.addItem(this.copyKeyToClipboard);
		// contextMenu.addItem(this.refreshKey);
		contextMenu.addItem(this.deleteKey);

		contextMenu.setDefaultItem(copyKeyToClipboard);
	}

	/**
	 * Create screen menu.
	 * 
	 * @param menu the menu
	 * @param instance the instance
	 */
	protected void makeMenu(Menu menu, int instance) {
		menu.add(HelpManager.getHelpMenu());
		super.makeMenu(menu, instance);
		menu.add(this.importKey);
		menu.add(this.generateKey);
		Key key = getSelection();
		if (key == null) {
			return;
		}
		// menu.add(this.editKey);
		menu.add(this.copyKeyToClipboard);
		// menu.add(this.refreshKey);
		menu.add(this.deleteKey);
		menu.setDefault(copyKeyToClipboard);
	}

	public void close() {
		KeyManager.getInstance().commitData();
		super.close();
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
	 * @see net.rim.device.api.ui.container.MainScreen#onSavePrompt()
	 */
	protected boolean onSavePrompt() {
		return true;
	}

	/**
	 * Handle add key.
	 * 
	 * @param indicates whether this is to be a new key or not.
	 */
	private void handleAddKey(boolean generateNew) {
		ImportKeyScreen s = new ImportKeyScreen(generateNew);
		UiApplication.getUiApplication().pushModalScreen(s);
		Key k = s.getKey();
		KeyManager km = KeyManager.getInstance();
		if (k != null) {
			km.addKey(k);
			Vector keys = km.getKeys();
			Tools.sortVector(keys);
			keyListField.setSize(keys.size());
			keyListField.invalidate();
		}
	}
}
