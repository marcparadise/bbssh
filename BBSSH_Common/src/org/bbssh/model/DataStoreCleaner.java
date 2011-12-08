package org.bbssh.model;

import net.rim.device.api.system.ControlledAccessException;
import net.rim.device.api.system.PersistentStore;

public class DataStoreCleaner {
	public static final byte CONNECTIONS = 0;
	public static final byte KEYS = 1;
	public static final byte MACROS = 2;
	public static final byte SETTINGS = 3;
	public static final byte BINDINGS = 4;
	
	public static int getCleanIteration() {
		return 3;
	}

	/**
	 * Clean out-of-date data which is no longer valid.
	 */
	public static void cleanData() {
		SettingsManager m = SettingsManager.getInstance();
		Settings s = SettingsManager.getSettings();
		int lastClean = s.getLastCleanupVersion();
		switch (lastClean) {
			case 0:
				// cleanup on old persistent stores, from pre 1.2 
				clean(0x3b876f970927ae00L); // base guid, probably nothing.
				clean(0x3b876f970927ae08L); // macros
				clean(0x3b876f970927ae09L); // settings
				clean(0x3b876f970927ae10L); // keystore
				clean(0x3b876f970927ae14L); // Sessions
			case 1:
				clean(MacroManager.MACRO_GUID);
			case 2:
				clean(KeyBindingManager.KEYBIND_GUID);
			// 3 is current, nothing to do. 
				

		}

		s.setLastCleanupVersion(getCleanIteration());
		m.commitData();

	}

	private static void clean(long id) {
		try {
			PersistentStore.destroyPersistentObject(id);
		} catch (ControlledAccessException e) {

		}
	}

	public static void cleanData(byte reposId) {
		switch (reposId) {
			case CONNECTIONS:
				ConnectionManager.getInstance().purgePersistentContent();
				break;
			case KEYS:
				KeyManager.getInstance().purgePersistentContent();
				break;
			case MACROS:
				MacroManager.getInstance().purgePersistentContent();
				break;
			case BINDINGS:
				KeyBindingManager.getInstance().purgePersistentContent();
				break;
			case SETTINGS:
				SettingsManager.getInstance().purgePersistentContent();
				break;

		}
	}
}
