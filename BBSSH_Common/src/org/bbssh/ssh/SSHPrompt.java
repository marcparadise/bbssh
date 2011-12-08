package org.bbssh.ssh;

import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.PasswordEditField;

public class SSHPrompt {
	BasicEditField editField;

	public SSHPrompt(String prompt, boolean echo, String defaultText) {
		if (echo) {
			editField = new BasicEditField(prompt, defaultText);
		} else {
			editField = new PasswordEditField(prompt, defaultText);
		}
	}

	public SSHPrompt(String prompt, boolean echo) {
		this(prompt, echo, "");
	}

	public BasicEditField getEditField() {
		return editField;
	}

	public String getAnswer() {
		if (editField != null) {
			return editField.getText();
		}
		return "";
	}
}
