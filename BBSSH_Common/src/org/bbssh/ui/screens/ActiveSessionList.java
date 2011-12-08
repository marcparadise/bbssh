package org.bbssh.ui.screens;

import java.util.Enumeration;
import java.util.Vector;

import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import org.bbssh.BBSSHApp;
import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.session.SessionManager;
import org.bbssh.ui.components.ConnectionListfieldCallback;

public class ActiveSessionList extends PopupScreen {
	ObjectListField field;
	private int numSession = 0;

	public ActiveSessionList() {
		super(new VerticalFieldManager(VerticalFieldManager.VERTICAL_SCROLL | VerticalFieldManager.VERTICAL_SCROLLBAR),
				DEFAULT_CLOSE | DEFAULT_MENU);
		Enumeration e = SessionManager.getInstance().getAvailableSessions();
		Vector v = new Vector();
		while (e.hasMoreElements()) {
			v.addElement(e.nextElement());
		}
		numSession = v.size();
		if (numSession == 0)
			return;
		ConnectionListfieldCallback cb = new ConnectionListfieldCallback(v, false);
		field = new ObjectListField() {
			protected boolean navigationClick(int status, int time) {
				handleSelection();
				return true;
			}

			protected boolean keyDown(int keycode, int time) {
				if (Keypad.key(keycode) == Keypad.KEY_ENTER) {
					handleSelection();
					return true;
				} else {
					return super.keyDown(keycode, time);
				}
			}

			void handleSelection() {
				int x = field.getSelectedIndex();
				if (x == -1)
					return;
				Object o = field.getCallback().get(field, x);
				if (!(o instanceof RemoteSessionInstance)) {
					return;
				}
				SessionManager.getInstance().setActiveSession((RemoteSessionInstance) o);
				BBSSHApp.inst().popScreen(ActiveSessionList.this);

			}
		};
		field.setSize(v.size());
		field.setRowHeight(cb.getRowHeight());
		field.setCallback(cb);
		add(field);
	}

	public boolean isDirty() {
		return false;
	}

	public int getNumSessions() {
		return this.numSession;
	}

}
