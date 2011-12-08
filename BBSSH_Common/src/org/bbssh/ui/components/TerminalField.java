package org.bbssh.ui.components;

import net.rim.blackberry.api.phone.Phone;
import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.applicationcontrol.ApplicationPermissionsManager;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Alert;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.KeyListener;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.XYPoint;
import net.rim.device.api.ui.XYRect;

import org.bbssh.exceptions.FontNotFoundException;
import org.bbssh.i18n.BBSSHResource;
import org.bbssh.keybinding.BoundCommand;
import org.bbssh.keybinding.KeyBindingHelper;
import org.bbssh.model.ConnectionProperties;
import org.bbssh.model.FontSettings;
import org.bbssh.model.KeyBindingManager;
import org.bbssh.model.SettingsManager;
import org.bbssh.platform.PlatformServicesProvider;
import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.session.SessionManager;
import org.bbssh.terminal.TerminalStateData;
import org.bbssh.terminal.VT320;
import org.bbssh.terminal.fonts.BBSSHFontManager;
import org.bbssh.terminal.fonts.FontRenderer;
import org.bbssh.ui.screens.TerminalScreen;
import org.bbssh.util.Logger;
import org.bbssh.util.Tools;

public class TerminalField extends Field {
	// This will change when the keyboard is shown/hidden, so we'll track this
	// to know
	protected int width, height;
	ResourceBundle bundle = ResourceBundle.getBundle(BBSSHResource.BUNDLE_ID,
			BBSSHResource.BUNDLE_NAME);
	private FontRenderer renderer;
	private int lastKeyControlTS;
	int lastNavTime = 0;
	private FontSettings oldFontSettings = null;
	private String expiringMessage;
	XYRect statusBarRegionRect = new XYRect(0, 0, 0, 0);
	private boolean statusInvalidated = false;
	private boolean processChars = true;

	private Font statusFont;
	SessionManager sessionMgr;
	private int messageTimer = -1;

	Runnable expireMessageTask = new Runnable() {

		public void run() {
			if (expiringMessage != null) {
				synchronized (expiringMessage) {
					messageTimer = -1; // no longer valid now that the task has
										// executed.
					expiringMessage = null;
					invalidateStatusIcons();
				}
			}
		}
	};

	public void attachInstance(RemoteSessionInstance rsi)
			throws FontNotFoundException {
		updateFontSettings(rsi.state.fs);
		sizeChanged(rsi.termInitPending);
		rsi.termInitPending = false;
		lastKeyControlTS = 0;
		oldFontSettings = null;
		expiringMessage = null;
		statusInvalidated = false;
	}

	public TerminalField() {
		Font f = getFont();
		sessionMgr = SessionManager.getInstance();
		statusFont = f
				.derive(Font.ITALIC, (int) ((float) f.getHeight() * 0.65));
		processChars = !PlatformServicesProvider.getInstance()
				.isReducedLayout();

	}

	protected void layout(int width, int height) {
		setPosition(0, 0);
		setExtent(width, height);
		this.width = width;
		this.height = height;

		statusBarRegionRect.height = statusFont.getHeight() + 16;
		statusBarRegionRect.width = (width / 10) * 9;
		statusBarRegionRect.x = width - statusBarRegionRect.width;
		statusBarRegionRect.y = 0;
	}

