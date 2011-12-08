package org.bbssh.session;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.i18n.FieldPosition;
import net.rim.device.api.i18n.MessageFormat;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.SystemListener2;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.util.IntHashtable;

import org.bbssh.BBSSHApp;
import org.bbssh.exceptions.FontNotFoundException;
import org.bbssh.i18n.BBSSHResource;
import org.bbssh.model.ConnectionProperties;
import org.bbssh.model.Key;
import org.bbssh.model.KeyManager;
import org.bbssh.net.session.SessionListener;
import org.bbssh.net.session.SshSession;
import org.bbssh.net.session.TelnetSession;
import org.bbssh.notifications.NotificationManager;
import org.bbssh.platform.PlatformServicesProvider;
import org.bbssh.terminal.TerminalStateData;
import org.bbssh.ui.components.KeyPasswordPrompt;
import org.bbssh.ui.screens.PasswordPromptPopup;
import org.bbssh.ui.screens.TerminalScreen;
import org.bbssh.util.Logger;
import org.bbssh.util.Tools;
import org.bbssh.util.Version;

/**
 * This class manages active connections/sessions and their screens.
 */
public class SessionManager implements SystemListener2, SessionListener {
	ResourceBundle bundle = ResourceBundle.getBundle(BBSSHResource.BUNDLE_ID, BBSSHResource.BUNDLE_NAME);
	String tempPass;
	boolean forceShutdown = false;
	private TerminalScreen terminal = null;
	public RemoteSessionInstance activeSession;
	private int nextSessionId = 0;
	private IntHashtable sessions = new IntHashtable();
	// Future expansion --- allows us to have multiple conneced session interfaces.
	private Hashtable sessionsForProperties = new Hashtable();

	private static SessionManager me;

	private SessionManager() {

	}

	/**
	 * Retrieve the one and only session manager instance
	 * 
	 * @return session manager instance
	 */
	public static SessionManager getInstance() {
		// technically should be synchronized, but our very first call is
		// executed safely -- and that's the one that will cause this initialization to
		// occur. We'll save the overhead on getInstance which will be used frequently.
		if (me == null) {
			me = new SessionManager();
		}
		return me;
	}

	/**
	 * Sets the active session by updating the terminal. Pushes term screen onto the display stack if it's not there -
	 * but will not uncovers it if it's buried.
	 * 
	 * @param session
	 */
	private synchronized void setActiveSessionImpl(RemoteSessionInstance session) {
		TerminalScreen s = getTerminalScreen();
		try {
			s.attachSession(session);
		} catch (FontNotFoundException e1) {
			Logger.error("FontNotFoundException in SessionManager.setActiveSessionImpl [ " + e1.getMessage() + " ] ");
			BBSSHApp.inst().invokeLater(new Runnable() {
				public void run() {
					Status.show(Tools.getStringResource(BBSSHResource.MSG_FONT_LOAD_FAILED));
				}
			});
			return;
		}
		try {
			if (s.isDisplayed()) {

			} else {
				UiApplication.getUiApplication().pushScreen(s);
			}
		} catch (Exception e) {
			Logger.fatal(e.getMessage() + "/" + e.toString());
		}

	}

