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
package org.bbssh.model;

import net.rim.device.api.synchronization.SyncObject;
import net.rim.device.api.synchronization.UIDGenerator;

import org.bbssh.terminal.VT320;

/**
 * Defines a connectable SSH or Telnet session.
 */
public class ConnectionProperties implements SyncObject, DataObject {
	public static final byte SESSION_TYPE_SSH = 0;
	public static final byte SESSION_TYPE_TELNET = 1;

	private short terminalCols;
	private short terminalRows;
	private String name;
	private String host;
	private String username;
	private String password;
	private String termType;
	private byte sessionType;
	private byte connectionType;
	private boolean pollingIO;
	private String httpProxyHost;
	private byte httpProxyMode;
	private int backgroundColorIndex;
	private int foregroundColorIndex;
	private boolean isNew;
	private int keyId;
	private int keepAliveTime;
	private byte defaultInputMode;
	private String autorunMacroName;
	private FontSettings fontSettings;
	private int uid;
	private boolean dirty;

	private boolean keepSizeOnVirtualKeyboardDisplay;
	private boolean useWifiIfAvailable;
	private int BESTimeout;
	private short scrollbackLines;
	private boolean altPrefixesMeta;
	private byte functionKeyMode;
	private boolean captureEnabled;

	public ConnectionProperties(int uid) {
		this(false);
		this.uid = uid;

	}

	public ConnectionProperties(ConnectionProperties src, boolean isNew) {
		this(src);
		this.isNew = isNew;
	}

	public ConnectionProperties(ConnectionProperties src) {
		this.terminalCols = src.terminalCols;
		this.terminalRows = src.terminalRows;
		this.name = src.name;
		this.host = src.host;
		this.username = src.username;
		this.password = src.password;
		this.termType = src.termType;
		this.sessionType = src.sessionType;
		this.connectionType = src.connectionType;
		this.pollingIO = src.pollingIO;
		this.httpProxyHost = src.httpProxyHost;
		this.httpProxyMode = src.httpProxyMode;
		this.backgroundColorIndex = src.backgroundColorIndex;
		this.foregroundColorIndex = src.foregroundColorIndex;
		this.isNew = false;
		// @todo - change to key by name and not index?
		this.keyId = src.keyId;
		this.keepAliveTime = src.keepAliveTime;
		this.defaultInputMode = src.defaultInputMode;
		this.autorunMacroName = src.autorunMacroName;
		this.defaultInputMode = src.defaultInputMode;
		this.fontSettings = new FontSettings(src.fontSettings);
		this.keepSizeOnVirtualKeyboardDisplay = src.keepSizeOnVirtualKeyboardDisplay;
		this.useWifiIfAvailable = src.useWifiIfAvailable;
		this.dirty = false;
		this.uid = UIDGenerator.getUID();
		this.BESTimeout = src.BESTimeout;
		this.scrollbackLines = src.scrollbackLines;
		this.altPrefixesMeta = src.altPrefixesMeta;
		this.functionKeyMode = src.functionKeyMode;
	}

