package org.bbssh.ui.components;

import net.rim.device.api.ui.TouchEvent;
import net.rim.device.api.ui.TouchGesture;
import net.rim.device.api.ui.UiApplication;

import org.bbssh.keybinding.KeyBindingHelper;
import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.terminal.TerminalStateData;
import org.bbssh.util.Logger;

/**
 * This extension of the 4.6 implementation adds touchscreen support for bindable key handling.
 * 
 * @author marc
 * 
 */
public class TerminalField_47 extends TerminalField {
	// @todo make this configurable
	private static final int SECOND_TOUCH_ACTION_DELAY = 200;

	protected boolean inTouchEvent = false;
	protected boolean processTouchEvent = true;
	private int eventOneTime;
	private int eventTwoTime;
	private int event;
	private int monitorId;
	private int repeatCount = 0;
	private int hoverCount = 0;

	public TerminalField_47() {
		super();
		resetSavedEvent();
	}

	protected final boolean touchEvent(TouchEvent message) {
		int eventId = message.getEvent();
		if (eventId == TouchEvent.DOWN) {
			inTouchEvent = true;
			return false;
		}

		if (eventId == TouchEvent.UP || eventId == TouchEvent.CANCEL) {
			inTouchEvent = false;

			// All repeating hover events (click_repeat and hover) WILL terminate
			// once we receive an UP or CANCEL
			hoverCount = 0;

			if (eventId == TouchEvent.CANCEL) {
				resetSavedEvent();
			}
			return true;
			// return super.touchEvent(message);
		}

		if (touchEventImpl(message)) {
			return true;

		}
		return true; // super.touchEvent(message);
	}

	protected void resetSavedEvent() {

		eventOneTime = 0;
		eventTwoTime = 0;
		event = 0;
		monitorId = -1;
		repeatCount = 0;
	}

	Runnable secondTouchEventMonitor = new Runnable() {
		public void run() {

			// If the two times match, then the first event is the only one we received
			// that's relevant. (It's possible we received otehrs, but they would
			// be unrelated to the first if this condition is true.)
			if (eventTwoTime == eventOneTime) {
				keyEvent(event, 0, eventTwoTime, false);
				resetSavedEvent();
			}
		}
	};

	protected boolean handleRepeatingEvent(TouchEvent m, int mappedEvent, int multiplier) {
		if (monitorId > -1) {
			// Make sure we don't have more than one running at a time -
			// timers are a tightly limited resource
			try {
				// 
				UiApplication.getUiApplication().cancelInvokeLater(monitorId);
			} catch (IllegalArgumentException e) {
				// This means that the second instance already ran - invalidating the timer.
				// Technically should not be psosible but that's no excuse to crash...
				Logger.error("handleRepeatingEvent: unexpected IllegalArgumentException");
				resetSavedEvent();
				return true;
			} finally {
				monitorId = -1;
			}
		}
		repeatCount++;

		if (repeatCount == 1) {
			event = mappedEvent + (KeyBindingHelper.KEY_MODE_ADJUST * multiplier);
			eventOneTime = eventTwoTime = m.getTime();
			monitorId = UiApplication.getUiApplication().invokeLater(secondTouchEventMonitor,
					SECOND_TOUCH_ACTION_DELAY, false);
			return true;
		} else if (repeatCount == 2) {
			boolean result = keyEvent(event + KeyBindingHelper.KEY_MODE_ADJUST, 0, eventTwoTime, false);
			resetSavedEvent();
			return result;
		}
		return false;

	}

	boolean dragging;
	int selAnchorX = 0;
	int selAnchorY = 0;
	int prevX = -1; 
	int prevY = -1; 

