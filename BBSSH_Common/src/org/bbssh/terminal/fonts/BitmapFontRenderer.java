package org.bbssh.terminal.fonts;

import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.XYPoint;

import org.bbssh.exceptions.FontNotFoundException;
import org.bbssh.model.FontSettings;

public class BitmapFontRenderer extends FontRenderer {
	BitmapFont font;


	/**
	 * Constructor, sets up renderer for usage.
	 * 
	 * @param settings font settings to load
	 */
	public BitmapFontRenderer(FontSettings settings) throws FontNotFoundException {
		super(settings);
		font = BBSSHFontManager.getInstance().getBitmapFont(settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.bbssh.terminal.SessionFontRenderer#drawChars(net.rim.device.api.ui.Graphics, int, int, char[], int, int,
	 * int, int)
	 */
	public void drawChars(Graphics g, int fg, int bg, long[] chars, int offset, int length, int x, int y) {
		if (font == null)
			return;
		font.setColor(fg, bg);
		XYPoint dim = getFontDimensions();
		int len = offset + length;
		for (int i = offset; i < len; i++) {
			font.drawChar(g, (char)chars[i], x, y);
			x += dim.x;
		}

	}
	public void drawChar(Graphics g, int fg, int bg, char c, int x, int y) {
		if (font == null)
			return;
		font.setColor(fg, bg);
		font.drawChar(g, c, x, y);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.bbssh.terminal.SessionFontRenderer#getFontDimensions()
	 */
	public XYPoint getFontDimensions() {
		if (font == null) {
			return null;
		}
		return font.getDimensions();
	}

}
