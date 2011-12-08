/*
 *  Copyright (C) 2010  Marc A. Paradise
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
package org.bbssh.ssh.kex;

import java.io.IOException;

import net.rim.device.api.crypto.RandomSource;

import org.bbssh.ssh.SSHMessages;
import org.bbssh.ssh.packets.SshPacket2;
import org.bbssh.util.Tools;

/**
 * Container and converter for the Kex initialization packet, as defined in RFC 4253, section 7.1
 */
public class KexInitData {
	byte[] cookie;
	String[] kexAlgorithms;
	String[] serverHostKeyAlgorithms;
	String[] clientToServerCryptoAlgorithms;
	String[] serverToClientCryptoAlgorithms;
	String[] MACClientToServer;
	String[] MACServerToClient;
	String[] compressionClientToServer;
	String[] compressionServerToClient;
	String[] languagesClientToServer;
	String[] languagesServerToClient;
	boolean firstKEXPacketFollowing;
	int reserved;
	SshPacket2 data;

	// @todo hmac-sha1-96, hmac-md5, hmac-md5-96
	public static final String[] SUPPORTED_HMAC_ALGORITHMS = new String[] { "hmac-sha1" };
	// @todo "ZLIB"
	public static final String[] SUPPORTED_COMPRESSION = new String[] { "none" };
	// @todo "ssh-rsa"
	public static final String[] SUPPORTED_HOST_KEY_ALGORITHMS = new String[] { "ssh-dss", "ssh-rsa" };
	public static final String[] SUPPORTED_KEX_ALGORITHMS = new String[] {
			"diffie-hellman-group14-sha1",
			"diffie-hellman-group1-sha1" };

	private KexInitData() {
	}

	public static KexInitData createInstance() {
		// @todo - we can support the full range of allowed hashes
		// now that we've switched over to BB crypto.
		// Note that in all cases,we must list our preferred choice *first*
		KexInitData out = new KexInitData();
		out.cookie = RandomSource.getBytes(16);
		out.kexAlgorithms = SUPPORTED_KEX_ALGORITHMS;
		out.serverHostKeyAlgorithms = SUPPORTED_HOST_KEY_ALGORITHMS;
		out.clientToServerCryptoAlgorithms = CipherManager.getInstance().getSupportedCiphers();
		out.serverToClientCryptoAlgorithms = out.clientToServerCryptoAlgorithms;
		out.MACClientToServer = SUPPORTED_HMAC_ALGORITHMS;
		out.MACServerToClient = SUPPORTED_HMAC_ALGORITHMS;
		out.compressionClientToServer = SUPPORTED_COMPRESSION;
		out.compressionServerToClient = SUPPORTED_COMPRESSION;
		out.languagesClientToServer = new String[] {};
		out.languagesServerToClient = new String[] {};

		// @todo - we must support server sending 'first KEX' TRUE, AND guessing correctly!
		out.firstKEXPacketFollowing = false;
		out.reserved = 0;
		return out;
	}

	public static KexInitData createInstanceFromPacket(SshPacket2 packet) {
		KexInitData out = new KexInitData();
		out.cookie = packet.getBytes(16);
		out.kexAlgorithms = packet.getStringList();
		out.serverHostKeyAlgorithms = packet.getStringList();
		out.clientToServerCryptoAlgorithms = packet.getStringList();
		out.serverToClientCryptoAlgorithms = packet.getStringList();
		out.MACClientToServer = packet.getStringList();
		out.MACServerToClient = packet.getStringList();
		out.compressionClientToServer = packet.getStringList();
		out.compressionServerToClient = packet.getStringList();
		out.languagesClientToServer = packet.getStringList();
		out.languagesServerToClient = packet.getStringList();
		out.firstKEXPacketFollowing = packet.getByte() == 1;
		out.reserved = packet.getInt32();
		return out;
	}

	public SshPacket2 createOutboundPacket() {
		data = new SshPacket2(SSHMessages.SSH_MSG_KEXINIT);
		data.putBytes(cookie);
		data.putNameList(kexAlgorithms);
		data.putNameList(serverHostKeyAlgorithms);
		data.putNameList(clientToServerCryptoAlgorithms);
		data.putNameList(serverToClientCryptoAlgorithms);
		data.putNameList(MACClientToServer);
		data.putNameList(MACServerToClient);
		data.putNameList(compressionClientToServer);
		data.putNameList(compressionServerToClient);
		data.putNameList(languagesClientToServer);
		data.putNameList(languagesServerToClient);
		data.putByte(firstKEXPacketFollowing ? (byte) 1 : (byte) 0);
		data.putInt32(0);
		return data;
	}

