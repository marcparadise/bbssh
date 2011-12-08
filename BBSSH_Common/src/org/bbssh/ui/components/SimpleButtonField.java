package org.bbssh.ui.components;

import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.component.LabelField;

public class SimpleButtonField extends Field {

	public static final int BORDER_STYLE_TOP_ONLY = 1;
	public static final int BORDER_STYLE_RECT = 2;
	public static final int BORDER_STYLE_ROUND_RECT = 3;
	public static final int BORDER_STYLE_NONE = 0;
	public static int CONTEXT_BUTTON_CLICKED = 150;
	private String label = "";
	private int textColorUnfocused;
	private int textColorFocused;
	private int backgroundColorUnfocused;
	private int backgroundColorFocused;
	private int preferredHeight;
	private int preferredWidth;
	private int horizontalPadding = 2;
	private int verticalPadding = 2;
	private int borderColor = 0xEEEEEEEE;
	private int borderStyle;
	

	/**
	 * Constructor
	 * 
	 * @param label label to display
	 * @param textColorUnfocused color of text when button does not have focus
	 * @param textColorFocused color of text when button has focus
	 * @param backgroundColorUnfocused background color when buttno does not have focus
	 * @param backgroundColorFocused backgruond color when button has focus.
	 * @param style style flags
	 */
	public SimpleButtonField(String label, int textColorUnfocused,
			int textColorFocused, int backgroundColorUnfocused,
			int backgroundColorFocused, long style) {
		super(style);
		if (label == null)
			label = "";
		this.label = label;
		this.textColorUnfocused = textColorUnfocused;
		this.textColorFocused = textColorFocused;
		this.backgroundColorUnfocused = backgroundColorUnfocused;
		this.backgroundColorFocused = backgroundColorFocused;
		Font font = getFont();
		preferredHeight = font.getHeight() + (verticalPadding * 2);
		preferredWidth = font.getAdvance(label) + (horizontalPadding * 2);
	}

	public SimpleButtonField(String label, long style) {
		this(label, Color.LIGHTGREY, Color.WHITE, Color.DARKGRAY, Color.LIGHTGREY, style);
	}

	public SimpleButtonField(long style) {
		this("", Color.LIGHTGREY, Color.WHITE, Color.DARKGRAY, Color.LIGHTGREY, 0);
	}

	/*
	 * (non-Javadoc)
	 * @see net.rim.device.api.ui.Field#onFocus(int)
	 */
	protected void onFocus(int direction) {
		super.onFocus(direction);
		invalidate();
	}

	/*
	 * (non-Javadoc)
	 * @see net.rim.device.api.ui.Field#onUnfocus()
	 */
	protected void onUnfocus() {
		super.onUnfocus();
		invalidate();
	}

	/*
	 * (non-Javadoc)
	 * @see net.rim.device.api.ui.Field#isFocusable()
	 */
	public boolean isFocusable() {
		return true;
	}

	/**
	 * @return the color of text to use when button does not haev focus.
	 */
	public int getTextColorUnfocused() {
		return this.textColorUnfocused;
	}

	/**
	 * @param textColorUnfocused text color to use when button does not have focus
	 */
	public void setTextColorUnfocused(int textColorUnfocused) {
		this.textColorUnfocused = textColorUnfocused;
	}

	/**
	 * @return the text color when control is focused.
	 */
	public int getTextColorFocused() {
		return this.textColorFocused;
	}

	/**
	 * set text color to use when control has focus.
	 * 
	 * @param textColorFocused color to use
	 */
	public void setTextColorFocused(int textColorFocused) {
		this.textColorFocused = textColorFocused;
	}

	/**
	 * @return the unfocused background color
	 */
	public int getBackgroundColorUnfocused() {
		return this.backgroundColorUnfocused;
	}

	/**
	 * Set the background color to use when the control has focus.
	 * 
	 * @param backgroundColorUnfocused color to use
	 */
	public void setBackgroundColorUnfocused(int backgroundColorUnfocused) {
		this.backgroundColorUnfocused = backgroundColorUnfocused;
	}

	/**
	 * @return the background color when control has focus
	 */
	public int getBackgroundColorFocused() {
		return this.backgroundColorFocused;
	}

	/**
	 * Set background color to use when control has focus.
	 * 
	 * @param backgroundColorFocused color to use
	 */
	public void setBackgroundColorFocused(int backgroundColorFocused) {
		this.backgroundColorFocused = backgroundColorFocused;
	}

	/**
	 * @return the horizontal padding
	 */
	public int getHorizontalPadding() {
		return horizontalPadding;
	}

