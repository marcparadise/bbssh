package org.bbssh.terminal.fonts;

import org.bbssh.exceptions.FontNotFoundException;

import net.rim.device.api.ui.XYPoint;

public class FontRecord {

	private String fileName;
	private XYPoint dimensions;
	private String dimensionName;
	private BitmapFont bitmapFont;
	private boolean legacy = false; 
	FontRecord(String faceName, String dimensionName, XYPoint dimensions) {
		this.fileName = faceName + dimensionName + ".png";
		this.dimensions = dimensions;
		this.dimensionName = dimensionName;
		// A kludge that allows us to keep the old midpssh fonts, which have a 
		// different starting offset. 
		legacy = fileName.toLowerCase().startsWith("legacy");
	}

	public void setBitmap(BitmapFont bitmapFont) {
		this.bitmapFont = bitmapFont;
	}

	public BitmapFont getBitmapFont() throws FontNotFoundException {
		// @todo this is a messy circular reference - merge these two classes.
		if (bitmapFont == null) {
			bitmapFont = new BitmapFont(this);
		}
		return this.bitmapFont;
	}

	public String toString() {
		return dimensionName;
	}

	public XYPoint getDimensions() {
		return dimensions;
	}

	public String getFileName() {
		return this.fileName;
	}

	public boolean isLegacyFont() {
		return legacy;
	}

}
