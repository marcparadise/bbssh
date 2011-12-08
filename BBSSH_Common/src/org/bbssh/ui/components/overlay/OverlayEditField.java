package org.bbssh.ui.components.overlay;

import net.rim.device.api.system.Display;
import net.rim.device.api.system.KeyListener;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.component.AutoTextEditField;
import net.rim.device.api.ui.component.TextField;

/**
 * This is an edit field designed for use in conjunction with OverlayManager, and will take up the full width of the
 * screen.
 * 
 * @author marc
 * 
 */
public class OverlayEditField extends AutoTextEditField {
	/** Escape key presssed */
	public static final int CONTEXT_ESCAPE_PRESSED = 100;
	public static final int CONTEXT_ENTER_PRESSED = 101;
	public static final int CONTEXT_ALT_ENTER_PRESSED = 102;
	public static final int CONTEXT_FOCUS_LOST = 103;

	private int backgroundColor;
	private int foregroundColor;

	/**
	 * Sets field colors
	 * 
	 * @param backgroundColor edit field background color
	 * @param foregroundColor edit field foreground color
	 */
	public void setColors(int backgroundColor, int foregroundColor) {
		this.backgroundColor = backgroundColor;
		this.foregroundColor = foregroundColor;

	}

	/**
	 * constructor
	 */
	public OverlayEditField() {
		// @todo make these attributes (autocap, autoperiod, autoexpand) configurable.
		super("", "", 1024, TextField.JUMP_FOCUS_AT_END | AutoTextEditField.AUTOCAP_OFF
				| AutoTextEditField.AUTOPERIOD_OFF);
	}

	public int getPreferredHeight() {
		return getFont().getHeight() + 8;
	}

	public int getPreferredWidth() {
		return Display.getWidth();
	}

	// protected void paintBackground(Graphics g) {
	// g.setBackgroundColor(backgroundColor);
	// g.fillRect(0, 0, getWidth(), getHeight());

	// super.paintBackground(g);
	// }
	/**
	 * Override of standard paint method that sets the fg/bg color.
	 * 
	 * @param Graphics graphics object
	 * @see net.rim.device.api.ui.component.BasicEditField#paint(net.rim.device.api.ui.Graphics)
	 */

	protected void paint(Graphics g) {
		int alpha = g.getGlobalAlpha();
		g.setGlobalAlpha(192);
		if (!drawFocus) {
			g.setColor(backgroundColor);
			// g.clear();
			g.fillRect(0, 0, getWidth(), getHeight());
			// g.setBackgroundColor(backgroundColor);
			g.setColor(foregroundColor);
			//			
		}
		super.paint(g);
		g.setGlobalAlpha(alpha);
	}

	boolean drawFocus;

	protected void drawFocus(Graphics graphics, boolean on) {
		drawFocus = on;
		super.drawFocus(graphics, on);
		drawFocus = false;
	}

	/**
	 * Notifies listener when user presses ESC or ENTER (non-Javadoc)
	 * 
	 * @see net.rim.device.api.ui.component.BasicEditField#keyDown(int, int)
	 */
	protected boolean keyDown(int keycode, int time) {
		FieldChangeListener listener = getChangeListener();
		if (listener == null)
			return super.keyDown(keycode, time);

		int key = Keypad.key(keycode);
		if (key == Keypad.KEY_ESCAPE) {
			fieldChangeNotify(CONTEXT_ESCAPE_PRESSED);
			return true;
		} else if (key == Keypad.KEY_ENTER) {
			int status = Keypad.status(keycode);
			if ((status & KeyListener.STATUS_ALT) > 0) {
				fieldChangeNotify(CONTEXT_ALT_ENTER_PRESSED);
			} else if ((status & KeyListener.STATUS_SHIFT) > 0) {
				return super.keyDown(keycode, time);
			} else {
				fieldChangeNotify(CONTEXT_ENTER_PRESSED);
			}
			return true;
		}
		return super.keyDown(keycode, time);
	}

	protected void onUnfocus() {
		super.onUnfocus();
		fieldChangeNotify(CONTEXT_FOCUS_LOST);
	}


}
