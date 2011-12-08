package org.bbssh.terminal.fonts;

import org.bbssh.exceptions.FontNotFoundException;
import org.bbssh.model.FontSettings;

import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.XYPoint;

public abstract class FontRenderer {
	private FontSettings settings;

	/**
	 * constructor for a new FontRenderer
	 * 
	 * @param settings
	 */
	public FontRenderer(FontSettings settings) throws FontNotFoundException {
		this.settings = settings;
	}

	/**
	 * Return the character dimensions of the current font.
	 * 
	 * @return XYPoint containing font width and height.
	 */
	abstract public XYPoint getFontDimensions();

	/**
	 * Renders the required text.
	 * 
	 * @param g graphics instance
	 * @param fg foreground color
	 * @param bg background color
	 * @param chars characters to render
	 * @param offset offset into the chars array where drawing should begin
	 * @param length number of characters from the char array to draw
	 * @param x x-position of where to start drawing
	 * @param y y-position of where to start drawing
	 */
	abstract public void drawChars(Graphics g, int fg, int bg,  long[] chars, int offset, int length, int x, int y);
	abstract public void drawChar(Graphics g, int fg, int bg,  char c, int x, int y);

	/**
	 * @return the settings for this renderer.
	 */
	public FontSettings getSettings() {
		return this.settings;
	}

}
