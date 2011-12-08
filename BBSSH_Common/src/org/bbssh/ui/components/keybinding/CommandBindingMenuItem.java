package org.bbssh.ui.components.keybinding;

import net.rim.device.api.i18n.ResourceBundleFamily;
import net.rim.device.api.ui.MenuItem;

import org.bbssh.i18n.BBSSHResource;
import org.bbssh.model.KeyBindingManager;
import org.bbssh.session.SessionManager;

public class CommandBindingMenuItem extends MenuItem {
	private long commandId;

	public CommandBindingMenuItem(long commandId, int ordinal, int priority) {
		super(KeyBindingManager.getInstance().getExecutableCommandById(commandId).toString(), ordinal, priority);
		this.commandId = commandId;
	}

	public CommandBindingMenuItem(int resId, long commandId, int ordinal, int priority) {
		super(ResourceBundleFamily.getBundle(BBSSHResource.BUNDLE_ID, BBSSHResource.BUNDLE_NAME), resId, ordinal,
				priority);
		this.commandId = commandId;
	}

	public void run() {
		KeyBindingManager.getInstance().getExecutableCommandById(getCommandId())
				.execute(SessionManager.getInstance().activeSession, null);
	}

	public long getCommandId() {
		return commandId;
	}

}
