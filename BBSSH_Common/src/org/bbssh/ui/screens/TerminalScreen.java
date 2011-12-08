/**
 * This file is part of "BBSSH" (c) 2010 Marc A. Paradise --LICENSE NOTICE-- This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version. This program is distributed in
 * the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA. --LICENSE NOTICE--
 */
package org.bbssh.ui.screens;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.UnsupportedOperationException;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.FullScreen;

import org.bbssh.command.CommandConstants;
import org.bbssh.exceptions.FontNotFoundException;
import org.bbssh.help.HelpManager;
import org.bbssh.i18n.BBSSHResource;
import org.bbssh.model.ConnectionProperties;
import org.bbssh.model.FontSettings;
import org.bbssh.model.SettingsManager;
import org.bbssh.net.session.Session;
import org.bbssh.platform.PlatformServicesProvider;
import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.session.SessionManager;
import org.bbssh.terminal.TerminalStateData;
import org.bbssh.ui.components.HeaderBar;
import org.bbssh.ui.components.TerminalField;
import org.bbssh.ui.components.keybinding.CommandBindingMenuItem;
import org.bbssh.ui.components.keybinding.ModalCommandBindingMenuItem;
import org.bbssh.ui.components.overlay.OverlayEditField;
import org.bbssh.ui.components.overlay.OverlayManager;
import org.bbssh.util.Logger;
import org.bbssh.util.Tools;
import org.bbssh.util.Version;

/**
 * Singleton that displays a terminal. Change between terminals by invoking setActiveSession.
 */
public class TerminalScreen extends FullScreen implements FieldChangeListener {
	ResourceBundle bundle = ResourceBundle.getBundle(BBSSHResource.BUNDLE_ID, BBSSHResource.BUNDLE_NAME);
	private OverlayManager overlayManager;
	private OverlayEditField edit;
	// private OverlayShortcutBar bar;
	protected TerminalField termField;
	protected int width, height;
	private boolean userRequestedKeyboard = false;
	private int oldWidth = -1;
	private int oldHeight = -1;

	private MenuItem itemDisconnect = new CommandBindingMenuItem(CommandConstants.DISCONNECT_SESSION, 0x00200000, 1);
	private MenuItem itemReconnect = new CommandBindingMenuItem(CommandConstants.RECONNECT_SESSION, 0x00200000, 1);
	private MenuItem itemMainScreen = new CommandBindingMenuItem(BBSSHResource.MENU_CONNECTION_LIST,
			CommandConstants.POP_TERMINAL_SCREEN, 0x00200000, 2);

	private MenuItem itemShowInputOverlay = new CommandBindingMenuItem(BBSSHResource.MENU_TERM_INPUT_SCREEN,
			CommandConstants.SHOW_OVERLAY_INPUT, 0x00300000, 1);
	private MenuItem itemShowShortcutBar = new CommandBindingMenuItem(BBSSHResource.MENU_TERM_SHORTCUT_BAR,
			CommandConstants.SHOW_OVERLAY_COMMANDS, 0x00300000, 2);
	private MenuItem itemToggleKeyboard;
	private MenuItem itemInputMode = new CommandBindingMenuItem(BBSSHResource.MENU_TERM_HYBRID_INPUT,
			CommandConstants.INPUT_MODE, 0x00300000, 4);
	private MenuItem itemShowSpecialKeys = new ModalCommandBindingMenuItem(CommandConstants.SHOW_SCREEN_SPECIAL_KEYS,
			0x00300000, 5);
	private MenuItem itemShowSymbols = new ModalCommandBindingMenuItem(BBSSHResource.MENU_SHOW_SYMBOLS,
			CommandConstants.SHOW_SYMBOLS, 0x00300000, 6);

	private MenuItem itemSetFont = new ModalCommandBindingMenuItem(BBSSHResource.MENU_SET_FONT,
			CommandConstants.SHOW_SCREEN_FONT, 0x00400000, 1);
	private MenuItem itemRunMacro = new ModalCommandBindingMenuItem(BBSSHResource.MENU_RUN_MACRO,
			CommandConstants.SHOW_SCREEN_MACRO_LIST, 0x00400000, 2);
	private MenuItem itemShowKeyBindings = new CommandBindingMenuItem(BBSSHResource.MENU_KEYBINDINGS,
			CommandConstants.SHOW_SCREEN_KEYBINDINGS, 0x00400000, 3);

	private MenuItem itemSendFeedback = new MenuItem(bundle, BBSSHResource.MENU_SEND_FEEDBACK, 0x00500000, 1) {
		public void run() {
			UiApplication.getUiApplication().invokeAndWait(new Runnable() {
				public void run() {
					if (SessionManager.getInstance().activeSession != null)
						Tools.sendFeedback(SessionManager.getInstance().activeSession.backingStore);
				}
			});
		}
	};