	/**
	 * Internal method invoked to reflect any size changes to the display region
	 * (includign that at initial load.) This initializes character grid
	 * height/width, and (if it occurs after session is launched) sends update
	 * to the remote server of dimension changes.
	 * 
	 * @param updateRemote
	 *            if true, also send a request to remote terminal requesting
	 *            term size change
	 */
	public void sizeChanged(boolean updateRemote) {
		// Okay, this is messy - basically don't process anything if we
		// haven'tinitialized. .
		if (width == 0 || height == 0)
			return;
		RemoteSessionInstance rsi = sessionMgr.activeSession;
		if (rsi == null)
			return;

		XYPoint dim = renderer.getFontDimensions();
		rsi.state.numColsVisible = width / dim.x;
		rsi.state.numRows = height / dim.y;

		rsi.backingStore = new Bitmap(width, height);
		rsi.backingStoreGR = PlatformServicesProvider.getInstance()
				.getGraphicsObjectForBitmap(rsi.backingStore);
		rsi.backingStoreGR.setColor(Tools.color[rsi.session.getProperties()
				.getBackgroundColorIndex()]);
		// Our font might not take us up to teh edges of the screen - so provide
		// a proper fill
		rsi.backingStoreGR.fillRect(0, 0, width, height);

		int virtualCols = rsi.state.numColsVisible;
		int virtualRows = rsi.state.numRows;

		boolean colOverride = false, rowOverride = false;
		if (rsi.state.settings.getTerminalCols() != 0) {
			// don't force a resize as we're overriding screen-based terminal
			// size.
			colOverride = true;
			virtualCols = rsi.state.settings.getTerminalCols();
		}

		if (rsi.state.settings.getTerminalRows() != 0) {
			// don't force a resize as we're overriding screen-based 
			// terminal size.
			rowOverride = true;
			virtualRows = rsi.state.settings.getTerminalRows();
		}

		if (colOverride && rowOverride)
			updateRemote = false;

		rsi.emulator.setScreenSize(virtualCols, virtualRows, updateRemote);
		rsi.state.maxHeight = rsi.emulator.getTerminalHeight();
		rsi.state.maxWidth = rsi.emulator.getTerminalWidth();
		redraw(true);
	}

	protected void paintBackground(Graphics graphics) {
		RemoteSessionInstance rsi = SessionManager.getInstance().activeSession;
		int color = 0;
		if (rsi != null)
			color = rsi.session.getProperties().getBackgroundColorIndex();
		graphics.setBackgroundColor(color);
		graphics.clear();
	}

	protected void paint(Graphics g) {
		RemoteSessionInstance rsi = sessionMgr.activeSession;

		if (rsi == null || rsi.backingStoreGR == null) {
			g.drawText("No session connected", 0, 0);
			g.pushRegion(statusBarRegionRect, 0, 0);
			paintStatusIcons(g);
			statusInvalidated = false;
			g.popContext();
			return;
		}

		rsi.state.debugPaintCount++;
		// If our terminal has been updated, this will refresh our bitmap.
		redrawBackingStore(rsi.backingStoreGR);
		// Now render the bitmap to screen.
		XYRect r = g.getClippingRect();

		g.drawBitmap(r, rsi.backingStore, r.x, r.y);
		// save current alpha so that we can restore it later.
		int alpha = g.getGlobalAlpha();
		g.pushRegion(statusBarRegionRect, 0, 0);
		paintStatusIcons(g);
		statusInvalidated = false;
		g.popContext();

		// g.setGlobalAlpha(175);
		// paintChild(g, overlayFields);

		g.setGlobalAlpha(alpha);

	}

