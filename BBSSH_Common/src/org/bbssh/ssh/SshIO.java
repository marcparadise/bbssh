/*
 * This file was originally part of "The Java Telnet Application" prior to incorporation into midpssh and later bsshh.
 * 
 * The remaining original code is (c) Matthias L. Jugel, Marcus Mei�ner 1996-2002.
 * 
 * The file was changed by Radek Polak to work as midlet in MIDP 1.0 This file has been modified by Karl von Randow for
 * MidpSSH. This file has been modified and largely rewritten by Marc A. Paradise for BBSHH.
 * 
 * 
 * --LICENSE NOTICE-- This program is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA. --LICENSE NOTICE--
 */
package org.bbssh.ssh;

import java.io.IOException;
import java.util.Vector;

import net.rim.device.api.crypto.CryptoException;
import net.rim.device.api.crypto.CryptoTokenException;
import net.rim.device.api.crypto.CryptoUnsupportedOperationException;
import net.rim.device.api.crypto.DHCryptoSystem;
import net.rim.device.api.crypto.DHKeyAgreement;
import net.rim.device.api.crypto.DHPublicKey;
import net.rim.device.api.crypto.DSAKeyPair;
import net.rim.device.api.crypto.DSAPrivateKey;
import net.rim.device.api.crypto.InvalidCryptoSystemException;
import net.rim.device.api.crypto.InvalidKeyException;
import net.rim.device.api.crypto.KeyPair;
import net.rim.device.api.crypto.MD5Digest;
import net.rim.device.api.crypto.NoSuchAlgorithmException;
import net.rim.device.api.crypto.RSAKeyPair;
import net.rim.device.api.crypto.RSAPrivateKey;
import net.rim.device.api.crypto.RandomSource;
import net.rim.device.api.crypto.SHA1Digest;
import net.rim.device.api.crypto.UnsupportedCryptoSystemException;

import org.bbssh.crypto.SignatureTools;
import org.bbssh.crypto.TypesReader;
import org.bbssh.model.ConnectionProperties;
import org.bbssh.model.Key;
import org.bbssh.net.session.Session;
import org.bbssh.net.session.SshSession;
import org.bbssh.ssh.kex.CipherAttributes;
import org.bbssh.ssh.kex.CipherManager;
import org.bbssh.ssh.kex.KexAgreement;
import org.bbssh.ssh.kex.KexInitData;
import org.bbssh.ssh.kex.KexStateData;
import org.bbssh.ssh.packets.PacketUserauthRequestPublicKey;
import org.bbssh.ssh.packets.SshPacket;
import org.bbssh.ssh.packets.SshPacket2;
import org.bbssh.ssh.v2.SshCrypto2;
import org.bbssh.util.Logger;
import org.bbssh.util.Tools;
import org.bbssh.util.Version;

import ch.ethz.ssh2.crypto.PEMDecoder;

/**
 * Secure Shell IO
 * 
 * @author Marcus Meissner
 * @version $Id: SshIO.java 517 2008-03-14 04:17:55Z karlvr $
 */
public class SshIO {
	/** Authentication mode - publickey */
	public static final byte MODE_PUBLICKEY = 1;
	/** Authentication mode - password */
	public static final byte MODE_PASSWORD = 2;
	/** Authentication mode - keyboard interactive */
	public static final byte MODE_KEYBOARD_INTERACTIVE = 3;

	/** We have startred up and have a valid connection */
	public static final byte STATE_INIT = 0;
	/** Initializtion is ocmplete, waiting for KEX init from server */
	public static final byte STATE_INIT_COMPLETE = 1;
	/** We are waiting for KEX_INIT */
	public static final byte STATE_KEX_INIT = 2;
	/** We have begun key exchange */
	public static final byte STATE_KEX_DH = 3;
	/** KEX_INIT is completed, and we are starting DH exchange */
	public static final byte STATE_KEX_DH_INIT = 4;
	/** KEX is fully complete, and we have a secure link but are not authenticated */
	public static final byte STATE_SECURE = 5;
	/** Opening channels */
	public static final byte STATE_OPENING_CHANNEL = 6;
	/** We've requested auth and are awaiting reply */
	public static final byte STATE_REQUESTING_AUTH = 7;
	/** Authentication is in progress */
	public static final byte STATE_AUTHENTICATING = 8;
	/** this state indicates that we're fully connected and negotiated, and authenticating. */
	public static final byte STATE_ONLINE = 9;
	/** eof has been sent, no further outbound data is allowed. */
	public static final byte STATE_EOF_SENT = 10;
	/** eof has been sent, no further inbound data is allowed. */
	public static final byte STATE_EOF_RECEIVED = 10;
	/** disconnected */
	public static final byte STATE_OFFLINE = 11;

	/** Key exchange has failed */
	public static final byte STATE_KEX_FAILED = 127;

	private SshSession sshSession;
	private SshPacket2 requestFailurePacket = new SshPacket2(SSHMessages.SSH_MSG_REQUEST_FAILURE);
	private SshPacket2 channelFailurePacket = new SshPacket2(SSHMessages.SSH_MSG_CHANNEL_FAILURE);
	/**
	 * key exchange state, containing client and server key data and agreement info.
	 */
	KexStateData kexState = null;
	/**
	 * variables for the connection
	 */
	private String remoteSystemId = ""; // ("SSH-<protocolmajor>.<protocolminor>-<version>\n")
	private SshCrypto2 crypto2;
	private int remoteId = -1;
	/**
	 * Username/password for auth.
	 * 
	 * @todo cleanup required.
	 */
	private String userName, password;
	protected StringBuffer dataToSend = new StringBuffer();
	private byte state;
	private byte authmode;
	// phase : handleBytes
	/**
	 * Used during state STATE_KEX_DH_INIT, indicates to ignore the next inbound packet due to server making wrong
	 * guesses as to crypto handshake
	 */
	private boolean ignoreNext;
	protected int outgoingseq = 0;
	SshPacket currentpacket;
	ConnectionProperties properties;
	/** Window size is the amount of data which */
	private int inboundWindowSize = 0x100000; // 1MB
	// note - original code had a 16k max, however spec says we MUST support packet
	// size of at LEAST 32k
	private int inboundMaxPacketSize = 0x8000; // //32k
	/**
	 * max outbound window size provided to us for the channel we request.
	 */
	private int outboundWindowSize = 0; // 1MB
	/**
	 * The channel ID of our terminal. This may need to be revisited as we expand to support additional channels, such
	 * as that required for tunnelling.
	 */
	private int localChannelId = -1;
	
	// Some debugging tools for cases where we seem to be misplacing channel data. 
	public long dataAcceptedBytes = 0;
	public long dataReceivedLen = 0;