	/**
	 * Constructor. Note that this is kept public only for purposes of dynamic creation of paltform-specific class
	 * instances. You should not be instantiating this class directly.
	 */
	public TerminalScreen() {
		super(new OverlayManager(), DEFAULT_MENU);

		String name = TerminalField.class.getName();
		termField = (TerminalField) Version.createOSObjectInstance(name);
		overlayManager = (OverlayManager) getDelegate();
		overlayManager.setChangeListener(this);
		overlayManager.setCentralField(termField);

		name = OverlayEditField.class.getName();
		edit = (OverlayEditField) Version.createOSObjectInstance(name);
		// bar = (OverlayShortcutBar) Version.createOSObjectInstance(OverlayShortcutBar.class.getName());
		// Apparentl there is a default change listener in OS5+ in some cases? Without setting to null first,
		// we get an exception because there's already a listener specified.
		edit.setChangeListener(null);
		edit.setChangeListener(this);
		// bar.setChangeListener(null);
		// bar.setChangeListener(this);
		edit.setChangeListener(null);
		edit.setChangeListener(this);
		// Set up our shortcut bar - this will be user configurable in hte future,
		// but hard-coded for now.
		// bar.setDetailInfoLine(
		// new LabelField() {
		// protected void paint(Graphics graphics) {
		// // Inverse color for the label so it stands out.
		// graphics.setBackgroundColor(termField.fgcolor);
		// graphics.setColor(termField.bgcolor);
		// graphics.clear();
		// super.paint(graphics);
		// }
		// });
		// // @todo - this will be constructed based on user preference.
		// bar.add(new CommandButton("I", this, new BoundCommand(new ShowOverlayInput()), "Show Input Overlay", 6));
		// bar.add(new CommandButton("X", this, new BoundCommand(new ShowSpecialKeysScreen()), "Show Special Keys", 6));
		// bar.add(new CommandButton("D", this, new BoundCommand(new DisconnectSession()), "Disconnect", 6));
		// bar.add(new CommandButton("+F", this, new BoundCommand(new IncrDecrFontSize(), 0), "Increase Font Size", 6));
		// bar.add(new CommandButton("-F", this, new BoundCommand(new IncrDecrFontSize(), 1), "Decrease Font Size", 6));
		// bar.add(new CommandButton("F", this, new BoundCommand(new ShowFontPopup()), "Change Font Face / Size", 6));
		// bar.add(new CommandButton("K", this, new BoundCommand(new ShowKeybindingScreen()),
		// "Keyboard Shortcuts", 6));
		// bar.add(new CommandButton("?", this, new BoundCommand(new ShowSessionDetailScreen()),
		// "Session Information", 6));
		// @todo bar - not closing properyl when command executed, also it's possible to navigate
		// off of it and have it stuck on screen until you show/hide it again.
		if (PlatformServicesProvider.getInstance().hasVirtualKeyboard()) {
			itemToggleKeyboard = new CommandBindingMenuItem(CommandConstants.SHOW_HIDE_KEYBOARD, 0x00300000, 3);
		} else {
			itemToggleKeyboard = new CommandBindingMenuItem(CommandConstants.NONE, 0x00300000, 3);
		}

		if (SettingsManager.getSettings().isTitlebarDisplayEnabled()) {
			if (!PlatformServicesProvider.getInstance().isEnhancedTitlebarSupported()) {
				// Add a standard titlebar if the enhanced isn't supported.
				HeaderBar title = new HeaderBar("Terminal");
				getOverlayManager().setTitleBar(title);
			}
		}
	}

	/**
	 * Override of base clase's setFont. Do not use. Throws an UnsupportedOperationExcecption exception - use
	 * setFont(FontSettings) instead.
	 */
	public void setFont(Font font) {
		throw new UnsupportedOperationException();
	}

	protected void onUiEngineAttached(boolean attached) {
		super.onUiEngineAttached(attached);
		if (attached) {
			SessionManager mgr = SessionManager.getInstance();
			if (mgr.activeSession != null) {
				if (termField != null) {
					termField.redraw(true);
				}
				PlatformServicesProvider.getInstance().lockOrientation(mgr.activeSession.state.orientationMode);
			}
		} else {
			PlatformServicesProvider.getInstance().unlockOrientation();

		}
	}

