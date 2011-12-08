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
package org.bbssh.ui.components;

import java.util.Vector;

import net.rim.device.api.i18n.ResourceBundleFamily;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import org.bbssh.i18n.BBSSHResource;
import org.bbssh.ssh.SSHPrompt;

/**
 *
 */
public final class SSHPromptScreen extends PopupScreen implements BBSSHResource, FieldChangeListener {
	private boolean okPressed;
	ResourceBundleFamily res = ResourceBundleFamily.getBundle(BUNDLE_ID, BUNDLE_NAME);
	private Vector prompts;
	private String[] responses;
	private ClickableButtonField submitButton;

	public SSHPromptScreen(String name, String instruction, Vector prompts, String defaultPassword) {
		super(new VerticalFieldManager(VERTICAL_SCROLL | VERTICAL_SCROLLBAR), DEFAULT_CLOSE);

		// setTitle();
		add(new LabelField(res.getString(SESSION_PROMPT_TITLE) + name));
		if (instruction.length() > 0) {
			add(new LabelField(instruction));
		}
		this.prompts = prompts;
		int count = prompts.size();
		for (int x = 0; x < count; x++) {
			add(((SSHPrompt) prompts.elementAt(x)).getEditField());
		}
		add(new SeparatorField());
		submitButton = new ClickableButtonField(res.getString(GENERAL_LBL_SUBMIT));
		submitButton.setChangeListener(this);
		add(submitButton);
	}

	/**
	 * Displays this as a modal window. After completion, you can call getValues
	 * to retrieve user-entered prompt answers.
	 */
	public void doModal() {
		// @todo should we force this onto the main event thread? This
		// could potentially come in from the comm thread...
		UiApplication.getUiApplication().invokeAndWait(new Runnable() {
			public void run() {
				int count = prompts.size();
				responses = new String[count];
				if (count > 0) {
					UiApplication.getUiApplication().pushModalScreen(SSHPromptScreen.this);
					if (okPressed) {
						for (int i = 0; i < count; i++) {
							responses[i] = ((SSHPrompt) prompts.elementAt(i)).getAnswer();
						}
					} else {
						responses = new String[0];
					}
				}
			}
		});

	}

	public String[] getResponses() {
		return responses;
	}

	public void fieldChanged(Field field, int context) {
		if (field == submitButton) {
			okPressed = true;
			UiApplication.getUiApplication().popScreen(this);
		}
	}

}
