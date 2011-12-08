/**
 * Copyright (c) 2010 Marc A. Paradise
 *
 * This file is part of "BBSSH"
 *
 * BBSSH is based upon MidpSSH by Karl von Randow.
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

package org.bbssh.model;

import java.util.Vector;

import org.bbssh.command.CommandConstants;
import org.bbssh.keybinding.PersistableCommandFactory;

import net.rim.device.api.synchronization.SyncObject;
import net.rim.device.api.synchronization.UIDGenerator;

/**
 * 
 * @author marc
 */

public class Macro implements SyncObject, DataObject {
	Vector commands = new Vector();
	String name;
	int uid;
	private boolean syncDirty;

	public Macro(Macro source) {
		int len = source.commands.size();
		this.name = source.name;
		this.commands = new Vector(len);
		this.uid = UIDGenerator.getUID();
		for (int x = 0; x < len; x++) {
			commands.addElement(new PersistableCommandFactory((PersistableCommandFactory)source.commands.elementAt(x)));
		
		}
		this.syncDirty = true; 
	}

	public Macro() {
		uid = UIDGenerator.getUID();
	}

	public Macro(int uid) {
		this.uid = uid;
	}

	public Macro(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}

	public int getUID() {
		return uid;
	}

	public boolean isSyncStateDirty() {
		return syncDirty;
	}

	public void setSyncStateDirty(boolean dirty) {
		syncDirty = dirty;

	}

	public Vector getCommandVector() {
		return commands;
	}

	// @todo this could be done better by checking this ahead of time, when to see what (if any)
	/**
	 * @return true if this macro should be executed in a background thread.
	 */
	public boolean isExecutionDelayed() {
		int len = commands.size();
		if (len > 3)
			return true;

		for (int x = 0; x < len; x++) {
			int id = ((PersistableCommandFactory) commands.elementAt(x)).getExecutableCommandId();
			if (id == CommandConstants.WAIT || id == CommandConstants.WAIT_FOR_ACTIVITY)
				return true;

		}
		return false;

	}

}