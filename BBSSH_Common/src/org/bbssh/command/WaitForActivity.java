package org.bbssh.command;

import org.bbssh.keybinding.ExecutableCommand;
import org.bbssh.net.session.SessionDataListener;
import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.session.SessionManager;
import org.bbssh.util.Logger;

/**
 * THis class is intended for use in macros. It allows you to stop macro execution until activity occurs in the session.
 * 
 * This is currently unimplemented and exists as a reminder to finish it. It will require an activityListener interface
 * implemnetation, and separate thread for macro monitoring and execution...
 * 
 * @author marc
 * 
 */
public class WaitForActivity extends ExecutableCommand implements SessionDataListener {

	Object notifier = new Object();
	RemoteSessionInstance rsi;

	// @todo - major problem ehre. WE have only a single instance of data

	public boolean execute(RemoteSessionInstance rsi, Object parameter) {
		if (parameter != null && (parameter instanceof Integer))
			return false;
		this.rsi = rsi;

		try {
			synchronized (notifier) { // this command itself will be executed on a separate thread ...
				// Get notified when we next receive data
				// @todo possible deadlock here - what if we receive data after registering but before
				// entering wait state? If that was the last data to be received on the channel,
				// we'll never stop executing.
				rsi.session.registerDataListener(this);
				if (parameter instanceof Integer) {
					Thread.sleep(((Integer) parameter).intValue());
				}
				notifier.wait();
			}
		} catch (InterruptedException e) {
			// Okay, that's really all we wanted - now the owning macro can proceed with execution.
			Logger.info("WaitForActivity command interrupted");
		} finally {
			rsi.session.unregisterDataListener(this);
		}
		return true;
	}

	public void onDataReceived(int sessionId, String data) {
		if (this.rsi == SessionManager.getInstance().getSession(sessionId)) {
			synchronized (notifier) {
				notifier.notify();
			}
		}
	}

	public int getDescriptionResId() {
		return CMD_DESC_WAIT_ACTIVITY;
	}

	public int getId() {
		return CommandConstants.WAIT_FOR_ACTIVITY;
	}

	public int getNameResId() {
		return CMD_NAME_WAIT_ACTIVITIY;
	}

	public boolean isConnectionRequired() {
		return true;
	}

	public boolean isKeyBindable() {
		return false;
	}

	public boolean isMacroAction() {
		return true;
	}

	public boolean isParameterRequired() {
		return true;
	}

	public String translateParameter(Object parameter) {
		if (parameter == null) {
			return " no delay";
		}
		return "(delayed " + ((Integer) parameter).intValue() + " ms.)";
	}

	public int getNotifyBehavior() {
		return NOTIFY_BEHAVIOR_NAME_ONLY;
	}

}
