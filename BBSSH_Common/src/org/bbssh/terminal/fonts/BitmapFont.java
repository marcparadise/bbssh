// BitmapFont: Class for using tiny fonts with subpixel antialising
// Formerly LCDFont.
//
// Copyright 2005 Roar Lauritzsen <roarl@pvv.org>
// Modified 2010 for use in BBSSH
// Modifications Copyright (C) 2010 Marc A. Paradise
//
// This class is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This class is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// The following link provides a copy of the GNU General Public License:
// http://www.gnu.org/licenses/gpl.txt
// If you are unable to obtain the copy from this address, write to
// the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
// Boston, MA 02111-1307 USA
package org.bbssh.terminal.fonts;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.XYPoint;

import org.bbssh.exceptions.FontNotFoundException;
import org.bbssh.util.Tools;

// @todo - merge this with FontRecord itself?
// @todo - BitmapFont instances should implement disposable - this is a
// a fair chunk of data that we might be able to discard.
public class BitmapFont {
	private final int imageWidth; // width of font bitmap
	private final int imageHeight; // height of font bitmap
	private int[] bwBuf; // Black-white version of font
	private int[] colorBuf; // Colored version of font
	private int[] currentBuf;
	private long[] cacheColor; // Color of each cached character
	private int FR, FG, FB, BR, BG, BB;
	private long currentColor;
	private int totalColumns = 0;
	FontRecord record;

	/**
	 * Constructor to create a bitmap font based on image resource name.
	 * 
	 * @param record
	 *            the source font record
	 * @throws FontNotFoundException
	 */
	// Create subpixel-antialiased font based on image resource name.
	protected BitmapFont(FontRecord record) throws FontNotFoundException {
		// @todo we need to retain these images so that we don't load multiple
		// copies if multiple sessions are running.
		// @todo erm, we should be using the file name...
		this.record = record;
		Bitmap fontImage = Tools.loadBitmap(record.getFileName());
		if (fontImage == null) {
			throw new FontNotFoundException();
		}
		imageWidth = fontImage.getWidth();
		imageHeight = fontImage.getHeight();

		bwBuf = new int[imageWidth * (imageHeight)];
		colorBuf = new int[imageWidth * (imageHeight)];

		fontImage.getARGB(bwBuf, 0, imageWidth, 0, 0, imageWidth, imageHeight);

		cacheColor = new long[255];
		currentBuf = bwBuf;

		currentColor = 0;
		totalColumns = imageWidth / record.getDimensions().x;

	}

	// Set the foreground and background color of font
	// For readability, input font colors will be modified
	public void setColor(int fg, int bg) {
		if (fg == 0xffffff && bg == 0) {
			currentBuf = bwBuf;
		} else {
			currentBuf = colorBuf;
			long color = ((long) fg << 32) + bg;
			if (currentColor != color) {
				currentColor = color;
				// First isolate the colors
				FR = (fg >> 16) & 0xFF;
				FG = (fg >> 8) & 0xFF;
				FB = fg & 0xFF;

				BR = (bg >> 16) & 0xFF;
				BG = (bg >> 8) & 0xFF;
				BB = bg & 0xFF;

				// // Because of the subpixel antialising, we cannot use the color
				// // directly. Instead, we have to select a more "pastel" color,
				// // and differentiate properly between background and foreground
				// if (28 * FR + 55 * FG + 17 * FB >= 28 * BR + 55 * BG + 17 * BB) {
				// // bright on dark
				// FR = (6 * FR + 10 * 0xff) / 16; // Brighten foreground
				// FG = (6 * FG + 10 * 0xff) / 16;
				// FB = (6 * FB + 10 * 0xff) / 16;
				// BR = (10 * BR) / 16; // Darken background
				// BG = (10 * BG) / 16;
				// BB = (10 * BB) / 16;
				// } else {
				// // dark on bright
				// FR = (10 * FR) / 16; // Darken foreground
				// FG = (10 * FG) / 16;
				// FB = (10 * FB) / 16;
				// BR = (6 * BR + 10 * 0xff) / 16; // Brighten background
				// BG = (6 * BG + 10 * 0xff) / 16;
				// BB = (6 * BB + 10 * 0xff) / 16;
				// }
				//
				//
				if (record.isLegacyFont()) {
					// Scale to range 0-256 for later ">>8" instead of "/255"
					FR += FR >> 7;
					FG += FG >> 7;
					FB += FB >> 7;
					BR += BR >> 7;
					BG += BG >> 7;
					BB += BB >> 7;
				}

			}
		}

	}

