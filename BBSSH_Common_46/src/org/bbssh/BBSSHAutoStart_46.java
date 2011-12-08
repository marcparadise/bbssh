package org.bbssh;

import net.rim.blackberry.api.messagelist.ApplicationIcon;
import net.rim.blackberry.api.messagelist.ApplicationIndicatorRegistry;
import net.rim.device.api.system.EncodedImage;

import org.bbssh.model.SettingsManager;

public class BBSSHAutoStart_46 extends BBSSHAutoStart {
	public BBSSHAutoStart_46() { 
		super();
		
	}
	protected void init() {
		super.init();
		if (SettingsManager.getSettings().isHomeScreenNotificationIconEnabled()) {
			// Register application icon.
			ApplicationIndicatorRegistry reg = ApplicationIndicatorRegistry.getInstance();
			reg.register(new ApplicationIcon(EncodedImage.getEncodedImageResource("small.png")), false, false);
		}
		if (SettingsManager.getSettings().isMessageIntegrationEnabled()) {
			// @todo message list integration
			
		}
	}

}
