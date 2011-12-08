/**
 * Copyright (c) 2010 Marc A. Paradise This file is part of "BBSSH" BBSSH is based upon MidpSSH by Karl von Randow.
 * MidpSSH was based upon Telnet Floyd and FloydSSH by Radek Polak. This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU
 * General Public License along with this program; if not, write to the Free Software Foundation, Inc., 675 Mass Ave,
 * Cambridge, MA 02139, USA.
 */
package org.bbssh.ui.screens;

import java.util.Vector;

import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.i18n.ResourceBundleFamily;
import net.rim.device.api.system.Application;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.MainScreen;

import org.bbssh.BBSSHApp;
import org.bbssh.help.HelpManager;
import org.bbssh.i18n.BBSSHResource;
import org.bbssh.model.ConnectionManager;
import org.bbssh.model.ConnectionProperties;
import org.bbssh.model.KeyBindingManager;
import org.bbssh.model.Settings;
import org.bbssh.model.SettingsManager;
import org.bbssh.net.ConnectionHelper;
import org.bbssh.patterns.UpdatingBackgroundTask;
import org.bbssh.platform.PlatformServicesProvider;
import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.session.SessionManager;
import org.bbssh.ui.components.ConnectionListfieldCallback;
import org.bbssh.ui.components.HeaderBar;
import org.bbssh.ui.components.MemoryDialog;
import org.bbssh.ui.components.PleaseWaitTaskMonitorScreen;
import org.bbssh.ui.screens.macros.MacroManagerScreen;
import org.bbssh.util.Logger;
import org.bbssh.util.Tools;
import org.bbssh.util.Version;

/**
 * From this screen user can launch or open an session, or proceed to various configuration screens.
 */
public class PrimaryScreen extends MainScreen {

	ResourceBundleFamily res = ResourceBundle.getBundle(BBSSHResource.BUNDLE_ID, BBSSHResource.BUNDLE_NAME);
	protected ListField sessions;

	private ConnectionInstancePropertiesScreen curDetailScreen;

	/** Session state management: 0x100000 */

	private final MenuItem sessionConnect = new MenuItem(res, BBSSHResource.MENU_CONNECT, 0x00100000, 0) {
		public void run() {
			handleConnectSession();
		}
	};
	private final MenuItem sessionResume = new MenuItem(res, BBSSHResource.MENU_RESUME, 0x00100000, 5) {
		public void run() {
			handleResumeOrConnectSession();
		}

	};
	private final MenuItem sessionReconnect = new MenuItem(res, BBSSHResource.MENU_RECONNECT, 0x00100000, 5) {
		public void run() {
			handleReconnectSession();
		}

	};
	MenuItem sessionDisconnect = new MenuItem(res, BBSSHResource.MENU_DISCONNECT, 0x00100000, 10) {
		public void run() {
			int node = sessions.getSelectedIndex();
			if (node > -1) {
				SessionManager.getInstance().disconnectSession(getSelectedRemoteSessionInstance());
				refreshConnections();
			}
		}
	};

	MenuItem sessionTerminate = new MenuItem(res, BBSSHResource.MENU_CLOSE_SESSION, 0x00100000, 15) {
		public void run() {
			int node = sessions.getSelectedIndex();
			if (node > -1) {
				SessionManager.getInstance().terminateSession(getSelectedRemoteSessionInstance());
			}
		}
	};
	/** Connection Definition management: 0x200000 */

	private final MenuItem connectionCreateNew = new MenuItem(res, BBSSHResource.MENU_NEW_CONNECTION, 0x00200010, 1) {
		public void run() {
			showSessionDetailScreen(true);
		}
	};

	private final MenuItem connectionEdit = new MenuItem(res, BBSSHResource.MENU_EDIT_CONNECTION, 0x00200020, 2) {
		public void run() {
			showSessionDetailScreen(false);
		}
	};

	private final MenuItem connectionDuplicate = new MenuItem(res, BBSSHResource.MENU_DUPLICATE, 0x00200030, 3) {
		public void run() {
			handleDuplicateSession();
		}
	};

	private final MenuItem connectionDelete = new MenuItem(res, BBSSHResource.MENU_DELETE_CONNECTION, 0x00200040, 4) {
		public void run() {
			handleDeleteCurrentSession();
		}

	};

