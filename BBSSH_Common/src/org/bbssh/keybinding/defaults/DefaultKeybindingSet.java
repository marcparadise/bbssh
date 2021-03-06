package org.bbssh.keybinding.defaults;

import org.bbssh.model.KeyBindingManager;
import org.bbssh.platform.PlatformServicesProvider;

public interface DefaultKeybindingSet {
	public void bindKeys(KeyBindingManager mgr, PlatformServicesProvider psp);

}
