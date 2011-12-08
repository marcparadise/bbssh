package org.bbssh.ui.screens;


/**
 * This PrimaryScreen implementation adds handling to allow a tap or click gesture to lanuch the selected/current
 * session.
 * 
 * @author marc
 * 
 */
public class PrimaryScreen_47 extends PrimaryScreen {
	public PrimaryScreen_47() {
		super();
	}
//	protected boolean touchEvent(TouchEvent message) {

//		if (sessionTreeField == null )
//			return super.touchEvent(message);
//		// @todo - only launch the thing if the currently selected item == clicked-on item. 
//		// How do we get that? There seems tobe no 'getTopIndex' - with that and getBottomIndex() 
//		// we could simply calculate the offset ... 
//		if (PlatformServicesProvider.getInstance().isTouchClickSupported()) {
//			// Click causes launch, allowing TAP to change selection. Needed for Storms. 
//			if (message.getEvent() == TouchEvent.CLICK) {
//				launchEditOrResume();
//				return true;
//			}
//		} else { 
//			// For screens with no CLICK, they wil have a nav devices for changing selection 
//			// so launch on TAP. 
//			TouchGesture g = message.getGesture(); 
//			if (g != null && g.getEvent() == TouchGesture.TAP) { 
//				launchEditOrResume();
//				return true; 
//			}
//		}
//		return super.touchEvent(message);
//	}
}