	public String[] getMACClientToServer() {
		return MACClientToServer;
	}

	public String[] getMACServerToClient() {
		return MACServerToClient;
	}

	public String[] getClientToServerCryptoAlgorithms() {
		return clientToServerCryptoAlgorithms;
	}

	public String[] getCompressionClientToServer() {
		return compressionClientToServer;
	}

	public String[] getCompressionServerToClient() {
		return compressionServerToClient;
	}

	public byte[] getCookie() {
		return cookie;
	}

	public SshPacket2 getData() {
		return data;
	}

	public boolean isFirstKEXPacketFollowing() {
		return firstKEXPacketFollowing;
	}

	public String[] getKexAlgorithms() {
		return kexAlgorithms;
	}

	public String[] getLanguagesClientToServer() {
		return languagesClientToServer;
	}

	public String[] getLanguagesServerToClient() {
		return languagesServerToClient;
	}

	public int getReserved() {
		return reserved;
	}

	public String[] getServerHostKeyAlgorithms() {
		return serverHostKeyAlgorithms;
	}

	public String[] getServerToClientCryptoAlgorithms() {
		return serverToClientCryptoAlgorithms;
	}

	public static KexAgreement findAgreement(KexInitData s, KexInitData c) throws IOException {
		KexAgreement a = new KexAgreement();
		a.kexAlgorithm = Tools.findFirstMatchingElement(c.getKexAlgorithms(), s.getKexAlgorithms());
		a.serverHostKeyAlgorithm = Tools.findFirstMatchingElement(c
				.getServerHostKeyAlgorithms(), s.getServerHostKeyAlgorithms());
		a.clientToServerCryptoAlgorithm = Tools.findFirstMatchingElement(c
				.getClientToServerCryptoAlgorithms(), s.getClientToServerCryptoAlgorithms());
		a.serverToClientCryptoAlgorithm = Tools.findFirstMatchingElement(c
				.getServerToClientCryptoAlgorithms(), s.getServerToClientCryptoAlgorithms());
		a.MACClientToServer = Tools.findFirstMatchingElement(c.getMACClientToServer(), s.getMACClientToServer());
		a.MACServerToClient = Tools.findFirstMatchingElement(c.getMACServerToClient(), s.getMACServerToClient());
		a.languageClientToServer = Tools.findFirstMatchingElement(c
				.getLanguagesClientToServer(), s.getLanguagesClientToServer());
		a.languageServerToClient = Tools.findFirstMatchingElement(c
				.getLanguagesServerToClient(), s.getLanguagesServerToClient());
		a.compressionClientToServer = Tools.findFirstMatchingElement(c
				.getCompressionClientToServer(), s.getCompressionClientToServer());
		a.compressionServerToClient = Tools.findFirstMatchingElement(c.getCompressionServerToClient(), s
				.getCompressionServerToClient());
		// @todo better handling of errors an exception - perhaps even throw exception
		// in comparator itself? Not an IO exception, but AgreementFailedException...

		if (a.kexAlgorithm == null) {
			throw new IOException("Could not agree upon KEX algorithm");
		}
		if (a.serverHostKeyAlgorithm == null) {
			throw new IOException("Could not agree upon Server Host Key algorithm");
		}
		if (a.clientToServerCryptoAlgorithm == null) {
			throw new IOException("Could not agree upon C->S Crypto algorithm");
		}
		if (a.serverToClientCryptoAlgorithm == null) {
			throw new IOException("Could not agree upon S->C Crypto algorithm");
		}
		if (a.MACClientToServer == null) {
			throw new IOException("Could not agree upon C->S MAC algorithm");
		}
		if (a.MACServerToClient == null) {
			throw new IOException("Could not agree upon S->C MAC algorithm");
		}
		// Note that we may have an empty string, but should NEVER have null for these
		// last two items.
		if (a.compressionClientToServer == null) {
			throw new IOException("Could not agree upon C->S Compression algorithm");
		}
		if (a.compressionServerToClient == null) {
			throw new IOException("Could not agree upon S->C Compression algorithm");
		}
		// Language is entirely optional, so null is valid.
		/**
		 * if (a.languageClientToServer == null) { throw new IOException("Could not agree upon C->S language"); } if
		 * (a.languageServerToClient == null) { throw new IOException("Could not agree upon S->C language"); }
		 */
		return a;

	}
}