	private void paintStatusIcons(Graphics g) {

		g.setGlobalAlpha(200);
		XYRect r = g.getClippingRect();
		// offset from the right by 10% - this will allow the standard
		// indicators
		// to be displayed without overwriting our own indicators
		int xpos = (int) (r.width - (r.width * .10));
		int ypos = 2;
		g.setFont(statusFont);
		if (sessionMgr.activeSession == null) {
			return;
		}
		if (sessionMgr.activeSession.state.settings.isCaptureEnabled()) {
			xpos = drawIndicator(
					g,
					xpos,
					ypos,
					Tools.getStringResource(BBSSHResource.STATUS_MSG_IND_RECORDING),
					Color.RED);

		}

		TerminalStateData state = sessionMgr.activeSession.state;
		if (state.typingMode == TerminalStateData.TYPING_MODE_LOCAL_SCROLL) {
			xpos = drawIndicator(
					g,
					xpos,
					ypos,
					Tools.getStringResource(BBSSHResource.STATUS_MSG_IND_SCROLL_MODE));
		}

		if (state.ctrlPressed) {
			xpos = drawIndicator(
					g,
					xpos,
					ypos,
					Tools.getStringResource(BBSSHResource.STATUS_MSG_CTRL_PRESSED));
		}
		if (state.altPressed) {
			xpos = drawIndicator(
					g,
					xpos,
					ypos,
					Tools.getStringResource(BBSSHResource.STATUS_MSG_ALT_PRESSED));
		}

		// Display the fake alt/shift notification icons only if the standard OS
		// versions won't be displayed -
		PlatformServicesProvider psp = PlatformServicesProvider.getInstance();
		if (state.isExternalArtificialStatusUpdate()
				|| (psp.hasTouchscreen() && (!psp.hasHardwareKeyboard() || !psp
						.isSliderExtended()))) {
			int status = state.getArtificialStatus(false);
			if ((status & KeypadListener.STATUS_ALT) > 0) {
				xpos = drawIndicator(
						g,
						xpos,
						ypos,
						Tools.getStringResource(BBSSHResource.STATUS_MSG_IND_BB_ALT));
			}
			if ((status & KeypadListener.STATUS_SHIFT_LEFT) > 0) {
				if (psp.hasShiftX()) {
					xpos = drawIndicator(
							g,
							xpos,
							ypos,
							Tools.getStringResource(BBSSHResource.STATUS_MSG_IND_BB_SHIFT));
				} else {
					xpos = drawIndicator(
							g,
							xpos,
							ypos,
							Tools.getStringResource(BBSSHResource.STATUS_MSG_IND_BB_SHIFT_LEFT));
				}
			} else if ((status & KeypadListener.STATUS_SHIFT_RIGHT) > 0) {
				xpos = drawIndicator(
						g,
						xpos,
						ypos,
						Tools.getStringResource(BBSSHResource.STATUS_MSG_IND_BB_SHIFT_RIGHT));
			} else if ((status & KeypadListener.STATUS_SHIFT) > 0) {
				xpos = drawIndicator(
						g,
						xpos,
						ypos,
						Tools.getStringResource(BBSSHResource.STATUS_MSG_IND_BB_SHIFT));
			}
		}
		int orientation = sessionMgr.activeSession.state.orientationMode;
		// For now only portrait/landscape locks are supported. east/west
		// not yet implemented - as this may require a more platform specific
		// implementation
		if (orientation != TerminalStateData.DIRECTION_ALL) {
			if (orientation == TerminalStateData.DIRECTION_NORTH
					|| orientation == TerminalStateData.DIRECTION_PORTRAIT) {
				xpos = drawIndicator(g, xpos, ypos, "P");
			} else {
				xpos = drawIndicator(g, xpos, ypos, "L");
			}
		}

		String message = getExpiringMessage();
		if (message != null) {
			xpos = drawIndicator(g, xpos, ypos, message);
		}

	}

	private int drawIndicator(Graphics g, int xpos, int ypos, String text,
			int color) {
		int size = statusFont.getAdvance(text) + 8;
		int h = statusFont.getHeight() + 8;
		xpos -= size;
		g.setColor(Color.SLATEGRAY);
		g.fillRoundRect(xpos, ypos, size, h, 14, 14);
		g.setColor(Color.BLACK);
		g.drawRoundRect(xpos, ypos, size, h, 14, 14);
		g.setColor(color);
		g.drawText(text, xpos + 4, ypos + 4);
		return xpos - 4;

	}

	private int drawIndicator(Graphics g, int xpos, int ypos, String text) {
		return drawIndicator(g, xpos, ypos, text, Color.GOLD);
	}

	protected boolean keyEvent(int keyCode, int status, int time) {
		return keyEvent(keyCode, status, time, true);
	}

	/**
	 * Handle any/all user-generated events by looking up custom event mappings
	 * and dispatching to them if they exist.
	 */
	protected boolean keyEvent(int keyCode, int status, int time,
			boolean resetVirtualStatus) {
		// Logger.debug("keyCode: " + keyCode + " status: " + status);
		RemoteSessionInstance rsi = sessionMgr.activeSession;
		if (rsi == null)
			return false;
		// If we're using a virtual keyboard, and this is an alted character,
		// then remove the alt flag - this will prevent situations where some
		// virt keyboard characters (such as 0 or currency) are only available
		// "alted" on the virt keyboard - and so become unavailable compeltely
		// if the user has bound them w/ alt, eg alt+0, alt+currency
		if (((TerminalScreen) getScreen()).isVirtualKeyboardVisible()) {
			if ((status & KeypadListener.STATUS_ALT) > 0
					&& Keypad.getUnaltedChar(Keypad.map(keyCode, status)) == 0) {
				status &= ~KeypadListener.STATUS_ALT;
			}
		}

		// If the caller tells us that we can reset virtual status, then we'll
		// just do so. Otherwise we'll decide for ourselves - if we're a
		// touchscreen device without a keyboard; or but it's a retracted
		// slider... then we'll not reset it. This mirrors the actual behavior
		// of (example) ALT not getting reset in resposne to a touch event,
		// keeping our internal state consistent with device state. It's ugly,
		// but until RIM gives us a way to simpyl query the Alt/Shift key
		// status.

		if (!resetVirtualStatus) {
			PlatformServicesProvider psp = PlatformServicesProvider
					.getInstance();
			resetVirtualStatus = psp.isReducedLayout();
			resetVirtualStatus = resetVirtualStatus
					|| (psp.hasTouchscreen() && (!psp.hasHardwareKeyboard() || !psp
							.isSliderExtended()));
			resetVirtualStatus = resetVirtualStatus
					|| rsi.state.isExternalArtificialStatusUpdate();
		}

		int s = rsi.state.getArtificialStatus(resetVirtualStatus);
		int newStatus = (status | s)
				& KeyBindingHelper.SUPPORTED_KEYPAD_MODIFIERS;
		BoundCommand mapping = KeyBindingManager.getInstance().getKeyBinding(
				keyCode, newStatus);
		boolean ret = false;
		if (mapping != null) {
			if (!mapping.execute(sessionMgr.activeSession, true)) {
				Logger.error("Command failed to execute: " + mapping);
			}
			// Even if the mapping failed to execute, the mapping *existed* - so
			// we don't want to
			// double-process this keystroke.
			ret = true;
		}
		invalidateStatusIcons();
		return ret;
	}

