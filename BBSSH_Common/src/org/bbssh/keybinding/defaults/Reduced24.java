package org.bbssh.keybinding.defaults;

import net.rim.device.api.ui.Keypad;

import org.bbssh.command.CommandConstants;
import org.bbssh.i18n.BBSSHResource;
import org.bbssh.keybinding.KeyBindingHelper;
import org.bbssh.model.KeyBindingManager;
import org.bbssh.platform.PlatformServicesProvider;
import org.bbssh.terminal.VT320;
import org.bbssh.util.Tools;

public class Reduced24 implements DefaultKeybindingSet  {

	public void bindKeys(KeyBindingManager mgr, PlatformServicesProvider psp) {
		mgr.bindKey(Keypad.KEY_SHIFT_X, 0, CommandConstants.TOGGLE_LOCAL_LSHIFT);
		mgr.bindKey(KeyBindingHelper.KEY_R24_QW, 0, CommandConstants.SHOW_OVERLAY_INPUT);
		mgr.bindKey(KeyBindingHelper.KEY_R24_0, 0, CommandConstants.SEND_TERMINAL_KEY, VT320.VK_SPACE);
		mgr.bindKey(KeyBindingHelper.KEY_R24_CV, 0, CommandConstants.TOGGLE_ALT);
		mgr.bindKey(KeyBindingHelper.KEY_R24_BN, 0, CommandConstants.TOGGLE_LOCAL_RSHIFT);
		mgr.bindKey(KeyBindingHelper.KEY_R24_ER, 0, CommandConstants.SEND_TERMINAL_KEY, VT320.VK_ESCAPE);
		mgr.bindKey(KeyBindingHelper.KEY_R24_TY, 0, CommandConstants.SEND_TERMINAL_KEY, VT320.VK_TAB);
		

		String msg = Tools.getStringResource(BBSSHResource.TERMINAL_MSG_KEY_UNBOUND);
		mgr.bindKey(KeyBindingHelper.KEY_R24_UI, 0, CommandConstants.SHOW_DEBUG_MESSAGE, msg);
		mgr.bindKey(KeyBindingHelper.KEY_R24_OP, 0, CommandConstants.SHOW_DEBUG_MESSAGE, msg);
		mgr.bindKey(KeyBindingHelper.KEY_R24_AS, 0, CommandConstants.SHOW_DEBUG_MESSAGE, msg);
		mgr.bindKey(KeyBindingHelper.KEY_R24_DF, 0, CommandConstants.SHOW_DEBUG_MESSAGE, msg);
		mgr.bindKey(KeyBindingHelper.KEY_R24_GH, 0, CommandConstants.SHOW_DEBUG_MESSAGE, msg);
		mgr.bindKey(KeyBindingHelper.KEY_R24_JK, 0, CommandConstants.SHOW_DEBUG_MESSAGE, msg);
		mgr.bindKey(KeyBindingHelper.KEY_R24_L, 0, CommandConstants.SHOW_DEBUG_MESSAGE, msg);
		mgr.bindKey(KeyBindingHelper.KEY_R24_ZX, 0, CommandConstants.SHOW_DEBUG_MESSAGE, msg);
		
	}

}
