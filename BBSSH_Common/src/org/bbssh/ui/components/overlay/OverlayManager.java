package org.bbssh.ui.components.overlay;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Manager;

/**
 * Use this manager for a screen that will draw no fields of its own normally (ie the full "canvas" of the screen is
 * populated by the screen's paint method), but for which you wish to display top and/or/ bottom fields as "overlay"
 * fields.
 * 
 * This manager assumes that it has access to the full height and width of the screen (excluding any displayed virtual
 * keyboard)
 * 
 */
public class OverlayManager extends Manager {
	private Field topField;
	private Field bottomField;
	private Field centralField;
	private Field titleBar;

	public static final int CONTEXT_CANCEL = 100;

	public OverlayManager() {
		super(0);
	}

	public OverlayManager(long style) {
		super(style);
	}

	public void hideTopField() {
		if (topField != null && topField.getManager() == this)
			delete(topField);
	}

	public void hideBottomField() {
		if (bottomField != null && bottomField.getManager() == this)
			delete(bottomField);
	}

	public void showTopField(Field topField) {
		this.topField = topField;
		if (topField.getManager() == this)
			return;

		add(topField); // make sure that we're registered as "manager"
	}

	public void showBottomField(Field bottomField) {
		this.bottomField = bottomField;
		if (bottomField.getManager() == this)
			return;
		add(bottomField); // make sure that we're registered as "manager"
	}

	protected void sublayout(int width, int height) {
		setExtent(width, height);
		setPosition(0, 0);
		int yOffset = 0;
		if (titleBar != null && titleBar.getManager() == this) {
			layoutChild(titleBar, width, titleBar.getPreferredHeight());
			yOffset = titleBar.getHeight();
		}
		if (centralField != null && centralField.getManager() == this) {
			layoutChild(centralField, width, height - yOffset);
			setPositionChild(centralField, 0, yOffset);
		}
		// These fields are drawn over top of the center field, and do not cause it to move.
		if (topField != null && topField.isVisible() && topField.getManager() == this) {
			layoutChild(topField, width, topField.getPreferredHeight());
			setPositionChild(topField, 0, yOffset);
		}

		if (bottomField != null && bottomField.isVisible() && bottomField.getManager() == this) {
			layoutChild(bottomField, width, bottomField.getPreferredHeight());
			setPositionChild(bottomField, 0, height - bottomField.getHeight());
		}
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @return true if the coordiante is within one of the fields owned by the overlay.
	 */
	public boolean isCoordinateInOverlayFields(int x, int y) {

		int idx = getFieldAtLocation(x, y);
		if (idx == -1)
			return false;
		Field f = getField(idx);
		if ((f == topField || f.getManager() == topField) && topField.isVisible())
			return true;
		if ((f == bottomField || f.getManager() == bottomField) && bottomField.isVisible())
			return true;
		return false;
	}

	public void setCentralField(Field centralField) {
		if (this.centralField != null)
			delete(this.centralField);
		this.centralField = centralField;
		add(centralField);
	}

	public void setTitleBar(Field titleBar) {
		if (this.titleBar != null)
			delete(this.titleBar);
		this.titleBar = titleBar;
		add(titleBar);
	}

	public Field getTitleBar() {
		return titleBar;
	}


};
