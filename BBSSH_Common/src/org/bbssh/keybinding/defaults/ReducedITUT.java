package org.bbssh.keybinding.defaults;

import net.rim.device.api.ui.Keypad;

import org.bbssh.command.CommandConstants;
import org.bbssh.i18n.BBSSHResource;
import org.bbssh.keybinding.KeyBindingHelper;
import org.bbssh.model.KeyBindingManager;
import org.bbssh.platform.PlatformServicesProvider;
import org.bbssh.terminal.VT320;
import org.bbssh.util.Tools;

public class ReducedITUT implements DefaultKeybindingSet {

	public void bindKeys(KeyBindingManager mgr, PlatformServicesProvider psp) {

		mgr.bindKey(Keypad.KEY_SHIFT_X, 0, CommandConstants.TOGGLE_LOCAL_LSHIFT);
		mgr.bindKey(KeyBindingHelper.KEY_ITUT_0, 0, CommandConstants.SEND_TERMINAL_KEY, VT320.VK_SPACE);
		mgr.bindKey(KeyBindingHelper.KEY_ITUT_1, 0, CommandConstants.SHOW_OVERLAY_INPUT);
		mgr.bindKey(KeyBindingHelper.KEY_ITUT_2, 0, CommandConstants.SEND_TERMINAL_KEY, VT320.VK_TAB);
		mgr.bindKey(KeyBindingHelper.KEY_ITUT_4, 0, CommandConstants.SEND_TERMINAL_KEY, VT320.VK_ESCAPE);

		mgr.bindKey(KeyBindingHelper.KEY_ITUT_7, 0, CommandConstants.TOGGLE_ALT);
		mgr.bindKey(KeyBindingHelper.KEY_ITUT_9, 0, CommandConstants.TOGGLE_LOCAL_ALT);
		mgr.bindKey(KeyBindingHelper.KEY_ITUT_8, 0, CommandConstants.TOGGLE_LOCAL_RSHIFT);


		String msg = Tools.getStringResource(BBSSHResource.TERMINAL_MSG_KEY_UNBOUND);
		mgr.bindKey(KeyBindingHelper.KEY_ITUT_3, 0, CommandConstants.SHOW_DEBUG_MESSAGE, msg);
		mgr.bindKey(KeyBindingHelper.KEY_ITUT_5, 0, CommandConstants.SHOW_DEBUG_MESSAGE, msg);
		mgr.bindKey(KeyBindingHelper.KEY_ITUT_6, 0, CommandConstants.SHOW_DEBUG_MESSAGE, msg);
		
	}

}
