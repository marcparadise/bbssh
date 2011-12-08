package org.bbssh.terminal.fonts;

import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.XYPoint;

import org.bbssh.exceptions.FontNotFoundException;
import org.bbssh.model.FontSettings;

public class TruetypeFontRenderer extends FontRenderer {
	Font font;
	XYPoint dimensions;

	/**
	 * Constructor
	 * 
	 * @param settings font settings to intialize with
	 * @throws FontNotFoundException
	 */
	public TruetypeFontRenderer(FontSettings settings) throws FontNotFoundException {
		super(settings);
		font = BBSSHFontManager.getInstance().getTruetypeFont(settings);
		dimensions = new XYPoint(font.getAdvance(Characters.EM_DASH), font.getHeight());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.bbssh.terminal.SessionFontRenderer#drawChars(net.rim.device.api.ui.Graphics, int, int, char[], int, int,
	 * int, int)
	 */
	public void drawChars(Graphics g, int fg, int bg, long[] chars, int offset, int length, int x, int y) {
		if (font == null || dimensions == null)
			return;
		g.setFont(font);

		g.setBackgroundColor(bg);
		g.setColor(fg);
		// SoftFont sf = SoftFont.getInstance();

		// XYPoint dim = getFontDimensions();
		// @todo for now we can use the more efficent drawText, but once we add line drawing handling
		// we'll need to expand that.
		int len = offset + length;
		for (int c = offset; c < len; c++) {
			char ch = (char) chars[c];
			// if (sf.inSoftFont(ch)) {
			// x+= dim.x;
			// sf.drawChar(g, chars[ch], x, y, dim.x, dim.y);
			// } else {
			// // give it extra room to draw - some characters need an extra 1-2 pixels, and if they get truncated the entire character
			// fails to draw. 
			g.drawText(ch, x, y, Graphics.TOP | Graphics.LEFT, dimensions.x * 2); 
			// @todo - why isn't this value matched with return value of g.drawText? Should be... 
			x += dimensions.x;
			// }
			//
			// }
		}

	}

	public void drawChar(Graphics g, int fg, int bg, char c, int x, int y) {
		g.setFont(font);
		g.drawText(c, x, y, Graphics.TOP | Graphics.LEFT, dimensions.x);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.bbssh.terminal.SessionFontRenderer#getFontDimensions()
	 */
	public XYPoint getFontDimensions() {
		return dimensions;

	}

}
