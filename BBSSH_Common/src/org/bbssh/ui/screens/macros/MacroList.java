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
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import org.bbssh.i18n.BBSSHResource;
import org.bbssh.model.Macro;
import org.bbssh.model.MacroManager;
import org.bbssh.model.macros.MacroExecutor;
import org.bbssh.session.SessionManager;
import org.bbssh.ui.components.ClickableButtonField;
import org.bbssh.ui.components.VectorListFieldCallback;
import org.bbssh.util.Tools;

/**
 * A simpel popup that allows the user to choose a macro to run.
 */
public final class MacroList extends PopupScreen implements FieldChangeListener, BBSSHResource {
	ObjectListField macroList;
	ButtonField manageMacros;
	LabelField instruction;
	ResourceBundleFamily res = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

	public MacroList() {
		super(new VerticalFieldManager(Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR), DEFAULT_CLOSE);
		instruction = new LabelField(res.getString(MACRO_LIST_INSTRUCTIONS));
		macroList = new ObjectListField() {
			protected boolean navigationClick(int status, int time) {
				getChangeListener().fieldChanged(this, 0);
				return true;
			}

			protected boolean keyChar(char key, int status, int time) {
				if (key == Characters.ENTER) {
					getChangeListener().fieldChanged(this, 1);
					return true;
				}
				return super.keyChar(key, status, time);
			}
		};
		macroList.setEmptyString(res, MACRO_LIST_NO_MACRO_STRING, 0);
		reload();
		macroList.setChangeListener(null);
		macroList.setChangeListener(this);
		manageMacros = new ClickableButtonField(res.getString(MACRO_LIST_MANAGE_MACROS));
		manageMacros.setChangeListener(null);
		manageMacros.setChangeListener(this);
		VerticalFieldManager mgr = new VerticalFieldManager();

		// This should keep the top stuff in a fixed position?
		mgr.add(instruction);
		mgr.add(manageMacros);
		mgr.add(new SeparatorField());
		add(mgr);
		add(macroList);

	}

	private void reload() {
		Vector v = MacroManager.getInstance().getTemporaryMacroNamesList();
		Tools.sortVector(v);
		macroList.setChangeListener(null);
		macroList.setCallback(new VectorListFieldCallback(v));
		macroList.setSize(v.size());
		macroList.setChangeListener(this);
		macroList.invalidate();
	}

	private void executeMacro(Macro m) {
		MacroExecutor.getInstance().executeMacro(m, SessionManager.getInstance().activeSession);
	}

	public void fieldChanged(Field field, int context) {
		if (field == macroList) {
			int sel = macroList.getSelectedIndex();
			if (sel > -1) {
				String name = (String) macroList.getCallback().get(macroList, sel);
				Macro m = MacroManager.getInstance().getMacro(name);
				executeMacro(m);
				close();
			}
		} else if (field == manageMacros) {
			UiApplication.getUiApplication().pushModalScreen(new MacroManagerScreen());
			reload();
		}
	}

}
