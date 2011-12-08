package org.bbssh.terminal.fonts;

import net.rim.device.api.ui.XYPoint;

import org.bbssh.exceptions.FontNotFoundException;
import org.bbssh.util.Tools;

/**
 * Class that encapsulates a single set of bitmap fonts. A set is all available sizes for a single font
 */
public class BitmapFontData {

	private FontRecord[] fonts;
	private String name;
	private String displayName;

	/**
	 * Constructor that initializes internal data from raw input.
	 * 
	 * @param record input record, which must be a pipe-delimited string containing 12 fields.
	 */
	public BitmapFontData(String record) {
		String[] data = Tools.splitString(record, '|');
		int len = data.length;
		if (len < 3) {
			throw new IllegalArgumentException();
		}
		fonts = new FontRecord[len - 2];
		
		name = data[0];
		displayName = data[1];
		for (int x = 2; x < len; x++) {
			// Each sub-record is in the format XxY
			// where X = width (pixels), Y = height (pixels)
			// and "x" is the literal delimiter. This will also be used for the name fof the font/file.
			String[] sizes = Tools.splitString(data[x], 'x');
			fonts[x - 2] = new FontRecord(name, data[x], new XYPoint(Integer.parseInt(sizes[0]), Integer
					.parseInt(sizes[1])));
		}
	}

	/**
	 * Return font of the requested size, loading it if it's not already loaded.
	 * 
	 * @param size point size, must be from 0-9
	 * @return BitmapFont instance
	 * @throws IllegalArgumentException if point size is out of range
	 * @throws FontNotFoundException if font could not be loaded.
	 */
	public BitmapFont getFont(int size) throws FontNotFoundException {
		if (size < 0 || size >= fonts.length) {
			throw new FontNotFoundException();
		}
		return fonts[size].getBitmapFont();
	}

	public FontRecord getFontRecord(int size) throws FontNotFoundException {
		if (size < 0 || size >= fonts.length) {
			throw new FontNotFoundException();
		}
		return fonts[size];
	}

	/**
	 * 
	 * @return all font records
	 */
	public FontRecord[] getFontRecords() {
		return fonts;
	}

	/**
	 * Get user-friendly name
	 * 
	 * @return user-friendly name of this font.
	 */
	public String getDisplayName() {
		return this.displayName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getDisplayName();
	}

	public boolean isFontSizeValid(byte fontSize) {
		if (fontSize < 0 && fontSize > fonts.length - 1)
			return false;
		return true;
	}

}
