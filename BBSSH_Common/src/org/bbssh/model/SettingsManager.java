package org.bbssh.model;

import java.io.EOFException;
import java.util.Vector;

import net.rim.device.api.synchronization.SyncObject;

import org.bbssh.io.SyncBuffer;

public class SettingsManager extends DefaultSyncCollection {
	private Settings settings;
	public static final long SETTINGS_GUID = 0xcc33d531af98444cL; // org.bbssh.model.Settings

	private static SettingsManager me;

	private SettingsManager() {
	}

	public void initialize() {
		// @todo - use this to give as element 1 -> internalSettings? Instead of
		// adding internal settings to the user-configurable settings object...

		Vector d = getDataVector();
		if (d.size() == 0 || d.elementAt(0) == null) {
			settings = new Settings();
			d.addElement(settings);
			commitData();
		} else {
			settings = (Settings) d.elementAt(0);
		}

	}

	public synchronized static SettingsManager getInstance() {
		if (me == null) {
			me = new SettingsManager();
		}
		return me;
	}

	public static synchronized Settings getSettings() {
		return getInstance().settings;
	}

	protected boolean convertImpl(SyncObject object, SyncBuffer buffer, int version) {
		if (!(object instanceof Settings))
			return false;
		Settings s = (Settings) object;
		buffer.writeField(s.isAutoCheckUpdateEnabled());
		buffer.writeField(s.getAPN());
		buffer.writeField(s.getAPNUserName());
		buffer.writeField(s.getAPNPassword());
		buffer.writeField(s.getShowPlaintextPassword());
		buffer.writeField(s.isVibrateOnAlertEnabled());
		// Free field: BYTE
		buffer.writeField((byte) 0);
		// Free field: INT
		buffer.writeField((int) 0);
		// Free field: INT
		buffer.writeField((int) 0);

		// Free field: BYTE
		buffer.writeField((byte) 0);
		// Free field: BYTE
		buffer.writeField((byte) 0);
		// Free field: BYTE
		buffer.writeField((byte) 0);
		// Oops - incorrect field was written here, this can be re-used for a boolean at any time.
		buffer.writeField(false);
		// END VERSION 0 FIELDS
		// VERSION 1 FIELDS
		// we can write this blindly, if user downgrades it wil just be ignored (then lost on save)
		buffer.writeField(s.isHomeScreenNotificationIconEnabled());

		// VERSION 2 FIELDS
		buffer.writeField(s.getLastCleanupVersion());

		// VERSION 3 FIELDS
		buffer.writeField(s.isAnonymousUsageStatsEnabled());
		buffer.writeField(s.isMessageIntegrationEnabled());

		// VERSION 4 FIELDS
		buffer.writeField(s.isBackgroundOnCloseEnabled());
		int count = s.getRememberOptionCount();
		buffer.writeField(count);
		for (int x = 0; x < count; x++) {
			buffer.writeField(s.getRememberOption(x));
		}
		// VERSION 5
		buffer.writeField(s.isTitlebarDisplayEnabled());

		// 6
		buffer.writeField(s.getShowKeyboardOnSliderClose());
		// 7
		buffer.writeField(s.getLastSaveVersion());
		// 8 - FFREE FIELD - boolean
		buffer.writeField(false);

		// 9
		ConnectionManager mgr = ConnectionManager.getInstance();
		buffer.writeField(mgr.getSyncVersion());
		mgr.serializeConnectionProperties(s.getDefaultConnectionProperties(), buffer);

		// 10
		buffer.writeField(s.getDisableKeybindWhenOnCall());

		// 11
		buffer.writeField(s.getUpdateCheckInterval());
		buffer.writeField(s.getLastUpdateCheckTime());

		// 12 - available
		buffer.writeField(true); 
		return true;

	}

	public SyncObject convertImpl(SyncBuffer buffer, int version, int UID, boolean syncDirty) {
		Settings s = new Settings();
		s.setSyncStateDirty(syncDirty);

		try {
			s.setAutoCheckUpdates(buffer.readNextBooleanField());
			s.setAPN(buffer.readNextStringField());
			s.setAPNUserName(buffer.readNextStringField());
			s.setAPNPassword(buffer.readNextStringField());
			s.setShowPlaintextPassword(buffer.readNextBooleanField());
			s.setVibrateOnAlertEnabled(buffer.readNextBooleanField());
			// @todo usage: if (version > safe-version) { use avail fields } else { ignore values }
			// available field:
			buffer.readNextByteField();
			// available field:
			buffer.readNextIntField();
			// available field:
			buffer.readNextIntField();
			// available field (3x byte)
			buffer.readNextByteField();
			buffer.readNextByteField();
			buffer.readNextByteField();
			// Mistake in earlier version - discard the next field
			buffer.readNextBooleanField();
			// END VERSION 0 FIELDS

			if (version >= 1) {
				s.setHomeScreenNotificationIconEnabled(buffer.readNextBooleanField());
			}
			if (version >= 2) {
				s.setLastCleanupVersion(buffer.readNextIntField());
			}
			if (version >= 3) {
				s.setAnonymousUsageStatsEnabled(buffer.readNextBooleanField());
				s.setMessageIntegrationEnabled(buffer.readNextBooleanField());
			}
			if (version >= 4) {
				s.setBackgroundOnClose(buffer.readNextBooleanField());
				int count = buffer.readNextIntField();
				for (int x = 0; x < count; x++) {
					s.setRememberOption(x, buffer.readNextBooleanField());
				}
			}
			if (version >= 5) {
				s.setTitlebarDisplayEnabled(buffer.readNextBooleanField());
			}
			if (version >= 6) {
				s.setShowKeyboardOnSliderClose(buffer.readNextBooleanField());
			}
			if (version >= 7) {
				s.setLastSaveVersion(buffer.readNextLongField());
			}
			if (version >= 8) {
				// free field:
				buffer.readNextBooleanField();
			}
			if (version >= 9) {
				s.setDefaultConnectionProperties(ConnectionManager.getInstance().deserializeConnectionProperties(
						buffer, buffer.readNextIntField()));
			}
			if (version >= 10) {
				s.setDisableKeybindsWhenOnCall(buffer.readNextBooleanField());
			}

			if (version >= 11) {
				s.setUpdateCheckInterval(buffer.readNextIntField());
				s.setLastUpdateCheckTime(buffer.readNextLongField());
			}

			if (version >= 12) {
				// available boolean field
				buffer.readNextBooleanField();
			}
		} catch (EOFException e) {
			s = null;
		}
		return s;

	}

	public String getSyncName() {
		return "BBSSH Configuration";
	}

	public int getSyncVersion() {
		return 12;
	}

	public long getPersistentStoreId() {
		return SETTINGS_GUID;
	}

	public void resetDefaults() {
		// Simple for us, we'll just re-initialize.
		initialize();
	}

	public boolean isSecureStoreRequired() {
		return false;
	}

}
