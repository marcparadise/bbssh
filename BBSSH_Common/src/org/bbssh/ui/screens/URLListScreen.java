/**
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

import java.util.Vector;

import javax.microedition.pim.Contact;
import javax.microedition.pim.ContactList;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;

import me.regexp.RE;
import me.regexp.RESyntaxException;
import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.invoke.AddressBookArguments;
import net.rim.blackberry.api.invoke.Invoke;
import net.rim.blackberry.api.invoke.MessageArguments;
import net.rim.blackberry.api.invoke.PhoneArguments;
import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.AddressException;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.MessagingException;
import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Clipboard;
import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.MainScreen;

import org.bbssh.BBSSHApp;
import org.bbssh.help.HelpManager;
import org.bbssh.i18n.BBSSHResource;
import org.bbssh.ui.components.MenuWrapper;
import org.bbssh.util.Tools;

/**
 * A list of urls. Allows user to view URLs in the buffer, copy them to clipboard, send via email, open email, view web
 * pages, etc.
 */
public class URLListScreen extends MainScreen implements BBSSHResource, FieldChangeListener {
	// FIXME - this is not correctly matching if a soft line wrap is occurring within the buffer,
	// since this will intepret it as a hard wrap and it will only match up to EOL.
	// @todo customizable RE matching?
	// ((http|ftp|https|ftp|wap)\\:\\/\\/\\S[\\n \t\t])
	public static RE urlMatcher =
			new RE(
					"(https?|ftp|gopher|telnet|file|notes|ms-help)://\\S+([ \\t.]|$)??");
	public static RE emailMatcher =
			new RE(
					"([a-zA-Z0-9_\\-\\.\\+]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)");
	public static RE phoneMatcher =
			new RE(
					"(1\\s*[-\\/\\.]?)?(\\((\\d{3})\\)|(\\d{3}))\\s*[-\\/\\.]?\\s*(\\d{3})\\s*[-\\/\\.]?\\s*(\\d{4})\\s*(([xX]|[eE][xX][tT])\\.?\\s*(\\d+))*");
	// @todo: secondary URL matcher?
	// [any].[any]? or optional, "loose URL matching" option?
	ResourceBundle res = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);
	ObjectListField field = new ObjectListField();

	public void fieldChanged(Field field, int context) {
		// don't care - ideally we would capture when teh Enter key was pressed only,
		// but that seems to require a customized class. Will come back to this...
	}

	/**
	 * Internal class representing a matching result of some kind.
	 */
	private static class Match {
		private static final int URL = 0;
		private static final int EMAIL = 1;
		private static final int PHONE = 2;
		private static final int INVALID = -1;
		private int type;
		private String value;

		public Match(int type, String value) {
			this.type = type;
			this.value = value;
		}

		public String toString() {
			return value;
		}
	}

	public URLListScreen(String text) {
		super(DEFAULT_CLOSE | DEFAULT_MENU);
		setTitle(res.getString(URLLIST_TITLE));
		try {
			Vector results = new Vector();
			addToList(results, text, urlMatcher, Match.URL);
			addToList(results, text, emailMatcher, Match.EMAIL);
			addToList(results, text, phoneMatcher, Match.PHONE);
			Object[] a;
			if (results.size() > 0) {
				a = Tools.vectorToArray(results);
			} else {
				a = new Object[] { new Match(Match.INVALID, res.getString(URLLIST_NO_MATCH)) };
			}
			field.setChangeListener(this);
			field.set(a);
			field.setSize(a.length);
		} catch (RESyntaxException e) {
			field.set(new Object[] { new Match(Match.INVALID, e.getMessage()) });
		}
		add(field);
	}

	private void addToList(Vector results, String text, RE pattern, int type) {
		int x = 0;
		while (pattern.match(text, x)) {
			String match = pattern.getParen(0).replace('\n', (char) 0).replace('\r', (char) 0).replace('\t', (char) 0);

			// // Strip out inappropriate ending characters -- applies mostly to HTML
			// while (match.length() > 1 && (match.endsWith("\n") || match.endsWith("\r") || match.
			// endsWith(".") || match.endsWith("\t"))) {
			// match = match.substring(0, match.length() - 1);
			// }

			results.addElement(new Match(type, match));
			x = pattern.getParenEnd(0) + 1;
		}
	}

	protected void makeMenu(Menu menu, int instance) {
		menu.add(HelpManager.getHelpMenu());
		super.makeMenu(menu, instance);
		Match sel = getCurrentSelection();
		if (sel == null) {
			return;
		}
		// Copy is always present.
		menu.add(copyToClipboard);
		switch (sel.type) {
			case Match.URL:
				menu.add(openBrowser);
				break;
			case Match.EMAIL:
				menu.add(sendEmail);
				menu.add(addContact);
				break;
			case Match.PHONE:
				menu.add(dialNumber);
				menu.add(addContact);
				break;
		}
		menu.setDefault(1);

	}

	protected void makeContextMenu(ContextMenu contextMenu) {
		super.makeContextMenu(contextMenu);
		Match sel = getCurrentSelection();
		if (sel == null) {
			return;
		}
		MenuWrapper wrapper = new MenuWrapper(contextMenu);

		// Copy is always present.
		wrapper.addItem(copyToClipboard);
		switch (sel.type) {
			case Match.URL:
				wrapper.addItem(openBrowser);
				break;
			case Match.EMAIL:
				wrapper.addItem(addContact);
				wrapper.addItem(sendEmail);
				sendEmail.setText(res.getString(URLLIST_MENU_SEND_EMAIL) + sel.value);
				break;
			case Match.PHONE:
				wrapper.addItem(addContact);
				wrapper.addItem(dialNumber);
				dialNumber.setText(res.getString(URLLIST_MENU_DIAL_NUMBER) + sel.value);
				break;
		}

	}

	Match getCurrentSelection() {
		int sel = field.getSelectedIndex();
		if (sel == -1) {
			return null;
		}
		Object o = field.get(field, sel);
		if (o == null || !(o instanceof Match)) {
			return null;
		}
		Match val = (Match) o;
		if (val.type == Match.INVALID) {
			return null;
		}
		return val;
	}

	//
	private MenuItem sendEmail = new MenuItem(res, URLLIST_MENU_SEND_EMAIL, 2, 10) {
		public void run() {
			try {
				if (!BBSSHApp.inst().requestPermission(ApplicationPermissions.PERMISSION_EMAIL,
						BBSSHResource.MSG_PERMISSIONS_MISSING_EMAIL_SCRAPER))
					return;

				Match m = getCurrentSelection();
				if (m == null) {
					return;
				}
				Message msg = new Message();
				Address a = new Address(m.value, m.value);
				Address[] addresses = { a };
				msg.addRecipients(net.rim.blackberry.api.mail.Message.RecipientType.TO, addresses);
				Invoke.invokeApplication(Invoke.APP_TYPE_MESSAGES,
						new MessageArguments(msg));
			} catch (AddressException ex) {
				Status.show(ex.getMessage());
			} catch (MessagingException ex) {
				Status.show(ex.getMessage());
			} finally {
				close();
			}

		}
	};
	private MenuItem copyToClipboard = new MenuItem(res, URLLIST_MENU_COPY, 1, 10) {
		public void run() {

			Match m = getCurrentSelection();
			if (m == null) {
				return;
			}
			Clipboard.getClipboard().put(m.toString());
			close();

		}
	};
	private MenuItem openBrowser = new MenuItem(res, URLLIST_MENU_OPEN_BROWSER, 3, 10) {
		public void run() {

			Match m = getCurrentSelection();
			if (m == null) {
				return;
			}
			// The permission required here is the app interaction perm - and we can't even run without it,
			// so no need to check it.
			Browser.getDefaultSession().displayPage(m.value);
			close();
		}

	};
	private MenuItem dialNumber = new MenuItem(res, URLLIST_MENU_DIAL_NUMBER, 4, 10) {
		public void run() {
			Match m = getCurrentSelection();
			if (m == null) {
				return;
			}
			if (!BBSSHApp.inst().requestPermission(ApplicationPermissions.PERMISSION_PHONE,
					BBSSHResource.MSG_PERMISSIONS_MISSING_PHONE_SCRAPER))
				return;
			PhoneArguments call = new PhoneArguments(PhoneArguments.ARG_CALL, m.value);
			Invoke.invokeApplication(Invoke.APP_TYPE_PHONE, call);
			close();

		}
	};
	private MenuItem addContact = new MenuItem(res, URLLIST_MENU_ADD_CONTACT, 5, 10) {
		public void run() {
			Match m = getCurrentSelection();
			if (m == null) {
				return;
			}
			if (!BBSSHApp.inst().requestPermission(ApplicationPermissions.PERMISSION_PIM,
					BBSSHResource.MSG_PERMISSIONS_MISSING_PIM_SCRAPER))
				return;
			ContactList contacts = null;
			try {
				contacts = (ContactList) PIM.getInstance().openPIMList(PIM.CONTACT_LIST,
						PIM.READ_WRITE);
				Contact c = (Contact) contacts.createContact();

				if (m.type == Match.EMAIL) {
					c.addString(Contact.EMAIL, 0, m.value);
				} else if (m.type == Match.PHONE) {
					c.addString(Contact.TEL, Contact.ATTR_WORK, m.value);
				}
				AddressBookArguments arg = new AddressBookArguments(AddressBookArguments.ARG_NEW, c);
				Invoke.invokeApplication(Invoke.APP_TYPE_ADDRESSBOOK, arg);

			} catch (PIMException e) {
				Status.show(e.getMessage());
			} finally {
				close();
			}
		}
	};

}