	// User-configurable components - 0x00300000
	private final MenuItem keyManager = new MenuItem(res, BBSSHResource.MENU_KEY_MANAGER, 0x00300000, 0) {
		public void run() {
			UiApplication.getUiApplication().pushScreen(new KeyManagerScreen());
		}
	};

	private final MenuItem keyBindMenu = new MenuItem(res, BBSSHResource.MENU_KEYBINDINGS, 0x00300010, 1) {
		public void run() {
			UiApplication.getUiApplication().pushScreen(new KeybindingScreen());
		}
	};

	private final MenuItem macrosMenu = new MenuItem(res, BBSSHResource.MENU_MACROS, 0x00300020, 2) {
		public void run() {
			UiApplication.getUiApplication().pushScreen(new MacroManagerScreen());
		}
	};

	private final MenuItem settingsMenu = new MenuItem(res, BBSSHResource.MENU_SETTINGS, 0x00300030, 3) {
		public void run() {
			UiApplication.getUiApplication().pushScreen(new SettingsScreen());
		}
	};

	// Help and feedback: 0x00500000
	private final MenuItem aboutMenu = new MenuItem(res, BBSSHResource.MENU_ABOUT, 0x00500000, 0) {
		public void run() {
			UiApplication.getUiApplication().pushScreen(new AboutScreen());
		}
	};

	private final MenuItem sendFeedback = new MenuItem(res, BBSSHResource.MENU_SEND_FEEDBACK, 0x00500010, 1) {
		public void run() {
			UiApplication.getUiApplication().invokeAndWait(new Runnable() {
				public void run() {
					Tools.sendFeedback(null);
				}
			});

		}
	};

	private boolean forceClose;