	protected boolean trackwheelClick(int status, int time) {
		boolean result = keyEvent(KeyBindingHelper.KEY_NAV_CLICK, status, time);
		if (result == false) {
			result = super.trackwheelClick(status, time);
		}

		return result;
	}

	/**
	 * Override of navigationClick that passes control to custom keyhandler. If
	 * unhandled, the default implementation is invoked.
	 * 
	 * @param status
	 * @param time
	 * @return
	 */
	protected boolean navigationClick(int status, int time) {
		boolean result = keyEvent(KeyBindingHelper.KEY_NAV_CLICK, status, time);
		if (!result) {
			result = super.navigationClick(status, time);
		}
		return result;
	}

	/**
	 * Override of navigationMovement that passes control to custom keyhandler.
	 * If unhandled, the default implemention is currently NOT invoked.
	 * 
	 * @param dx
	 *            delta in X
	 * @param dy
	 *            delty in Y
	 * @param status
	 * @param time
	 * @return
	 */
	protected boolean navigationMovement(int dx, int dy, int status, int time) {
		if (sessionMgr.activeSession == null)
			return true;

		if (sessionMgr.activeSession.state.typingMode == TerminalStateData.TYPING_MODE_LOCAL_SCROLL) {
			if (dy != 0) {
				if ((status & KeyListener.STATUS_ALT) > 0) {
					sessionMgr.activeSession.scrollViewVertical(0, dy > 0);
				} else {
					sessionMgr.activeSession.scrollViewVertical(Math.abs(dy),
							dy > 0);
				}
			}

			if (dx != 0) {
				if ((status & KeyListener.STATUS_ALT) > 0) {
					sessionMgr.activeSession.scrollViewHorizontal(0, dx > 0);
				} else {
					sessionMgr.activeSession.scrollViewHorizontal(Math.abs(dx),
							dx > 0);
				}
			}
		} else {
			// Rate-limit to prevent flooding if this is a 4-way devices
			if ((status & KeyListener.STATUS_FOUR_WAY) > 0) {
				if (time - lastNavTime < 100) {
					return true;
				}
				lastNavTime = time;
			}
			if (dy < 0) {
				keyEvent(KeyBindingHelper.KEY_NAV_UP, status, time);
			} else if (dy > 0) {
				keyEvent(KeyBindingHelper.KEY_NAV_DOWN, status, time);
			}
			if (dx < 0) {
				keyEvent(KeyBindingHelper.KEY_NAV_LEFT, status, time);
			} else if (dx > 0) {
				keyEvent(KeyBindingHelper.KEY_NAV_RIGHT, status, time);
			}
		}
		// in no case do we want default directional handling.
		return true;
	}

