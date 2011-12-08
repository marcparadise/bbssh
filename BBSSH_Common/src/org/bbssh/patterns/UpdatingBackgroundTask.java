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
package org.bbssh.patterns;

/**
 * Task that runs in the backgruond an provides updates via a
 * registered TaskUpdateListener interface.
 */
public abstract class UpdatingBackgroundTask implements Runnable {
	private TaskUpdateListener listener;
	private boolean canceled;

	/**
	 * Constructor for a task
	 */
	public UpdatingBackgroundTask() {
	}

	public void setListener(TaskUpdateListener listener) {
		this.listener = listener;
	}

	public void updateListener(String message) {
		if (listener != null) {
			listener.taskUpdate(message, 0);
		}
	}

	public void updateListener(String message, int progressValue) {
		if (listener != null) {
			listener.taskUpdate(message, progressValue);
		}
	}

	public synchronized boolean isCanceled() {
		return canceled;
	}

	public synchronized void setCanceled() {
		canceled = true;
	}

	public void run() {
		if (!isCanceled()) {
			if (listener != null) {
				listener.taskBegin();
			}
		}
		if (!isCanceled()) {
			execute();
		}
		if (listener != null) {
			listener.taskComplete();
		}
	}

	/**
	 * Invoked when this tasks is run as a thread, execute performs the
	 * work in question.  You may optionally invoke updateListener to provide
	 * status updates to the registerd listener as you progress.
	 *
	 * You must check isCanceled() for any lengthy operation, since the
	 * executor for this thread may have the ability to request a cancel.
	 */
	abstract public void execute();
}
