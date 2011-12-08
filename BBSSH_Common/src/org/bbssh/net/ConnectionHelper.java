/*
e *  Copyright (C) 2010 Marc A. Paradise
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
package org.bbssh.net;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.servicebook.ServiceRecord;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.system.WLANInfo;

import org.bbssh.model.Settings;
import org.bbssh.model.SettingsManager;

// @todo this must be version specific, to allow usage of the new connection tools in 5.0 and 6.0  ; also 
// retry properly upon failure. 
public class ConnectionHelper {
	public static final byte CONNECTION_TYPE_TCP = 0;
	public static final byte CONNECTION_TYPE_BES = 1;
	public static final byte CONNECTION_TYPE_WIFI = 2;
	public static final byte CONNECTION_TYPE_WAP2 = 3;
	public static final byte CONNECTION_TYPE_MDS_PUBLIC = 4;
	public static final byte CONNECTION_TYPE_AUTO = 5;
	public static final byte CONNECTION_TYPE_COUNT = 5;

	public static final byte PROXY_MODE_NONE = 0;
	public static final byte PROXY_MODE_PERSISTENT = 1;
	public static final byte PROXY_MODE_TRANSIENT = 1;

	public static HttpConnection getHttpConnection(byte connType, String URL) throws IOException {
		HttpConnection c = null;
		StringBuffer conn = new StringBuffer(URL);
		modifyConnectionString(connType, conn, 0);
		c = (HttpConnection) Connector.open(conn.toString());
		int rc = c.getResponseCode();
		if (rc != HttpConnection.HTTP_OK) {
			throw new IOException("HTTP Error: " + rc);
		}
		return c;
	}

	/**
	 * 
	 * @param connType
	 * @param conn
	 * @param attemptWifi
	 * @return true if the connection type provided can accept APN values.
	 */
	private static boolean modifyConnectionString(byte connType, StringBuffer conn, int timeout) {
		boolean apnOK = false;
		switch (connType) {
			case ConnectionHelper.CONNECTION_TYPE_TCP:
				// Socket connection. This is also the only one that acepts
				// additional APN connection info.
				conn.append(";deviceside=true");
				apnOK = true;
				break;
			case ConnectionHelper.CONNECTION_TYPE_BES:
				conn.append(";deviceside=false");
				if (timeout > 0) {
					conn.append(";ConnectionTimeout=").append(timeout * 1000);
				}
				break;
			case ConnectionHelper.CONNECTION_TYPE_WIFI:
				conn.append(";deviceside=true;interface=wifi");
				break;
			case ConnectionHelper.CONNECTION_TYPE_WAP2:
				// Note that we've arlaedy validated that WAP2 is available.
				conn.append(";ConnectionUID=").append(getWAP2UID());
				break;
			case ConnectionHelper.CONNECTION_TYPE_MDS_PUBLIC:
				conn.append(";deviceside=false;ConnectionType=mds-public;ConnectionTimeout=3600000");
				break;

		}
		return apnOK;
	}

	// @todo - clean this up.. too, maybe just getConnection?
	public static void configureConnectionString(byte connType, StringBuffer conn, int timeout) {
		boolean apnOK = modifyConnectionString(connType, conn, timeout);
		if (!apnOK) {
			return;
		}
		Settings s = SettingsManager.getSettings();
		String apn = s.getAPN();
		String userName = s.getAPNUserName();
		String password = s.getAPNPassword();
		if (apn != null && apn.length() > 0) {
			conn.append(";apn=").append(apn);
			if (userName != null && userName.length() > 0) {
				conn.append(";tunnelauthusername=").append(userName);
				if (password != null && password.length() > 0) {
					conn.append(";tunnelauthpassword=").append(password);
				}
			}
		}
	}

	/**
	 * Find WAP2 service book and return its UID. Note that it does this first by specifiying WPTCP records, then taking
	 * the first active record that is NOT wifi and NOT mms.
	 * 
	 * @return UID of WAP2 servicebook, or null if none exists.
	 */
	public static String getWAP2UID() {
		ServiceBook sb = ServiceBook.getSB();
		ServiceRecord[] records = sb.findRecordsByCid("WPTCP");
		String uid = null;

		for (int i = records.length - 1; i >= 0; i--) {
			if (records[i].isValid() && !records[i].isDisabled()) {
				// we assume here that getUid can't return null.
				String id = records[i].getUid().toLowerCase();
				if (id.indexOf("wifi") == -1 && id.indexOf("mms") == -1) {
					// @todo -- need someone to test this who has an ISP that allows unusual ports.
					uid = records[i].getUid();
					break;
				}
			}
		}
		return uid;
	}

	// not currently in use, a the service book name seems to vary internationally
	public static boolean isMDSPublicSupported() {
		// @todo : return CoverageInfo.isCoverageSufficient(CoverageInfo.COVERAGE_MDS))?

		ServiceBook sb = ServiceBook.getSB();
		ServiceRecord[] records = sb.findRecordsByCid("IPPP");
		if (records != null) {
			for (int i = records.length - 1; i >= 0; i--) {
				ServiceRecord rec = records[i];
				if (rec.isValid() && !rec.isDisabled() && rec.getName().toLowerCase().indexOf("bibs") > 0) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 
	 * @return true if wifi is enabled and currently connected.
	 */
	public static boolean isWifiAvailable() {
		return ((WLANInfo.getWLANState() == WLANInfo.WLAN_STATE_CONNECTED) && RadioInfo
				.areWAFsSupported(RadioInfo.WAF_WLAN));
	}

	public static int getPermissionForConnType(int i) {
		switch (i) {
			case ConnectionHelper.CONNECTION_TYPE_MDS_PUBLIC:
			case ConnectionHelper.CONNECTION_TYPE_WAP2:
			case ConnectionHelper.CONNECTION_TYPE_TCP:
				return ApplicationPermissions.PERMISSION_EXTERNAL_CONNECTIONS;

			case ConnectionHelper.CONNECTION_TYPE_BES:
				return ApplicationPermissions.PERMISSION_INTERNAL_CONNECTIONS;
			case ConnectionHelper.CONNECTION_TYPE_WIFI:
				return ApplicationPermissions.PERMISSION_WIFI;

		}
		return 0;
	}
}