	protected boolean keyChar(char c, int status, int time) {
		// Logger.debug("keyChar: " + c);
		if (!processChars)
			return true;
		if (sessionMgr.activeSession == null
				|| sessionMgr.activeSession.state == null)
			return super.keyChar(c, status, time);

		TerminalStateData state = sessionMgr.activeSession.state;
		if (state.typingMode == TerminalStateData.TYPING_MODE_LOCAL_SCROLL) {
			showExpiringAlertMessage(BBSSHResource.STATUS_MSG_IN_SCROLL_MODE);
			return true;
		}
		if (keyEvent(c, status, time))
			return true;

		state.setArtificialStatus(0);
		// If we're in direct mode; or we're in "special key" mode where
		// CTRL or ALT mode is enabled , then send text straight through.
		// even in hybrid mode, we won't show the editor window in this
		// scenario.
		if (state.typingMode == TerminalStateData.TYPING_MODE_DIRECT
				|| state.altPressed || state.ctrlPressed) {
			sessionMgr.activeSession.emulator.keyTyped(0, c,
					state.getModifierKeyState(status, true));
			// This will change alt status, etc as well as outbound buffer size.
			invalidateStatusIcons();
			return true;
		}

		// If we're in hybrid mode, then display the input overlay and popup the
		// value
		String value = null;
		if (state.typingMode == TerminalStateData.TYPING_MODE_HYBRID) {
			if (c != '\n' && c != '\r') {
				value = new String(new char[] { c });
			}
			((TerminalScreen) getScreen()).showInputOverlay(value);

		}
		return true;
	}

	protected boolean keyControl(char c, int status, int time) {
		// Even a single volume press for some reason results in two volume
		// events.
		// This will filter those duplicates out while not limiting rate too
		// much.
		if ((time - lastKeyControlTS) < 50) {
			return true;
		}
		RemoteSessionInstance rsi = sessionMgr.activeSession;
		if (rsi == null)
			return true;
		lastKeyControlTS = time;
		if (c == Characters.CONTROL_VOLUME_DOWN
				|| c == Characters.CONTROL_VOLUME_UP) {
			if ((status & rsi.state.getArtificialStatus(false) & KeyBindingHelper.SUPPORTED_KEYPAD_MODIFIERS) == 0
					&& suppressBinding()) {
				return super.keyControl(c, status, time);

			}
			keyEvent(c, status, time);
			return true;
		}
		rsi.state.setArtificialStatus(0);
		invalidateStatusIcons();
		return super.keyControl(c, status, time);
	}

	protected boolean suppressBinding() {
		// If we are going to get an exception for tryign .... don't try - we
		// can't suppress.
		if (ApplicationPermissionsManager.getInstance().getPermission(
				ApplicationPermissions.PERMISSION_PHONE) == ApplicationPermissions.VALUE_DENY)
			return false;

		if (Phone.getActiveCall() != null) {
			if (SettingsManager.getSettings().getDisableKeybindWhenOnCall()) {
				showExpiringAlertMessage(BBSSHResource.TERMINAL_MSG_BINDING_DISABLED);
				return true;
			}
		}
		return false;
	}

	protected boolean keyDown(int keycode, int time) {
		int key = Keypad.key(keycode);
		int stat = Keypad.status(keycode);
		RemoteSessionInstance rsi = sessionMgr.activeSession;
		if (rsi == null)
			return super.keyDown(keycode, time);
		// Volume Down and Volume Up are delivered only in 5.0+ - but the
		// keyControl
		// version of the same works for all versions including 5 & 6. Skip
		// processing it
		// here , and pick it up there.
		if (key == Keypad.KEY_VOLUME_DOWN || key == Keypad.KEY_VOLUME_UP)
			return super.keyDown(keycode, time);

		// For media and phone keys, we will need to ignore them if a call is in
		// progress.
		if (key == Keypad.KEY_SEND || key == Keypad.KEY_END
				|| key == Keypad.KEY_SPEAKERPHONE) {
			if ((stat & rsi.state.getArtificialStatus(false) & KeyBindingHelper.SUPPORTED_KEYPAD_MODIFIERS) == 0
					&& suppressBinding())
				return super.keyDown(keycode, time);
		}
		if (keyEvent(key, stat, time)) {
			return true;
		}
		return super.keyDown(keycode, time);
	}

	protected void onObscured() {
		sessionMgr.notifyActiveSessionObscured();
		if (sessionMgr.activeSession != null)
			sessionMgr.activeSession.state.setArtificialStatus(0);

		super.onObscured();
	}

	protected void onExposed() {
		sessionMgr.notifyActiveSessionExposed();
		if (sessionMgr.activeSession != null)
			sessionMgr.activeSession.state.setArtificialStatus(0);
		super.onExposed();
	}

