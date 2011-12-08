package org.bbssh.platform;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.util.IntEnumeration;
import net.rim.device.api.util.IntHashtable;
import net.rim.device.api.util.IntIntHashtable;
import net.rim.device.api.util.IntVector;

import org.bbssh.i18n.BBSSHResource;
import org.bbssh.keybinding.KeyBindingHelper;
import org.bbssh.terminal.TerminalStateData;
import org.bbssh.ui.components.FileSelectorPopupScreen;
import org.bbssh.util.Logger;
import org.bbssh.util.Version;

/**
 * A platform-version specific class that allows the appplication to poll for provided services and perform
 * platform-dependent operations in a platform-independent way.
 * 
 * @author marc
 * 
 */
public class PlatformServicesProvider {
	private static PlatformServicesProvider me;
	protected String deviceName;
	// We're going to capture this data up front isntead of using the
	// getHardwareLayout method every time, because we need to do it so frequently.

	private boolean layoutReduced24;
	private boolean layout39;
	private boolean layout32;
	private boolean layoutPhoneITUT;
	private boolean device8700 = false; // several conditions are driven off of this, so we'll capture it once up front.
	private Boolean hasCameraFocusKey = null; // this is complex enough that we'll only want to calcualte it once.

	/**
	 * Do not use this constructor directly; instead use getInstance. This is public because it must be created
	 * dynamically via reflection at runtime.
	 */
	public PlatformServicesProvider() {
		deviceName = DeviceInfo.getDeviceName();
		determineLayout();
		device8700 = deviceName.startsWith("87");

	}

	public void determineLayout() {
		switch (getHardwareLayout()) {
			case Keypad.HW_LAYOUT_32:
				Logger.info("Layout: 32");
				layout32 = true;
				break;
			case Keypad.HW_LAYOUT_39:
				Logger.info("Layout: 39");
				layout39 = true;
				break;
			case Keypad.HW_LAYOUT_REDUCED_24:
				Logger.info("Layout: 24");
				layoutReduced24 = true;
				break;
			case KeyBindingHelper.HW_LAYOUT_ITUT: // introduced in 5.0, but we don't actually get a const for it until
				// 6.0
				Logger.info("Layout: ITUT-18");
				layoutPhoneITUT = true; // 18 key layout
				break;
			default:
				Logger.error("Unknown layout: " + Keypad.getHardwareLayout());
				break;

		}
	}

	/**
	 * @return correct PlatformServiceProvider for the currently installed OS version on this device.
	 */
	public static synchronized PlatformServicesProvider getInstance() {
		if (me == null) {
			try {
				Class cl = PlatformServicesProvider.class;
				me = (PlatformServicesProvider) Version
						.createOSObjectInstance(cl.getName());
			} catch (Throwable e) {
				// @todo yep, need exception handlign here too
				Logger.error("Exception in creating PlatformServiceProvider: " + e.getMessage());
			}
		}
		return me;

	}

	/**
	 * @return true if this device has a physical keyboard.
	 */
	public boolean hasHardwareKeyboard() {
		// Through 4.5 we always have a hardware keyboard.
		return true;
	}

	/**
	 * @return true if this device supports virtual keyboards.
	 */
	public boolean hasVirtualKeyboard() {
		// We don't see virtual keyboards until 4.7, no need to check for it.
		return false;

	}

	/**
	 * @param bmp bitmap
	 * @return graphics object instance for the provided bitmap.
	 */
	public Graphics getGraphicsObjectForBitmap(Bitmap bmp) {
		return new Graphics(bmp);
	}

	/**
	 * @return true if touchscreen is supported on this OS AND the device itself has a touchscreen.
	 */
	public boolean hasTouchscreen() {
		// No touchscreens until 4.7
		return false;
	}