	protected void makeMenu(Menu menu, int instance) {
		super.makeMenu(menu, instance);
		// THis is.. not pretty, but we don't want to display the full menu if
		// the edit field has focus.
		if (edit.isVisible() && edit.isFocus()) {
			return;
		}
		PlatformServicesProvider psp = PlatformServicesProvider.getInstance();
		boolean showKeyboardOption = !psp.hasHardwareKeyboard() || (psp.hasSlider() && !psp.isSliderExtended());

		MenuItem defItem = null;
		RemoteSessionInstance rsi = SessionManager.getInstance().activeSession;
		TerminalStateData state = rsi.state;

		String text;
		if (showKeyboardOption) {
			// Some menu text updates dynamically based on state
			if (isVirtualKeyboardVisible()) {
				text = bundle.getString(BBSSHResource.MENU_TERM_HIDE_KEYBOARD);
			} else {
				text = bundle.getString(BBSSHResource.MENU_TERM_SHOW_KEYBOARD);
			}
			itemToggleKeyboard.setText(text);
		}
		if (psp.isReducedLayout()) {
			if (state.typingMode == TerminalStateData.TYPING_MODE_DIRECT) {
				text = bundle.getString(BBSSHResource.MENU_TERM_SCROLLING);
			} else {
				text = bundle.getString(BBSSHResource.MENU_TERM_DIRECT_INPUT);
			}

		} else {
			if (state.typingMode == TerminalStateData.TYPING_MODE_DIRECT) {
				text = bundle.getString(BBSSHResource.MENU_TERM_HYBRID_INPUT);
			} else if (state.typingMode == TerminalStateData.TYPING_MODE_HYBRID) {
				text = bundle.getString(BBSSHResource.MENU_TERM_SCROLLING);
			} else {

				text = bundle.getString(BBSSHResource.MENU_TERM_DIRECT_INPUT);
			}
		}
		itemInputMode.setText(text);

		menu.add(itemSendFeedback);
		menu.add(HelpManager.getHelpMenu());
		menu.add(itemMainScreen);
		menu.add(itemShowKeyBindings);

		if (rsi.session.getConnectionState() == Session.CONNSTATE_CONNECTED) {
			// Separate screens that send data from those that do other stuff...
			menu.add(itemShowInputOverlay);
			menu.add(itemShowShortcutBar);
			menu.add(itemShowSpecialKeys);
			if (showKeyboardOption) {
				menu.add(itemToggleKeyboard);
			}
			// Symbols menu only works if we have an available keyboard -
			// otherwise our hack for displaying it fails since the menu option itself
			// is not present.
			if (!psp.hasTouchscreen() || psp.isSliderExtended()) {
				menu.add(itemShowSymbols);
			}
			menu.add(itemRunMacro);
			menu.add(itemInputMode);
			menu.add(itemSetFont);
			defItem = itemShowInputOverlay;
		}

		if (rsi.isConnected()) {
			menu.add(itemDisconnect);
			if (defItem == null)
				defItem = itemDisconnect;
		} else {

			menu.add(itemReconnect);
			defItem = itemMainScreen;
		}

		menu.setDefault(defItem);
	}

	public void toggleInputOverlay() {
		if (edit.isVisible()) {
			hideInputOverlay();
		} else {
			// Whenever we show the input overlay, we are to show the virtual
			// keyboard if it's available.
			overlayManager.showBottomField(edit);
			showVirtualKeyboard(false);
			edit.setFocus();
			edit.setCursorPosition(edit.getTextLength());
		}
	}

	/**
	 * Hide the virtual keyboard.
	 */
	public final void hideVirtualKeyboard() {
		userRequestedKeyboard = false;
		setVirtualKeyboardVisibility(false);
	}

	/**
	 * Override this to provide platform implementation of keyboard state check.
	 * 
	 * @return true if the keyboard is visible on -screen
	 */
	public boolean isVirtualKeyboardVisible() {
		return false;
	}

	/**
	 * Show the virtual keyboard if it's available on the current platform. Note that this will not show the virtual
	 * keyboard if the OS says it's not appropriate - such as when the physical keyboard is shown.
	 * 
	 * @param userInitiated
	 *            - true if this was requested by the user via a command or menu; false if it was invoked by an internal
	 *            operation (such as a keyboard closing or an edit field displaying).
	 */
	public final void showVirtualKeyboard(boolean userInitiated) {
		userRequestedKeyboard = userInitiated;
		setVirtualKeyboardVisibility(true);
	}

	/**
	 * Default implementation does nothing,. Platform specific overrides must correctly show or hide the virtual
	 * keyboard, taking into account hardware state (eg slider keyboard extended)
	 * 
	 * @param b
	 */
	protected void setVirtualKeyboardVisibility(boolean visible) {

	}