	public void redraw(boolean fullRefresh) {
		if (renderer == null)
			return;
		// cache local reference because we use this many times in this method.
		RemoteSessionInstance rsi = sessionMgr.activeSession;
		if (rsi == null) {
			return;
		}
		// Ensure that the backing store doesn't get modified at the same time
		// we're
		// requesting a refresh. This indirectly guarantees that the drawing
		// operation
		// can't occur while modifications to the underlyin gbuffer are still in
		// progress.
		rsi.state.refreshRequired = true;
		if (fullRefresh) {
			rsi.state.debugFullRefreshCount++;
			rsi.state.fullRefreshRequired = true;
			rsi.state.numColsVisible = rsi.emulator.getWidth();
			invalidate();
			return;
		} else {
			rsi.state.debugPartialRefreshCount++;

		}
		XYPoint fontSize = renderer.getFontDimensions();
		int bottomTermRow = Math.min(rsi.state.topTermRow
				+ rsi.emulator.screenBase + rsi.state.numRows,
				rsi.emulator.bufSize);
		int drawRow = 0;
		// Find out which rows are invalid, so that we don't repaint the entire
		// screen if we don't need to.
		// We will also always invalidate the status bar region.
		invalidateStatusIcons();
		int first = -1;
		int last = -1;
		for (int line = rsi.state.topTermRow + rsi.emulator.screenBase; line < bottomTermRow; line++, drawRow += fontSize.y) {
			if (rsi.emulator.isLineDirty(line)) {
				if (first == -1) {
					first = drawRow;

				}
				last = drawRow;
			}
		}
		this.invalidate(0, first, getWidth(), (last + fontSize.y) - first);
	}

	protected void redrawBackingStoreImpl(Graphics g) {
		RemoteSessionInstance rsi = sessionMgr.activeSession;
		if (rsi == null)
			return;

		TerminalStateData state = rsi.state;
		XYPoint fontSize = renderer.getFontDimensions();
		rsi.state.debugPaintBackStoreCount++;
		ConnectionProperties prop = rsi.session.getProperties();
		int bgcoloridx = prop.getBackgroundColorIndex();
		int fgcoloridx = prop.getForegroundColorIndex();

		int bottomTermRow = Math.min(rsi.state.topTermRow + rsi.state.numRows,
				rsi.emulator.getBufferSize());
		int rightTermCol = Math.min(rsi.emulator.getTerminalWidth(),
				(state.left + state.numColsVisible));
		// Declare these outside the loops so they don't continually get
		// re-pushed onto the stack.
		int x, termCol, count, fg, bg, currAttr;
		int drawRow = 0, drawCol = 0;
		long[] row;

		for (int line = state.topTermRow, index = rsi.emulator.windowBase
				+ state.topTermRow; line < bottomTermRow; line++, index++, drawRow += fontSize.y) {

			state.debugLineEvalCount++;
			if (!state.fullRefreshRequired && !rsi.emulator.isLineDirty(index)) {
				continue;
			}
			state.debugLinePaintCount++;
			rsi.emulator.setLineClean(line);

			drawCol = 0;

			row = rsi.emulator.terminalData[index];
			count = 0;
			for (termCol = rsi.state.left; termCol < rightTermCol; termCol += count) {
				currAttr = (int) (row[termCol] >> 32);
				if ((state.typingMode == TerminalStateData.TYPING_MODE_SELECT && (currAttr & VT320.XATTR_SELECTED) != 0)) {
					bg = 0xAAAAAA;
					fg = 0x111111;

				} else {
					fg = getColor(currAttr, fgcoloridx, true);
					bg = getColor(currAttr, bgcoloridx, false);

				}
				// We're not going to do an actual blink (that requires timed
				// regular repaints, which we want to
				// avoid for perf reasons on a mobile platform. Instead, treat
				// as invert.
				if ((currAttr & VT320.BLINK) != 0) {
					currAttr |= VT320.INVERT;
				}
				if ((currAttr & VT320.INVERT) != 0) {
					// Logger.debug("Attribute: INVERT");
					int t = bg;
					bg = fg;
					fg = t;
				}

				// How many cols can we draw in this pass? Look to see how many
				// have identical attributes.
				for (count = 0, x = termCol; x < rightTermCol
						&& x < state.maxWidth; x++, count++) {
					// @todo - any risk of breakage if a col has an attr value
					// but no text? eg we should
					// clear it but are not?
					if (row[x] < ' ') {
						row[x] = ' ';
					}
					if ((row[x] >> 32) != currAttr)
						break;
				}

				// Logger.debug(" ** drawing from " + colPos + " to " + (addr -
				// 1));
				g.setColor(fg);
				g.setBackgroundColor(bg);
				// Fill the background...
				g.clear(drawCol, drawRow, fontSize.x * count, fontSize.y);
				renderer.drawChars(g, fg, bg, row, termCol, count, drawCol,
						drawRow);

				if ((currAttr & VT320.UNDERLINE) > 0) {
					g.setColor(fg);
					// offset by two lines if we're inverted, otherwise one --
					// this ensures that it will show up.
					int yVal = drawRow + fontSize.y
							- ((currAttr & VT320.INVERT) > 0 ? 2 : 1);
					g.drawLine(drawCol, yVal, drawCol + (fontSize.x * count),
							yVal);
				}

				if (count == 0) {
					Logger.error("Char Draw Count == 0, should not have happened - rendering may be corrupt");
					count++;
				}
				drawCol += fontSize.x * count;
			}

		}

		// If we've gone through all visible rows but still have unaccounted for
		// height,
		// then fix this now by filling in anythign remainign.
		if (drawRow < height) {
			g.setBackgroundColor(Tools.color[bgcoloridx]);
			g.clear(0, drawRow, width, height - drawRow);
		}

		// Set cursor color to be the opposite of the text color here (inverse)
		int attr = (int) (rsi.emulator.terminalData[rsi.emulator.cursorY][rsi.emulator.cursorX] >> 32);
		g.setColor(getColor(attr, fgcoloridx, true));
		g.setBackgroundColor(getColor(attr, bgcoloridx, false));

		if (state.typingMode == TerminalStateData.TYPING_MODE_SELECT) {
			drawCursor(g, rsi.state.selectionCursorX,
					rsi.state.selectionCursorY, fontSize);
		} else {
			drawCursor(g, rsi.emulator.cursorX, rsi.emulator.cursorY, fontSize);
		}
		rsi.state.refreshRequired = false;
		rsi.state.fullRefreshRequired = false;

	}

