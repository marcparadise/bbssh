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
import net.rim.device.api.synchronization.SyncObject;
import net.rim.device.api.synchronization.UIDGenerator;

import org.bbssh.i18n.BBSSHResource;
import org.bbssh.model.DataObject;
import org.bbssh.model.KeyBindingManager;

/**
 * This simple factory exists to avoid complications around storing complex objects. in the persistent story.
 */
public class PersistableCommandFactory implements SyncObject, DataObject {
	private long boundTo; // ideally, we'd use this for ID since it is unique. but ID must be an int.
	private int executableCommandId;
	private Object param;
	private long id;
	private boolean syncDirty;
	private String name;
	BoundCommand cmd = null;

	public PersistableCommandFactory(PersistableCommandFactory source) {
		this.boundTo = source.boundTo;
		this.executableCommandId = source.executableCommandId;
		// @todo - this is going to need better support using getParameterType... currently
		// we only supportLong, string, Integer but additional types can introduce further problems
		if (source.param instanceof String) {
			param =  source.param.toString();
		} else if (source.param instanceof Integer) {
			param = new Integer(((Integer) source.param).intValue());
		} else if (source.param instanceof Long) {
			param = new Long(((Long) source.param).longValue());
		} else if (source.param instanceof Short) {
			param = new Short(((Short) source.param).shortValue());
		} else if (source.param instanceof Double) {
			param = new Float(((Float) source.param).floatValue());
		} else if (source.param instanceof Float) {
			param = new Double(((Double) source.param).doubleValue());
		} else if (source.param instanceof Byte) {
			param = new Byte(((Byte) source.param).byteValue());
		}
		// @todo - if this is a numeric object ... we've a problem here as changing the value
		this.id = UIDGenerator.getUID();
		this.syncDirty = true;
		this.cmd = null;
	}

	public PersistableCommandFactory(long boundTo, int executableCommandId) {
		this(boundTo, executableCommandId, null);
	}

	public PersistableCommandFactory(long boundTo, int executableCommandId, Object param) {
		this.boundTo = boundTo;
		this.executableCommandId = executableCommandId;
		this.param = param;
		this.id = UIDGenerator.getUID();
	}

	public PersistableCommandFactory(int id) {
		this.id = id;
	}

	public synchronized BoundCommand getBoundCommandInstance() {
		if (cmd == null) {
			cmd = new BoundCommand(KeyBindingManager.getInstance().getExecutableCommandById(executableCommandId), param);
		}
		return cmd;
	}

	public int getUID() {
		return (int) id;
	}

	public int getExecutableCommandId() {
		return this.executableCommandId;
	}

	public void setExecutableCommandId(int executableCommandId) {
		this.executableCommandId = executableCommandId;
		this.name = null;
	}

	public Object getParam() {
		return this.param;
	}

	public void setParam(Object param) {
		this.param = param;
		this.name = null;
	}

	public long getBoundTo() {
		return this.boundTo;
	}

	public void setBoundTo(long boundTo) {
		this.boundTo = boundTo;
	}

	public boolean isSyncStateDirty() {
		return syncDirty;
	}

	public void setSyncStateDirty(boolean dirty) {
		syncDirty = dirty;
	}

	public String toString() {
		if (name == null) {
			ExecutableCommand cmd = KeyBindingManager.getInstance().
					getExecutableCommandById(getExecutableCommandId());
			StringBuffer buffer = new StringBuffer();
			buffer.append(ResourceBundle.getBundle(BBSSHResource.BUNDLE_ID, BBSSHResource.BUNDLE_NAME)
					.getString(cmd.getNameResId()))
					.append(" ")
					.append(cmd.translateParameter(getParam()));
			name = buffer.toString();
		}
		return name;

	}

}