	public void hideInputOverlay() {
		if (edit.isVisible())
			overlayManager.hideBottomField();

		// If the user did not explicitly ask for the keyboard
		// hide it when the input window is hidden.
		if (!userRequestedKeyboard)
			hideVirtualKeyboard();
	}

	public void showShortcutOverlay() {
		showExpiringMessage("Toolbar unavailable until 2.1 release.");
		// overlayManager.showTopField(bar);
		// bar.setFocus();
	}

	public void hideShortcutOverlay() {
		// if (bar.isVisible())
		// overlayManager.hideTopField();
		// hideOverlayManager(false);
	}

	protected boolean isOverlayActive() {
		// return bar.isVisible() || edit.isVisible();
		return edit.isVisible();
	}

	protected OverlayManager getOverlayManager() {
		return overlayManager;
	}

	/**
	 * Hides the overlay manager if no overlay windows are visible, or if 'force' is true.
	 */
	public void hideOverlayManager() {
		// if (overlayFields.isVisible()) {
		// if (force) {
		// @todo clean up of overlay manager handling.
		// if (bar.isVisible()) {
		// overlayManager.hideTopField();
		// }
		if (edit.isVisible()) {
			overlayManager.hideBottomField();
			if (!userRequestedKeyboard) {
				hideVirtualKeyboard();
			}
		}
		// } else {
		// if (bar.isVisible() || edit.isVisible()) {
		// return;
		// }
		// }
		// }
		// if (overlayFields.getManager() != null)
		// delete(overlayFields);

	}

	/*
	 * (non-Javadoc)
	 * @see net.rim.device.api.ui.FieldChangeListener#fieldChanged(net.rim.device.api.ui.Field, int)
	 */
	public void fieldChanged(Field field, int context) {

		// @todo should the overlay hide itself when one of these events is received?
		// Because in all cases, we're hiding ... perhaps overlay manager should
		// handle these events internally, and pass through the ones of interest -
		// in addition to hiding self?
		RemoteSessionInstance rsi = SessionManager.getInstance().activeSession;
		if (field == edit) {
			switch (context) {
				// @todo enter/alt/focus - preference driven
				case OverlayEditField.CONTEXT_ENTER_PRESSED:
					rsi.sendTwoPartString(edit.getText() + "\n");
					edit.setText("");
					break;
				case OverlayEditField.CONTEXT_ALT_ENTER_PRESSED:
					rsi.sendTwoPartString(edit.getText());
					edit.setText("");
					break;
				case OverlayEditField.CONTEXT_ESCAPE_PRESSED:
					edit.setText("");
					break;
				default:
					return;

			}
			hideInputOverlay();
			// } else if ((field != null && field.getManager() == bar)) {
			// // one of the command buttons has been pressed.
			// if (context == CommandButton.CONTEXT_BUTTON_CLICKED) {
			// hideShortcutOverlay();
			// }
			// } else if (field == bar) {
			// if (context == OverlayShortcutBar.CONTEXT_CANCEL) {
			// hideShortcutOverlay();
			// } else if (context == CommandButton.CONTEXT_BUTTON_CLICKED) {
			// hideShortcutOverlay();
			// }
		} else if (field == overlayManager) {
			if (context == OverlayManager.CONTEXT_CANCEL) {
				hideOverlayManager();

			}
		}

	}

	// @todo this screen should not be aware of the overlay edit fields to begin with - let the
	// manager handle them fully?

	protected OverlayEditField getEditField() {
		return edit;
	}

	boolean forceShutdown = false;

	public void setForceShutdown(boolean forceShutdown) {
		this.forceShutdown = forceShutdown;
	}

	protected void sublayout(int width, int height) {

		boolean refresh = true;
		// Logger.debug("TerminalScreen.sublayout: " + height + " " + width);
		super.sublayout(width, height);
		if (SessionManager.getInstance().activeSession == null)
			return;
		if (width == oldWidth && height == oldHeight) {
			// nothing to do if nothing has changed...
			return;
		}

		// only do this after initialization is finished.
		if (oldWidth != -1 || oldHeight != -1) {
			if (width != oldWidth) {
				// width change means orientation changed
				this.width = width;
				this.height = height;
				termField.sizeChanged(true);
			} else {
				// We only care if height is different from the last time through - that indicates
				// that keyboard has been shown or hidden.

				if (height < oldHeight) {
					// Shrunk? Keyboard shown, was hidden
					if (userRequestedKeyboard) {
						// User specifically asked to show the keyboard. In this case, we'll resize
						// appropriately.
						this.width = width;
						this.height = height;
						termField.sizeChanged(true);
					} else {
						// user did not request kbd, which means we auto-displayed it with the entry field
						// so do not resize (@todo make this optional) and do not record the new size
						// so that when the size change is undone we don't unnecessarily process an update.
						/*
						 * - maybe prompt for this? 
						 * if (terminal.resizeDisplayWhenEditOverlayKeyboardDisplayed)
						 */
						refresh = false;
					}
				} else if (height > oldHeight) {
					// Expanded? Keyboard hidden, was shown and we were shrunken...
					// Because of this, we know that we always want to resize our display
					this.width = width;
					this.height = height;
					termField.sizeChanged(true);
					// since the user hasn't requested the kbd if we aren't showing it
					userRequestedKeyboard = false;
				}
			}
		}
		if (oldWidth == -1) {
			// initial
			this.width = width;
			this.height = height;
			termField.sizeChanged(false);
		}

		if (refresh) {
			oldWidth = width;
			oldHeight = height;
		}

	}