	/*
	 * 
	 * (non-Javadoc)
	 * @see net.rim.device.api.ui.Screen#touchEvent(net.rim.device.api.ui.TouchEvent)
	 */
	protected boolean touchEventImpl(TouchEvent message) {
		int eventId = message.getEvent();
		int key = 0;
		RemoteSessionInstance rsi = sessionMgr.activeSession;
		if (rsi == null || rsi.state == null)
			return true; 
		
		TouchGesture g = message.getGesture();
		if (rsi.state.typingMode == TerminalStateData.TYPING_MODE_SELECT) {
			if (eventId == TouchEvent.DOWN) {
				dragging = true;
				selAnchorX = message.getX(1);
				selAnchorY = message.getY(1);
				rsi.state.selectionCursorX = selAnchorX;
				rsi.state.selectionCursorY = selAnchorY;
			} else if (eventId == TouchEvent.UP) {
				dragging = false;
			} else if (eventId == TouchEvent.MOVE) {
				if (dragging) {
					int x = message.getX(1); 
	//				int y = message.getY(1); 
					if (x == prevX) { 
					} if (x < prevX) { 
						
					} if (x > prevX) { 
						
					}
		
				}
			}
			if (dragging) {
			}
			redraw(true);
			return true;
		}
		if (eventId == TouchEvent.CLICK) {
			return handleRepeatingEvent(message, mapTouchEvent(message.getX(1), message.getY(1)),
						KeyBindingHelper.KEY_MODE_MULTIPLIER_CLICK);

		} else if (eventId == TouchEvent.UNCLICK) {

		} else if (eventId == TouchEvent.GESTURE) {
			switch (g.getEvent()) {
				case TouchGesture.HOVER:
					if (++hoverCount == 1) { // prevent flooding - hover ONCE.
						key = mapTouchEvent(message.getX(1), message.getY(1))
								+ (KeyBindingHelper.KEY_MODE_ADJUST * KeyBindingHelper.KEY_MODE_MULTIPLIER_HOVER);
					}
					break;

				case TouchGesture.CLICK_REPEAT:
					// We can't use CLICK_REPAET because it's always preceded by a CLICK
					break;

				case TouchGesture.TAP:
					return handleRepeatingEvent(message, mapTouchEvent(message.getX(1), message.getY(1)),
							KeyBindingHelper.KEY_MODE_MULTIPLIER_TAP);
				case TouchGesture.SWIPE:
					if (rsi.state.typingMode == TerminalStateData.TYPING_MODE_LOCAL_SCROLL) {
						int dir = g.getSwipeDirection();
						if ((dir & TouchGesture.SWIPE_NORTH) > 0) {
							rsi.scrollViewVertical(0, false);
						} else if ((dir & TouchGesture.SWIPE_SOUTH) > 0) {
							rsi.scrollViewVertical(0, true);
						} else if ((dir & TouchGesture.SWIPE_EAST) > 0) {
							rsi.scrollViewHorizontal(0, false);
						} else if ((dir & TouchGesture.SWIPE_WEST) > 0) {
							rsi.scrollViewHorizontal(0, true);
						}
					} else {
						// CLICK *may* precede SWIPE in Storm - so we
						// need to make sure it doesn't get processed for event binding when we receive a swipe
						// @todo - do we want to regionalize swipe based on end location?
						// @todo What about user defeined swipe region sequences? e
						resetSavedEvent();
						key = resolveSwipeDirection(g.getSwipeDirection());
					}
					break;

			}
		}

		// Note that touch events do NOT reset the on-screen alt status, etc - so if keyboard is out
		// this must also not reset the artifical alt status so that we can stay in sync.
		if (key > 0 && keyEvent(key, 0, message.getTime(), false)) {
			return true;
		}

		return false;
	}

	

	public int mapTouchEvent(int x, int y) {
		// Divide the screen into a 9-cell grid.
		int w = getWidth() / 3;
		int h = getHeight() / 3;
		boolean east = false;
		boolean vcenter = false;
		// Find out which range our value falls into.
		if (x <= w) {
			// default false
		} else if (x <= w * 2) {
			east = false;
			vcenter = true;
		} else {
			east = true;
		}
		// There's likely a much more elegant way of doing this - for example,
		// bitmask the values; or even build an array of these values and mod
		// the x/y into an array index [0-8]
		if (y <= h) {
			if (vcenter) {
				return KeyBindingHelper.KEY_TOUCH_TAP_NORTH;
			} else if (east) {
				return KeyBindingHelper.KEY_TOUCH_TAP_NORTHEAST;
			} else {
				return KeyBindingHelper.KEY_TOUCH_TAP_NORTHWEST;
			}

		} else if (y <= h * 2) {
			if (vcenter) {
				return KeyBindingHelper.KEY_TOUCH_TAP_CENTER;
			} else if (east) {
				return KeyBindingHelper.KEY_TOUCH_TAP_EAST;
			} else {
				return KeyBindingHelper.KEY_TOUCH_TAP_WEST;
			}

		} else {
			if (vcenter) {
				return KeyBindingHelper.KEY_TOUCH_TAP_SOUTH;
			} else if (east) {
				return KeyBindingHelper.KEY_TOUCH_TAP_SOUTHEAST;
			} else {
				return KeyBindingHelper.KEY_TOUCH_TAP_SOUTHWEST;
			}
		}
	}

	public static int resolveSwipeDirection(int swipeDirection) {
		// @todo this is more of a static utility function...but can't go in KeybindingHelper which isn't
		// aware of the touch screen (it's at the base version....)
		// Map to our own constants, which are present across platform versions.
		switch (swipeDirection) {
			case TouchGesture.SWIPE_EAST:
				return KeyBindingHelper.KEY_TOUCH_SWIPE_EAST;
			case TouchGesture.SWIPE_NORTH:
				return KeyBindingHelper.KEY_TOUCH_SWIPE_NORTH;
			case TouchGesture.SWIPE_SOUTH:
				return KeyBindingHelper.KEY_TOUCH_SWIPE_SOUTH;
			case TouchGesture.SWIPE_WEST:
				return KeyBindingHelper.KEY_TOUCH_SWIPE_WEST;
			case (TouchGesture.SWIPE_SOUTH | TouchGesture.SWIPE_EAST):
				return KeyBindingHelper.KEY_TOUCH_SWIPE_SOUTHEAST;
			case (TouchGesture.SWIPE_SOUTH | TouchGesture.SWIPE_WEST):
				return KeyBindingHelper.KEY_TOUCH_SWIPE_SOUTHWEST;
			case (TouchGesture.SWIPE_NORTH | TouchGesture.SWIPE_EAST):
				return KeyBindingHelper.KEY_TOUCH_SWIPE_NORTHEAST;
			case (TouchGesture.SWIPE_NORTH | TouchGesture.SWIPE_WEST):
				return KeyBindingHelper.KEY_TOUCH_SWIPE_NORTHWEST;
		}
		return 0;

	}
}