	public synchronized void setActiveSession(final RemoteSessionInstance session) {
		activeSession = session;
		cancelNotifications(session);
		if (UiApplication.getUiApplication().isEventThread()) {
			setActiveSessionImpl(session);
		} else {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					setActiveSessionImpl(session);
				}
			});
		}
	}

	/**
	 * Switches display to another active session
	 * 
	 * @param switchTo
	 *            session to switch to.
	 */
	public synchronized void setActiveSession(int switchTo) {
		RemoteSessionInstance con = (RemoteSessionInstance) sessions.get(switchTo);
		if (con == null) {
			return;
		}
		setActiveSession(con);

	}

	/**
	 * Reconnects the provided session instance, if it is disconnected.
	 * 
	 * @param inst
	 *            a disconnected remote session instance
	 */
	public void reconnectSession(RemoteSessionInstance inst) {
		if (inst.isConnected()) {
			return; // can only reconnect disconnected
		}
		sessions.remove(inst.session.getSessionId());
		Vector v = (Vector) sessionsForProperties.get(inst.session.getProperties());
		v.removeElement(inst);
		connectSession(inst.session.getProperties());
	}

	private synchronized RemoteSessionInstance initiateSession(ConnectionProperties prop) {
		RemoteSessionInstance c = new RemoteSessionInstance();
		c.state = new TerminalStateData(prop);
		nextSessionId++;
		// New connections have extra work ahead...
		if (prop.getSessionType() == ConnectionProperties.SESSION_TYPE_SSH) {
			c.session = new SshSession(prop, nextSessionId, this);
		} else {
			c.session = new TelnetSession(prop, nextSessionId, this);
		}

		sessions.put(nextSessionId, c);
		Vector v = (Vector) sessionsForProperties.get(prop);
		if (v == null) {
			v = new Vector();
			sessionsForProperties.put(prop, v);
		}
		v.addElement(c);

		return c;
	}

	/**
	 * Creates an active session for specified session properties, or resumes it if it is already active. If multiple
	 * sessions are available, it will look for a connected one. It will resume teh first connected session; if none are
	 * connected, it will resume the first session (even if disconnected)
	 * 
	 * @param prop
	 *            session properties representing the session to activate.
	 */
	public void initiateOrResumeSession(ConnectionProperties prop) {
		RemoteSessionInstance rsi = getFirstConnectedSession(prop);
		if (rsi == null) {
			connectSession(initiateSession(prop));
		} else {
			setActiveSession(rsi);
		}

		Vector v = (Vector) sessionsForProperties.get(prop);
		if (v == null || v.size() == 0) {
			connectSession(prop);
		} else {
			setActiveSession((RemoteSessionInstance) v.elementAt(0));
		}

	}

	public void connectSession(ConnectionProperties prop) {
		connectSession(initiateSession(prop));
	}

	String tempUsername;

	// @todo - this doesn't belong here. Who shoudl actually push the passowrd
	// prompt
	// @todo - redundant with the generic-ish ssh prompt handling. A way to
	// consolidate?
	private void updateCredentials() {
		UiApplication.getUiApplication().invokeAndWait(new Runnable() {

			public void run() {
				PasswordPromptPopup prompt =
						new PasswordPromptPopup(bundle.getString(BBSSHResource.SESSION_PROMPT_USERNAME_PASS),
								tempUsername, false);
				if (prompt.show()) {
					tempUsername = prompt.getUsername();
					tempPass = prompt.getPassword();
				}
			}
		});
	}

	private void connectSession(RemoteSessionInstance c) {
		if (c == null)
			return;
		c.emulator = c.session.getEmulator();
		setActiveSession(c);

		if (!c.isConnected()) {
			// If password and username are blank; or password only is blank and
			// no key is set,
			// prompt for password.

			final ConnectionProperties prop = c.session.getProperties();
			tempUsername = prop.getUsername();
			tempPass = null;
			if (prop.getPassword() == null && (tempUsername == null || prop.getKeyId() == -1)) {
				updateCredentials();
			}
			c.session.setUserName(tempUsername);
			c.session.setPassword(tempPass);

			c.session.connect();
		}
		refreshConnectionList();
	}

	public void disconnectSession(int sessionId) {
		Logger.info("Disconnecting: " + sessionId);
		disconnectSession((RemoteSessionInstance) sessions.get(sessionId));

	}

	private void disconnectSession(final RemoteSessionInstance rsi, final boolean terminate) {
		if (rsi == null || rsi.session == null) {
			Logger.warn("Requested to disconnect session, but no session provided.");
			return;

		}
		try {
			rsi.state.suppressNotify = terminate;
			rsi.session.disconnect();
		} finally {
			// @todo - this shoudl be done in a background thread? Only a
			// concern if we ever have to blcok for a
			// significant period of time on our connectio mutex.
			if (terminate) {
				Vector v = (Vector) sessionsForProperties.get(rsi.session.getProperties());
				v.removeElement(rsi);
				sessions.remove(rsi.session.getSessionId());
				if (activeSession == rsi) {
					Logger.warn("Terminating active session.");
					activeSession = null;
					if (terminal != null && terminal.isDisplayed()) {
						Logger.warn("Forcing terminate pop.");
						UiApplication.getUiApplication().popScreen(terminal);
					}
				}
			}
			refreshConnectionList();
		}

	}

	/**
	 * Begins the disconnect process in a background thread.
	 * 
	 * @param rsi
	 */
	public void disconnectSession(final RemoteSessionInstance rsi) {
		disconnectSession(rsi, false);
	}

	/**
	 * Iterates through all managed sessions and returns the total which are currently connected.
	 * 
	 * @return number of connected sessions
	 */
	public boolean doesActiveSessionExist() {
		Enumeration e = sessions.elements();
		while (e.hasMoreElements()) {
			RemoteSessionInstance rsi = (RemoteSessionInstance) e.nextElement();
			if (rsi.isConnected()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return Enumeration of RemoteSessionInstance
	 */
	public Enumeration getAvailableSessions() {
		return sessions.elements();
	}

	public RemoteSessionInstance getSession(int sessionId) {
		return (RemoteSessionInstance) sessions.get(sessionId);
	}

	public void terminateAllSessions() {
		Enumeration e = sessions.elements();
		forceShutdown = true;
		while (e.hasMoreElements()) {
			((RemoteSessionInstance) e.nextElement()).session.disconnect();
		}
	}

	public void refreshConnectionList() {
		BBSSHApp.inst().getPrimaryScreen().refreshConnections();
	}

	public void notifyAppDeactivate() {
		refreshNotifications();
	}

	public synchronized TerminalScreen getTerminalScreen() {
		if (terminal == null) {
			String name = TerminalScreen.class.getName();
			terminal = (TerminalScreen) Version.createOSObjectInstance(name);
			;
		}
		return terminal;
	}

	// @todo does this listener belong in notificationmanager?
	public void backlightStateChange(boolean on) {
		if (on) {
			notifyAppActivate();
		} else {
			notifyAppDeactivate();
		}

	}

	public void cradleMismatch(boolean mismatch) {
	}

	public void fastReset() {
		terminateAllSessions();
	}

	public void powerOffRequested(int reason) {
	}

	public void usbConnectionStateChange(int state) {
	}

	public void batteryGood() {
	}

	public void batteryLow() {
	}

	public void batteryStatusChange(int status) {
	}

	public void powerOff() {
	}

	public void powerUp() {
	}

	/**
	 * For a given session, this will close it (if necessary) then remove it compeltely - rendering it inaccessible the
	 * user.
	 * 
	 * @param sessionId
	 */
	public void terminateSession(int sessionId) {

		terminateSession(getSession(sessionId));
	}

	public void terminateSession(final RemoteSessionInstance rsi) {
		if (rsi != null && rsi.session != null) {
			disconnectSession(rsi, true);
		}
	}

	public RemoteSessionInstance getFirstConnectedSession(ConnectionProperties prop) {
		Vector v = getSessions(prop);
		int size;
		if (v == null || ((size = v.size()) == 0)) {
			return null;
		}
		RemoteSessionInstance rsi = null;
		RemoteSessionInstance ret = null;
		for (int x = 0; x < size; x++) {
			rsi = ((RemoteSessionInstance) v.elementAt(x));
			if (rsi != null && rsi.session != null && rsi.session.isConnectionActive()) {
				ret = rsi;
				break;
			}
		}
		return ret;

	}

	public Vector getSessions(ConnectionProperties prop) {
		Object o = sessionsForProperties.get(prop);
		Vector v;
		if (o instanceof Vector) {
			v = (Vector) o;
		} else {
			v = new Vector();
			sessionsForProperties.put(prop, v);
		}

		return (Vector) o;

	}

	public RemoteSessionInstance getFirstSession(ConnectionProperties prop) {
		if (prop == null)
			return null;
		Vector v = getSessions(prop);
		if (v == null || v.size() == 0)
			return null;
		return (RemoteSessionInstance) v.elementAt(0);

	}

	public void notifyActiveSessionExposed() {
		cancelNotifications(activeSession);
		if (activeSession != null) {
			PlatformServicesProvider.getInstance().lockOrientation(activeSession.state.orientationMode);
			// @todo as this is key mapping it may belong with the other
			onDisplayInvalid(activeSession.session.getSessionId());
		}

	}

	public void notifyActiveSessionObscured() {
	}

	public void notifySessionListExposed() {
		// When the session list is exposed.. we may be able to remove this.

	}

	public void cancelNotifications(RemoteSessionInstance rsi) {
		if (rsi != null && rsi.state.notified) {
			rsi.state.notified = false;
		}
		NotificationManager.inst().updateNotificationIndicators(false, rsi);

	}

	private String getKeyPasswordInternal(Key key) {
		String password = null;
		KeyPasswordPrompt passwordScreen = new KeyPasswordPrompt();
		if (passwordScreen.doModal(key.getFriendlyName())) {
			password = passwordScreen.getPassword();
			if (password.length() == 0) {
				password = null;
			} else {
			}
			if (passwordScreen.getSavePassword()) {
				key.setPassphrase(password);
				KeyManager.getInstance().commitData();
			}
		}
		return password;
	}

	public String getKeyPassword(int sessionId, Key key) {
		// @todo - make a separate KeyPasswordRequestor implementation, much
		// cleaner than this foolishness.
		final Key k = key;
		UiApplication.getUiApplication().invokeAndWait(new Runnable() {
			public void run() {
				tempPass = getKeyPasswordInternal(k);
			}
		});
		String local = tempPass;
		tempPass = null; // make sure this password can get freed up- (? do
							// strings get freed in the jvm?)
		return local;

	}

	public void onDisplayDirty(int sessionId) {
		if (forceShutdown)
			return;

		terminal.redraw(false);
	}

	public void onDisplayInvalid(int sessionId) {
		if (forceShutdown)
			return;
		terminal.redraw(true);
	}

	// onSessionError then, onSessionDisconnected
	public void onSessionConnected(int sessionId) {
		if (forceShutdown)
			return;
		// This will update home screen icons, notification icons, etc.
		setSessionNotifiedState(sessionId);

	}

	public void onSessionDisconnected(int sessionId, int bytesWritten, int bytesRead) {
		if (forceShutdown)
			return;
		RemoteSessionInstance rsi = (RemoteSessionInstance) sessions.get(sessionId);
		if (rsi == null)
			return;

		if (rsi == activeSession) {
			terminal.hideOverlayManager();
		}
		MessageFormat mf = new MessageFormat(bundle.getString(BBSSHResource.MSG_USAGE_SUMMARY));
		StringBuffer sb = new StringBuffer();
		mf.format(new Object[] {
				Tools.byteCountToHumanReadableString(bytesRead), Tools.byteCountToHumanReadableString(bytesWritten),
				Tools.byteCountToHumanReadableString(bytesRead + bytesWritten)
		}, sb, new FieldPosition(0));

		String value = sb.toString();
		Logger.warn(value);
		// Moving the bottom margin to the bottom of the screen will ensure that scrolling is permitted -- so when we
		// append the message, the screen will be able to scroll as necessary.
		rsi.emulator.setBottomMargin(rsi.emulator.getTerminalHeight(), false);
		rsi.emulator.putStringStartLine(value);
		// ANy time we force text into the terminal, we need to invalidate it to
		// force repaint.
		onDisplayInvalid(sessionId);
		if (isTerminalActive()) {
			if (rsi == activeSession) {
				terminal.showExpiringMessage(bundle.getString(BBSSHResource.TERMINAL_MSG_DISCONNECTED));
			} else {
				terminal.showExpiringMessage(MessageFormat.format(
						bundle.getString(BBSSHResource.TERMINAL_MSG_DISCONNECTED_OTHER), new Object[] {
							rsi.session.getProperties()
						}));
			}
		}
		setSessionNotifiedState(sessionId);

	}

	/*
	 * Invoked when asession error causes the connection to be terminated.
	 * Received before onSessionDisconnected. (non-Javadoc)
	 * 
	 * @see org.bbssh.net.session.SessionListener#onSessionError(int,
	 * java.lang.String)
	 */
	public void onSessionError(int sessionId, String errorMessage) {
		if (forceShutdown)
			return;

		// rsi.state.addNotificationMessage("Error: " + error);

		RemoteSessionInstance rsi = getSession(sessionId);
		if (rsi == null)
			return;

		final String error = bundle.getString(BBSSHResource.MSG_NOTICE) + errorMessage;
		rsi.state.error = true;
		rsi.emulator.setBottomMargin(rsi.state.numRows, false);
		rsi.emulator.putStringStartLine(Tools.CRLF);
		rsi.emulator.putStringStartLine(error);
		rsi.emulator.putStringStartLine(Tools.CRLF);
		onDisplayInvalid(sessionId);
		setSessionNotifiedState(sessionId);

	}

	/**
	 * @return true if the terminal screen is currently displayed foremost on screen.
	 */
	public boolean isTerminalActive() {
		BBSSHApp app = BBSSHApp.inst();
		if (terminal == null)
			return false;
		if (!app.isForeground())
			return false;

		Screen s = app.getActiveScreen();
		if (s == terminal)
			return true;
		if (terminal.isVisible())
			return true;

		// if a popup screen is on top of the terminal (such as macro
		// selector or font dialog)
		// we'll count that as terminal active.
		if (terminal.isDisplayed() && s instanceof PopupScreen) {
			return true;
		}
		return false;
	}

	public void refreshNotifications() {
		NotificationManager.inst().updateNotificationIndicators(false, null);
	}

	public void setSessionNotifiedState(int sessionId) {
		if (forceShutdown)
			return;
		RemoteSessionInstance rsi = getSession(sessionId);
		if (rsi == null) {
			return;
		}
		if (!isTerminalActive()) {
			boolean newNotification = !rsi.state.notified;
			rsi.state.notified = true;
			NotificationManager.inst().updateNotificationIndicators(newNotification, rsi);
			// @todo - should NotificationManager also handle primaryscreen updates?
		}
		refreshConnectionList();
	}

	/**
	 * notify of a bell tone
	 * 
	 * @param connId
	 */
	public void onSessionRemoteAlert(int connId) {
		if (forceShutdown)
			return;
		RemoteSessionInstance rsi = getSession(connId);
		if (rsi == null) {
			return;
		}
		if (isTerminalActive()) {
			if (rsi != activeSession) {
				terminal.showExpiringMessage(MessageFormat.format(
						bundle.getString(BBSSHResource.TERMINAL_MSG_ALERT_IN_SESSION), new Object[] {
							rsi.session.getProperties()
						}));
			}

		}
		setSessionNotifiedState(connId);
	}

	public ConnectionProperties getPropertiesForInstance(int sessionId) {
		RemoteSessionInstance rsi = getSession(sessionId);
		if (rsi == null)
			return null;

		if (rsi.session == null)
			return null;

		return rsi.session.getProperties();

	}

	/**
	 * Invoked when the application has been activated, eg swappedi n from teh background, or when we receive backlight
	 * on event.
	 */
	public void notifyAppActivate() {
		// stop any blinking/alert notification behavior.
		NotificationManager.inst().resetNotificationState();

		// Restore proper orientation lock (as much as we can - a bug prevents
		// full restoration
		// in that if the user changed orientation, locking it won't set it back
		// to what we want.)
		if (isTerminalActive()) {
			notifyActiveSessionExposed();

		} else {
			// Cancel active Notifications, but do not change icon state.
			cancelNotifications(null);
			PlatformServicesProvider.getInstance().unlockOrientation();
		}
		refreshNotifications();
		refreshConnectionList();
	}

}
