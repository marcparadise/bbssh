package org.bbssh.model;

import java.io.EOFException;
import java.util.Vector;

import net.rim.device.api.i18n.Locale;
import net.rim.device.api.synchronization.SyncCollection;
import net.rim.device.api.synchronization.SyncConverter;
import net.rim.device.api.synchronization.SyncObject;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.CodeSigningKey;
import net.rim.device.api.system.ControlledAccess;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.util.DataBuffer;

import org.bbssh.io.SyncBuffer;
import org.bbssh.util.Logger;

/**
 * Class that can be registered at app startup as a SyncCollection provider. Does most of the work of maintaining
 * compatibility for both synchronization and version-proof data serialization.
 * 
 * @author marc
 */
abstract public class DefaultSyncCollection implements SyncCollection, SyncConverter {
	private Vector internalList = new Vector();
	private boolean loaded = false;

	/**
	 * Retrieves the vector used to manage data for this collection. Even internally any access to the wrapped member
	 * must use this accessor in order to ensure that it's loaded.
	 * 
	 * @return
	 */
	protected final synchronized Vector getDataVector() {
		if (!loaded) {
			loadData();

		}
		return internalList;
	}

	public final boolean addSyncObject(SyncObject object) {
		getDataVector().addElement(object);
		return true;
	}

	public final SyncObject getSyncObject(int uid) {
		Vector dv = getDataVector(); // using the getter ensures that it loads.
		int max = dv.size();
		for (int x = 0; x < max; x++) {
			if (uid == ((SyncObject) dv.elementAt(x)).getUID()) {
				return (SyncObject) dv.elementAt(x);
			}
		}

		return null;
	}

	public final SyncObject[] getSyncObjects() {
		Vector dv = getDataVector(); // using the getter ensures that it loads.
		int len = dv.size();
		SyncObject[] ret = new SyncObject[len];
		for (int x = 0; x < len; x++) {
			ret[x] = (SyncObject) dv.elementAt(x);
		}
		return ret;
	}

	// This seems to be invoked by the framework when restoring a given sync collection from a backup,
	// prior to performing the restore.
	public final boolean removeAllSyncObjects() {
		getDataVector().removeAllElements();
		return true;
	}

	public final boolean removeSyncObject(SyncObject object) {
		return getDataVector().removeElement(object);
	}

	public final boolean updateSyncObject(SyncObject oldObject, SyncObject newObject) {
		Vector dv = getDataVector(); // using the getter ensures that it loads.
		int idx = dv.indexOf(oldObject);
		if (idx == -1) {
			return false;
		}
		dv.setElementAt(newObject, idx);
		return true;
	}

	public final void beginTransaction() {
		loadData();
	}

	public final void endTransaction() {
		commitData();
	}

	public final void clearSyncObjectDirty(SyncObject object) {
		Vector dv = getDataVector(); // using the getter ensures that it loads.
		int max = dv.size();
		for (int x = 0; x < max; x++) {
			((DataObject) dv.elementAt(x)).setSyncStateDirty(false);
		}
	}

	public String getSyncName(Locale locale) {
		// @todo figure out how to actually implement this with localization
		return getSyncName();
	}

	public final int getSyncObjectCount() {
		return getDataVector().size();
	}

	public final boolean isSyncObjectDirty(SyncObject object) {
		if (object instanceof DataObject) {
			return ((DataObject) object).isSyncStateDirty();
		}
		return false;
	}

	public final void setSyncObjectDirty(SyncObject object) {
		if (object instanceof DataObject) {
			((DataObject) object).setSyncStateDirty(true);
		}

	}

	public final boolean convert(SyncObject object, DataBuffer buffer, int version) {
		return convertImpl(object, new SyncBuffer(buffer), version);
	}

	public final SyncObject convert(DataBuffer data, int version, int UID) {
		return convertImpl(new SyncBuffer(data), version, UID, false);
	}

	protected abstract SyncObject convertImpl(SyncBuffer buffer, int version, int uID, boolean syncDirty);

	protected abstract boolean convertImpl(SyncObject object, SyncBuffer buffer, int version);

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.synchronization.SyncCollection#getSyncConverter()
	 */
	public final SyncConverter getSyncConverter() {
		return this;
	}

	PersistentObject persistent;

	public synchronized final PersistentObject getPersistentObject() {
		if (persistent == null) {
			persistent = PersistentStore.getPersistentObject(getPersistentStoreId());
		}
		return persistent;
	}

	/**
	 * This will be invoked when loadData finishes execution, allowing you to perform any customizations in your
	 * subclass.
	 * 
	 * @param numLoaded
	 *            number of records loaded
	 */
	protected void dataLoadComplete(int numLoaded) {
	}

	/**
	 * Loads data from child class's persistent store, relying on implementation to parse the data. This will load
	 * blindly, even if data is already loaded. Invoke this from your constructor
	 */
	public void loadData() {
		loadData(true);
	}

	abstract public boolean isSecureStoreRequired();

