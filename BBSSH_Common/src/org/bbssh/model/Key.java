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

import java.util.Date;

import net.rim.device.api.crypto.DSACryptoSystem;
import net.rim.device.api.crypto.DSAKeyPair;
import net.rim.device.api.crypto.DSAPrivateKey;
import net.rim.device.api.crypto.DSAPublicKey;
import net.rim.device.api.synchronization.SyncObject;
import net.rim.device.api.synchronization.UIDGenerator;

import org.bbssh.crypto.TypesReader;
import org.bbssh.util.Logger;

/**
 * Simple class that represents a named key. It contains data that represents either an imported key in e
 * 
 * @todo future: can we store this in the BB/RIM key ring?
 * @todo future: let's not store the unencrypted at all, we really only need to store the RIM key...
 */
public class Key implements SyncObject, DataObject {
	public final static int INVALID_ID = -1;
	private boolean nativeKey;

	private String sourceURL;
	private int id;
	private long dateAdded;
	private String friendlyName;
	private String passphrase;
	private byte[] data;
	private boolean syncDirty;

	/**
	 * Constructor for a RIM DSAKeyPair key.
	 * 
	 * @param friendlyName
	 * @param data - data in string-byte format: P/Q/G/pub/priv
	 */
	public Key(String friendlyName, byte[] data) {
		dateAdded = new Date().getTime();
		this.sourceURL = "";
		this.friendlyName = friendlyName;
		this.passphrase = "";
		this.data = data;
		nativeKey = true;
		syncDirty = true;
		id = UIDGenerator.getUID();
	}

	/**
	 * 
	 * @param sourceURL Original source of this key
	 * @param friendlyName
	 * @param data byte representation of PEM private key file.
	 * @param passphrase If 'data' is an encrypted key, this must be the passcode.
	 */
	public Key(String sourceURL, String friendlyName, byte[] data, String passphrase) {
		dateAdded = new Date().getTime();
		this.sourceURL = sourceURL;
		this.friendlyName = friendlyName;
		if (passphrase != null && passphrase.length() == 0) {
			this.passphrase = null;
		} else {
			this.passphrase = passphrase;
		}
		this.data = data;
		this.syncDirty = true;
		id = UIDGenerator.getUID();

	}

	public Key(String sourceURL, String friendlyName, byte[] data) {
		this(sourceURL, friendlyName, data, null);
	}

	public Key(int uID) {
		this.id = uID;
		this.syncDirty = false;

	}

	public byte[] getData() {
		return data;
	}

	private DSAKeyPair dsaKP;

	public DSAKeyPair getKeyPair() {
		if (dsaKP == null) {
			try {
				TypesReader r = new TypesReader(data);

				Logger.debug("Key.getKeyPair() - creating DSACryptoSystem");
				DSACryptoSystem dcs = new DSACryptoSystem(r.readByteString(), r.readByteString(), r.readByteString());
				byte[] pub = r.readByteString();
				byte[] priv = r.readByteString();
				Logger.debug("Key.getKeyPair() - creating creating DSAPrivateKey, DSAPublicKey and containing DSAKeyPair");
				dsaKP = new DSAKeyPair(new DSAPublicKey(dcs, pub), new DSAPrivateKey(dcs, priv));
			} catch (Throwable e) {
				Logger.error("DSAKeyPair creation resulted in exception: " + e.getClass().getName() + " : "
						+ e.getMessage());
			}

		}
		return dsaKP;

	}

	public Date getDateAdded() {
		return new Date(dateAdded);
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String toString() {
		return friendlyName;
	}

	public String getPassphrase() {
		return passphrase;
	}

	public void setPassphrase(String passphrase) {
		this.passphrase = passphrase;
	}

	public String getSourceURL() {
		return sourceURL;
	}

	public void setSourceURL(String sourceURL) {
		this.sourceURL = sourceURL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.synchronization.SyncObject#getUID()
	 */
	public int getUID() {
		return id;
	}

	/**
	 * @param dateAdded long represnetation of date this was created.
	 */
	protected void setDateAdded(long dateAdded) {
		this.dateAdded = dateAdded;
	}

	/**
	 * @param friendlyName name of this key used for display purposes.
	 */
	protected void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}

	/**
	 * @param key data
	 */
	protected void setData(byte[] data) {
		this.data = data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.bbssh.model.DataObject#isSyncStateDirty()
	 */
	public boolean isSyncStateDirty() {
		return syncDirty;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.bbssh.model.DataObject#setSyncStateDirty(boolean)
	 */
	public void setSyncStateDirty(boolean dirty) {
		syncDirty = dirty;

	}

	/**
	 * @return true if this is a rim crypto native key
	 */
	public boolean isNativeKey() {
		return this.nativeKey;
	}

	/**
	 * @param nativeKey sets indicator as to whether this is a RIM crypto native key
	 */
	public void setNativeKey(boolean nativeKey) {
		this.nativeKey = nativeKey;
	}
}