	private int getColor(int attributes, int defaultIndex, boolean foreground) {
		int requestedIndex;
		if (foreground) {
			requestedIndex = ((attributes & VT320.COLOR_FG) >> 4) - 1;
		} else {
			requestedIndex = ((attributes & VT320.COLOR_BG) >> 8) - 1;
		}
		if (requestedIndex == -1) { // not set - use default value.
			requestedIndex = defaultIndex;
		}

		if (foreground) {
			if ((attributes & VT320.BOLD) != 0)
				return Tools.boldcolor[requestedIndex];
			if ((attributes & VT320.LOW) != 0)
				return Tools.lowcolor[requestedIndex];
		}
		return Tools.color[requestedIndex];

	}

	// private String debugGetString(long[] row, int offset) {
	// int size = row.length - offset;
	//
	// char[] data = new char[size];
	// for (int x = offset; x < row.length; x++) {
	// data[x - offset] = (char) (row[x] & 0xFF);
	//
	// }
	//
	// return new String(data);
	// }

	protected void drawCursor(Graphics g, int cursorX, int cursorY,
			XYPoint fontSize) {
		RemoteSessionInstance rsi = sessionMgr.activeSession;
		if (rsi == null)
			return;

		cursorY = rsi.emulator.screenBase + cursorY;

		// draw cursor if it's visible in the current viewport
		// screenbase + pos > windowbase and < [end of displayed - must calc
		// displayed line in common place.)
		if (rsi.emulator.showcursor
				&& (cursorY >= rsi.emulator.windowBase && cursorY < rsi.emulator.windowBase
						+ rsi.emulator.getTerminalHeight())) {
			int height = fontSize.y / 3; // this is our cursor height
			g.fillRect(
					(cursorX - rsi.state.left) * fontSize.x,
					((cursorY - rsi.state.topTermRow - rsi.emulator.windowBase) * fontSize.y)
							+ (fontSize.y - height), fontSize.x, height);
		}

	}

