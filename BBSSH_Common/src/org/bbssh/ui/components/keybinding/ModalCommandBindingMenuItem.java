package org.bbssh.ui.components.keybinding;

import net.rim.device.api.ui.UiApplication;

public class ModalCommandBindingMenuItem extends CommandBindingMenuItem {
	public ModalCommandBindingMenuItem(long commandId, int ordinal, int priority) {
		super(commandId, ordinal, priority);
	}

	public ModalCommandBindingMenuItem(int resId, long commandId, int ordinal, int priority) {
		super(resId, commandId, ordinal, priority);
	}

	public void run() {
		UiApplication.getUiApplication().invokeAndWait(new Runnable() {
			public void run() {
				ModalCommandBindingMenuItem.super.run();
			}

		});

	}

}
