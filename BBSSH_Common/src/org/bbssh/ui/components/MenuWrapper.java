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

package org.bbssh.ui.components;
import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.Menu;

/**
 * A simple wrapper to contain Menu and ContextMenu,
 * so that we don't need to constantly code duplicated menu items in
 * makeContextMenu vs makeMenu
 */
public class MenuWrapper {
	private Menu menu;
	private ContextMenu contextMenu;
	public MenuWrapper(Menu menu) {
		this.menu = menu;
	}
	public MenuWrapper(ContextMenu contextMenu) {
		this.contextMenu = contextMenu;
	}
	public void addItem(MenuItem item) {
		if (contextMenu == null) {
			menu.add(item);
		} else {
			contextMenu.addItem(item);
		}
	}
	public void setDefault(MenuItem item) {
		if (contextMenu == null) {
			menu.setDefault(item);
		} else {
			contextMenu.setDefaultItem(item);
		}
	}
}
