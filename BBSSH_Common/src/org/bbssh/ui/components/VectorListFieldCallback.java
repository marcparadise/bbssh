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

package org.bbssh.ui.components;

import java.util.Vector;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;

/**
 * This list field callback allows you to register the owning form as a callbaack handler when selections are made in
 * the list, which is populated based on the provided vector.
 */
public class VectorListFieldCallback implements ListFieldCallback {
	private final Vector data;

	
	/**
	 * Constructor for listfield callback that redners data from a vector.
	 * 
	 * @param data a vector containing the rows to display.
	 */
	public VectorListFieldCallback(Vector data) {
		this.data = data;
	}

	public Vector getVector() { 
		return data; 
	}
	public void drawListRow(ListField listField, Graphics graphics, int index, int y, int width) {
		Object x = get(listField, index);
		if (x != null) {
			graphics.drawText(x.toString(), 0, y, 0, width);
		}
	}

	public Object get(ListField listField, int index) {
		if (index < data.size()) {
			return data.elementAt(index);
		}
		return null;
	}

	public int indexOfList(ListField listField, String prefix, int start) {
		int count = data.size();
		for (int x = 0; x < count; x++) {
			Object d = data.elementAt(x);
			if (d != null && d.toString().startsWith(prefix)) {
				return x;
			}
		}
		return -1;
	}

	public int getPreferredWidth(ListField listField) {
		return Display.getWidth();
		// s/b widest item in list 
	}
}