	/**
	 * Instantiates a new primary screen.
	 */
	public PrimaryScreen() {
		super(DEFAULT_MENU | DEFAULT_CLOSE);
		// Note that subclass of PrimaryScreen will implement proper enhanced
		// titlebar (6.0)
		if (!PlatformServicesProvider.getInstance().isEnhancedTitlebarSupported()) {
			HeaderBar title = new HeaderBar(res.getString(BBSSHResource.TITLE_CONNECTIONS));
			title.setBackgroundColor(0);
			title.setFontColor(0xFFFFFF);
			title.setDrawSeparator(false);
			setTitle(title);
		}
		sessions = new ListField();

		// Okay, this is pointless - the intiialziation dialog has to run later,
		// because otherwise we're pushing it onto the stack before teh event
		// dispatcher has
		// started. But if we're running it later, we're actually already runing
		// it too late to
		// be useful...
		// UiApplication.getUiApplication().invokeLater(new Runnable() {
		// public void run() {
		// waitForInitialiation();
		// }
		// });

		// No time to look now, but the internal call in psp constrcutor doesn't
		// seem to always get called...
		PlatformServicesProvider.getInstance().determineLayout();
		sessions = new ListField(BBSSHResource.ABOUT_MENU_CHECK_UPDATES_NOW);
		// = new TreeField(new ConnectionListCallback(), TreeField.FOCUSABLE);
		sessions.setEmptyString(res.getString(BBSSHResource.INFO_PRIMARY_NO_CONNECTION_DEFINED), DrawStyle.HCENTER);
		ConnectionListfieldCallback cb = new ConnectionListfieldCallback(false);
		sessions.setRowHeight(cb.getRowHeight());
		sessions.setCallback(cb);
		populateConnectionList();

		add(sessions);
		performBackgroundUpdateCheck();
		// do this later - otherwise we try to push the dialogs onto the display
		// before the event loop starts.
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				showStartupDialogs();
			}
		});

	}

	private void populateConnectionList() {
		sessions.setSize(ConnectionManager.getInstance().getConnections().size());
		sessions.invalidate();
	}

	/**
	 * Waits for background load of startup data to be completed.
	 */
	// private void waitForInitialiation() {
	// Logger.info("PrimaryScreen waiting for initialization.");
	// PleaseWaitTaskMonitorScreen wait = new PleaseWaitTaskMonitorScreen(new
	// UpdatingBackgroundTask() {
	// public void execute() {
	// updateListener(res.getString(PRIMARY_INITIALIZING));
	// synchronized (BBSSHApp.inst().getInitializationMutex()) {
	// // no action - just want to block ehre until the init task is complete.
	// Note that
	// // we couldn't block in the main thread above, as that would prevent UI
	// refreshes from occurring -
	// // we wouldn't show our "please wait"...
	// }
	// }
	// }, false);
	// wait.launch();
	// Logger.info("Initialization complete - continuing with primaryscreen display.");
	//
	// }

	protected ConnectionProperties getPropertiesForCurrentSelection() {
		return getPropertiesForNode(sessions.getSelectedIndex());
	}

	protected ConnectionProperties getPropertiesForNode(int node) {
		if (node == -1)
			return null;

		Vector v = ConnectionManager.getInstance().getConnections();
		if (v.size() > node)
			return (ConnectionProperties) v.elementAt(node);
		return null;

	}

	public void makeMenu(Menu menu, int instance) {
		super.makeMenu(menu, instance);
		int node = sessions.getSelectedIndex();
		ConnectionProperties prop = getPropertiesForNode(node);
		RemoteSessionInstance inst = getSelectedRemoteSessionInstance();
		MenuItem def;
		boolean connectedInstance = inst != null && inst.session.isConnectionActive();
		boolean context = (instance == Menu.INSTANCE_CONTEXT);

		// Anything that has an instance associated with it can eitehr
		// resume the session, or terminate it (disconnect and close)
		if (inst == null) {
			// Current or formerly active session

			if (prop == null) {
				// no selection at all...
				def = connectionCreateNew;
			} else {
				def = sessionConnect;
				menu.add(sessionConnect);
			}
		} else {
			def = sessionResume;
			menu.add(sessionResume);
			if (connectedInstance) {
				menu.add(sessionDisconnect);
				if (!context) {
					menu.add(sessionTerminate);
				}
			} else {
				menu.add(sessionReconnect);
				menu.add(sessionTerminate);

			}
		}

		if (!connectedInstance) {
			menu.add(connectionDelete);
		}
		menu.add(connectionEdit);
		if (context) {
			if (inst == null) {
				menu.add(connectionCreateNew);
			}
		} else {
			menu.add(connectionCreateNew);
			if (prop != null) {
				menu.add(connectionDuplicate);
			}
			// These are always available.
			menu.add(keyManager);
			menu.add(keyBindMenu);
			menu.add(macrosMenu);
			menu.add(settingsMenu);
			menu.add(HelpManager.getHelpMenu());
			menu.add(sendFeedback);
			menu.add(aboutMenu);
		}

		menu.setDefault(def);

	}

	private void backdoorLogLevel(final int level, final String desc) {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				Logger.setLogLevel(level);
				Status.show("Logging set to " + desc);
			}
		});
	}

	private void backdoorToggleFileLogging(final boolean enable) {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				if (enable) {
					if (Logger.isFileLoggingEnabled()) {
						Status.show("Logging is already enabled. Logs being written to: " + Logger.getFileName());

					} else {
						try {
							if (BBSSHApp.inst().requestPermission(ApplicationPermissions.PERMISSION_FILE_API,
									BBSSHResource.MSG_PERMISSIONS_MISSING_FILE_ACCESS_LOGGING)) {
								Logger.enableFileLogging();
								Status.show("Logging  enabled enabled. Logs being written to: " + Logger.getFileName());
							}
						} catch (Throwable t) {
							Status.show("Unable to enable logging to file due to error " + t.getMessage().toString()
									+ " : " + t.getMessage());
						}
					}
				} else {
					if (Logger.isFileLoggingEnabled()) {
						Logger.disableFileLogging();
						Status.show("Logging output file turned off");
					} else {
						Status.show("Logging to file was not enabled to begin with.");
					}
				}
			}
		});

	}

	private void backdoorDoStartupDialogs() {
		UiApplication.getUiApplication().invokeLater(new Runnable() {

			public void run() {
				Settings s = SettingsManager.getSettings();
				s.setRememberOption(Settings.REMEMBER_LICENSE_AGREEMENT_COMPLETE, false);
				s.setRememberOption(Settings.REMEMBER_CHECKED_UPDATE_OK, false);
				s.setRememberOption(Settings.REMEMBER_CHECKED_SEND_USAGE_STATS_OK, false);
				showStartupDialogs();
			}
		});
	}

	protected boolean openProductionBackdoor(int backdoorCode) {
		switch (backdoorCode) {
			case ('L' << 24) | ('Y' << 16) | ('O' << 8) | 'T': // LYOT
				Status.show("Layout data written to log file");
				PlatformServicesProvider.getInstance().determineLayout();
				return true;

			case ('L' << 24) | ('G' << 16) | ('M' << 8) | 'X': // LGMX
				backdoorLogLevel(Logger.LOG_LEVEL_DEBUG, "debug/full.");
				return true;

			case ('L' << 24) | ('G' << 16) | ('I' << 8) | 'N': // LGIN
				backdoorLogLevel(Logger.LOG_LEVEL_INFO, "info/trace.");
				return true;

			case ('L' << 24) | ('G' << 16) | ('W' << 8) | 'N': // LGWN
				backdoorLogLevel(Logger.LOG_LEVEL_WARN, "warn (default).");
				return true;

			case ('L' << 24) | ('G' << 16) | ('T' << 8) | 'M': // LGTM // toggle terminal logging
				backdoorToggleTerminalLogging();
				return true;

			case ('L' << 24) | ('G' << 16) | ('F' << 8) | 'S': // LGFS // log file start
				backdoorToggleFileLogging(true);
				return true;
			case ('L' << 24) | ('G' << 16) | ('F' << 8) | 'X': // LGFX // log file end
				backdoorToggleFileLogging(false);
				return true;

			case ('P' << 24) | ('R' << 16) | ('M' << 8) | 'S': // PRMS
				UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run() {
						UiApplication.getUiApplication().pushScreen(new PermissionsHelperScreen(false));
					}
				});
				return true;

			case ('S' << 24) | ('T' << 16) | ('R' << 8) | 'T': // STRT
				backdoorDoStartupDialogs();
				return true;

			case ('B' << 24) | ('N' << 16) | ('D' << 8) | 'B': // BNDB
				new Thread(new Runnable() {
					public void run() {
						KeyBindingManager.getInstance().setDebugBindings();
					}
				}).start();
				return true;

			case ('R' << 24) | ('C' << 16) | ('V' << 8) | 'R': // RCVR
				UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run() {
						UiApplication.getUiApplication().pushModalScreen(
								new RecoveryScreen(BBSSHResource.RECOVERY_INSTRUCTION));
						forceClose = true;
						close();
					}
				});
				return true; // handled
		}
		return super.openProductionBackdoor(backdoorCode);
	}

	private void backdoorToggleTerminalLogging() {
		ConnectionProperties prop = getPropertiesForCurrentSelection();
		if (prop == null)
			return;
		Dialog.ask(Dialog.D_OK, res.getString(prop.isCaptureEnabled() ? BBSSHResource.PRIMARY_TERMINAL_LOGGING_DISABLED
				: BBSSHResource.PRIMARY_TERMINAL_LOGGING_ENABLED));
		prop.setCaptureEnabled(!prop.isCaptureEnabled());

	}

	protected RemoteSessionInstance getSelectedRemoteSessionInstance() {
		return SessionManager.getInstance().getFirstSession(getPropertiesForCurrentSelection());
	}

	protected boolean keyChar(char c, int status, int time) {
		switch (c) {
			case Keypad.KEY_ENTER:
				handleResumeOrConnectSession();
				return true;
			case 'e':
			case 'E':
				showSessionDetailScreen(false);
				return true;
			case 'c':
			case 'C':
				showSessionDetailScreen(true);
				return true;
			case Keypad.KEY_BACKSPACE:
			case 'd':
			case 'D':
				handleDeleteCurrentSession();
				return true;
		}

		return super.keyChar(c, status, time);

	}

	private void startCloseTasks() {
		PleaseWaitTaskMonitorScreen m = new PleaseWaitTaskMonitorScreen(new UpdatingBackgroundTask() {
			public void execute() {
				updateListener("Shutting down...");
				BBSSHApp.inst().shutdownTasks();
			}
		});
		// we'll go to the background and begin our shutdown. It shouldn't take long, but this way we're immediately
		// responsive while we do any required cleanup tasks.
		UiApplication.getUiApplication().requestBackground();
		m.launch();
		super.close();
	}

	public void close() {
		if (forceClose) {
			// A sloppy close, requested due to corrupt state.
			super.close();
			System.exit(-1);
			return;
		}

		SessionManager mgr = SessionManager.getInstance();

		if (mgr.doesActiveSessionExist()) {
			Settings s = SettingsManager.getSettings();
			// If this was explicitly set, then just do what the user siad.
			if (s.getRememberOption(Settings.REMEMBER_OPT_BACKGROUND_ON_CLOSE)) {
				if (s.isBackgroundOnCloseEnabled()) {
					UiApplication.getUiApplication().requestBackground();
				} else {
					startCloseTasks();
				}
				return;
			}
			// Prompt if they haven't set it.

			int result =
					MemoryDialog.ask(BBSSHResource.MSG_CONFIRM_EXIT_CONN_OPEN, new int[] {
							BBSSHResource.MSG_CONFIRM_EXIT_ANS_BACKGROUND, BBSSHResource.MSG_CONFIRM_EXIT_ANS_EXIT,
							BBSSHResource.MSG_CONFIRM_EXIT_ANS_NEVERMIND
					}, 0);
			if (result == 2) { // nevermind
				return;
			}

			boolean background = (result == 0);
			// Remember?
			if (MemoryDialog.getRememberSelection()) {
				s.setRememberOption(Settings.REMEMBER_OPT_BACKGROUND_ON_CLOSE, true);
				s.setBackgroundOnClose(background);
			}
			if (background) {
				UiApplication.getUiApplication().requestBackground();
				return;
			}

		}
		startCloseTasks();

	}

	/**
	 * Displays session detail screen for the selected item, first copying the properties before modification. This is
	 * so that we don't incorrectly persist updates that the user does not want to retain. validates that if a new
	 * session is not being created, that a valid session is selected. Once the detail form completes, this will save
	 * the updates.
	 * 
	 * @param newSession
	 *            indicates whether the user is creating a new sessions.
	 */
	private void showSessionDetailScreen(boolean newSession) {
		ConnectionProperties prop;
		if (newSession) {
			prop = new ConnectionProperties(SettingsManager.getSettings().getDefaultConnectionProperties(), true);
		} else {
			prop = getPropertiesForCurrentSelection();
			if (prop == null) {
				return;
			}
		}
		editConnectionProperties(prop);
	}

	public void editConnectionProperties(final ConnectionProperties prop) {
		Application.getApplication().invokeLater(new Runnable() {
			public void run() {
				curDetailScreen = new ConnectionInstancePropertiesScreen(prop);
				UiApplication.getUiApplication().pushScreen(curDetailScreen);
			}
		});
	}

	/**
	 * Override of onExposed, we are using this to perform appropriate behavior when the user returns from
	 * defining/editing a session, or to switch session screens.
	 */
	protected void onExposed() {
		if (sessions == null) {
			return;
		}

		SessionManager.getInstance().notifySessionListExposed();
		//
		// This can get exposed when returning from a live session
		// so we will need to invalidate our list -- this will cause
		// use to properly reflect active state of connections and
		// notifications.
		invalidate();

		// If we weren't in the process of editing a connection,
		// we don't have any further action to take here.
		if (curDetailScreen == null || !curDetailScreen.isSaved()) {
			return;
		}

		// Finally, if the changes were saved, update the array with our
		// current copy.
		Logger.info("PrimaryScreen.onExposed: returning from saved session edit, updating.");
		ConnectionProperties prop = curDetailScreen.getEditedProperties();
		addAndRefresh(prop);
		curDetailScreen = null;
		if (SessionManager.getInstance().getFirstConnectedSession(prop) != null) {
			Dialog.inform(res.getString(BBSSHResource.PRIMARY_SOME_CHANGES_NEED_RECONNECT));
		}
	}

	protected void addAndRefresh(ConnectionProperties prop) {
		if (prop == null)
			return;
		Logger.info("PrimaryScreen.addAndRefresh - start");
		Vector v = ConnectionManager.getInstance().getConnections();
		ConnectionProperties orig =
				ConnectionManager.getInstance().getConnectionPropertiesById(Integer.toString(prop.getUID()));
		if (prop.isNew() || orig == null) {
			Logger.info("PrimaryScreen.addAndRefresh - conn is new, clearing new flag.");
			prop.setNew(false);
			v.addElement(prop);
		} else {
			Logger.info("PrimaryScreen.addAndRefresh - conn is existing, replacing it: " + orig.getName() + " -> "
					+ prop.getName());
			// @todo better search for original
			// Inefficient search, but it'll work for now - most folks aren't
			// looking at hundreds of defined connectionss.
			int idx = v.lastIndexOf(orig);
			if (idx == -1) {
				Logger.error("PS.ar - could not find original connection ");
				return;
			}
			Logger.info("PrimaryScreen.addAndRefresh - replacing at index: " + idx);
			v.setElementAt(prop, idx);
		}
		Tools.sortVector(v);
		populateConnectionList();
		saveConnections();

	}

	private void saveConnections() {
		// @todo - race possibility here, need to be much cleaner about this.
		new Thread() {
			public void run() {
				ConnectionManager.getInstance().commitData();
			};
		}.start();

	}

	public void startOrResumeSession(ConnectionProperties prop) {
		SessionManager mgr = SessionManager.getInstance();
		RemoteSessionInstance inst = SessionManager.getInstance().getFirstSession(prop);
		if (inst == null) {
			handleConnectSession();
		} else {
			mgr.setActiveSession(inst);
		}
	}

	private void handleResumeOrConnectSession() {
		RemoteSessionInstance inst = getSelectedRemoteSessionInstance();
		if (inst == null) {
			handleConnectSession();
		} else {
			SessionManager.getInstance().setActiveSession(inst);
		}

	}

	private void handleReconnectSession() {
		RemoteSessionInstance inst = getSelectedRemoteSessionInstance();
		SessionManager.getInstance().reconnectSession(inst);
	}

	/**
	 * opens and initiates connection of the selected session after first verifying permissions.
	 */
	private void handleConnectSession() {
		// int node = sessions.getSelectedIndex();
		ConnectionProperties prop = getPropertiesForCurrentSelection();
		if (prop == null)
			return;

		if (prop.getUseWifiIfAvailable()) {
			// No big deal if we can't get this - at least we have TCP
			if (!BBSSHApp.inst().requestPermission(
					ConnectionHelper.getPermissionForConnType(ApplicationPermissions.PERMISSION_WIFI),
					BBSSHResource.MSG_NET_PERMISSIONS_MISSING_WIFI)) {
				Logger.error("Expected failure - permission denied for wifi attempt.");

			}
		}

		if (!BBSSHApp.inst().requestPermission(ConnectionHelper.getPermissionForConnType(prop.getConnectionType()),
				BBSSHResource.MSG_NET_PERMISSIONS_MISSING_ADD_NOW)) {
			Logger.error("Cannot complete connection due to permissions denied for selected connection type.");
			return;
		}

		if (SettingsManager.getSettings().isHomeScreenNotificationIconEnabled()) {
			if (!BBSSHApp.inst().requestPermission(ApplicationPermissions.PERMISSION_MEDIA,
					BBSSHResource.MSG_PERMISSIONS_MISSING_MEDIA)) {
				Logger.error("Disabling home screen notifications due to permission denied.");
				SettingsManager.getSettings().setHomeScreenNotificationIconEnabled(false);
				SettingsManager.getInstance().commitData();
			}
		}

		SessionManager.getInstance().connectSession(prop);
		// Refresh with latest info. For now we're being lazy and just
		// repopulating the whole thing.
		populateConnectionList();

		// // Okay - if a connection exists...
		// if (sessions.getCookie(node) instanceof ConnectionProperties) {
		// // Let session manager figure out what's going on for this definition
		// ...
		// SessionManager.getInstance().initiateOrResumeSession(
		// (ConnectionProperties) sessions.getCookie(node));
		// } else {
		// SessionManager.getInstance().setActiveSession(nodeConnectionMap.get(node));
		// }

	}

	/**
	 * Check for updates.
	 */
	private void performBackgroundUpdateCheck() {
		Settings s = SettingsManager.getSettings();

		// Development mode not allowed to bypass auto update checks.
		if (Version.isReleaseMode() && !s.isAutoCheckUpdateEnabled() && !s.isAnonymousUsageStatsEnabled()) {
			return;
		}
		// Do our check in the background thread so as not to block the main
		// thread at all.
		new Thread("Updates") {
			public void run() {
				Version.checkAndPromptForUpdates(false);
			}
		}.start();
	}

	protected void handleDuplicateSession() {
		ConnectionProperties prop = getPropertiesForCurrentSelection();
		if (prop == null)
			return;

		ConnectionProperties newProp = new ConnectionProperties(prop);
		newProp.setName(newProp.getName() + res.getString(BBSSHResource.IND_COPY));
		addAndRefresh(newProp);

	}

	private void showStartupDialogs() {
		boolean saveNeeded = false;
		Settings s = SettingsManager.getSettings();
		if (!s.areInitialOptionsSet()) {
			UiApplication.getUiApplication().pushModalScreen(new StartupPopup());
			if (!s.getRememberOption(Settings.REMEMBER_LICENSE_AGREEMENT_COMPLETE)) {
				Logger.error("License agreement not completed, closing.");
				System.exit(0);
				return;
			}
			saveNeeded = true;
		}
		if (!Version.doesAppVersionMatchOSVersion()) {
			if (Dialog.ask(Dialog.D_YES_NO, res.getString(BBSSHResource.MSG_WRONG_VERSION), 0) == Dialog.YES) {
				Version.goToDownloadSite();
				System.exit(0);
				Logger.error("Not using latest OS version, user chose download. Terminating.");
			} else {
				Logger.error("Not using latest OS version, user ignores.");
			}
		}
		if (!s.getRememberOption(Settings.REMEMBER_DO_NOT_SHOW_DATA_WARN)) {
			MemoryDialog.ask(BBSSHResource.MSG_DATA_USAGE_WARN, new int[] {
				BBSSHResource.GENERAL_LBL_OK
			}, 0);
			if (MemoryDialog.getRememberSelection()) {
				s.setRememberOption(Settings.REMEMBER_DO_NOT_SHOW_DATA_WARN, true);
				saveNeeded = true;
			}
		}
		if (saveNeeded)
			SettingsManager.getInstance().commitData();

		// @todo reinstate this once it's human-usable. It's a mess right now
		// ...
		// if (!s.getRememberOption(Settings.REMEMBER_PERM_SHOWN)) {
		// UiApplication.getUiApplication().pushScreen(new
		// PermissionsHelperScreen(false));
		//
		// }

	}

	public void refreshConnections() {
		// invalidate();
		// doens't exist for some reason... very odd since it inherits from
		// Field...
		sessions.invalidate();
	}

	/**
	 * If the app version supports homescreen shortcuts, override this to remove the specifeid shortcut
	 */
	protected void removeShortcut(ConnectionProperties prop) {
		// homescreen shortcuts not supported until 6.0

	}

	private boolean handleDeleteCurrentSession() {
		ConnectionProperties prop = getPropertiesForCurrentSelection();
		ConnectionManager cfg = ConnectionManager.getInstance();
		if (prop == null)
			return false;
		if (SessionManager.getInstance().getFirstConnectedSession(prop) != null) {
			Status.show(res.getString(BBSSHResource.MSG_NOT_ALLOWED_CONNECTED));
			return false;

		}
		// note that we do allow existing sessions that are not connected.
		if (Dialog.ask(Dialog.D_DELETE, res.getString(BBSSHResource.MSG_CONFIRM_DEL_CONNECTION), 0) == Dialog.DELETE) {
			// @todo - should this be in the background?
			SessionManager mgr = SessionManager.getInstance();
			Vector sessions = mgr.getSessions(prop);
			if (sessions != null) {
				for (int x = 0; x < sessions.size(); x++) {
					mgr.terminateSession((RemoteSessionInstance) sessions.elementAt(x));
				}
			}
			removeShortcut(prop);
			Vector v = cfg.getConnections();
			v.removeElement(prop);
			Tools.sortVector(v);
			saveConnections();
			populateConnectionList();
			return true;

		}
		return false;

	}
}
