package org.bbssh.ui.components;

import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;

import org.bbssh.terminal.fonts.FontRenderer;

public class FontDisplayField extends Field {
	private FontRenderer renderer;
	private int height;
	private static final long[] line1;
	private static final long[] line2;
	private static final long[] line3;
	static {
		char[] l1 = "The quick red fox jumps over the lazy brown dog".toCharArray();
		char[] l2 = "1234567890".toCharArray();
		char[] l3 = "{}!@#$%^&*()_+-=[]\\,./".toCharArray();
		line1 = new long[l1.length];
		line2 = new long[l2.length];
		line3 = new long[l3.length];
		// Okay, we can't use arraycopy here b/c we're converting between primitive types..
		for (int x = 0; x < l1.length; x++) {
			line1[x] = l1[x];
		}
		for (int x = 0; x < l2.length; x++) {
			line2[x] = l2[x];
		}
		for (int x = 0; x < l2.length; x++) {
			line3[x] = l3[x];
		}

	}

	public FontDisplayField(FontRenderer renderer) {
		setRenderer(renderer);
	}

	public void setRenderer(FontRenderer renderer) {
		this.renderer = renderer;
		height = renderer.getFontDimensions().y + 4;
		updateLayout();
	}

	protected void layout(int width, int height) {
		setExtent(Math.min(width, getPreferredWidth()), Math.min(height, getPreferredHeight()));
	}

	public int getPreferredHeight() {
		return (renderer.getFontDimensions().y * 3) + 8;
	}

	public int getPreferredWidth() {
		// use our longest line
		return (renderer.getFontDimensions().x * line1.length) + 8;
	}

	protected void paint(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());

		int y = 0;
		renderer.drawChars(g, Color.WHITE, Color.BLACK, line1, 0, line1.length, 0, y);
		y += height;

		renderer.drawChars(g, Color.BLUEVIOLET, Color.BLACK, line2, 0, line2.length, 0, y);
		y += height;

		renderer.drawChars(g, Color.BEIGE, Color.BLACK, line3, 0, line3.length, 0, y);
	}

}
