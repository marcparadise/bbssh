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
package org.bbssh.keybinding;

import net.rim.device.api.i18n.ResourceBundleFamily;

import org.bbssh.i18n.BBSSHResource;
import org.bbssh.session.RemoteSessionInstance;

/**
 * Abstract class that encapsulates a discrete action that can be taken while connected to an active emulation session.
 * 
 */
public abstract class ExecutableCommand implements BBSSHResource {
	protected ResourceBundleFamily res = ResourceBundleFamily.getBundle(BUNDLE_ID, BUNDLE_NAME);
	public static final int PARAM_TYPE_NONE = 0;
	public static final int PARAM_TYPE_TERM_KEY = 1;
	public static final int PARAM_TYPE_ALPHANUM = 2;
	public static final int PARAM_TYPE_INT = 3;
	public static final int PARAM_TYPE_DIRECTIONAL_KEY = 4;

	protected static final int NOTIFY_BEHAVIOR_NONE = 0;
	protected static final int NOTIFY_BEHAVIOR_NAME_ONLY = 1;
	protected static final int NOTIFY_BEHAVIOR_VALUE_ONLY = 2;
	protected static final int NOTIFY_BEHAVIOR_NAME_AND_VALUE = 3;

	/**
	 * gets the unique ID that represents this command.
	 * 
	 * @return unique command id
	 */
	abstract public int getId();

	/**
	 * Required method that performs any work associated with with this command.
	 * 
	 * If you maintain any state data in your ExecutableCommand you must manage synchronization.
	 * 
	 * This is likely to become protected in the future, and will be invoked only by the framework after validations are
	 * complete.
	 * 
	 * @param inst session against which this command will be applied.
	 * @param parameter additional parameter if required. implementors must check to ensure this is valid and of
	 *            required data type.
	 * @return true if successfully handled.
	 * 
	 *         #see net.rim.device.api.system.KeypadListener
	 */
	public abstract boolean execute(RemoteSessionInstance inst, Object parameter);

	/**
	 * Implement this method to provide a user-friendly (and localized) short name of a command.
	 * 
	 * @return red-id of a user-friendly name
	 */
	public abstract int getNameResId();

	/**
	 * Override this method to provide a user-friendly (and localized) long description of your command.
	 * 
	 * @return res-id of the description of this command.
	 */
	public abstract int getDescriptionResId();

	/**
	 * Return true if you require a valid argument.
	 */
	public abstract boolean isParameterRequired();

	/**
	 * Returns true if this accepts a parameter that is entirely optional.
	 * 
	 * @return true if optional parameter is accepted.
	 */
	public boolean isParameterOptional() {
		return false;
	}

	/**
	 * provide displayable name of this command.
	 */
	public String toString() {
		return res.getString(getNameResId());
	}

	/**
	 * Invoke this to determine if a command can be bound by the user as a keystroke-based command.
	 * 
	 * @return true if the command is available for key binding.
	 */
	public abstract boolean isKeyBindable();

	/**
	 * Invoke this to determine if a command can be used as a macro action.
	 * 
	 * @return true if this is available as a macro action.
	 */
	public abstract boolean isMacroAction();

	/**
	 * Return a translated description of the parameter value passed in. This is used in key bindings, for displaying
	 * proper name.
	 * 
	 * @param parameter
	 * @return translation of the parameter value. "N/A" if no translation found or applicable.
	 */
	public String translateParameter(Object parameter) {
		if (parameter instanceof String) {
			return (String) parameter;
		}

		if (parameter instanceof Integer || parameter instanceof Long) {
			return parameter.toString();
		}
		return "";
	}

	public boolean equals(Object compareTo) {
		if (compareTo == null)
			return false;
		if (!(compareTo instanceof ExecutableCommand)) {
			return false;
		}
		if (((ExecutableCommand) compareTo).getId() == getId()) {
			return true;
		}
		return false;
	}

	// @todo can we look at marker interfaces instead of these various "isConnectionRequired/et al)

	/**
	 * Override this to control whether a given command is available for current platform (hardware + software)
	 * implementation.
	 * 
	 * @return true if available.
	 */
	public boolean isAvailableOnCurrentPlatform() {
		return true;
	}

	/**
	 * @return true if a connection must be
	 */
	public abstract boolean isConnectionRequired();

	// @todo : validate(final Object parameter)
	// @todo : getParameterType(int pos)

	/**
	 * @return true if this command requires a UI lock. Thi sis generally necessary if the command will be presenting
	 *         modifying the UI in any way (such as changing which screen is displayed, showing a menu, popup, etc).
	 */
	public boolean isUILockRequired() {
		return false;
	}

	/**
	 * 
	 * @return a NOTIFY_BEHAVIOR_* const that describes what shoudl be displayed on the terminal when this command is
	 *         executed
	 */
	public int getNotifyBehavior() {
		return NOTIFY_BEHAVIOR_NONE;
	}

}