	/**
	 * Devices that have a focus mode don't seem to support binding the camera focus button (usually conv key 1) with
	 * Alt/Shift combinations.
	 * 
	 * @return true if this device has a camera focus key; typically this is conv key 1 held to the halfway position.
	 * 
	 */
	public boolean hasCameraFocusKey() {
		// This can only be true in OS's 4.7 and later - that OS is
		// the first shipped with phones that had an AF camera. Because this is a 4.5 library,
		// we know it must be false now.
		// However, since we're basing this on model for now (no better mechanism found)
		// the above doesn't matter too much ...
		if (hasCameraFocusKey == null) {
			// Okay, this is unfortunate but what we have to do. I am also not
			// able to be 100% sure that it's accurate - because some devices
			// have focus keys even though the simulators don't, and I can't be certain
			// what that list is.
			boolean haskey = deviceName.startsWith("97") 
					|| deviceName.startsWith("98") || deviceName.startsWith("9520") || deviceName.startsWith("9550")
					|| deviceName.startsWith("9630") || deviceName.startsWith("8980");
			// Note: so far 
			// new devices 9900, 9930 do not have it.  99
			hasCameraFocusKey = haskey && Keypad.isValidKeyCode(KeyBindingHelper.KEY_CAMERA_FOCUS_SDK) ? Boolean.TRUE
					: Boolean.FALSE;
		}
		return hasCameraFocusKey.booleanValue();
	}

	/**
	 * @return true if the platform and device has a lock key.
	 */
	public boolean hasLockKey() {
		return false; // we're not currently supporting binding to the lock key, because we can't block teh lock
		// behavior.
		// return Keypad.isValidKeyCode(KeyBindingHelper.KEY_LOCK_SDK) ;
	}

	/**
	 * @return true if this platform implementation has pinch gesture support (or can fake it)
	 */
	public boolean isTouchPinchSupported() {
		return false;
	}

	public boolean hasNavSwipeSupport() {
		return false;
	}

	/**
	 * 
	 * @return true if this platform provides support for platform notification and messaging
	 */
	public boolean isNotificationSupportAvailable() {
		return true;
	}

	public static FileSelectorPopupScreen getFileSelectorPopup() {
		Class cl = FileSelectorPopupScreen.class;
		return (FileSelectorPopupScreen) Version.createOSObjectInstance(cl.getName());
	}

	public int lockOrientation(int direction) {
		// @todo - TerminalStateData is not the proper home for this.
		return TerminalStateData.DIRECTION_ALL;

	}

	public int unlockOrientation() {
		return TerminalStateData.DIRECTION_ALL;
	}

	public boolean isTouchClickSupported() {
		return false;
	}

	public boolean isEnhancedTitlebarSupported() {
		return false;
	}

	public boolean hasSlider() {
		return false;
	}

	public boolean isSliderExtended() {
		return false;
	}

	protected IntIntHashtable eventmap;
	protected IntIntHashtable eventcatmap;
	protected IntHashtable categoryEventMap;

	/**
	 * Returns event categories for current dev/os. INitializes event data if unused, since this data is only used in
	 * the UI for managing mappings.
	 * 
	 * @return available event categories for current device and OS.
	 */
	public IntEnumeration getEventCategories() {
		// Since this and the others will get called frequently from within a UI thread,
		// let's put the check ahead of the function call...
		if (eventmap == null)
			populateAvailableEventMap();
		return categoryEventMap.keys();
	}

	public IntVector getEventsForCategory(int cat) {
		if (eventmap == null)
			populateAvailableEventMap();
		return (IntVector) categoryEventMap.get(cat);
	}

	public int getEventResourceId(int eventId) {
		if (eventmap == null)
			populateAvailableEventMap();
		return eventmap.get(eventId);
	}

	public int getEventCategory(int eventId) {
		if (eventmap == null)
			populateAvailableEventMap();
		return eventcatmap.get(eventId);
	}

	protected final void addKey(int id, int res, int cat) {
		eventmap.put(id, res);
		eventcatmap.put(id, cat);

		IntVector catvec = (IntVector) categoryEventMap.get(cat);
		if (catvec == null) {
			catvec = new IntVector(32);
			categoryEventMap.put(cat, catvec);
		}
		catvec.addElement(id);
	}

	public boolean hasConvKey1() {
		return !deviceName.startsWith("88");
	}

	public boolean hasConvKey2() {
		return !(this.deviceName.startsWith("9670") || this.deviceName.startsWith("98") || this.deviceName.startsWith("99"));

	}

	/**
	 * Ths populates the event mappings for the current software and hardware versions. For available mappings it will
	 * also populate the proper category and name resource mappings. It will be used for both event processing, and
	 * keybindign management (UI)
	 * 
	 */