	/**
	 * Update the character at position "offset" to use the current FG/BG color.
	 * 
	 * @param offset
	 */
	private void renderColorChar(int offset) {
		// @todo - let's see how often we call this in a typical session. Perhaps
		// we could cache a wider range of characters?
		int R, G, B, idx, col;
		XYPoint dimensions = record.getDimensions();

		int maxy = dimensions.y; // not sure how efficient the JVM is
		int maxx = dimensions.x; // so let's pull this out instead of putting it in the loop
		// Paint this glyph with the current FG/BG colors, starting from the top
		// left and moving to the bottom right
		for (int y = 0; y < maxy; y++) {
			for (int x = 0; x < maxx; x++) {
				// Get from the black and white buffer for our baseline.
				idx = offset + y * imageWidth + x;
				col = bwBuf[idx];
				R = (col >> 16) & 0xff;
				G = (col >> 8) & 0xff;
				B = col & 0xff;
				// If this is the background color, we'll do a simple replacement.
				if (R == 0 && G == 0 && B == 0) {
					R = BR;
					G = BG;
					B = BB;
				} else {
					// fonts are drawn with attempted subpixel hinting, so
					// we need to adjust each color (except saturated colros which we can
					// replace without the additional operations ). 
					R = (R == 255) ? FR : ((FR * R + BR * (255 - R)) >> 8);
					G = (G == 255) ? FG : ((FG * G + BG * (255 - G)) >> 8);
					B = (B == 255) ? FB : ((FB * B + BB * (255 - B)) >> 8);
				}

				// stick it in the color image buffer for this font so that as long as we're
				// drawing this color, we don't have to perform this again.
				colorBuf[idx] = (R << 16) + (G << 8) + B;
				// @todo - profile this. if needed since we have a limited number of colors (8) we can 
				// maintain bufers for each 
			}
		}
	}

	/**
	 * Draws a single character, using the current color as buffered.
	 * 
	 * @param g
	 *            graphics to render the drawing onto
	 * @param c
	 *            character to draw
	 * @param x
	 *            x position to start drawing
	 * @param y
	 *            y position to start drawing
	 */
	public void drawChar(Graphics g, char c, int x, int y) {
		XYPoint dimensions = record.getDimensions();

		// @todo - line-drawing support changes
		// @todo we should also cache drawn characters.
		// SoftFont sf = SoftFont.getInstance();
		// if (sf.inSoftFont(c)) {
		// // if ((currAttr & VDUBuffer.INVISIBLE) == 0)
		// sf.drawChar(g, c, x, y, dimensions.x, dimensions.y);
		// // if ((currAttr & VDUBuffer.UNDERLINE) != 0)
		// // g.drawLine(c * charWidth + xoffset,
		// // (l + 1) * charHeight - charDescent / 2 + yoffset,
		// // c * charWidth + charWidth + xoffset,
		// // (l + 1) *
		// // continue;
		// return;
		// }
		// Before we can treat "c" as our index into the bitmap...
		// 127 through 159 and 173 are control codes; our bitmaps do not haev entries for them
		if (c <= ' ' || (c >= 127 && c <= 159) || c == 173) {
			return;
		}
		int offset = 33; // skip the first 33 characters 0 - 32: they are not renderable
							// and are not included in our
		// bitmap.

		// anything after 159 has to account for the gap in the bitmap - we don't have control characters in the bitmap.
		// There 33 control characters
		// / Legacy fonts don't ahve anything higher than 127.

		if (record.isLegacyFont() && c > 127) {
			return;

		} else {
			if (c > 159) {
				offset += 33;
			}
			// 173 is another non-rendered control character
			if (c > 173) {
				offset++;
			}
		}
		// "c" now becomes our index into the bitmap image for this character.
		c -= offset;
		offset = (c / totalColumns) * (dimensions.y * imageHeight) + ((c % totalColumns) * dimensions.x);
		// Logger.debug("Char: " + c + " offset: " + offset);
		if (currentBuf == colorBuf && cacheColor[c] != currentColor) {
			renderColorChar(offset);
			cacheColor[c] = currentColor;
		}
		g.drawRGB(currentBuf, offset, imageWidth, x, y, dimensions.x, dimensions.y);
	}

	public XYPoint getDimensions() {
		return record.getDimensions();
	}

}
