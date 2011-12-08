/**
 * Copyright (c) 2010 Marc A. Paradise
 *
 * This file is part of "BBSSH"
 *
 * BBSSH is based upon MidpSSH by Karl von Randow.
 * MidpSSH was based upon Telnet Floyd and FloydSSH by Radek Polak.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

package org.bbssh.terminal.fonts;

import net.rim.device.api.io.FileNotFoundException;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.FontFamily;
import net.rim.device.api.ui.FontManager;

import org.bbssh.exceptions.FontNotFoundException;
import org.bbssh.model.FontSettings;
import org.bbssh.util.Logger;
import org.bbssh.util.Tools;

/**
 * Adds truetype font support over the base implementation.
 * 
 */
public class BBSSHFontManager_50 extends BBSSHFontManager {
	public Font getTruetypeFont(FontSettings request) throws FontNotFoundException {
		int idx = request.getFontId();
		if (idx < 0 || idx >= truetypeFontNames.length) {
			// @todo -description here would help: bad font name
			throw new FontNotFoundException();
		}
		// @todo a simple wrapper class to aggregate name info would be cleaner.
		Font font = truetypeFonts[idx];
		if (font == null) {
			try {
				String name = truetypeFontNames[idx];
				// Note - under OS7 we're seeing RESOURCE_MISSING return code - however 
				// under earlier versions we were getting other than SUCCESS even when it succeeded. 
				FontManager.getInstance().load(	Tools.getResourceInputStream(name + ".ttf"), name, FontManager.APPLICATION_FONT);
				
				FontFamily family = FontFamily.forName(name);
				if (family == null) {
					throw new FontNotFoundException();
				}

				// Retrieved size doesn't matter, we'll derive the requested size in points.
				font = family.getFont(Font.PLAIN, 10);

				if (font == null) {
					throw new FontNotFoundException();
				}
				truetypeFonts[idx] = font;
			} catch (ClassNotFoundException e) {
				throw new FontNotFoundException();
			} catch (IllegalArgumentException e) {
				Logger.fatal("Could not load TT font " + request.getFontId(), e);
				throw new FontNotFoundException();
			} catch (FileNotFoundException e) {
				Logger.fatal("Could not load TT font " + request.getFontId(), e);
				throw new FontNotFoundException();
			}
		}
		// @todo - more advance style is possible with these fonts now, including
		// italic bold, AA, etc. Erm, how do we enable/disable AA? Can we?
		// @todo - size limits don't matter for truetype. Don't limit them...

		return font.derive(Font.PLAIN, request.getFontSize());
	}

	/**
	 * Return an appropriate FontRenderer based on the options provided. This override will provide a truetype renderer if appropriate. 
	 * 
	 * @param settings font configuration options.
	 * @return font renderer instance
	 * @throws FontNotFoundException if the font could not be loaded
	 */
	public FontRenderer getRenderer(FontSettings settings) throws FontNotFoundException {
		FontRenderer renderer = super.getRenderer(settings);
		if (renderer == null && settings.getFontType() == FontSettings.FONT_TT) {
			renderer = new TruetypeFontRenderer(settings);
		}
		return renderer;
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.bbssh.terminal.fonts.BBSSHFontManager#areTruetypeFontsSupported()
	 */
	public boolean areTruetypeFontsSupported() {
		return true;
	}

}