	/**
	 * connection has been closed for one reason or another.
	 * 
	 * @todo not implemented yet.
	 */
	public static final byte STATE_DISCONNECTED = 10;
	/**
	 * diffie-hellman-group-1 (oakley group 2) prime as specified in http://www.ietf.org/rfc/rfc2409.txt section 6.2
	 */
	public static final byte[] DIFFIE_HELLMAN_GROUP_1 = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
			(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xC9, (byte) 0x0F, (byte) 0xDA,
			(byte) 0xA2, (byte) 0x21, (byte) 0x68, (byte) 0xC2, (byte) 0x34, (byte) 0xC4, (byte) 0xC6, (byte) 0x62,
			(byte) 0x8B, (byte) 0x80, (byte) 0xDC, (byte) 0x1C, (byte) 0xD1, (byte) 0x29, (byte) 0x02, (byte) 0x4E,
			(byte) 0x08, (byte) 0x8A, (byte) 0x67, (byte) 0xCC, (byte) 0x74, (byte) 0x02, (byte) 0x0B, (byte) 0xBE,
			(byte) 0xA6, (byte) 0x3B, (byte) 0x13, (byte) 0x9B, (byte) 0x22, (byte) 0x51, (byte) 0x4A, (byte) 0x08,
			(byte) 0x79, (byte) 0x8E, (byte) 0x34, (byte) 0x04, (byte) 0xDD, (byte) 0xEF, (byte) 0x95, (byte) 0x19,
			(byte) 0xB3, (byte) 0xCD, (byte) 0x3A, (byte) 0x43, (byte) 0x1B, (byte) 0x30, (byte) 0x2B, (byte) 0x0A,
			(byte) 0x6D, (byte) 0xF2, (byte) 0x5F, (byte) 0x14, (byte) 0x37, (byte) 0x4F, (byte) 0xE1, (byte) 0x35,
			(byte) 0x6D, (byte) 0x6D, (byte) 0x51, (byte) 0xC2, (byte) 0x45, (byte) 0xE4, (byte) 0x85, (byte) 0xB5,
			(byte) 0x76, (byte) 0x62, (byte) 0x5E, (byte) 0x7E, (byte) 0xC6, (byte) 0xF4, (byte) 0x4C, (byte) 0x42,
			(byte) 0xE9, (byte) 0xA6, (byte) 0x37, (byte) 0xED, (byte) 0x6B, (byte) 0x0B, (byte) 0xFF, (byte) 0x5C,
			(byte) 0xB6, (byte) 0xF4, (byte) 0x06, (byte) 0xB7, (byte) 0xED, (byte) 0xEE, (byte) 0x38, (byte) 0x6B,
			(byte) 0xFB, (byte) 0x5A, (byte) 0x89, (byte) 0x9F, (byte) 0xA5, (byte) 0xAE, (byte) 0x9F, (byte) 0x24,
			(byte) 0x11, (byte) 0x7C, (byte) 0x4B, (byte) 0x1F, (byte) 0xE6, (byte) 0x49, (byte) 0x28, (byte) 0x66,
			(byte) 0x51, (byte) 0xEC, (byte) 0xE6, (byte) 0x53, (byte) 0x81, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
			(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
	/**
	 * diffie-hellman-group-14 (oakley group 14) prime as specified in http://www.ietf.org/rfc/rfc3526.txt section 3
	 */
	public static final byte[] DIFFIE_HELLMAN_GROUP_14 = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
			(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xC9, (byte) 0x0F, (byte) 0xDA,
			(byte) 0xA2, (byte) 0x21, (byte) 0x68, (byte) 0xC2, (byte) 0x34, (byte) 0xC4, (byte) 0xC6, (byte) 0x62,
			(byte) 0x8B, (byte) 0x80, (byte) 0xDC, (byte) 0x1C, (byte) 0xD1, (byte) 0x29, (byte) 0x02, (byte) 0x4E,
			(byte) 0x08, (byte) 0x8A, (byte) 0x67, (byte) 0xCC, (byte) 0x74, (byte) 0x02, (byte) 0x0B, (byte) 0xBE,
			(byte) 0xA6, (byte) 0x3B, (byte) 0x13, (byte) 0x9B, (byte) 0x22, (byte) 0x51, (byte) 0x4A, (byte) 0x08,
			(byte) 0x79, (byte) 0x8E, (byte) 0x34, (byte) 0x04, (byte) 0xDD, (byte) 0xEF, (byte) 0x95, (byte) 0x19,
			(byte) 0xB3, (byte) 0xCD, (byte) 0x3A, (byte) 0x43, (byte) 0x1B, (byte) 0x30, (byte) 0x2B, (byte) 0x0A,
			(byte) 0x6D, (byte) 0xF2, (byte) 0x5F, (byte) 0x14, (byte) 0x37, (byte) 0x4F, (byte) 0xE1, (byte) 0x35,
			(byte) 0x6D, (byte) 0x6D, (byte) 0x51, (byte) 0xC2, (byte) 0x45, (byte) 0xE4, (byte) 0x85, (byte) 0xB5,
			(byte) 0x76, (byte) 0x62, (byte) 0x5E, (byte) 0x7E, (byte) 0xC6, (byte) 0xF4, (byte) 0x4C, (byte) 0x42,
			(byte) 0xE9, (byte) 0xA6, (byte) 0x37, (byte) 0xED, (byte) 0x6B, (byte) 0x0B, (byte) 0xFF, (byte) 0x5C,
			(byte) 0xB6, (byte) 0xF4, (byte) 0x06, (byte) 0xB7, (byte) 0xED, (byte) 0xEE, (byte) 0x38, (byte) 0x6B,
			(byte) 0xFB, (byte) 0x5A, (byte) 0x89, (byte) 0x9F, (byte) 0xA5, (byte) 0xAE, (byte) 0x9F, (byte) 0x24,
			(byte) 0x11, (byte) 0x7C, (byte) 0x4B, (byte) 0x1F, (byte) 0xE6, (byte) 0x49, (byte) 0x28, (byte) 0x66,
			(byte) 0x51, (byte) 0xEC, (byte) 0xE4, (byte) 0x5B, (byte) 0x3D, (byte) 0xC2, (byte) 0x00, (byte) 0x7C,
			(byte) 0xB8, (byte) 0xA1, (byte) 0x63, (byte) 0xBF, (byte) 0x05, (byte) 0x98, (byte) 0xDA, (byte) 0x48,
			(byte) 0x36, (byte) 0x1C, (byte) 0x55, (byte) 0xD3, (byte) 0x9A, (byte) 0x69, (byte) 0x16, (byte) 0x3F,
			(byte) 0xA8, (byte) 0xFD, (byte) 0x24, (byte) 0xCF, (byte) 0x5F, (byte) 0x83, (byte) 0x65, (byte) 0x5D,
			(byte) 0x23, (byte) 0xDC, (byte) 0xA3, (byte) 0xAD, (byte) 0x96, (byte) 0x1C, (byte) 0x62, (byte) 0xF3,
			(byte) 0x56, (byte) 0x20, (byte) 0x85, (byte) 0x52, (byte) 0xBB, (byte) 0x9E, (byte) 0xD5, (byte) 0x29,
			(byte) 0x07, (byte) 0x70, (byte) 0x96, (byte) 0x96, (byte) 0x6D, (byte) 0x67, (byte) 0x0C, (byte) 0x35,
			(byte) 0x4E, (byte) 0x4A, (byte) 0xBC, (byte) 0x98, (byte) 0x04, (byte) 0xF1, (byte) 0x74, (byte) 0x6C,
			(byte) 0x08, (byte) 0xCA, (byte) 0x18, (byte) 0x21, (byte) 0x7C, (byte) 0x32, (byte) 0x90, (byte) 0x5E,
			(byte) 0x46, (byte) 0x2E, (byte) 0x36, (byte) 0xCE, (byte) 0x3B, (byte) 0xE3, (byte) 0x9E, (byte) 0x77,
			(byte) 0x2C, (byte) 0x18, (byte) 0x0E, (byte) 0x86, (byte) 0x03, (byte) 0x9B, (byte) 0x27, (byte) 0x83,
			(byte) 0xA2, (byte) 0xEC, (byte) 0x07, (byte) 0xA2, (byte) 0x8F, (byte) 0xB5, (byte) 0xC5, (byte) 0x5D,
			(byte) 0xF0, (byte) 0x6F, (byte) 0x4C, (byte) 0x52, (byte) 0xC9, (byte) 0xDE, (byte) 0x2B, (byte) 0xCB,
			(byte) 0xF6, (byte) 0x95, (byte) 0x58, (byte) 0x17, (byte) 0x18, (byte) 0x39, (byte) 0x95, (byte) 0x49,
			(byte) 0x7C, (byte) 0xEA, (byte) 0x95, (byte) 0x6A, (byte) 0xE5, (byte) 0x15, (byte) 0xD2, (byte) 0x26,
			(byte) 0x18, (byte) 0x98, (byte) 0xFA, (byte) 0x05, (byte) 0x10, (byte) 0x15, (byte) 0x72, (byte) 0x8E,
			(byte) 0x5A, (byte) 0x8A, (byte) 0xAC, (byte) 0xAA, (byte) 0x68, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
			(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF

	};

	/**
	 * Generator for DH-group-14 and DH-group-1 is 2.
	 */
	public static final byte[] DIFFIE_HELLMAN_GENERATOR = new byte[] { 2 };

	// authentication
	/**
	 * Initialise SshIO
	 * 
	 * @param sshSession
	 */
	public SshIO(SshSession sshSession) {

		this.sshSession = sshSession;
		// @todo this is REALLY ugly... This also needs to be cleaned up with session hanlding rewrite.
		this.properties = sshSession.getProperties();

	}

	/**
	 * write data to our back end
	 * 
	 * @param b
	 *            data to write
	 */
	public void write(byte[] b) throws IOException {
		sshSession.sendData(b);
	}

	/**
	 * Send SSHMessages.SSH_MSG_DISCONNECT.
	 * 
	 * @param reason
	 *            - reason code: reference RFC 4253 S 11.1 for details.
	 * @param reasonText
	 *            human-readable disconnect reason.
	 * @throws IOException
	 */
	public void sendDisconnect(int reason, String reasonText) throws IOException {
		if (remoteId != -1) {

			SshPacket2 pn = new SshPacket2(SSHMessages.SSH_MSG_DISCONNECT);
			pn.putInt32(reason);
			pn.putString(reasonText);
			pn.putString("en");
			sendPacket2(pn);
			setState(STATE_DISCONNECTED);
		}
	}

	/**
	 * When connected, send outbound data. If not connected, the data will be buffered for when the connection is
	 * available.
	 * 
	 * @param data
	 * @param offset
	 * @param length
	 * @throws IOException
	 */
	public void sendData(byte[] data, int offset, int length) throws IOException {
		dataToSend.append(new String(data, offset, length));
		if (state == STATE_ONLINE) {
			// if (sshSession.getOutputBufferLength() == 0) {
			sendOutboundChannelData();
			// }
		} else if (state >= STATE_EOF_SENT) {
			Logger.warn("Received request to send channel data after EOF sent. Ignoring.");
		}
	}

	/**
	 * If any outbound data is queued, send it in a CHANNEL_DATA packet and reset the data queue.
	 * 
	 * @throws IOException
	 */
	protected void sendOutboundChannelData() throws IOException {
		if (dataToSend == null)
			return;
		SshPacket2 pn = new SshPacket2(SSHMessages.SSH_MSG_CHANNEL_DATA);
		pn.putInt32(remoteId); // channel id
		pn.putString(dataToSend.toString());
		sendPacket2(pn);

		outboundWindowSize -= dataToSend.length();
		dataToSend = new StringBuffer();

	}

	/**
	 * Handles all incoming data when we haev not yet initialized version strings.
	 * 
	 * @param buff
	 *            inbound data buffer
	 * @param offset
	 *            starting offset within the buffer
	 * @param offsetEnd
	 *            ending offset within the buffer
	 * @return new offset.
	 * @throws IOException
	 */
	private int handleInitState(byte[] buff, int offset, int length, int offsetEnd) throws IOException {
		byte b; // of course, byte is a signed entity (-128 -> 127)

		while (offset < offsetEnd) {
			b = buff[offset++];
			// both sides MUST send an identification string of the form
			// "SSH-protocolversion-softwareversion comments",
			// followed by newline character(asc 10)
			remoteSystemId += (char) b;

			// Note if server version is 1.99 then LINE FEED (\r - asc 13)
			// MAY not be sent, but CR (\n - asc 10) WILL still be sent.
			if (b == '\n') {
				if (remoteSystemId.startsWith("SSH-")) {
					int remotemajor = Integer.parseInt(remoteSystemId.substring(4, 5));
					String minorverstr = remoteSystemId.substring(6, 8);
					if (!Character.isDigit(minorverstr.charAt(1))) {
						minorverstr = minorverstr.substring(0, 1);
					}
					int remoteminor = Integer.parseInt(minorverstr);

					// reference RFC4253 section 5.1
					if (remotemajor == 1 && remoteminor == 99) {
						remotemajor = 2;
						remoteminor = 0;
					}

					if (remotemajor != 2) {
						throw new IOException("Unsupported SSH version: " + remotemajor);
					}
					setState(STATE_INIT_COMPLETE);
					currentpacket = new SshPacket2();
					break;
				} else {
					// Lines sent during init that do not start SSH- SHOULDbe ignored
					remoteSystemId = "";
				}
			}
		}
		return offset;
	}

	private void setState(byte state) {
		this.state = state;
		if (state < STATE_ONLINE) {
			sshSession.setConnectionState(Session.CONNSTATE_CONNECTING);
		} else if (state == STATE_ONLINE) {
			sshSession.setConnectionState(Session.CONNSTATE_CONNECTED);
		} else if (state <= STATE_DISCONNECTED) {
			sshSession.setConnectionState(Session.CONNSTATE_DISCONNECTING);
		} else {
			// This is up to our container - disconnection occurs when the network state
			// is set to disconected; we can only say that the protocol is
			// disconnected.
			// sshSession.setConnectionState(Session.CONNSTATE_DISCONNECTED);
		}
	}

	/**
	 * If server has also sent first KEX packet, we must check to make ure that server's "guesses" are valid. Wrong
	 * guesses means that the next packet will be sent based on wrong assumptions (eg, wrong KEX algorithm means that
	 * the next KEX algorithm packet can't be the right one), and so we must ignore it.
	 */
	private void checkIgnoreNextPacket() {
		// @todo we must check ALL possible matches - as the spec does not exclude any of them
		if (!Tools.doFirstElementsMatch(kexState.serverKEX.getKexAlgorithms(), kexState.clientKEX.getKexAlgorithms())
				|| !Tools.doFirstElementsMatch(kexState.serverKEX.getServerHostKeyAlgorithms(), kexState.clientKEX
						.getServerHostKeyAlgorithms())
				|| !Tools.doFirstElementsMatch(kexState.serverKEX.getMACClientToServer(), kexState.clientKEX
						.getMACClientToServer())
				|| !Tools.doFirstElementsMatch(kexState.serverKEX.getMACServerToClient(), kexState.clientKEX
						.getMACServerToClient())) {

			ignoreNext = true;

		}

	}

	private String handleChannelOpenConfirmation2(SshPacket2 p) throws IOException {
		localChannelId = p.getInt32(); // localId
		remoteId = p.getInt32();
		// Open PTY
		SshPacket2 pn = new SshPacket2(SSHMessages.SSH_MSG_CHANNEL_REQUEST);
		pn.putInt32(remoteId);
		pn.putString("pty-req");
		pn.putByte((byte) 0); // want reply- no (for now)
		pn.putString(getTerminalID());
		// @todo maybe we want to enable replies so we can confirm that
		// this request was accepted, before we send the next one or "shell"?
		pn.putInt32(getTerminalWidth());
		pn.putInt32(getTerminalHeight());
		pn.putInt32(0);
		pn.putInt32(0);
		pn.putString("");
		sendPacket2(pn);

		// Open Shell
		pn = new SshPacket2(SSHMessages.SSH_MSG_CHANNEL_REQUEST);
		pn.putInt32(remoteId);
		pn.putString("shell");
		// @todo maybe we want to enable replies so we can confirm that
		// this request was accepted, before we begin shunting data along the pipe?
		pn.putByte((byte) 0); // want reply (for now)
		sendPacket2(pn);
		setState(STATE_ONLINE);
		sendOutboundChannelData();
		return "Shell opened\r\n";

	}

	private String handleUserAuthFailure2(SshPacket2 p) throws IOException {
		if (state != STATE_AUTHENTICATING) {
			throw new IOException("Received authentication failure ");
		}

		String message = authenticate2();
		if (message != null) {
			return message;
		} else {
			// Session.CONN_STATE_AUTH_FAILED
			return "Authentication failed.\r\nAvailable methods are: " + p.getString() + "\r\n";
		}

	}

	private String handleUserAuthBanner(SshPacket2 p) throws IOException {
		if (state >= STATE_REQUESTING_AUTH && state < STATE_ONLINE) {
			String message = p.getString();
			// p.getString -- language tag
			Logger.debug("BANNER received: " + message);
			return "\r\n" + message + "\r\n";

		}
		Logger.error("BANNER received and not expected: not in auth.");
		return "";

	}

	private String handleUserAuthSuccess2(SshPacket2 p) throws IOException {
		if (state != STATE_AUTHENTICATING) {
			// @todo in these cases, our exception handling mUST send a disconnect.
			throw new IOException("Received authentication success with no auth request outstanding. ");
		}
		setState(STATE_OPENING_CHANNEL);

		// Open channel
		SshPacket2 pn = new SshPacket2(SSHMessages.SSH_MSG_CHANNEL_OPEN);
		pn.putString("session");
		pn.putInt32(0); // sender-channel ID
		pn.putInt32(inboundWindowSize); // initial window size
		pn.putInt32(inboundMaxPacketSize); // max packet size
		sendPacket2(pn);
		return "Authentication accepted\r\n";

	}

	public void sendUserauthInfoResponse(String[] responses) throws IOException {
		if (state != STATE_AUTHENTICATING) {
			// @todo in these cases, our exception handling mUST send a disconnect.
			throw new IOException("Received AUTH REQUEST message with no auth request outstanding. ");
		}
		SshPacket2 buf = new SshPacket2(SSHMessages.SSH_MSG_USERAUTH_INFO_RESPONSE);
		buf.putInt32(responses.length);
		for (int i = 0; i < responses.length; i++) {
			buf.putString(responses[i]);
		}
		sendPacket2(buf);
	}

	/**
	 * Acceptp data from the remote host. Blocks until data is available. Dispatches to appropriate method based on
	 * state (handleInitState or handlePacket)
	 * 
	 * @param buff
	 *            input buffer to parse.
	 * @param offset
	 *            starting point within the buffer
	 * @param length
	 *            number of bytes to use from the buffer.
	 * @return array of bytes to be displayed.
	 * @throws IOException
	 */
	public byte[] handleSSH(byte buff[], int offset, int length, Session session) throws IOException {
		String result;
		int end = offset + length;
		if (state == STATE_INIT) {
			offset = handleInitState(buff, offset, length, end);
		}
		result = "";

		// While we haven't consumed all of the data, stuff it into one more more packets.
		while (offset < end) {
			offset = currentpacket.addPayload(buff, offset, (end - offset));
			if (currentpacket.isFinished()) {
				result = result + handlePacket2((SshPacket2) currentpacket, session);
				if ((state < STATE_ONLINE || state == STATE_DISCONNECTED) && result != null && result.length() > 0) {
					Logger.info(result);
				}
				currentpacket = new SshPacket2(crypto2);
			}
		}

		return result.getBytes();
	}

	/**
	 * Given a host key return the finger print string for that key.
	 * 
	 * @param host_key
	 * @return
	 */
	private String fingerprint(byte[] host_key) {
		// @todo - is this all we use this member for? If so,
		// it shouldn't be a member at all... never mind a STATIC member.
		MD5Digest digest = new MD5Digest();
		digest.update(host_key);
		byte[] fprint = digest.getDigest();
		return Tools.getBytesAsHexString(fprint, fprint.length);
		// @todo host key validation has to occur - easiest to do it based
		// on hash -> hostname mapping?
	}

	/**
	 * Handle SSH protocol Version 2
	 * 
	 * @param p
	 *            the packet we will process here.
	 * @param session
	 *            the session sending us this data.
	 * @return a array of bytes
	 */
	protected String handlePacket2(SshPacket2 p, Session session) throws IOException {
		sshSession.inputPacketCount++;
		if (state == STATE_KEX_DH_INIT) {
			if (ignoreNext) {
				ignoreNext = false;
				return "Invalid algorithm assumed by server, discarding...\r\n";
			}
		}
		byte type = p.getType();
		// don't assemble the string if there's no need...

		switch (type) {

			case SSHMessages.SSH_MSG_KEXINIT: {
				try {
					return handleKexInit2(p);
				} catch (IOException e) {
					sendDisconnect(3, e.getMessage());
					return e.getMessage();
				}

			}
			case SSHMessages.SSH_MSG_KEXDH_REPLY:
				try {
					return handleKexDHReply2(p);
				} catch (IOException e) {
					sendDisconnect(3, e.getMessage());
					return e.getMessage();
				}

			case SSHMessages.SSH_MSG_NEWKEYS: {
				// @todo make sure this works for ongoing sessions (rekeying)
				try {
					return handleNewKeysRequest2(p);
				} catch (IOException e) {
					sendDisconnect(3, "Key exchange failed.");
					return e.getMessage();
				}
			}

			case SSHMessages.SSH_MSG_SERVICE_ACCEPT:
				if (state != STATE_REQUESTING_AUTH) {
					throw new IOException("Received SERVICE_ACCEPT for unrecognized service: " + p.getString());
				} else {
					Logger.info("SERVICE_ACCEPT received: " + p.getString());
				}
				// Server has accepted our auth service request, let's start actual authentication now.
				setState(STATE_AUTHENTICATING);
				return authenticate2();
			case SSHMessages.SSH_MSG_USERAUTH_BANNER:
				return handleUserAuthBanner(p);

			case SSHMessages.SSH_MSG_USERAUTH_FAILURE:
				return handleUserAuthFailure2(p);

			case SSHMessages.SSH_MSG_USERAUTH_INFO_REQUEST:
				sendUserauthInfoResponse(handleUserAuthInfoRequest(p));
				break;

			case SSHMessages.SSH_MSG_CHANNEL_OPEN_FAILURE:
				throw new IOException("Server refused channel open request, cannot continue.");

			case SSHMessages.SSH_MSG_USERAUTH_SUCCESS:
				return handleUserAuthSuccess2(p);

			case SSHMessages.SSH_MSG_CHANNEL_OPEN_CONFIRMATION:
				return handleChannelOpenConfirmation2(p);
				// From the spec, we *shouldn't get this* unless the server
				// requests a channel open and we accept...
			case SSHMessages.SSH_MSG_CHANNEL_WINDOW_ADJUST:
				return handleChannelWindowAdjust(p);

			case SSHMessages.SSH_MSG_CHANNEL_DATA:
				// @todo We'll need a ChannelHandler: onWindowAdjust, onData, onClose, onOpenConfirm
				return handleChannelData(p, session);

			case SSHMessages.SSH_MSG_CHANNEL_EOF:
				// Indicates we wil receive no more data on this channel.
				Logger.warn(" Received EOF for channel : " + p.getInt32() + " - CLOSE expected next.");
				break;

			case SSHMessages.SSH_MSG_CHANNEL_CLOSE:
				Logger.warn(" Received CLOSE for channel : " + p.getInt32());
				remoteId = -1;
				localChannelId = -1;
				// @todo once we supprot multiple channels, this will need to be refined:
				// we can't close unless everything is done, including any active forwarding;
				// in addition we need to check to make sure that the close channel is one we have open.
				sendDisconnect(11, "Finished");
				break;

			case SSHMessages.SSH_MSG_DISCONNECT:
				int reason = p.getInt32(); // disconnect reason
				String msg = "\r\nDisconnected: " + p.getString() + "(RC " + reason + ")\r\n";
				Logger.warn(msg);

				session.disconnect();
				return msg;

			case SSHMessages.SSH_MSG_GLOBAL_REQUEST:
				Logger.warn("Denying global request: " + p.getString());
				// want-reply flag - some servers use this w/ a bogus request to manage keepalive.
				if (p.getByte() == 1) {
					sendPacket2(requestFailurePacket);
				}
				break;

			case SSHMessages.SSH_MSG_CHANNEL_REQUEST:
				return handleChannelRequest(p);

			case SSHMessages.SSH_MSG_DEBUG:
				if (p.getByte() > 0) { // always-display
					// language-tag is also in this msg, but we don't really care
					// about it.
					// @todo control code filtering of text here and elsewhere

					return "DEBUG_MSG: " + p.getString() + "\r\n";

				} else {
					Logger.debug("Remote debug message: " + p.getString());
				}
				break;
			case SSHMessages.SSH_MSG_REQUEST_FAILURE:
				Logger.warn("Unexpected SSH_MSG_REQUEST_FAILURE");
				break;
			case SSHMessages.SSH_MSG_REQUEST_SUCCESS:
				Logger.warn("Unexpected SSH_MSG_REQUEST_SUCCESS");
				break;
			case SSHMessages.SSH_MSG_IGNORE:
				Logger.debug("Unexpected SSH_MSG_IGNORE");
				break;

		}
		return "";

	}

	private void sendChannelFailurePacket(String name, byte wantReply) throws IOException {
		Logger.warn("Denying remote channel request: " + name);
		// want-reply flag - some servers use this w/ a bogus request to manage keepalive
		if (wantReply == 1) {
			sendPacket2(channelFailurePacket);
		}

	}

	public void sendChannelClose() throws IOException {
		if (remoteId != -1) {
			SshPacket2 eofChannel = new SshPacket2(SSHMessages.SSH_MSG_CHANNEL_EOF);
			eofChannel.putInt32(remoteId);
			sendPacket2(eofChannel);
			SshPacket2 closeChannel = new SshPacket2(SSHMessages.SSH_MSG_CHANNEL_CLOSE);
			closeChannel.putInt32(remoteId);
			sendPacket2(closeChannel);
			remoteId = -1;
			localChannelId = -1;
		}

	}

	/**
	 * @param signal
	 *            one of: ABRT ALRM FPE HUP ILL INT KILL PIPE QUIT SEGV TERM USR1 USR2
	 * @throws IOException
	 */
	public void sendSIGRequest(String signal) throws IOException {
		SshPacket2 sig = new SshPacket2(SSHMessages.SSH_MSG_CHANNEL_REQUEST);
		sig.putInt32(remoteId);
		sig.putByte((byte) 0);
		sig.putString(signal);
		sendPacket2(sig);

	}

	private String handleChannelRequest(SshPacket2 p) throws IOException {
		int channel = p.getInt32();
		String name = p.getString();
		byte wantReply = p.getByte();
		Logger.info("Received channel request: " + channel + " / " + name + " / " + wantReply);
		if (channel != localChannelId) {
			Logger.error("Channel incorrect - expected " + localChannelId + " and got " + channel);
			sendChannelFailurePacket(name, wantReply);
			return "";
		}

		// @todo - dispatch to channel request handler?
		if (name.equalsIgnoreCase("exit-status")) { // RFC 4254 s 6.10
			int exitStatus = p.getInt32();
			sendChannelClose();
			Logger.warn("Remote exit status: " + exitStatus);
			this.sshSession.disconnect();
			return "\r\nRemote channel closed\r\n";

		} else if (name.equalsIgnoreCase("exit-signal")) { // RFC 4254 s 6.10
			String sigName = p.getString();
			boolean coreDump = (p.getByte() == 1);
			String message = p.getString();
			sendChannelClose();
			Logger.info("exit-signal cdump: " + coreDump + " message: " + message);
			// String languageTag = p.getString();

			this.sshSession.disconnect();
			// @todo in this and other cases of channel data, we need to filter out control codes.
			return "\r\nRemote channel terminated with SIG " + sigName + "\r\n" + message + "\r\n";
		}
		// No other channelrequests supported right now.
		sendChannelFailurePacket(name, wantReply);
		return "";

	}

	private String handleChannelWindowAdjust(SshPacket2 p) {
		int chan = p.getInt32();
		// @todo - BUG - a lot of cases call for UINT32, not INT32!
		int size = p.getInt32();
		Logger.info(" remote session requested req " + size + " bytes on chan " + chan);
		if (chan == localChannelId) {
			// @todo - we should have some error handling if the total is > 2^32-1
			outboundWindowSize += size;
		} else {
			// @todo - this shouldn't happen....
			Logger.warn("    bad channel " + chan);
		}
		return "";
	}

	protected String handleChannelData(SshPacket2 p, Session session) throws IOException {
		if (state >= STATE_EOF_RECEIVED) {
			Logger.error("\r\nChannel data received after EOF, ignored.");
			return "";
		}
		// @todo - set default log level to INFO , while DEBUG is only when specifically enabled.

		// @todo we need to start tracking within channel - esp if we plan to support sftp /scp
		p.getInt32(); // localId
		// extended charset support
		// @todo UTF8 - requires TRANSLATION LAYER
		// byte[] b = p.getMpInt();
		// String data = new String(b);// getString();
		// No charset support
		String data = p.getString();
		int len = data.length();
		dataReceivedLen += len;
		if (len < inboundWindowSize) {
			inboundWindowSize -= data.length();
		}
		if (len == inboundWindowSize) {
			Logger.info("Window size matched, requesting adjustment.");
			inboundWindowSize -= data.length();
			sendChannelWindowAdjustPacket();
		} else if (len > inboundWindowSize) {
			Logger.info("Window size exceeded, truncating data and requesting adjustment.");
			// Accept only up to the max data we'll allow
			data = data.substring(0, inboundWindowSize);
			inboundWindowSize = 0;
			sendChannelWindowAdjustPacket();
		}
		dataAcceptedBytes += data.length();
		return data;
	}

	private void sendChannelWindowAdjustPacket() throws IOException {
		SshPacket2 pn = new SshPacket2(SSHMessages.SSH_MSG_CHANNEL_WINDOW_ADJUST);
		// @todo - make this a constant 1MB or something... .
		pn.putInt32(remoteId);
		pn.putInt32(0x100000);
		// @todo - is there no ack for this?! this could mean we start accepting data sent
		// prior to the change notice sent to server... data we should be ignoring
		inboundWindowSize += 0x100000;

		sendPacket2(pn);

	}

	/**
	 * Attempt authentication. This is a rentrant method. If a given auth attempt fails, it will try the next level
	 * down, as follows:
	 * <ul>
	 * <li>Public Key</li>
	 * <li>Password</li>
	 * <li>Keyboard Interactive</li>
	 * </ul>
	 * Note that it currently does not respect the auth methods supporte dby the server, instead blindly trying all
	 * three down the list. - public key authentication if any key exists.
	 * 
	 * @return
	 * @throws IOException
	 */
	private String authenticate2() throws IOException {
		// @todo to be compliant with spec, I believe that we should be
		// listening to what the server tells us it supports in terms of auth modes
		// and only attempting those supported.
		SshPacket2 buf = new SshPacket2(SSHMessages.SSH_MSG_USERAUTH_REQUEST);
	
		Key k = sshSession.getKey();
		if (k != null && k.getId() != Key.INVALID_ID && authmode < MODE_PUBLICKEY) {
			Logger.debug("Attempting publickey auth.");
			authmode = MODE_PUBLICKEY;
			String type;

			String pass = k.getPassphrase();
			KeyPair key = null;
			if (k.isNativeKey()) {
				key = k.getKeyPair();
			} else {
				do {
					try {
						key = PEMDecoder.decode(k.getData(), pass);
					} catch (InvalidKeyException e) {
						throw new IOException("Key is not valid.");

					} catch (IOException e) {
						// Assume password failed.
						// @todo - this is NOT then reliant on session,a nd SHOULD be handled in a separate interface
						// eg not SessionListener
						Logger.warn("IOException " + e.getMessage() + " - could not decrypt, assuming password error and prompting for password.");
						pass = sshSession.getListener().getKeyPassword(sshSession.getSessionId(), k);
						if (pass == null) {
							Logger.error("New password didn't fix the problem- failing."); 
							throw e;
						}
					}
				} while (key == null);
			}
			PacketUserauthRequestPublicKey ua;
			try {
				if (key instanceof DSAKeyPair) {
					DSAPrivateKey pk = ((DSAKeyPair) key).getDSAPrivateKey();

					ua = new PacketUserauthRequestPublicKey(pk, sessionId, "ssh-connection", userName, "ssh-dss");
					type = "DSA";
				} else if (key instanceof RSAKeyPair) {
					RSAPrivateKey pk = ((RSAKeyPair) key).getRSAPrivateKey();
					ua = new PacketUserauthRequestPublicKey(pk, sessionId, "ssh-connection", userName, "ssh-rsa");

					type = "RSA";
				} else {
					throw new IOException("Unknown private key encryption.");
				}
			} catch (CryptoException e) {
				throw new IOException("Unexpected CryptoException occurred: " + e.toString());
			}

			byte[] b = ua.getPayload();

			// A bit of a bridging hack here between old and
			// new auth systems - the packet will automaticlaly incldue the
			// userauthrequest byte in our SshPacket2 implementation,
			// so we need to skip past that in our raw data buffer from
			// the new implementation. (The new implementation must include that
			// byte as it must be present when the checksum is calculated.)
			buf.putBytes(b, 1, b.length - 1);
			sendPacket2(buf);
			return "Sent public key: " + type + ".\r\n";
		}

		buf.putString(userName);
		buf.putString("ssh-connection");

		if (authmode < MODE_PASSWORD && password != null && password.length() > 0) {
			// @todo - we aren't we checking to see if this is allowed? We
			// *have* that info!
			authmode = MODE_PASSWORD;

			buf.putString("password");
			buf.putByte((byte) 0);
			buf.putString(password);
			sendPacket2(buf);
			// @todo Session.CONN_STATE_SENT_PASSWORD

			return "Sent password\r\n";
		} else if (authmode < MODE_KEYBOARD_INTERACTIVE) {
			// @todo - we aren't we checking to see if this is allowed? We
			// *have* that info!
			/* Attempt keyboard-interactive auth */
			authmode = MODE_KEYBOARD_INTERACTIVE;

			buf.putString("keyboard-interactive");
			buf.putString("");
			buf.putString("");
			sendPacket2(buf);
			// @todo Session.CONN_STATE_BEGIN_KBD_INTERACTIVE

			return "Start keyboard-interactive\r\n";
		} else {
			return null;
		}
	}

	public void sendPacket2(SshPacket2 packet) throws IOException {
		write(packet.getPayLoad(crypto2, outgoingseq));
		sshSession.outputPacketCount++;
		outgoingseq++;
	}

	private byte[] sessionId;

	/**
	 * Obtains key from hash, as per RFC 4253 S 2, applying rules about minium lengths. Takes a further steps for bb
	 * crypto compatibiltiy, and ensures that the key is EXACTLY the expected length - it will trim anything longer than
	 * required.
	 * 
	 * @param hash
	 *            digest which contains the KEX data digest thus far.
	 * @param keyLen
	 *            length of the key for the agreed-upon encrpytion type.
	 * @return
	 */
	private byte[] calculateFinalKey(SHA1Digest hash, int keyLen) {
		byte[] source = hash.getDigest();
		while (keyLen > source.length) {
			SshPacket2 buf = new SshPacket2();
			buf.putMpInt(kexState.K);
			buf.putBytes(kexState.H);
			buf.putBytes(source);
			byte[] b = buf.getData();
			hash.update(b);
			byte[] foo = hash.getDigest();
			byte[] bar = new byte[source.length + foo.length];
			// First copy in what we started with
			System.arraycopy(source, 0, bar, 0, source.length);
			// Now append our new hash.
			System.arraycopy(foo, 0, bar, source.length, foo.length);
			source = bar;
		}
		return Tools.trimBytesToLength(source, keyLen);
	}

	/**
	 * Implements handling for KEX_INIT message, as per http://www.ietf.org/rfc/rfc4253.txt Section 7.1
	 * 
	 * @todo replace DH kex init with DH group exchange, per rfc4419
	 * @todo - on ioexception thrown , make sure ew send disconnect on keys message.
	 */
	private String handleKexInit2(SshPacket2 srvPacket) throws IOException {
		// @todo - test init key size of BigINteger(1024 bit)
		try {
			if (state == STATE_KEX_INIT) {
				throw new IOException("Received KEX INIT while prior init was not resolved");
			}
			setState(STATE_KEX_INIT);
			// We receive info on what the server will support; and
			// must respond in kind.
			kexState = new KexStateData();
			kexState.serverKEX = KexInitData.createInstanceFromPacket(srvPacket);
			kexState.clientKEX = KexInitData.createInstance();
			SshPacket2 clnPacket = kexState.clientKEX.createOutboundPacket();
			sendPacket2(clnPacket);
			setState(STATE_KEX_DH_INIT);

			// I_S and I_C are the payload of the client and server SSH_MSG_KEXINIT,
			// including the message ID itself. Our packets don't include message ID,
			// so we have to insert it in order for the hash to verify properly.
			kexState.I_S = Tools.insertByteValue((byte) SSHMessages.SSH_MSG_KEXINIT, srvPacket.getData());
			kexState.I_C = Tools.insertByteValue((byte) SSHMessages.SSH_MSG_KEXINIT, clnPacket.getData());
			kexState.agreement = KexInitData.findAgreement(kexState.serverKEX, kexState.clientKEX);
			if (kexState.serverKEX.isFirstKEXPacketFollowing()) {
				checkIgnoreNextPacket();
			}

			byte[] prime;
			if (kexState.agreement.kexAlgorithm.equals("diffie-hellman-group1-sha1")) {
				prime = DIFFIE_HELLMAN_GROUP_1; // Oakley Group 2 - RFC2409
			} else {
				prime = DIFFIE_HELLMAN_GROUP_14; // group14-sha1 - Oakley Group 14 - RFC3526
			}

			try {
				kexState.dhCryptoSystem = new DHCryptoSystem(prime, new byte[] { 2 });
			} catch (InvalidCryptoSystemException ex) {
				throw new IOException("InvalidCryptoSystemException: " + ex.getMessage());
			} catch (UnsupportedCryptoSystemException ex) {
				throw new IOException("UnsupportedCryptoSystemException: " + ex.getMessage());
			}
			kexState.keyPair = kexState.dhCryptoSystem.createDHKeyPair();

			SshPacket2 dhInit = new SshPacket2(SSHMessages.SSH_MSG_KEXDH_INIT);
			dhInit.putMpInt(kexState.keyPair.getDHPublicKey().getPublicKeyData()); // e
			sendPacket2(dhInit);

		} catch (CryptoTokenException ex) {
			throw new IOException("CryptoTokenException: " + ex.getMessage());
		} catch (CryptoUnsupportedOperationException ex) {
			throw new IOException("CryptoUnsupportedOperationException: " + ex.getMessage());
		}
		return "Negotiating...";

	}

	/**
	 * This is received upon completion of key negotiation , wherein the server accepts our keys. If we do NOT accept
	 * the server keys, we MUST send a disconnect at this point. Note that once we receive this message, all subsequent
	 * messages MUST be decrypted using the data obtained during KEX.
	 * 
	 * @param p
	 *            new key request packet.
	 * @return
	 * @throws IOException
	 */
	private String handleNewKeysRequest2(SshPacket2 p) throws IOException {
		if (state != STATE_SECURE) {
			// SSH_DISCONNECT_KEY_EXCHANGE_FAILED = 3
			if (state == STATE_KEX_FAILED) {
				throw new IOException("Key exchange failed -- agreement could not be reached.");
			} else {
				throw new IOException("Received key acceptance from server but was not expectign it.");
			}

		}
		// After MSG_NEWKEYS, everything must be encrypted - so we'll send this before updating keys.
		sendPacket2(new SshPacket2(SSHMessages.SSH_MSG_NEWKEYS));

		// Great, now that we've agreed and everyone is happy, let's
		// update our keys, tell the server, and submit the user auth request.
		// request user authentication
		try {
			updateKeys();
		} catch (NoSuchAlgorithmException e) {
			// This should never happen, but let's play it safe...
			sendDisconnect(3, "Key exchange failed - invalid algorithm.");
			throw new IOException("Key exchange failed -- algorithm not found.");

		}

		setState(STATE_REQUESTING_AUTH);
		SshPacket2 pn = new SshPacket2(SSHMessages.SSH_MSG_SERVICE_REQUEST);
		pn.putString("ssh-userauth");
		sendPacket2(pn);

		// Note: do we want to null out kexdata at this point? In no case do we
		// need any of it - a key re-exchange will negate it, and everything else that
		// needs those values will now have been initialize.
		return "Requesting authentication\r\n";
	}

	/**
	 * Handler for message SSH_MSG_KEXDH_REPLY; reference RFC4253 sec. 8.0 for implementation explanation.
	 * 
	 * @param p
	 *            packet as receievd from the server
	 * @return status update string to be displayed on terminal
	 * @throws IOException
	 *             in event of any failure
	 */
	private String handleKexDHReply2(SshPacket2 p) throws IOException {
		try {
			if (state != STATE_KEX_DH_INIT) {
				throw new IOException("Received SSHMessages.SSH_MSG_KEXDH_REPLY but not in DH_INIT!");
			}
			kexState.K_S = p.getByteString();
			kexState.serverPubKey = new DHPublicKey(kexState.dhCryptoSystem, p.getMpInt()); // f
			kexState.SigOfH = p.getByteString(); // Signature of H
			kexState.K = DHKeyAgreement.generateSharedSecret(kexState.keyPair.getDHPrivateKey(), kexState.serverPubKey,
					false);

			SshPacket2 buf = new SshPacket2();
			buf.putString(Version.getVersionSSHIDString().trim().getBytes()); // client's identification string
			buf.putString(remoteSystemId.trim().getBytes()); // server's identification string, excluding crlf
			buf.putString(kexState.I_C); // payload of the client's SSH_MSG_KEXINIT (including message ID)
			buf.putString(kexState.I_S); // payload of the server's SSH_MSG_KEXINIT (including message ID)
			buf.putString(kexState.K_S); // server host key
			buf.putMpInt(kexState.keyPair.getDHPublicKey().getPublicKeyData()); // e, the public key we sent
			buf.putMpInt(kexState.serverPubKey.getPublicKeyData()); // f, the public key we received in this message.
			buf.putMpInt(kexState.K); // K, the shared secret

			SHA1Digest sha1 = new SHA1Digest();
			sha1.update(buf.getBytes());

			// This will be used in verifying that the message originated from the server's key
			kexState.H = sha1.getDigest();

			// if we update keys later, session ID is not allowed to change
			if (sessionId == null) {
				sessionId = new byte[kexState.H.length];
				System.arraycopy(kexState.H, 0, sessionId, 0, kexState.H.length);
			}

			// Ah, finally - we can build our signature
			// we have what we need to validate that the server has the correct data/keys.
			// The specifics will vary based on host key
			validateServerKeys();

			// When we get the NEW_KEYS request, we'll refresh our crypto data.
			setState(STATE_SECURE);

		} catch (CryptoException ex) {
			// We tell the user here -- but we won't actually disconnect until
			// we receiev NEWKEYS, per the spec.
			setState(STATE_KEX_FAILED);
			// @todo we're not showing this retuned string?
			return "\r\nKEX FAILED: " + ex.toString() + "(" + ex.getMessage() + ")\r\n";
		}
		// Okay, we're good. Tell the server that we accept this.
		// @todo look at a better option for fingerprint: - visual fingerprint?
		// @todo host checking.

		return "OK\r\n" + kexState.keyAlgorithm + " " + fingerprint(kexState.K_S) + "\r\n";
	}

	/**
	 * Set up crypto and associated data keys as agreed upon in the key exchange. Reference RFC 4253 section 7.2 for
	 * explanation and details of the implementation.
	 * 
	 * @throws NoSuchAlgorithmException
	 */
	private void updateKeys() throws NoSuchAlgorithmException {
		CipherManager m = CipherManager.getInstance();
		CipherAttributes sendAttr = m.getCipherAttributes(kexState.agreement.clientToServerCryptoAlgorithm);
		CipherAttributes recvAttr = m.getCipherAttributes(kexState.agreement.serverToClientCryptoAlgorithm);

		SHA1Digest hash = new SHA1Digest();
		SshPacket2 buf = new SshPacket2();
		int sendKeySize = sendAttr.getKeySize();
		int recvKeySize = recvAttr.getKeySize();
		buf.putMpInt(kexState.K);
		buf.putBytes(kexState.H);
		buf.putByte((byte) 0); // we'll be replacing this for each new hash we're generating .
		int x = buf.getLength() - 1; // mark the location so we know to replace here.
		buf.putBytes(sessionId);
		byte[] data = buf.getData();

		// @todo - can make all of these into a single fn call -
		// getSizedHashValue(data, 'A', minLength = 0, maxLength = 0)
		data[x] = 'A'; // HASH(K || H || "A" || session_id)
		hash.update(data);
		// We trim below b/c sometimes RIM crypto is picky about IVs that are too long.
		byte[] sndIV = Tools.trimBytesToLength(hash.getDigest(), sendKeySize);

		data[x] = 'B'; // HASH(K || H || "B" || session_id)
		hash.update(data);
		// We trim below b/c sometimes RIM crypto is picky about IVs that are too long.
		byte[] rcvIV = Tools.trimBytesToLength(hash.getDigest(), recvKeySize);

		data[x] = 'C'; // HASH(K || H || "C" || session_id)
		hash.update(data);
		byte[] sndKey = calculateFinalKey(hash, sendKeySize);

		data[x] = 'D'; // HASH(K || H || "D" || session_id)
		hash.update(data);
		byte[] rcvKey = calculateFinalKey(hash, recvKeySize);

		data[x] = 'E';
		hash.update(data); // HASH(K || H || "E" || session_id)
		byte[] sndMAC = hash.getDigest();

		data[x] = 'F'; // HASH(K || H || "F" || session_id)
		hash.update(data);
		byte[] rcvMAC = hash.getDigest();

		crypto2 = new SshCrypto2(sendAttr, recvAttr, sndIV, rcvIV, sndKey, rcvKey, sndMAC, rcvMAC);
	}

	public void sendTerminalSizeUpdate() throws IOException {

		if (state < STATE_ONLINE) {
			Logger.error("Received request to send terminal size update, but authentication not completed.");
			return;
		}
		SshPacket2 pn = new SshPacket2(SSHMessages.SSH_MSG_CHANNEL_REQUEST);
		pn.putInt32(remoteId); // v recipient channel
		pn.putString("window-change");
		pn.putByte((byte) 0); // want reply- no
		pn.putInt32(getTerminalWidth()); // terminal width, columns
		pn.putInt32(getTerminalHeight()); // terminal height, rows
		pn.putInt32(0); // terminal width, pixels
		pn.putInt32(0); // terminal height, pixels
		sendPacket2(pn);
	}

	/**
	 * Send_SSH_NOOP (no arguments) Sends a NOOP packet to keep the connection alive.
	 * 
	 * @return empty string.
	 * @throws IOException
	 */
	public String Send_SSH_NOOP() throws IOException {
		SshPacket2 packet = new SshPacket2(SSHMessages.SSH_MSG_IGNORE);
		packet.putString("");
		sendPacket2(packet);
		return "";
	}

	/** @todo - these passthroughs do NOT belong here */
	protected String getTerminalID() {
		return sshSession.getTerminalID();
	}

	/** @todo - these passthroughs do NOT belong here */
	protected int getTerminalHeight() {
		return sshSession.getTerminalHeight();
	}

	/** @todo - these passthroughs do NOT belong here */
	protected int getTerminalWidth() {
		return sshSession.getTerminalWidth();
	}

	static public byte getNotZeroRandomByte() {
		byte ret = 0;
		while (ret == 0) {
			ret = RandomSource.getBytes(1)[0];
		}
		return ret;
	}

	private String[] handleUserAuthInfoRequest(SshPacket2 p) {
		/*
		 * http://tools.ietf.org/html/rfc4256#ref-SSH-ARCH S 3.3
		 */
		String name = p.getString();
		if (name == null || name.length() == 0) {
			name = properties.getUsername();
		}
		final String instruction = p.getString(); // service name
		p.getString(); // language tag
		int numPrompts = p.getInt32();
		final Vector prompts = new Vector();
		for (int i = 0; i < numPrompts; i++) {
			String text = p.getString();
			// @todo - again, this REALLY doesn't belong here.
			// @todo - 'password' is not set?
			if (text.startsWith("password:")) {
				prompts.addElement(new SSHPrompt(text, p.getByte() == 1, password));
			} else {
				prompts.addElement(new SSHPrompt(text, p.getByte() == 1));
			}
		}
		return sshSession.authPrompt(name, instruction, prompts, password);

	}

	/**
	 * Validates that server key received in SSH_DH_KEX_INIT is correct.
	 * 
	 * @throws IOException
	 */
	private void validateServerKeys() throws IOException, CryptoException {
		TypesReader keyData = new TypesReader(kexState.K_S);
		kexState.keyAlgorithm = keyData.readString();
		// @todo RSA verify as RSACryptoSystem, etc.
		if (kexState.keyAlgorithm.equals("ssh-dss")) {
			// RIM DSS validation does not permit keys larger than 2048, so we must do our own.
			SignatureTools.verifyDSASignature(kexState.H, kexState.SigOfH, keyData.readTrimmedCryptoIntegerString(),
					keyData.readTrimmedCryptoIntegerString(), keyData.readTrimmedCryptoIntegerString(), keyData
							.readTrimmedCryptoIntegerString());

		} else if (kexState.keyAlgorithm.equals("ssh-rsa")) {
			// Alas, RIM crypto is not recognizing our RSA pub key as valid, so once again
			// we must do our own...
			if (!SignatureTools.verifyRSASignature(keyData, kexState.SigOfH, kexState.H)) {
				throw new IOException("RSA signature validation failed.");
			}

		} else {
			// This might occur if the server sends us an invalid value, but otherwise
			// is impossible since we have agreed on this algorithm
			// before being able to reach this point.
			throw new IOException("Unknown encoding algorithm: " + kexState.keyAlgorithm);
		}
	}

	/**
	 * Set password to use for this connection when authentication request is received, if user/pass auth is supported
	 * and pub key auth is not used.
	 * 
	 * @param password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Set username to use for this connection when authentication request is received, if user/pass auth is supported
	 * and pub key auth is not used.
	 * 
	 * @param userName
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	public KexAgreement getAgreement() {
		if (kexState == null)
			return null;

		return kexState.agreement;
	}

	public void onConnected() throws IOException {
		setState(STATE_INIT);
		write(Version.getVersionSSHIDString().getBytes());
	}

	// public void onDataReceived(Session session, byte[] data) {
	// }
	//
	// public void onWriteCompletion(Session session) {
	// try {
	// sendOutboundChannelData();
	// } catch (IOException e) {
	// Logger.error("IOException in SshIO.onWriteCompletion [ " + e.getMessage() + " ] ");
	// }
	// }

}
