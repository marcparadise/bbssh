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

package org.bbssh.ui.screens.macros;

import java.util.Vector;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.i18n.ResourceBundleFamily;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.container.MainScreen;

import org.bbssh.i18n.BBSSHResource;
import org.bbssh.model.Macro;
import org.bbssh.model.MacroManager;
import org.bbssh.ui.components.VectorListFieldCallback;
import org.bbssh.util.Tools;

/**
 * Screen to view and manage macros.
 */
public final class MacroManagerScreen extends MainScreen implements BBSSHResource {
	// Keep track of what we're changing, separately from the original store
	// this will let the user cancel.
	ObjectListField listField;
	VectorListFieldCallback vlfcb;
	Vector macros;
	ResourceBundleFamily res = ResourceBundle.getBundle(BBSSHResource.BUNDLE_ID, BBSSHResource.BUNDLE_NAME);
	MenuItem dupMacro = new MenuItem(res, MENU_DUPLICATE, 10000, 10) {
		public void run() {
			int sel = listField.getSelectedIndex();
			if (sel == -1) {
				return;
			}
			String name = (String) macros.elementAt(sel);
			MacroManager mgr = MacroManager.getInstance();
			Macro m = mgr.getMacro(name);
			if (m == null)
				return;
			Macro dup = mgr.duplicateMacro(m);
			macros.insertElementAt(dup.toString(), sel + 1);
			listField.setSize(macros.size());
			listField.invalidate();
		};

	};

	MenuItem newMacro = new MenuItem(res, MACRO_MENU_NEW, 10000, 10) {
		public void run() {
			MacroEditorScreen editor = new MacroEditorScreen();
			UiApplication.getUiApplication().pushModalScreen(editor);
			if (editor.isSaved()) {
				Macro m = editor.getMacro();
				setDirty(true);
				MacroManager.getInstance().addMacro(m);
				macros.insertElementAt(m.toString(), 0);
				Tools.sortVector(macros);
				listField.setSize(macros.size());
				listField.invalidate();
			}
		}
	};
	MenuItem editMacro = new MenuItem(res, MACRO_MENU_EDIT, 10000, 10) {
		public void run() {
			int sel = listField.getSelectedIndex();
			if (sel == -1) {
				return;
			}
			String name = (String) macros.elementAt(sel);
			MacroManager mgr = MacroManager.getInstance();
			Macro m = mgr.getMacro(name);
			if (m == null)
				return;
			MacroEditorScreen editor = new MacroEditorScreen(m);
			UiApplication.getUiApplication().pushModalScreen(editor);
			if (editor.isSaved()) {
				// user could have changed the name - let's remove the old one and add the
				// new in the same place, just in case.
				macros.removeElementAt(sel);
				macros.addElement(m.getName());
				Tools.sortVector(macros);
				m = editor.getMacro();
				mgr.delMacro(name);
				mgr.addMacro(m);
				listField.invalidate();
				setDirty(true);
			}
		}
	};
	MenuItem delMacro = new MenuItem(res, MACRO_MENU_DEL, 10000, 10) {
		public void run() {
			int sel = listField.getSelectedIndex();
			if (sel == -1) {
				return;
			}
			if (Dialog.ask(Dialog.D_DELETE, res.getString(MSG_CONFIRM_DELETE), Dialog.DELETE) != Dialog.DELETE) {
				return;
			}

			String name = (String) macros.elementAt(sel);
			// if they edited it, lose those too
			macros.removeElementAt(sel);
			MacroManager.getInstance().delMacro(name);
			listField.setSize(macros.size());
			listField.invalidate();

		}
	};

	public MacroManagerScreen() {
		super(DEFAULT_CLOSE | DEFAULT_MENU);
		listField = new ObjectListField();
		macros = MacroManager.getInstance().getTemporaryMacroNamesList();
		vlfcb = new VectorListFieldCallback(macros);
		listField.setCallback(vlfcb);
		listField.setSize(macros.size());
		listField.setEmptyString(res, MACRO_MANAGER_EMPTY_LIST, 0);
		setTitle(res.getString(MACRO_MANAGER_TITLE));
		add(listField);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.ui.container.MainScreen#makeMenu(net.rim.device.api.ui.component.Menu, int)
	 */
	protected void makeMenu(Menu menu, int instance) {
		super.makeMenu(menu, instance);
		MenuItem def;
		if (listField.getSelectedIndex() > -1) {
			menu.add(dupMacro);
			menu.add(delMacro);
			menu.add(editMacro);
			def = editMacro;
		} else {
			def = newMacro;
		}
		menu.add(newMacro);

		if (instance != Menu.INSTANCE_CONTEXT) {
			// Additional items for full menu
		}
		menu.setDefault(def);
	}

	public boolean isDirty() {
		return false; // we update as we go, and commit on close.
	}

	public boolean onClose() {
		MacroManager.getInstance().commitData();
		return super.onClose();
	}

}
