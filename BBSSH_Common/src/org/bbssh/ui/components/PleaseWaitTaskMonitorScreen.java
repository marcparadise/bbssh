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

import org.bbssh.patterns.TaskUpdateListener;
import org.bbssh.patterns.UpdatingBackgroundTask;

import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

/**
 * Display this message when performing a backgruond operation. This does permit the user to cancel using the Cancel
 * button.
 * 
 */
public class PleaseWaitTaskMonitorScreen extends PopupScreen
		implements TaskUpdateListener {
	UpdatingBackgroundTask task;
	private final LabelField message;
	private boolean allowCancel;

	/**
	 * Constructor for this screen
	 * 
	 * 
	 * @param task task to execute upon invocation of launch()
	 * @param allowCancel if true, user will be permitted to cancel the operation via the ESC key. (not supported yet)
	 */
	public PleaseWaitTaskMonitorScreen(UpdatingBackgroundTask task, boolean allowCancel) {
		super(new VerticalFieldManager(VERTICAL_SCROLL | VERTICAL_SCROLLBAR), DEFAULT_CLOSE);
		this.task = task;
		task.setListener(this);
		this.allowCancel = allowCancel;
		message = new LabelField("Initializing.");
		add(message);

	}

	/**
	 * Constructor for this screen that causes screen to disallow cancels.
	 * 
	 * @param task task to execute upon invocation of launch()
	 */
	public PleaseWaitTaskMonitorScreen(UpdatingBackgroundTask task) {
		this(task, false);
	}

	protected boolean keyDown(int keycode, int time) {
		// @todo support allowCancel flag - and a cancel button?
		if (allowCancel) {
			if (Keypad.key(keycode) == Keypad.KEY_ESCAPE) {
				task.setCanceled();
				return true;
			}
		}
		return super.keyDown(keycode, time);
	}

	/**
	 * DIsplays the screen and launches the background task.
	 */
	public void launch() {
		Thread thread = new Thread(task, "PlWait");
		thread.start();
		UiApplication.getUiApplication().pushModalScreen(this);
	}

	/**
	 * This is invoked by the executing thread, and is used to update the UI with received info.
	 * 
	 * @param text
	 * @param progressValue
	 */
	public void taskUpdate(final String text, int progressValue) {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				message.setText(text);
			}
		});
	}

	/**
	 * Notification from our executing thread that we have begun execution.
	 */
	public void taskBegin() {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				message.setText("Processing...");
			}
		});

	}

	public void taskComplete() {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				UiApplication.getUiApplication().
						popScreen(PleaseWaitTaskMonitorScreen.this);
			}
		});
	}
}