	protected void populateAvailableEventMap() {
		if (eventmap != null) {
			return;
		}
		eventmap = new IntIntHashtable(128);
		eventcatmap = new IntIntHashtable(128);
		categoryEventMap = new IntHashtable(24);

		// Several limitations apply ONLY to the 8700, so let's just grab it once.
		// Every supported device so far has a dedicated Enter, Backspace, and Escape keys.
		addKey(Keypad.KEY_ENTER, BBSSHResource.KEYNAME_ENTER, KeyBindingHelper.CAT_KEYBOARD);
		addKey(Keypad.KEY_ESCAPE, BBSSHResource.KEYNAME_ESCAPE, KeyBindingHelper.CAT_PHONE);
		addKey(Keypad.KEY_BACKSPACE, BBSSHResource.KEYNAME_BACKSPACE, KeyBindingHelper.CAT_KEYBOARD);
		// every profile tested has at least one convenience key, but wihch convenience key varies
		// by model.
		if (hasConvKey1()) {
			addKey(Keypad.KEY_CONVENIENCE_1, BBSSHResource.KEYNAME_CONVENIENCE_1, KeyBindingHelper.CAT_PHONE);
		}

		if (hasConvKey2()) {
			addKey(Keypad.KEY_CONVENIENCE_2, BBSSHResource.KEYNAME_CONVENIENCE_2, KeyBindingHelper.CAT_PHONE);
		}

		// we specifically need to know if a dedicaetd currency key is available.
		if (Keypad.hasCurrencyKey()) {
			addKey(KeyBindingHelper.KEY_CURRENCY, BBSSHResource.KEYNAME_CURRENCY, KeyBindingHelper.CAT_KEYBOARD);
		}
		if (Keypad.hasSendEndKeys()) {
			addKey(Keypad.KEY_END, BBSSHResource.KEYNAME_END, KeyBindingHelper.CAT_PHONE);
			addKey(Keypad.KEY_SEND, BBSSHResource.KEYNAME_SEND, KeyBindingHelper.CAT_PHONE);

		}

		if (!layout32)
			addKey(Keypad.KEY_MENU, BBSSHResource.KEYNAME_MENU, KeyBindingHelper.CAT_PHONE);

		// AKA Mute - 8700s, 8200s, and the 9670 do not have a dedicated Mute button.
		if (!(device8700 || deviceName.startsWith("9670") || deviceName.startsWith("82")))
			addKey(Keypad.KEY_SPEAKERPHONE, BBSSHResource.KEYNAME_SPEAKERPHONE, KeyBindingHelper.CAT_PHONE);

		// Note that we'll override this with reduced key layouts as " " is combined with 0...
		addKey(Keypad.KEY_SPACE, BBSSHResource.KEYNAME_SPACE, KeyBindingHelper.CAT_KEYBOARD);
		if (!device8700) {
			addKey(Keypad.KEY_VOLUME_DOWN, BBSSHResource.KEYNAME_VOLUME_DOWN, KeyBindingHelper.CAT_MEDIA);
			addKey(Keypad.KEY_VOLUME_UP, BBSSHResource.KEYNAME_VOLUME_UP, KeyBindingHelper.CAT_MEDIA);
		}
		if (!layoutPhoneITUT && !layoutReduced24) {
			addKey(KeyBindingHelper.KEY_ZERO, BBSSHResource.KEYNAME_ZERO, KeyBindingHelper.CAT_KEYBOARD);
		}
		// Okay, this is another icky one.
		if (hasCameraFocusKey()) {
			addKey(KeyBindingHelper.KEY_CAMERA_FOCUS, BBSSHResource.KEYNAME_CAMERA_FOCUS, KeyBindingHelper.CAT_MEDIA);
		}
		if (hasNavigationMethod()) {
			addKey(KeyBindingHelper.KEY_NAV_CLICK, BBSSHResource.KEYNAME_NAV_CLICK, KeyBindingHelper.CAT_NAV);
			addKey(KeyBindingHelper.KEY_NAV_DOWN, BBSSHResource.KEYNAME_NAV_DOWN, KeyBindingHelper.CAT_NAV);
			addKey(KeyBindingHelper.KEY_NAV_LEFT, BBSSHResource.KEYNAME_NAV_LEFT, KeyBindingHelper.CAT_NAV);
			addKey(KeyBindingHelper.KEY_NAV_RIGHT, BBSSHResource.KEYNAME_NAV_RIGHT, KeyBindingHelper.CAT_NAV);
			addKey(KeyBindingHelper.KEY_NAV_UP, BBSSHResource.KEYNAME_NAV_UP, KeyBindingHelper.CAT_NAV);
			if (hasNavSwipeSupport()) {
				addKey(KeyBindingHelper.KEY_NAV_SWIPE_NORTH, BBSSHResource.KEYNAME_SWIPE_NORTH, KeyBindingHelper.CAT_NAV);
				addKey(KeyBindingHelper.KEY_NAV_SWIPE_SOUTH, BBSSHResource.KEYNAME_SWIPE_SOUTH, KeyBindingHelper.CAT_NAV);
				addKey(KeyBindingHelper.KEY_NAV_SWIPE_EAST, BBSSHResource.KEYNAME_SWIPE_EAST, KeyBindingHelper.CAT_NAV);
				addKey(KeyBindingHelper.KEY_NAV_SWIPE_WEST, BBSSHResource.KEYNAME_SWIPE_WEST, KeyBindingHelper.CAT_NAV);
			}
		}
		// Whether it be connected to KEY_NEXT or KEY_DEL, all devices that are not touchscreen only seem to have a SYM
		// key.
		if (hasHardwareKeyboard())
			addKey(KeyBindingHelper.KEY_SYM, BBSSHResource.KEYNAME_SYM, KeyBindingHelper.CAT_KEYBOARD);// "SYM"

		if (hasForwardBackwardMediaKeys()) {
			addKey(KeyBindingHelper.KEY_FORWARD_SDK, BBSSHResource.KEYNAME_FORWARD, KeyBindingHelper.CAT_MEDIA);
			addKey(KeyBindingHelper.KEY_BACKWARD_SDK, BBSSHResource.KEYNAME_BACKWARD, KeyBindingHelper.CAT_MEDIA);
		}

		// Special keys for smaller/reduced layouts
		int layout = getHardwareLayout();
		if (layout == Keypad.HW_LAYOUT_REDUCED_24) {
			addKey(KeyBindingHelper.KEY_R24_QW, BBSSHResource.KEYNAME_R24_QW, KeyBindingHelper.CAT_KEYBOARD);
			addKey(KeyBindingHelper.KEY_R24_ER, BBSSHResource.KEYNAME_R24_ER, KeyBindingHelper.CAT_KEYBOARD);
			addKey(KeyBindingHelper.KEY_R24_TY, BBSSHResource.KEYNAME_R24_TY, KeyBindingHelper.CAT_KEYBOARD);
			addKey(KeyBindingHelper.KEY_R24_UI, BBSSHResource.KEYNAME_R24_UI, KeyBindingHelper.CAT_KEYBOARD);
			addKey(KeyBindingHelper.KEY_R24_OP, BBSSHResource.KEYNAME_R24_OP, KeyBindingHelper.CAT_KEYBOARD);
			addKey(KeyBindingHelper.KEY_R24_AS, BBSSHResource.KEYNAME_R24_AS, KeyBindingHelper.CAT_KEYBOARD);
			addKey(KeyBindingHelper.KEY_R24_DF, BBSSHResource.KEYNAME_R24_DF, KeyBindingHelper.CAT_KEYBOARD);
			addKey(KeyBindingHelper.KEY_R24_GH, BBSSHResource.KEYNAME_R24_GH, KeyBindingHelper.CAT_KEYBOARD);
			addKey(KeyBindingHelper.KEY_R24_JK, BBSSHResource.KEYNAME_R24_JK, KeyBindingHelper.CAT_KEYBOARD);
			addKey(KeyBindingHelper.KEY_R24_L, BBSSHResource.KEYNAME_R24_L, KeyBindingHelper.CAT_KEYBOARD);
			addKey(KeyBindingHelper.KEY_R24_ZX, BBSSHResource.KEYNAME_R24_ZX, KeyBindingHelper.CAT_KEYBOARD);
			addKey(KeyBindingHelper.KEY_R24_CV, BBSSHResource.KEYNAME_R24_CV, KeyBindingHelper.CAT_KEYBOARD);
			addKey(KeyBindingHelper.KEY_R24_BN, BBSSHResource.KEYNAME_R24_BN, KeyBindingHelper.CAT_KEYBOARD);
			addKey(KeyBindingHelper.KEY_R24_0, BBSSHResource.KEYNAME_R24_0, KeyBindingHelper.CAT_KEYBOARD);
			addKey(KeyBindingHelper.KEY_R24_M, BBSSHResource.KEYNAME_R24_M, KeyBindingHelper.CAT_KEYBOARD);
		} else if (layout == KeyBindingHelper.HW_LAYOUT_ITUT) {
			addKey(KeyBindingHelper.KEY_ITUT_1, BBSSHResource.KEYNAME_ITUT_1, KeyBindingHelper.CAT_KEYBOARD);
			addKey(KeyBindingHelper.KEY_ITUT_2, BBSSHResource.KEYNAME_ITUT_2, KeyBindingHelper.CAT_KEYBOARD);
			addKey(KeyBindingHelper.KEY_ITUT_3, BBSSHResource.KEYNAME_ITUT_3, KeyBindingHelper.CAT_KEYBOARD);
			addKey(KeyBindingHelper.KEY_ITUT_4, BBSSHResource.KEYNAME_ITUT_4, KeyBindingHelper.CAT_KEYBOARD);
			addKey(KeyBindingHelper.KEY_ITUT_5, BBSSHResource.KEYNAME_ITUT_5, KeyBindingHelper.CAT_KEYBOARD);
			addKey(KeyBindingHelper.KEY_ITUT_6, BBSSHResource.KEYNAME_ITUT_6, KeyBindingHelper.CAT_KEYBOARD);
			addKey(KeyBindingHelper.KEY_ITUT_7, BBSSHResource.KEYNAME_ITUT_7, KeyBindingHelper.CAT_KEYBOARD);
			addKey(KeyBindingHelper.KEY_ITUT_8, BBSSHResource.KEYNAME_ITUT_8, KeyBindingHelper.CAT_KEYBOARD);
			addKey(KeyBindingHelper.KEY_ITUT_9, BBSSHResource.KEYNAME_ITUT_9, KeyBindingHelper.CAT_KEYBOARD);
			addKey(KeyBindingHelper.KEY_ITUT_0, BBSSHResource.KEYNAME_ITUT_0, KeyBindingHelper.CAT_KEYBOARD);
		}

		if (hasTouchscreen()) {
			populateTouchscreenAvailableEvents();
		}

	}

