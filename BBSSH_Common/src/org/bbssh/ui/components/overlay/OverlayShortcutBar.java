package org.bbssh.ui.components.overlay;

import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.HorizontalFieldManager;

public class OverlayShortcutBar extends HorizontalFieldManager {
	public static int CONTEXT_CANCEL = 100;
	LabelField detail = new LabelField();

	public OverlayShortcutBar(int style, FieldChangeListener listener) {
		super(style);
		setChangeListener(listener);
	}

	public OverlayShortcutBar(FieldChangeListener listener) {
		setChangeListener(listener);
	}

	public OverlayShortcutBar() {
		super();
	}

	public void setDetailInfoLine(LabelField detail) {
		this.detail = detail;
		add(detail);
	}

	public int getPreferredHeight() {
		if (getFieldCount() > 0) {
			int height = getField(0).getPreferredHeight() * 2;
			if (detail != null) {
				height += detail.getPreferredHeight();
			}

			return height;
		}
		return super.getPreferredHeight();
	}

	public LabelField getDetailInfoLine() {
		return detail;
	}

	protected boolean keyDown(int keycode, int time) {
		if (Keypad.key(keycode) == Keypad.KEY_ESCAPE) {
			fieldChangeNotify(CONTEXT_CANCEL);
			return true;
		}
		return super.keyDown(keycode, time);
	}

	protected boolean keyChar(char ch, int status, int time) {
		Field f = getFieldWithFocus();
		// 
		// If a field has focus and the user presses eter or space, they probably want to
		// trigger that field and not search for a field beginnign with ' ' or '\n'...
		if (f != null && f.getManager() == this && (ch == Characters.SPACE || ch == Characters.ENTER)) {
			// This will pass notification through to the field.
			return super.keyChar(ch, status, time);
		}
		// Check to see if the character matches the first character of one of our supported children
		// if so, simulate selection.
		CommandButton cb;
		String label;
		for (int x = getFieldCount() - 1; x >= 0; x--) {
			f = getField(x);
			if (f instanceof CommandButton) {
				cb = (CommandButton) f;
				label = cb.getLabel().toLowerCase();
				if (label.length() > 0 && label.charAt(0) == ch) {
					cb.setFocus();
					cb.onClicked();
					return true;
				}

			}
		}
		return super.keyChar(ch, status, time);
	}

	private int getAdjustedFieldCount() {
		int count = getFieldCount();
		// exclude the detail line
		if (detail != null)
			count--;
		return count;

	}

	protected void sublayout(int maxWidth, int maxHeight) {
		// We want each field to be equally sized - ideall this will also haev to take into account the
		// size needs of each individual field, so that larger text will subtract from avg size.
		setExtent(maxWidth, maxHeight);
		int count = getAdjustedFieldCount();

		// @todo  Why not just use VerticalFieldManager { FlowFieldManager, HelpField } ?  
		int avgWidth = (maxWidth / count) - 1;
		int xpos = 0;
		for (int x = 0; x < count; x++) {
			Field f = getField(x);
			layoutChild(f, avgWidth, maxHeight / 2);
			setPositionChild(f, xpos, 0);
			xpos += f.getWidth() + 1;
		}
		if (detail != null) {
			// Last field is our "help text" field and gets a line of its own.
			layoutChild(detail, maxWidth, maxHeight / 2);
			setPositionChild(detail, 0, (maxHeight / 2) + 1);
		}
	}

	public void handleHoverEvent(int x, int y) {
		int idx = getFieldAtLocation(x, y);
		if (idx == -1)
			return;
		Field f = getField(idx);
		f.setFocus();

	}

}
