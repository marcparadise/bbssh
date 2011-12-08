/*
 *  Copyright (C) 2010 Marc A. Paradise
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.bbssh.model;

import java.io.EOFException;
import java.io.IOException;
import java.util.Vector;

import net.rim.device.api.synchronization.SyncObject;
import net.rim.device.api.synchronization.UIDGenerator;

import org.bbssh.exceptions.KeyInUseException;
import org.bbssh.io.SyncBuffer;
import org.bbssh.util.Tools;

/**
 * This class manages public/private key pairs.
 */
public class KeyManager extends DefaultSyncCollection {
	private static KeyManager me;
	public static final long KEYSTORE_GUID = 0x76d582d56863f22cL;// org.bbssh.model.KeyManager

	
	private KeyManager() {

	}

	public static synchronized KeyManager getInstance() {
		if (me == null) {
			me = new KeyManager();
		}
		return me;
	}

	public void initialize() {
		loadData();
	}

	public synchronized int addKey(Key key) {
		key.setId(UIDGenerator.getUID());
		getDataVector().addElement(key);
		return key.getId();
	}

	/**
	 * Returns the number of connections using this key
	 * 
	 * @param key
	 * @return non-null vector of sessionsproperties of any users of this key. This list may be empty.
	 */
	public int getNumUsers(Key key) {
		Vector sessions = ConnectionManager.getInstance().getConnections();
		int keyId = key.getId();
		int count = sessions.size();
		int useCount = 0;
		for (int i = 0; i < count; ++i) {
			ConnectionProperties prop = (ConnectionProperties) sessions.elementAt(i);
			if (prop.getKeyId() == keyId) {
				useCount++;
			}
		}
		return useCount;
	}

	/**
	 * Safely removed the specified key. if any session has this key assigned, it will not permit the key to be removed.
	 * 
	 * @param key the key to remove
	 * @throws KeyInUseException if the key is in use and cannot be deleted.
	 */
	public void deleteKey(Key key) throws KeyInUseException {
		if (getNumUsers(key) > 0) {
			throw new KeyInUseException();
		}
		getDataVector().removeElement(key);
	}

	/**
	 * Returns a list of keys contained by this KeyManager.
	 * 
	 * @return true
	 */
	public Vector getKeys() {
		return getDataVector();
	}

	public int findKeyIndexById(int keyId) {
		if (keyId == -1)
			return -1;
		Vector keyList = getDataVector();
		int count = keyList.size();
		for (int i = 0; i < count; ++i) {
			Key key = (Key) keyList.elementAt(i);
			if (key.getId() == keyId) {
				return i;
			}
		}
		return -1;
	}

	public Key getKey(int keyId) {
		int x = findKeyIndexById(keyId);
		if (x > -1) {
			return (Key) getDataVector().elementAt(x);
		}
		return null;
	}

	/**
	 * Creates a new key and loads it frmo the specified source.
	 * 
	 * @param name
	 * @param source raw string of source key in OpenSSL format.
	 * @return key instance
	 * @throws IllegalArgumentException if source URL is not valid
	 * @throws IOException
	 */
	public static Key loadKey(String name, String source) throws IllegalArgumentException, IOException {
		// @todo we need to handle file, message (string), and URL sources.
		// @todo add applicationmenu option to import key attachmetn frmo email?
		// @todo how to send key by email or bb messenger
		StringBuffer data;
		if (source.startsWith("file://")) {
			data = Tools.getLocalFileContents(source);
		} else {
			data = Tools.getHTTPFileContents(source);
		}

		return new Key(source, name, data.toString().getBytes());
	}

	public boolean convertImpl(SyncObject object, SyncBuffer buffer, int version) {
		if (!(object instanceof Key))
			return false;

		Key key = (Key) object;

		// BEGIN VERSION 0 FIELDS
		buffer.writeField(key.getFriendlyName());
		buffer.writeField(key.getSourceURL());
		buffer.writeField(key.getDateAdded().getTime());
		buffer.writeField(key.getPassphrase());
		buffer.writeField(key.getData());
		// END VERSION 0 FIELDS
		if (version > 0) {
			buffer.writeField(key.isNativeKey());
		}

		return true;
	}

	public SyncObject convertImpl(SyncBuffer buffer, int version, int UID, boolean syncDirty) {
		Key ret = new Key(UID);
		ret.setSyncStateDirty(syncDirty);
		try {
			ret.setFriendlyName(buffer.readNextStringField());
			ret.setSourceURL(buffer.readNextStringField());
			ret.setDateAdded(buffer.readNextLongField());
			ret.setPassphrase(buffer.readNextStringField());
			ret.setData(buffer.readNextByteArrayField());
			if (version > 0) {
				ret.setNativeKey(buffer.readNextBooleanField());
			} else {
				ret.setNativeKey(false); // unsupported in v0 implementations.
			}
		} catch (EOFException e) {
			ret = null;
		}

		return ret;
	}

	public String getSyncName() {
		return "BBSSH Private Keys";
	}

	public int getSyncVersion() {
		return 1;
	}

	public long getPersistentStoreId() {
		return KEYSTORE_GUID;
	}

	public boolean isSecureStoreRequired() {
		return true;
	}

}