	private void populateTouchscreenAvailableEvents() {
		if (isTouchPinchSupported()) {
			addKey(KeyBindingHelper.KEY_TOUCH_PINCH_IN, BBSSHResource.KEYNAME_TOUCH_PINCH_IN,
					KeyBindingHelper.CAT_TOUCH_GESTURE);
			addKey(KeyBindingHelper.KEY_TOUCH_PINCH_OUT, BBSSHResource.KEYNAME_TOUCH_PINCH_OUT,
					KeyBindingHelper.CAT_TOUCH_GESTURE);
		}
		addKey(KeyBindingHelper.KEY_TOUCH_SWIPE_NORTH, BBSSHResource.KEYNAME_SWIPE_NORTH,
					KeyBindingHelper.CAT_TOUCH_GESTURE);
		addKey(KeyBindingHelper.KEY_TOUCH_SWIPE_SOUTH, BBSSHResource.KEYNAME_SWIPE_SOUTH,
					KeyBindingHelper.CAT_TOUCH_GESTURE);
		addKey(KeyBindingHelper.KEY_TOUCH_SWIPE_EAST, BBSSHResource.KEYNAME_SWIPE_EAST,
					KeyBindingHelper.CAT_TOUCH_GESTURE);
		addKey(KeyBindingHelper.KEY_TOUCH_SWIPE_WEST, BBSSHResource.KEYNAME_SWIPE_WEST,
					KeyBindingHelper.CAT_TOUCH_GESTURE);
		addKey(KeyBindingHelper.KEY_TOUCH_SWIPE_NORTHWEST, BBSSHResource.KEYNAME_SWIPE_NORTHWEST,
					KeyBindingHelper.CAT_TOUCH_GESTURE);
		addKey(KeyBindingHelper.KEY_TOUCH_SWIPE_SOUTHWEST, BBSSHResource.KEYNAME_SWIPE_SOUTHWEST,
					KeyBindingHelper.CAT_TOUCH_GESTURE);
		addKey(KeyBindingHelper.KEY_TOUCH_SWIPE_NORTHEAST, BBSSHResource.KEYNAME_SWIPE_NORTHEAST,
					KeyBindingHelper.CAT_TOUCH_GESTURE);
		addKey(KeyBindingHelper.KEY_TOUCH_SWIPE_SOUTHEAST, BBSSHResource.KEYNAME_SWIPE_SOUTHEAST,
					KeyBindingHelper.CAT_TOUCH_GESTURE);

		addKey(KeyBindingHelper.KEY_TOUCH_TAP_CENTER, BBSSHResource.KEYNAME_TOUCH_REGION_CENTER,
					KeyBindingHelper.CAT_TOUCH_TAP);
		addKey(KeyBindingHelper.KEY_TOUCH_TAP_NORTH, BBSSHResource.KEYNAME_TOUCH_REGION_NORTH,
					KeyBindingHelper.CAT_TOUCH_TAP);
		addKey(KeyBindingHelper.KEY_TOUCH_TAP_SOUTH, BBSSHResource.KEYNAME_TOUCH_REGION_SOUTH,
					KeyBindingHelper.CAT_TOUCH_TAP);
		addKey(KeyBindingHelper.KEY_TOUCH_TAP_EAST, BBSSHResource.KEYNAME_TOUCH_REGION_EAST,
					KeyBindingHelper.CAT_TOUCH_TAP);
		addKey(KeyBindingHelper.KEY_TOUCH_TAP_WEST, BBSSHResource.KEYNAME_TOUCH_REGION_WEST,
					KeyBindingHelper.CAT_TOUCH_TAP);
		addKey(KeyBindingHelper.KEY_TOUCH_TAP_NORTHWEST, BBSSHResource.KEYNAME_TOUCH_REGION_NORTHWEST,
					KeyBindingHelper.CAT_TOUCH_TAP);
		addKey(KeyBindingHelper.KEY_TOUCH_TAP_SOUTHWEST, BBSSHResource.KEYNAME_TOUCH_REGION_SOUTHWEST,
					KeyBindingHelper.CAT_TOUCH_TAP);
		addKey(KeyBindingHelper.KEY_TOUCH_TAP_NORTHEAST, BBSSHResource.KEYNAME_TOUCH_REGION_NORTHEAST,
					KeyBindingHelper.CAT_TOUCH_TAP);
		addKey(KeyBindingHelper.KEY_TOUCH_TAP_SOUTHEAST, BBSSHResource.KEYNAME_TOUCH_REGION_SOUTHEAST,
					KeyBindingHelper.CAT_TOUCH_TAP);

		addKey(KeyBindingHelper.KEY_TOUCH_DBTAP_CENTER, BBSSHResource.KEYNAME_TOUCH_REGION_CENTER,
					KeyBindingHelper.CAT_TOUCH_DOUBLE_TAP);
		addKey(KeyBindingHelper.KEY_TOUCH_DBTAP_NORTH, BBSSHResource.KEYNAME_TOUCH_REGION_NORTH,
					KeyBindingHelper.CAT_TOUCH_DOUBLE_TAP);
		addKey(KeyBindingHelper.KEY_TOUCH_DBTAP_SOUTH, BBSSHResource.KEYNAME_TOUCH_REGION_SOUTH,
					KeyBindingHelper.CAT_TOUCH_DOUBLE_TAP);
		addKey(KeyBindingHelper.KEY_TOUCH_DBTAP_EAST, BBSSHResource.KEYNAME_TOUCH_REGION_EAST,
					KeyBindingHelper.CAT_TOUCH_DOUBLE_TAP);
		addKey(KeyBindingHelper.KEY_TOUCH_DBTAP_WEST, BBSSHResource.KEYNAME_TOUCH_REGION_WEST,
					KeyBindingHelper.CAT_TOUCH_DOUBLE_TAP);
		addKey(KeyBindingHelper.KEY_TOUCH_DBTAP_NORTHWEST, BBSSHResource.KEYNAME_TOUCH_REGION_NORTHWEST,
					KeyBindingHelper.CAT_TOUCH_DOUBLE_TAP);
		addKey(KeyBindingHelper.KEY_TOUCH_DBTAP_SOUTHWEST, BBSSHResource.KEYNAME_TOUCH_REGION_SOUTHWEST,
					KeyBindingHelper.CAT_TOUCH_DOUBLE_TAP);
		addKey(KeyBindingHelper.KEY_TOUCH_DBTAP_NORTHEAST, BBSSHResource.KEYNAME_TOUCH_REGION_NORTHEAST,
					KeyBindingHelper.CAT_TOUCH_DOUBLE_TAP);
		addKey(KeyBindingHelper.KEY_TOUCH_DBTAP_SOUTHEAST, BBSSHResource.KEYNAME_TOUCH_REGION_SOUTHEAST,
					KeyBindingHelper.CAT_TOUCH_DOUBLE_TAP);

		addKey(KeyBindingHelper.KEY_TOUCH_HOVER_CENTER, BBSSHResource.KEYNAME_TOUCH_REGION_CENTER,
					KeyBindingHelper.CAT_TOUCH_HOVER);
		addKey(KeyBindingHelper.KEY_TOUCH_HOVER_NORTH, BBSSHResource.KEYNAME_TOUCH_REGION_NORTH,
					KeyBindingHelper.CAT_TOUCH_HOVER);
		addKey(KeyBindingHelper.KEY_TOUCH_HOVER_SOUTH, BBSSHResource.KEYNAME_TOUCH_REGION_SOUTH,
					KeyBindingHelper.CAT_TOUCH_HOVER);
		addKey(KeyBindingHelper.KEY_TOUCH_HOVER_EAST, BBSSHResource.KEYNAME_TOUCH_REGION_EAST,
					KeyBindingHelper.CAT_TOUCH_HOVER);
		addKey(KeyBindingHelper.KEY_TOUCH_HOVER_WEST, BBSSHResource.KEYNAME_TOUCH_REGION_WEST,
					KeyBindingHelper.CAT_TOUCH_HOVER);
		addKey(KeyBindingHelper.KEY_TOUCH_HOVER_NORTHWEST, BBSSHResource.KEYNAME_TOUCH_REGION_NORTHWEST,
					KeyBindingHelper.CAT_TOUCH_HOVER);
		addKey(KeyBindingHelper.KEY_TOUCH_HOVER_SOUTHWEST, BBSSHResource.KEYNAME_TOUCH_REGION_SOUTHWEST,
					KeyBindingHelper.CAT_TOUCH_HOVER);
		addKey(KeyBindingHelper.KEY_TOUCH_HOVER_NORTHEAST, BBSSHResource.KEYNAME_TOUCH_REGION_NORTHEAST,
					KeyBindingHelper.CAT_TOUCH_HOVER);
		addKey(KeyBindingHelper.KEY_TOUCH_HOVER_SOUTHEAST, BBSSHResource.KEYNAME_TOUCH_REGION_SOUTHEAST,
					KeyBindingHelper.CAT_TOUCH_HOVER);

		if (isTouchClickSupported()) {
			addKey(KeyBindingHelper.KEY_TOUCH_CLICK_CENTER, BBSSHResource.KEYNAME_TOUCH_REGION_CENTER,
					KeyBindingHelper.CAT_TOUCH_CLICK);
			addKey(KeyBindingHelper.KEY_TOUCH_CLICK_NORTH, BBSSHResource.KEYNAME_TOUCH_REGION_NORTH,
					KeyBindingHelper.CAT_TOUCH_CLICK);
			addKey(KeyBindingHelper.KEY_TOUCH_CLICK_SOUTH, BBSSHResource.KEYNAME_TOUCH_REGION_SOUTH,
					KeyBindingHelper.CAT_TOUCH_CLICK);
			addKey(KeyBindingHelper.KEY_TOUCH_CLICK_EAST, BBSSHResource.KEYNAME_TOUCH_REGION_EAST,
					KeyBindingHelper.CAT_TOUCH_CLICK);
			addKey(KeyBindingHelper.KEY_TOUCH_CLICK_WEST, BBSSHResource.KEYNAME_TOUCH_REGION_WEST,
					KeyBindingHelper.CAT_TOUCH_CLICK);
			addKey(KeyBindingHelper.KEY_TOUCH_CLICK_NORTHWEST, BBSSHResource.KEYNAME_TOUCH_REGION_NORTHWEST,
					KeyBindingHelper.CAT_TOUCH_CLICK);
			addKey(KeyBindingHelper.KEY_TOUCH_CLICK_SOUTHWEST, BBSSHResource.KEYNAME_TOUCH_REGION_SOUTHWEST,
					KeyBindingHelper.CAT_TOUCH_CLICK);
			addKey(KeyBindingHelper.KEY_TOUCH_CLICK_NORTHEAST, BBSSHResource.KEYNAME_TOUCH_REGION_NORTHEAST,
					KeyBindingHelper.CAT_TOUCH_CLICK);
			addKey(KeyBindingHelper.KEY_TOUCH_CLICK_SOUTHEAST, BBSSHResource.KEYNAME_TOUCH_REGION_SOUTHEAST,
					KeyBindingHelper.CAT_TOUCH_CLICK);

			addKey(KeyBindingHelper.KEY_TOUCH_DBCLICK_CENTER, BBSSHResource.KEYNAME_TOUCH_REGION_CENTER,
					KeyBindingHelper.CAT_TOUCH_DOUBLE_CLICK);
			addKey(KeyBindingHelper.KEY_TOUCH_DBCLICK_NORTH, BBSSHResource.KEYNAME_TOUCH_REGION_NORTH,
					KeyBindingHelper.CAT_TOUCH_DOUBLE_CLICK);
			addKey(KeyBindingHelper.KEY_TOUCH_DBCLICK_SOUTH, BBSSHResource.KEYNAME_TOUCH_REGION_SOUTH,
					KeyBindingHelper.CAT_TOUCH_DOUBLE_CLICK);
			addKey(KeyBindingHelper.KEY_TOUCH_DBCLICK_EAST, BBSSHResource.KEYNAME_TOUCH_REGION_EAST,
					KeyBindingHelper.CAT_TOUCH_DOUBLE_CLICK);
			addKey(KeyBindingHelper.KEY_TOUCH_DBCLICK_WEST, BBSSHResource.KEYNAME_TOUCH_REGION_WEST,
					KeyBindingHelper.CAT_TOUCH_DOUBLE_CLICK);
			addKey(KeyBindingHelper.KEY_TOUCH_DBCLICK_NORTHWEST, BBSSHResource.KEYNAME_TOUCH_REGION_NORTHWEST,
					KeyBindingHelper.CAT_TOUCH_DOUBLE_CLICK);
			addKey(KeyBindingHelper.KEY_TOUCH_DBCLICK_SOUTHWEST, BBSSHResource.KEYNAME_TOUCH_REGION_SOUTHWEST,
					KeyBindingHelper.CAT_TOUCH_DOUBLE_CLICK);
			addKey(KeyBindingHelper.KEY_TOUCH_DBCLICK_NORTHEAST, BBSSHResource.KEYNAME_TOUCH_REGION_NORTHEAST,
					KeyBindingHelper.CAT_TOUCH_DOUBLE_CLICK);
			addKey(KeyBindingHelper.KEY_TOUCH_DBCLICK_SOUTHEAST, BBSSHResource.KEYNAME_TOUCH_REGION_SOUTHEAST,
					KeyBindingHelper.CAT_TOUCH_DOUBLE_CLICK);

		}

	}

