package org.bbssh.session;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Graphics;

import org.bbssh.i18n.BBSSHResource;
import org.bbssh.net.session.Session;
import org.bbssh.terminal.TerminalStateData;
import org.bbssh.terminal.VT320;
import org.bbssh.ui.screens.TerminalScreen;

/**
 * Simple data container class for the settings, terminal and screen data as applied to a live/previously live session
 * 
 * @author marc
 * 
 */
public class RemoteSessionInstance {
	public VT320 emulator;
	public Session session;
	public TerminalStateData state;
	public Graphics backingStoreGR = null;
	public Bitmap backingStore = null;
	public boolean termInitPending = true; 

	/**
	 * Scroll the view associated with this session by the specified number of lines.
	 * 
	 * @param lines number of liens to scroll - use 0 to scroll current term height -1
	 * @param down true to scroll down, false to scroll up
	 */
	public void scrollViewVertical(int lines, boolean down) {
		if (lines == 0) {
			lines = emulator.getTerminalHeight() - 1;
		}

		if (!down) {
			lines = -lines;
		}

		// @todo more of this in common than not.

		boolean isActiveSession = SessionManager.getInstance().activeSession == this;
		// Logger.debug("scroll 0: " + lines + " wb: " + rsi.emulator.windowBase + " ttr: " + rsi.state.topTermRow);
		if (lines < 0) {
			// Clamp at min of top row or desired row.
			if (emulator.windowBase + state.topTermRow > 0) {
				if (emulator.windowBase + state.topTermRow + lines > 0) {
					state.topTermRow += lines;
				} else {
					state.topTermRow = -emulator.windowBase;
				}
				if (isActiveSession)
					TerminalScreen.getInstance().forceRefresh();
			} else {
				if (isActiveSession)
					TerminalScreen.getInstance().showExpiringAlertMessage(BBSSHResource.TERMINAL_MSG_AT_TOP);
			}
		} else if (lines > 0) {
			// Logger.debug("scroll 0: " + lines + " bs: " + rsi.emulator.bufSize + " nr: " + rsi.state.numRows);
			// All scrolling is relative to the currnet window location in the buffer - this ensuring that our
			// MAXIMUM scroll offset is 0, which indicates the top of the buffer at draw-time.
			if (state.topTermRow < 0) {
				if (state.topTermRow + lines <= 0) {
					state.topTermRow += lines; // 
				} else {
					state.topTermRow = 0;
				}
				if (isActiveSession)
					TerminalScreen.getInstance().forceRefresh();
			} else {
				if (isActiveSession)
					TerminalScreen.getInstance().showExpiringAlertMessage(BBSSHResource.TERMINAL_MSG_AT_BOTTOM);
			}
		} else {
			return;
		}
		// Logger.debug("scroll 1: " + lines + " wb: " + rsi.emulator.windowBase + " ttr: " + rsi.state.topTermRow);
	}

	public void scrollViewHorizontal(int cols, boolean right) {
		boolean isActiveSession = SessionManager.getInstance().activeSession == this;
		if (cols == 0) {
			cols = state.numColsVisible - 1;
		}
		if (!right) {
			cols = -cols;
		}
		if (cols < 0) {
			if (state.left > 0) {
				if (state.left + cols >= 0) {
					state.left += cols;
				} else {
					state.left = 0;
				}
				if (isActiveSession)
					TerminalScreen.getInstance().forceRefresh();
			} else {
				if (isActiveSession)
					TerminalScreen.getInstance().showExpiringAlertMessage(BBSSHResource.TERMINAL_MSG_AT_LEFT);

			}
		} else {
			int max = emulator.getWidth() - state.numColsVisible - 1;
			if (state.left < max) {
				if (state.left + cols < max) {
					state.left += cols;
				} else {
					state.left = max;
				}
				if (isActiveSession)
					TerminalScreen.getInstance().forceRefresh();
			} else {
				if (isActiveSession)
					TerminalScreen.getInstance().showExpiringAlertMessage(BBSSHResource.TERMINAL_MSG_AT_RIGHT);

			}
		}

	}

	/**
	 * Sends string to the terminal first splitting it into first-character -> and remainder if state indicates that we
	 * should be sending ctrl/alted character as the first character. First character will be sent with appropriate
	 * modifiers.
	 * 
	 * @param value
	 */
	public void sendTwoPartString(String value) {
		if (value == null)
			return;
		if (emulator == null)
			return;
		if (state == null)
			return;

		int len = value.length();
		if (len == 0)
			return;
		// Send first character separately using CTRL and/or ALT modifier,
		// if the state indicates that this is expected.
		if (state.altPressed || state.ctrlPressed) {
			emulator.keyTyped(0, value.charAt(0), state.getModifierKeyState(true));
			if (len == 1) {
				return;
			}
			value = value.substring(1);
		}
		emulator.stringTyped(value);
	}

	public boolean isConnected() {
		if (session != null)
			return session.isConnected();
		return false;
	}



}