	public ConnectionProperties(boolean isNew) {
		backgroundColorIndex = 0; // black
		foregroundColorIndex = 7; // white;
		keyId = -1;
		keepAliveTime = 60;

		// Default to bitmap, lucida console, 8x13
		fontSettings = new FontSettings(FontSettings.FONT_BITMAP, (byte) 3, (byte) 3);
		this.isNew = isNew;
		this.dirty = true;
		if (isNew) {
			this.uid = UIDGenerator.getUID();
		}
		functionKeyMode = VT320.FK_LINUX;
		termType = "linux";
		keepSizeOnVirtualKeyboardDisplay = false;
		useWifiIfAvailable = true;
		scrollbackLines = 0;
		altPrefixesMeta = true;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getTermType() {
		return termType;
	}

	public void setTermType(String termType) {
		this.termType = termType;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String toString() {
		return name;
	}

	/**
	 * Get the session type, which must be one of the Session.SESSION_TYPE_* const values.
	 * 
	 * @return session type.
	 */
	public byte getSessionType() {
		return sessionType;
	}

	/**
	 * Get the connection type, which must be one of the Session.CONNECTION_TYPE_* const values.
	 * 
	 * @return connection type.
	 */
	public byte getConnectionType() {
		return connectionType;
	}

	/**
	 * Set the connection type for this session, one of the CONNECTION_TYPE_* const values.
	 * 
	 * @param connectionType
	 *            connection type.
	 */
	public void setConnectionType(byte connectionType) {
		this.connectionType = connectionType;
	}

	public void setSessionType(byte sessionType) {
		this.sessionType = sessionType;
	}

	public String getHttpProxyHost() {
		return httpProxyHost;
	}

	public void setHttpProxyHost(String httpProxyHost) {
		this.httpProxyHost = httpProxyHost;
	}

	public byte getHttpProxyMode() {
		return httpProxyMode;
	}

	public void setHttpProxyMode(byte httpProxyMode) {
		this.httpProxyMode = httpProxyMode;
	}

	public boolean isPollingIO() {
		return pollingIO;
	}

	public void setPollingIO(boolean pollingIO) {
		this.pollingIO = pollingIO;
	}

	public short getTerminalCols() {
		return terminalCols;
	}

	public void setTerminalCols(short terminalCols) {
		this.terminalCols = terminalCols;
	}

	public short getTerminalRows() {
		return terminalRows;
	}

	public void setTerminalRows(short terminalRows) {
		this.terminalRows = terminalRows;
	}

	public int getBackgroundColorIndex() {
		if (backgroundColorIndex > 7 || backgroundColorIndex < 0) 
			return 0; 
		return backgroundColorIndex;
	}

	public void setBackgroundColorIndex(int backgroundColorIndex) {
		if (backgroundColorIndex < 0 || backgroundColorIndex > 7)
			this.backgroundColorIndex = 0;
		else
			this.backgroundColorIndex = backgroundColorIndex;
	}

	public int getForegroundColorIndex() {
		if (foregroundColorIndex > 7 || foregroundColorIndex < 0) 
			return 7; 
		return foregroundColorIndex;
	}

	public void setForegroundColorIndex(int foregroundColorIndex) {
		if (foregroundColorIndex < 0 || foregroundColorIndex > 7)
			this.foregroundColorIndex = 7;
		else
			this.foregroundColorIndex = foregroundColorIndex;
	}

	public void setFontSettings(FontSettings settings) {
		this.fontSettings = settings;
	}

	public FontSettings getFontSettings() {
		return fontSettings;

	}

	public byte getDefaultInputMode() {
		return this.defaultInputMode;
	}

	public void setDefaultInputMode(byte defaultInputMode) {
		this.defaultInputMode = defaultInputMode;
	}

	public String getAutorunMacroName() {
		return this.autorunMacroName;
	}

	/**
	 * Sets the macro to be executed after a PTY session is opened.
	 * 
	 * @param name
	 *            the macro name to execute, or null/blank for no macro.
	 */

	public void setAutorunMacroName(String name) {
		this.autorunMacroName = name;
	}

	public boolean isNew() {
		return isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	/**
	 * Return true if the instances contain the same data.
	 * 
	 * @param o
	 *            object to compare to.
	 * @return true if the objects instance sare equal in value.
	 */
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o != this) {
			return false;
		}
		if (!(o instanceof ConnectionProperties)) {
			return false;
		}
		return true;
	}

	public int getKeyId() {
		return keyId;
	}

	public void setKeyId(int keyId) {
		this.keyId = keyId;
	}

	public int getKeepAliveTime() {
		return keepAliveTime;
	}

	public void setKeepAliveTime(int keepAliveTime) {
		this.keepAliveTime = keepAliveTime;
	}

	public boolean getKeepSizeOnVirtualKeyboardDisplay() {
		return this.keepSizeOnVirtualKeyboardDisplay;
	}

	public void setKeepSizeOnVirtualKeyboardDisplay(boolean keepSizeOnVirtualKeyboardDisplay) {
		this.keepSizeOnVirtualKeyboardDisplay = keepSizeOnVirtualKeyboardDisplay;
	}

	public int getUID() {
		return uid;
	}

	public String getUIDAsString() {
		return String.valueOf(uid);

	}

	public void setUID(int uid) {
		this.uid = uid;
	}

	public boolean isSyncStateDirty() {
		return dirty;
	}

	public void setSyncStateDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public boolean getUseWifiIfAvailable() {
		return useWifiIfAvailable;
	}

	public void setUseWifiIfAvailable(boolean useWifiIfAvailable) {
		this.useWifiIfAvailable = useWifiIfAvailable;

	}

	/**
	 * set BES timeout in seconds. May be ignored based on server policy.
	 * 
	 * @param BESTimeout
	 */
	public void setBESTimeout(int BESTimeout) {
		this.BESTimeout = BESTimeout;
	}

	/**
	 * @return get BES timeout in seconds, used when connecting.
	 */
	public int getBESTimeout() {
		return this.BESTimeout;
	}

	public void setScrollbackLines(short lines) {
		this.scrollbackLines = lines;
	}

	public short getScrollbackLines() {
		return scrollbackLines;
	}

	public void setAltPrefixesMeta(boolean altPrefixesMeta) {
		this.altPrefixesMeta = altPrefixesMeta;
	}

	public boolean getAltPrefixesMeta() {
		return altPrefixesMeta;
	}

	public void setFunctionKeyMode(byte functionKeyMode) {
		this.functionKeyMode = functionKeyMode;
	}

	public byte getFunctionKeyMode() {
		return functionKeyMode;
	}

	/**
	 * @return true if capture is enabled fro this session.
	 */
	public boolean isCaptureEnabled() {
		return captureEnabled;

	}

	/**
	 * Enable capture of this session to teminal log. note that this value is not persisted at this time.
	 * 
	 * @param captureEnabled
	 */
	public void setCaptureEnabled(boolean captureEnabled) {
		this.captureEnabled = captureEnabled;
	}
}
