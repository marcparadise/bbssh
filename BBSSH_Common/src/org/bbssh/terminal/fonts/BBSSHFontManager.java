package org.bbssh.terminal.fonts;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import net.rim.device.api.io.FileNotFoundException;
import net.rim.device.api.system.UnsupportedOperationException;
import net.rim.device.api.ui.Font;

import org.bbssh.exceptions.FontNotFoundException;
import org.bbssh.io.ConfigLineReader;
import org.bbssh.model.FontSettings;
import org.bbssh.util.Tools;
import org.bbssh.util.Version;

/**
 * Singleton that provides font management services to the client application.
 */
public class BBSSHFontManager {
	private static BBSSHFontManager me;
	private BitmapFontData[] bitmapFonts;
	protected Font[] truetypeFonts;
	protected String[] truetypeFontNames;

	/**
	 * Constructor for BBSSHFontManager.
	 */
	public BBSSHFontManager() {

	}

	/**
	 * Load font data from index file.
	 * 
	 * @throws FileNotFoundException
	 * @throws FontInitializationFailedException
	 */
	private void loadFontData() throws FileNotFoundException, FontInitializationFailedException {
		InputStream stream = Tools.getResourceInputStream("font.idx");
		ConfigLineReader reader = new ConfigLineReader(stream);
		int idx = 0, count = 0;
		try {
			// First valid line
			count = Integer.parseInt(reader.readNextLine());
			bitmapFonts = new BitmapFontData[count];
			// remaining lines each contains a 12-field font definition record
			while (idx < count) {
				bitmapFonts[idx++] = new BitmapFontData(reader.readNextLine());
			}
			// Next, truetype list. Even though we don't support truetype in prior to 5.0,
			// we can still load the list of font info.
			count = Integer.parseInt(reader.readNextLine());
			idx = 0;
			truetypeFonts = new Font[count];
			truetypeFontNames = new String[count];
			while (idx < count) {
				truetypeFontNames[idx++] = reader.readNextLine();
			}
			stream.close();

		} catch (EOFException eof) {
			if (idx < count) {
				throw new FontInitializationFailedException("Unexpected end of font definition file.");
			}
		} catch (IOException ioe) {
			throw new FontInitializationFailedException("Unable to read font definition file.");
		} catch (NumberFormatException nfe) {
			throw new FontInitializationFailedException("Invalid format in font definition file.");

		} catch (IllegalArgumentException iae) {
			throw new FontInitializationFailedException("Font definition record " + idx
					+ " contains incorrect number of fields.");

		}
	}

	/**
	 * Retrieve instance of the font factory.
	 * 
	 * @return font factory instance
	 * @throws UnsupportedOperationException if initialize() hasn't been invoked first.
	 */
	public static synchronized BBSSHFontManager getInstance() {
		if (me == null) {
			throw new UnsupportedOperationException();
		}

		return me;
	}

	/**
	 * Separate initailize allows us to more cleanly throw exceptions rather than someone having to worry about those
	 * exceptions every time they call getInstance. This will initialize our singleton with the correct instance of
	 * BBSSHFontManager based on current platform version.
	 * 
	 * @throws FileNotFoundException if index file font.idx doesn't exist
	 * @throws FontInitializationFailedException if index file can't be read.
	 */
	public static synchronized void initialize() throws FileNotFoundException, FontInitializationFailedException {
		Class cl = BBSSHFontManager.class;
		me = (BBSSHFontManager) Version.createOSObjectInstance(cl.getName());
		me.loadFontData();

	}

	/**
	 * Return the requested font. If unsupported on the current platform or if the requested font doesn't exist, then
	 * FontNotFoundException is thrown.
	 * 
	 * @param request
	 * @return the requested font instance
	 * @throws FontNotFoundException
	 */
	public Font getTruetypeFont(FontSettings request) throws FontNotFoundException {
		throw new FontNotFoundException();
	}

	/**
	 * Get the bitmap font that matches the requested font
	 * 
	 * @param request
	 * @return requested bitmap font, if found
	 * @throws FontNotFoundException
	 */
	public BitmapFont getBitmapFont(FontSettings request) throws FontNotFoundException {
		int id = request.getFontId();
		if (id < 0 || id > bitmapFonts.length - 1) {
			throw new FontNotFoundException();
		}
		return bitmapFonts[id].getFont(request.getFontSize());
	}

	// public Font getNativeFont(FontSettings request) {
	// FontFamily.getFontFamilies();
	// }

	/**
	 * Get list of available custom truetype fonts
	 * 
	 * @return array of fonts
	 */
	public Font[] getTTFonts() {
		return truetypeFonts;
	}

	/**
	 * Return supported fonts.
	 * 
	 * @return array of font data about supported bitmap fonts.
	 */
	public BitmapFontData[] getBitmapFonts() {
		return bitmapFonts;

	}

	/**
	 * Return an appropriate FontRenderer based on the options provided
	 * 
	 * @param settings font configuration options.
	 * @return font renderer instance
	 * @throws FontNotFoundException if the font could not be loaded
	 */
	public FontRenderer getRenderer(FontSettings settings) throws FontNotFoundException {

		FontRenderer renderer = null;
		if (settings.getFontType() == FontSettings.FONT_BITMAP) {
			renderer = new BitmapFontRenderer(settings);
		}
		return renderer;
	}

	/**
	 * @return true if this platform version supports truetype fonts.
	 */
	public boolean areTruetypeFontsSupported() {
		return false;
	}

	/**
	 * @return truetype font names
	 */
	public String[] getTTFontNames() {
		return this.truetypeFontNames;
	}

	public BitmapFontData getBitmapFontData(int fontId) throws FontNotFoundException {
		if (fontId < 0 || fontId > bitmapFonts.length - 1) {
			throw new FontNotFoundException();
		}
		return bitmapFonts[fontId];
	}
	// @todo - okay this is a mess - we should be doing : 
	// FontData
	//  -> FontRecord 
	//  -> BitmapFont
	
}
