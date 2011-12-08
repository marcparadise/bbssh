/*
 *  Copyright (C) 2010 Marc A. Paradise
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.bbssh.keybinding;

import net.rim.device.api.i18n.ResourceBundle;

import org.bbssh.i18n.BBSSHResource;

/**
 * Simple class used to encapsulate a remote-terminal keystrokes
 */
public final class TerminalKey {
	private Integer value;
	private int resId;

	public TerminalKey(int value, int resId) {
		this.value = new Integer(value);
		this.resId = resId;
	}

	public String toString() {
		return ResourceBundle.getBundle(BBSSHResource.BUNDLE_ID, BBSSHResource.BUNDLE_NAME).getString(resId);
	}

	public Integer getValue() {
		return value;
	}
 
	public int getIntValue() {
		if (value == null) {
			return 0;
		}
		return value.intValue();
	}

}