	/**
	 * Displays the specifeid message on the terminal screen, if the terminal session specified is the active session.
	 * 
	 * @param rsi
	 * @param message
	 */
	// @todo - move interface to SessionManager?
	public void showExpiringMessage(RemoteSessionInstance rsi, String message) {
		if (SessionManager.getInstance().activeSession == rsi)
			termField.showExpiringMessage(message);
	}

	public void showExpiringMessage(String message) {
		termField.showExpiringMessage(message);
	}

	/**
	 * @return font settings of terminal instance currently being viewed.
	 */
	public FontSettings getFontSettings() {
		return termField.getFontSettings();
	}

	/**
	 * Refreshes font and resizes as appropriate
	 * 
	 * @param fs
	 *            font settings to use.
	 * @throws FontNotFoundException
	 */
	public void updateFontSettings(FontSettings fs) throws FontNotFoundException {
		if (SessionManager.getInstance().activeSession == null)
			return;
		SessionManager.getInstance().activeSession.state.fs = fs;
		if (termField.updateFontSettings(fs)) {
			termField.sizeChanged(true);
		}

	}

	public void invalidateStatusIcons() {
		termField.invalidateStatusIcons();
	}

	public void showInputOverlay(String value) {
		if (value != null) {
			edit.setText(value);
			edit.setCursorPosition(value.length());
		}

		if (!edit.isVisible()) {
			toggleInputOverlay();
			showVirtualKeyboard(false);
		}
	}

	public void clearSession() {
		if (isVisible())
			invalidate();
	}

	// Switch the session which is actively displayed. Note that undisplayed sessions
	// are continuing to run, but are not attached to the rendering screen.
	public void attachSession(RemoteSessionInstance rsi) throws FontNotFoundException {
		if (rsi == null) {
			Logger.error("TerminalScreen.attachSession - unexpected null RSI");
			return;
		}
		ConnectionProperties prop = rsi.session.getProperties();
		edit.setColors(Tools.color[prop.getForegroundColorIndex()], prop.getBackgroundColorIndex());
		// This will pre-emptively restablish the lock - BEFORE the screen is displayed and layout executed
		// which prevents spurious resizes. (The layout is not changed until after we are attached)
		PlatformServicesProvider.getInstance().lockOrientation(rsi.state.orientationMode);
		termField.attachInstance(rsi);

		Field f = getOverlayManager().getTitleBar();
		if (f instanceof HeaderBar) {
			HeaderBar h = (HeaderBar) f;
			h.setTitle(rsi.state.settings.getName());
			h.setBackgroundColor(prop.getBackgroundColorIndex());
			h.setFontColor(prop.getForegroundColorIndex());
		}

	}

	protected boolean keyDown(int keycode, int time) {
		return super.keyDown(keycode, time);
	}

	public void forceRefresh() {
		termField.redraw(true);
	}

	public void showExpiringAlertMessage(int messageId) {
		termField.showExpiringAlertMessage(messageId);
	}

	public Menu getMenu(int instance) {
		Menu menu = super.getMenu(instance);
		// remove item for show/hide keyboard - we have our own impementation tha we must use.
		if (menu == null)
			return menu;

		for (int i = menu.getSize() - 1; i >= 0; i--) {
			MenuItem menuItem = menu.getItem(i);
			// This is the ordinal used for keyboard show/hide
			if (menuItem.getOrdinal() == 20480) {
				menu.deleteItem(i);
			}

		}
		return menu;
	}

	// @todo remove this.
	public static TerminalScreen getInstance() {
		return SessionManager.getInstance().getTerminalScreen();
	}

	public void redraw(boolean forceRefresh) {
		termField.redraw(forceRefresh);
	}

}
