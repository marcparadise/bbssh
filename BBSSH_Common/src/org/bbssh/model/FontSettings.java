package org.bbssh.model;

import net.rim.device.api.synchronization.SyncObject;
import net.rim.device.api.ui.Font;

import org.bbssh.exceptions.FontNotFoundException;
import org.bbssh.terminal.fonts.BBSSHFontManager;
import org.bbssh.terminal.fonts.BitmapFontData;
import org.bbssh.terminal.fonts.FontRecord;
import org.bbssh.util.Logger;

public class FontSettings implements SyncObject {
	/** Bitmap font */
	public static final byte FONT_BITMAP = 0;

	/** Truetype font */
	public static final byte FONT_TT = 1;

	public static final byte DEFAULT_BITMAP_FONT_SIZE = 3; // 3 = 10 point
	public static final byte DEFAULT_TRUETYPE_FONT_SIZE = 16;
	

	private byte fontId;
	private byte fontType;
	private byte fontSize;	
	 

	/**
	 * Constructor for FontSettings instance
	 * 
	 * @param fontId index that uniquely identifies the desired font. Restricted by number of available fonts. Not
	 *            validated in this usage.
	 * @param fontType FONT_BITMAP or FONT_TT
	 * @param fontSize not validated - assumed to be a safe font size for the provided font. 
	 */
	public FontSettings(byte fontType, byte fontId, byte fontSize) {
		this.fontId = fontId;
		this.fontType = fontType;
		this.fontSize = fontSize;
	}

	/**
	 * Constructor that copies parameters from a source FontSettings instance.
	 * 
	 * @param src
	 */
	public FontSettings(FontSettings src) {
		this.fontId = src.fontId;
		this.fontType = src.fontType;
		this.fontSize = src.fontSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o == null || !(o instanceof FontSettings)) {
			return false;

		}
		
		if (o == this) { 
			return true; 
		}
		
		FontSettings s = (FontSettings) o;
		if (s.fontId != fontId || s.fontSize != fontSize || s.fontType != fontType) {
			return false;
		}

		return true;
	}

	/**
	 * @return the ID for the bitmap font to use
	 */
	public byte getFontId() {
		return this.fontId;
	}

	/**
	 * @param fontId sets internal ID of the font to use. This must correspond to acceptable font r
	 */
	public void setFontId(byte fontId) {
		this.fontId = fontId;
	}

	/**
	 * @return the font type, FONT_TTF or FONT_BITMAP
	 */
	public byte getFontType() {
		return this.fontType;
	}

	/**
	 * @param fontType set type of font, FONT_TTF or FONT_BITMAP
	 */
	public void setFontType(byte fontType) {
		this.fontType = fontType;
	}

	/**
	 * @return the font size (0-9)
	 */
	public byte getFontSize() {
		return this.fontSize;
	}

	/**
	 * Does not set font size if it's otu of range
	 * 
	 * @param fontSize font size to set
	 */
	public void setFontSize(byte fontSize) {
		if (fontType == FONT_BITMAP) {
			try {
				if (!BBSSHFontManager.getInstance().getBitmapFontData(this.getFontId()).isFontSizeValid(fontSize)) 
					return;
			} catch (FontNotFoundException e) {
				Logger.error("FontNotFoundException in FontSettings.setFontSize [ " + e.getMessage() + " ] ");
			}
		} else {
			if (fontSize < 3 || fontSize > 99) {
				return;
			}
		}
		this.fontSize = fontSize;
	}

	public int getUID() {
		return 0;
	}

	public String toString() {
		try {
			if (fontType == FONT_BITMAP) {
				BBSSHFontManager m = BBSSHFontManager.getInstance();
				BitmapFontData data = m.getBitmapFontData(this.getFontId());
				FontRecord rec = data.getFontRecord(fontSize);
				return data.getDisplayName() + " " + rec.toString();
			} else {
				Font f = BBSSHFontManager.getInstance().getTruetypeFont(this);
				return f.getFontFamily().getName() + " " + fontSize;
			}
		} catch (FontNotFoundException e) {
		}
		return ""; 
		

	}

}