	/**
	 * loads data from persistent store, relying on implementation to parse data. Invoked internally when we on-demand
	 * load data; or when the synchronization framework invokes beginTransaction.
	 * 
	 * @param internal
	 */
	public synchronized final void loadData(boolean internal) {
		if (loaded)
			return;
		PersistentObject per = getPersistentObject();
		Vector v = null;

		// even if it's required, there's no guarantee that the data was previously saved secured
		// so we neeed to handle both scenarios.
		if (isSecureStoreRequired()) {
			CodeSigningKey key = getSigningKey();
			if (key == null) {
				Logger.error("Could not load data from secured store - failed to obtain signing key.");
			} else {
				try {
					v = (Vector) per.getContents(key);
				} catch (Throwable t) {
					Logger.error("Could not load data from secured store: " + t.getMessage() + " " + t);
				}
			}
		}
		if (v == null) {
			Logger.info("Attempting unsecure load.");
			v = (Vector) per.getContents();
		}
		byte[] rawData;
		if (v == null || v.elementAt(0) == null) {
			v = new Vector();
			DataBuffer b = new DataBuffer(4, true);
			b.writeInt(0);// length
			rawData = b.getArray();
			v.addElement(rawData);
			per.setContents(v);
			per.commit();
		} else {
			rawData = (byte[]) v.elementAt(0);
		}

		DataBuffer d = new DataBuffer(rawData, 0, rawData.length, true);
		internalList.removeAllElements();
		try {
			int count = d.readInt();
			// @todo int fieldCount = count >> 16;

			for (int x = 0; x < count; x++) {
				// @todo - pull up syncdirty handling
				// syncDiry = readBoolean
				internalList.addElement(convertImpl(new SyncBuffer(d), d.readInt(), d.readInt(), d.readBoolean()));
				// lastObj.setSyncDirty(syncDirty)
			}
		} catch (EOFException e) {
			Logger.error("Reached unexpected EOF while loading connection data.");
		}
		loaded = true;
		dataLoadComplete(internalList.size());

	}

	private CodeSigningKey getSigningKey() {
		int moduleHandle = CodeModuleManager.getModuleHandle("BBSSH_Common");
		return CodeSigningKey.get(moduleHandle, "NOET");

	}

	/**
	 * Invoke this method at any time to save data to persistent store. Override it at your own risk.
	 */
	public void commitData() {
		commitData(true);
	}

	/**
	 * Invoked to commit data to eprsistent store or to sync/backup.
	 * 
	 * @param internal
	 */
	private synchronized final void commitData(boolean internal) {
		if (internalList == null) {
			Logger.error("Unexpected null internalList in commitData for " + this.getSyncName());
			return;
		}

		Vector v = new Vector();
		DataBuffer b = new DataBuffer(true);
		int max = internalList.size();
		// @TODO Hm - if we have a failure in writing one of thjese records,
		// our record count will be wrong...
		b.writeInt(max);// number of records
		for (int x = 0; x < max; x++) {
			SyncObject o = (SyncObject) internalList.elementAt(x);
			if (internal) {
				// Additional header when we're writing this data to persistent store.
				b.writeInt(getSyncVersion());
				b.writeInt(o.getUID());
				if (o instanceof DataObject) {
					b.writeBoolean(((DataObject) o).isSyncStateDirty());
				}
			}
			convertImpl(o, new SyncBuffer(b), getSyncVersion());
		}
		v.addElement(b.getArray());
		PersistentObject per = getPersistentObject();
		boolean saved = false;
		if (isSecureStoreRequired()) {
			CodeSigningKey key = getSigningKey();
			if (key == null) {
				Logger.error("Could not save data in secured store - failed to obtain signing key.");
			} else {
				per.setContents(new ControlledAccess(v, getSigningKey()));
				saved = true;
			}
		}
		if (!saved) {
			Logger.info("Saving data insecure.");
			per.setContents(v);
		}

		per.commit();

	}

	public boolean isLoaded() {
		return this.loaded;
	}

	public abstract long getPersistentStoreId();

	/**
	 * In the event that a purge of data is being performed, this is invoke dby the framework. At ime of invocation, all
	 * persistent values have been removed. Your override must re-set any internal defaults as requierd. Do not override
	 * if you have no defaults to reset.
	 */
	public void resetDefaults() {

	}

	/**
	 * s This is invoked when the persistent store is being cleared and recreated. Use this to clear any internal state,
	 * but do NOT reset any defaults - this will be invoked prior to repopulating data as well as when a purge occurs.
	 * Do not override if you have no internal state data.
	 */
	public void resetState() {

	}

	public final void purgePersistentContent() {

		internalList.removeAllElements();
		// Delete and recreate our persistent store
		PersistentStore.destroyPersistentObject(getPersistentStoreId());
		// Force reinitialize of persistent store.
		persistent = PersistentStore.getPersistentObject(getPersistentStoreId());
		// Allow derived classes to do their own cleanup and state reset.
		resetState();

		resetDefaults();

		commitData();
	}
}