	public boolean hasNavigationMethod() {
		// @todo - only STORM, STORM 2 do not - SO FAR..
		return (!deviceName.startsWith("95"));
	}

	public boolean hasForwardBackwardMediaKeys() {
		// Created specifically for overriding in 5.0 - only 5.0 and later devices have these keys,
		// and RIM provides an API for us to use in 5.0
		return false;
	}

	public boolean hasMuteKey() {
		// 8700, 8200, 9670 do not have. 5.0+ will override this with Keypad.hasMuteKey
		if (device8700 || deviceName.startsWith("82") || deviceName.startsWith("9670"))
			return false;
		return true;

	}

	public boolean hasVolumeControls() {
		if (device8700)
			return false;
		return true;
	}

	public boolean hasShiftX() {
		return layoutPhoneITUT || layoutReduced24;
	}

	public boolean hasLeftShift() {
		return layout32 || layout39 || (hasVirtualKeyboard() && !hasHardwareKeyboard());
	}

	public boolean hasRightShift() {
		return layout32 || layout39 || (hasVirtualKeyboard() && !hasHardwareKeyboard());
	}

	public boolean isEventValidForDevice(int evt) {
		if (eventmap == null)
			populateAvailableEventMap();
		return eventmap.containsKey(evt);
	}

	/**
	 * @return true if terminals should process keyChar. Reduced keysets shoudl not because we instead treat ALL keys as
	 *         bindable.
	 */
	public boolean isReducedLayout() {
		return layoutPhoneITUT || layoutReduced24;
	}

	public boolean hasTrackwheel() {
		// 8700 is our only supported device that uses a trackwheel instead of a trackpad.
		return device8700;
	}

	public boolean hasAccelerometer() {
		return hasTouchscreen();
	}

	public int getHardwareLayout() {
		// Here we return this value as-is -- under the 6.0 instance we have
		// some additional considerations, but for 4.5 - 5.0 it will accurately return the presence
		// of a hardware keyboard. 
		return Keypad.getHardwareLayout();
	}
	public String getOSVersion() { 
		return "4.5"; 
	}

}
