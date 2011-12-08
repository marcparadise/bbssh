package org.bbssh.model;

import java.io.EOFException;
import java.util.Vector;

import net.rim.device.api.synchronization.SyncObject;

import org.bbssh.io.SyncBuffer;
import org.bbssh.terminal.VT320;
import org.bbssh.util.Tools;

public class ConnectionManager extends DefaultSyncCollection {
	private static ConnectionManager me;
	public static final long CONNECTIONS_GUID = 0x22efb83d1a41163bL; // org.bbssh.model.ConnectionManager

	private ConnectionManager() {

	}

	public void initialize() {
		loadData();
	}

	public synchronized static ConnectionManager getInstance() {
		if (me == null) {
			me = new ConnectionManager();
		}
		return me;
	}

	public synchronized Vector getConnections() {
		return getDataVector();
	}

	/**
	 * Exposing the actual serialization of connection properties so that settingsmanager can include an embedded set of
	 * connection properties that represent the default new session values.
	 * 
	 * @param prop
	 * @param buffer
	 */
	public void serializeConnectionProperties(ConnectionProperties prop, SyncBuffer buffer) {
		FontSettings font = prop.getFontSettings();

		// BEGIN VERSION 0 FIELDS
		buffer.writeField(prop.getName());
		buffer.writeField(prop.getHost());
		buffer.writeField(prop.getUsername());
		buffer.writeField(prop.getPassword());
		buffer.writeField(prop.getKeyId());
		buffer.writeField(prop.getKeepAliveTime());
		buffer.writeField(prop.getTermType());
		buffer.writeField(prop.getTerminalRows());
		buffer.writeField(prop.getTerminalCols());
		buffer.writeField(prop.getSessionType());
		buffer.writeField(prop.getConnectionType());
		buffer.writeField(prop.isPollingIO());
		buffer.writeField(prop.getHttpProxyHost());
		buffer.writeField(prop.getHttpProxyMode());
		buffer.writeField(prop.getBackgroundColorIndex());
		buffer.writeField(prop.getForegroundColorIndex());
		buffer.writeField(prop.getDefaultInputMode());
		buffer.writeField(prop.getAutorunMacroName());
		buffer.writeField(font.getFontId());
		buffer.writeField(font.getFontType());
		buffer.writeField(font.getFontSize());
		// VERSION 1 FIELDS
		// available boolean.
		buffer.writeField(false);
		// 2
		buffer.writeField(prop.getKeepSizeOnVirtualKeyboardDisplay());
		// 3
		buffer.writeField(prop.getUseWifiIfAvailable());
		// 4
		buffer.writeField(prop.getBESTimeout());
		// 5
		buffer.writeField(prop.getScrollbackLines());
		// 6
		buffer.writeField(prop.getAltPrefixesMeta());
		// 7
		buffer.writeField(prop.getFunctionKeyMode());
		// 8 - no new fields, but we have some modified reading of colors.

	}

	protected boolean convertImpl(SyncObject object, SyncBuffer buffer, int version) {
		if (!(object instanceof ConnectionProperties))
			return false;
		serializeConnectionProperties((ConnectionProperties) object, buffer);
		return true;

	}

	public ConnectionProperties deserializeConnectionProperties(SyncBuffer buffer, int version) {
		ConnectionProperties prop = new ConnectionProperties(false);
		try {
			prop.setName(buffer.readNextStringField());
			prop.setHost(buffer.readNextStringField());
			prop.setUsername(buffer.readNextStringField());
			prop.setPassword(buffer.readNextStringField());
			prop.setKeyId(buffer.readNextIntField());
			prop.setKeepAliveTime(buffer.readNextIntField());
			prop.setTermType(buffer.readNextStringField());
			prop.setTerminalRows(buffer.readNextShortField());
			prop.setTerminalCols(buffer.readNextShortField());
			prop.setSessionType(buffer.readNextByteField());
			prop.setConnectionType(buffer.readNextByteField());
			prop.setPollingIO(buffer.readNextBooleanField());
			prop.setHttpProxyHost(buffer.readNextStringField());
			prop.setHttpProxyMode(buffer.readNextByteField());
			if (version >= 8) {
				prop.setBackgroundColorIndex(buffer.readNextIntField());
				prop.setForegroundColorIndex(buffer.readNextIntField());
			} else {
				// In version 8 and prior, color was an actual color value. We've changed it to an index into the color
				// table. We'll set up some reasonable defaults. The only problem here is if someone had a color value
				// of 1-7 and meant to have a very dark shade of blue ;)
				int bg = Tools.convertColorToANSITable(buffer.readNextIntField(), 0);
				int fg = Tools.convertColorToANSITable(buffer.readNextIntField(), 7);
				if (fg == bg) { 
					bg = 0; 
					fg = 7; 
				}
				prop.setBackgroundColorIndex(bg);
				prop.setForegroundColorIndex(fg);
			}
			prop.setDefaultInputMode(buffer.readNextByteField());
			prop.setAutorunMacroName(buffer.readNextStringField());
			byte id = buffer.readNextByteField();
			byte type = buffer.readNextByteField();
			byte size = buffer.readNextByteField();

			prop.setFontSettings(new FontSettings(type, id, size));
			if (version >= 1) {
				// available boolean field
				buffer.readNextBooleanField();
			}
			if (version >= 2) {
				prop.setKeepSizeOnVirtualKeyboardDisplay(buffer.readNextBooleanField());
			}
			if (version >= 3) {
				prop.setUseWifiIfAvailable(buffer.readNextBooleanField());
			}
			if (version >= 4) {
				prop.setBESTimeout(buffer.readNextIntField());
			}
			if (version >= 5) {
				prop.setScrollbackLines(buffer.readNextShortField());
			}
			if (version >= 6) {
				prop.setAltPrefixesMeta(buffer.readNextBooleanField());
			}
			if (version >= 7) {
				prop.setFunctionKeyMode(buffer.readNextByteField());
			} else {
				prop.setFunctionKeyMode(VT320.FK_LINUX);
			}
			// reserved: version > 8 modifies the color value loaded. next up: 9

		} catch (EOFException e) {
			prop = null;
		}
		return prop;

	}

	protected SyncObject convertImpl(SyncBuffer buffer, int version, int UID, boolean syncDirty) {
		ConnectionProperties prop = deserializeConnectionProperties(buffer, version);
		prop.setSyncStateDirty(syncDirty);
		prop.setUID(UID);
		return prop;

	}

	public String getSyncName() {
		return "BBSSH Saved Connections";
	}

	public int getSyncVersion() {
		return 8;
	}

	public long getPersistentStoreId() {
		return CONNECTIONS_GUID;
	}

	public ConnectionProperties getConnectionPropertiesById(String shortcutID) {
		int id;
		try {
			id = Integer.parseInt(shortcutID);
		} catch (NumberFormatException e) {
			return null;
		}
		// @todo let's just keep a hashtable of these referenced by ID..
		Vector v = getConnections();
		for (int x = v.size() - 1; x >= 0; x--) {
			ConnectionProperties p = (ConnectionProperties) v.elementAt(x);
			if (p == null) // just to be safe.
				continue;

			if (p.getUID() == id) {
				return p;
			}

		}
		return null;
	}

	public boolean isSecureStoreRequired() {
		return true;
	}

}