	/**
	 * We maintain a "backing store" image that we paint onto the screen. When a
	 * repaint is required but nothing has changed, nothing further is needed
	 * because the image is saved. However, if the image needs to change (such
	 * as when text arrives in the terminal) we must redraw it. This method
	 * handles the redrawing. When our content is invalidated, we need to
	 * recreate that image with the new contents. We will refresh our image with
	 * the lines that the model says are dirty -- note that when we do this, the
	 * model is driving the behavior nad not the view - so we don't care which
	 * rows the OS says are dirty.
	 */
	protected void redrawBackingStore(Graphics g) {
		RemoteSessionInstance rsi = sessionMgr.activeSession;
		if (rsi == null || !rsi.state.refreshRequired) {
			return;
		}

		// Ensure that the backing store doesn't get modified at the same time
		// we're
		// requesting a refresh. This indirectly guarantees that the drawing
		// operation
		// can't occur while modifications to the underlyin gbuffer are still in
		// progress.
		long start = System.currentTimeMillis();
		synchronized (rsi.emulator.getTermBufferMutex()) {
			rsi.state.debugRedrawStartWaitTime += (System.currentTimeMillis() - start);
			redrawBackingStoreImpl(g);
		}
	}

	public void invalidateStatusIcons() {
		if (statusInvalidated)
			return;
		statusInvalidated = true;
		invalidate(statusBarRegionRect.x, statusBarRegionRect.y,
				statusBarRegionRect.width, statusBarRegionRect.height);

	}

	/**
	 * Update the current font used. Make sure to invoke "sizeChanged"
	 * afterwards, if the font differs.
	 * 
	 * @param settings
	 * @return true if the font has been changed.
	 * @throws FontNotFoundException
	 */
	public boolean updateFontSettings(FontSettings settings)
			throws FontNotFoundException {
		if (SessionManager.getInstance().activeSession == null)
			return false;

		renderer = BBSSHFontManager.getInstance().getRenderer(settings);

		// We only want to do this AFTER the initial font setup is complete
		if (oldFontSettings == null) {
			oldFontSettings = new FontSettings(settings);
			return true;
		} else if (oldFontSettings.equals(settings)) {
			return false;
		}
		oldFontSettings = new FontSettings(settings);
		redraw(true);
		return true;

	}

	/**
	 * returns font settings currently in use.
	 */
	public FontSettings getFontSettings() {
		if (renderer == null)
			return null;
		return renderer.getSettings();
	}

	public void showExpiringAlertMessage(int messageId) {
		showExpiringMessage(Tools.getStringResource(messageId));
		if (SettingsManager.getSettings().isVibrateOnAlertEnabled()) {
			Alert.startVibrate(100);
		}
	}

	public void showExpiringMessage(final String expiringMessage) {
		if (isVisible()) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					setExpiringMessage(expiringMessage);
					if (messageTimer != -1) {
						UiApplication.getUiApplication().cancelInvokeLater(
								messageTimer);
					}
					messageTimer = UiApplication.getUiApplication()
							.invokeLater(expireMessageTask,
									500 + (25 * expiringMessage.length()),
									false);
					invalidateStatusIcons();
				}
			});
		}

	}

	private synchronized String getExpiringMessage() {
		return expiringMessage;
	}

	private synchronized void setExpiringMessage(String expiringMessage) {

		this.expiringMessage = expiringMessage;
	}

	public boolean isFocusable() {
		return true;
	}

	/**
	 * Maintain
	 */

	protected boolean keyStatus(int keycode, int time) {
		RemoteSessionInstance rsi = sessionMgr.activeSession;
		if (rsi == null)
			return true;
		// note: if user has changed status while virtual keyboard is visible -
		// do NOT toggle
		// virtual status in that case, as it creates an inconsistent result --
		// because we disregard
		// alt coming from virtual keyboard when processing the keystroke.

		if (PlatformServicesProvider.getInstance().hasTouchscreen()
				&& !((TerminalScreen) getScreen()).isVirtualKeyboardVisible()) {
			int key = Keypad.key(keycode);
			switch (key) {
			case (Keypad.KEY_SHIFT_RIGHT):

				rsi.state.toggleArtificialStatus(KeyListener.STATUS_SHIFT
						| KeyListener.STATUS_SHIFT_RIGHT, false);
				break;
			case (Keypad.KEY_SHIFT_LEFT):
				rsi.state.toggleArtificialStatus(KeyListener.STATUS_SHIFT
						| KeyListener.STATUS_SHIFT_LEFT, false);
				break;
			case (Keypad.KEY_ALT):
				rsi.state.toggleArtificialStatus(KeyListener.STATUS_ALT, false);
				break;
			}
		}
		invalidateStatusIcons();
		return true;

	}

}