	/**
	 * @return the vertical padding
	 */
	public int getVerticalPadding() {
		return verticalPadding;
	}

	public int getPreferredHeight() {

		return preferredHeight;
	}

	public int getPreferredWidth() {
		return preferredWidth;
	}

	protected void layout(int maxWidth, int maxHeight) {
		// Respect the maximum width and height available from our manager
		setExtent(Math.min(preferredWidth, maxWidth), Math.min(preferredHeight, maxHeight));
	}

	protected void paint(Graphics graphics) {
		// Draw background
		boolean focus = isFocus();
		int width = getWidth(); 
		int height = getHeight();
		graphics.setColor(focus ? backgroundColorFocused : backgroundColorUnfocused);
		graphics.fillRect(0, 0, width, height);
		graphics.setColor(borderColor); // Color.LIGHTGRAY is 5.0 only
		
		// @todo - BorderStyle option - rect, none, roundrect.
		switch (borderStyle) { 
			case BORDER_STYLE_TOP_ONLY:
				graphics.drawLine(0, 0, width, 0);
				break;
			case BORDER_STYLE_RECT: 
				graphics.drawRect(0, 0, width, height);
				break;
			case BORDER_STYLE_ROUND_RECT:
			case BORDER_STYLE_NONE: 
		}
		graphics.setColor(focus ? getTextColorFocused(): getTextColorUnfocused());
		
		drawText(graphics); 
		
	}

	public void drawText(Graphics graphics) { 
		int width = getWidth();
		int fontWidth = getFont().getAdvance(label);

		int x;
		int style = getFieldStyle(); 
		if ((style & LabelField.FIELD_HCENTER)  > 0) {
			x = (width - (fontWidth / 2));
		} else if ((style & LabelField.FIELD_RIGHT) > 0) {
			x = (width - fontWidth - getHorizontalPadding());
		} else {
			x = getHorizontalPadding();
		}

		graphics.drawText(getLabel(), x, getVerticalPadding());

	}
	protected void drawFocus(Graphics graphics, boolean on) {
		// Don't draw the default focus
	}

	// @todo - touch support? Crap.. how do we make a BASE class platform-specific... ugh.

	/**
	 * This override invokes fieldChangeNotify if navigation is pressed.
	 * 
	 * @param status
	 * @param time
	 * @see net.rim.device.api.ui.Field#navigationClick(int, int)
	 */
	protected boolean navigationClick(int status, int time) {
		onClicked();
		return true;
	}

	/**
	 * This override invokes fieldChangeNotify if enter is pressed while this button has focus.
	 * 
	 * @param keycode
	 * @param time
	 * @see net.rim.device.api.ui.Field#keyDown(int, int)
	 */
	protected boolean keyDown(int keycode, int time) {
		int key = Keypad.key(keycode);
		if (key == Keypad.KEY_ENTER || key == Keypad.KEY_SPACE) {
			onClicked();
			return true;

		}
		return super.keyDown(keycode, time);
	}

	public void onClicked() {
		fieldChangeNotify(CONTEXT_BUTTON_CLICKED);
	}

	/**
	 * @return this field's label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Changes the text label of this button. Note that at present, this will <b>not</b> cause the control to resize if
	 * the text extent changes.
	 * 
	 * @param label new text to display
	 */
	public void setLabel(String label) {
		this.label = label;
		updateSizeAndRefresh();
	}

	public void setPadding(int vertical, int horizontal) {
		if (vertical > -1)
			this.verticalPadding = vertical;
		if (horizontal > -1)
			this.horizontalPadding = horizontal;
		updateSizeAndRefresh();
	}

	private void updateSizeAndRefresh() {
		Font font = getFont();
		preferredHeight = font.getHeight() + (verticalPadding * 2);
		preferredWidth = font.getAdvance(label) + (horizontalPadding * 2);
		updateLayout();

	}
	public void setFont(Font font) {
		super.setFont(font);
		updateSizeAndRefresh();
	}

	/**
	 * @return the borderColor
	 */
	public int getBorderColor() {
		return this.borderColor;
	}

	/**
	 * @param borderColor the borderColor to set
	 */
	public void setBorderColor(int borderColor) {
		this.borderColor = borderColor;
	}

	/**
	 * @param verticalPadding the verticalPadding to set
	 */
	public void setVerticalPadding(int verticalPadding) {
		this.verticalPadding = verticalPadding;
		updateSizeAndRefresh();
	}
	
	public void setBorderStyle(int borderStyle) { 
		this.borderStyle = borderStyle; 
		updateSizeAndRefresh();
	}

}
