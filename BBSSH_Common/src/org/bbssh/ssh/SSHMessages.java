package org.bbssh.ssh;

import net.rim.device.api.util.IntHashtable;

public interface SSHMessages {
	public class DebugAide {
		public static IntHashtable ht;
		static {
			ht = new IntHashtable(100);
			ht.put(SSH_MSG_DISCONNECT, "DISCONNECT");
			ht.put(SSH_MSG_IGNORE, "IGNORE");
			ht.put(SSH_MSG_UNIMPLEMENTED, "UNIMPLEMENTED");
			ht.put(SSH_MSG_DEBUG, "DEBUG");
			ht.put(SSH_MSG_SERVICE_REQUEST, "SERVICE_REQUEST");
			ht.put(SSH_MSG_SERVICE_ACCEPT, "SERVICE_ACCEPT");
			ht.put(SSH_MSG_KEXINIT, "KEXINIT");
			ht.put(SSH_MSG_KEXDH_INIT, "KEXDH_INIT");
			ht.put(SSH_MSG_KEXDH_REPLY, "KEXDH_REPLY");
			ht.put(SSH_MSG_NEWKEYS, "NEWKEYS");
			ht.put(SSH_MSG_USERAUTH_REQUEST, "USERAUTH_REQUEST");
			ht.put(SSH_MSG_USERAUTH_FAILURE, "USERAUTH_FAILURE");
			ht.put(SSH_MSG_USERAUTH_SUCCESS, "USERAUTH_SUCCESS");
			ht.put(SSH_MSG_USERAUTH_SUCCESS, "USERAUTH_SUCCESS");
			ht.put(SSH_MSG_USERAUTH_BANNER, "USERAUTH_BANNER");
			ht.put(SSH_MSG_USERAUTH_INFO_REQUEST, "USERAUTH_INFO_REQUEST");
			ht.put(SSH_MSG_USERAUTH_INFO_REQUEST, "USERAUTH_INFO_REQUEST");
			ht.put(SSH_MSG_USERAUTH_INFO_RESPONSE, "USERAUTH_INFO_RESPONSE");
			ht.put(SSH_MSG_GLOBAL_REQUEST, "GLOBAL_REQUEST");
			ht.put(SSH_MSG_REQUEST_SUCCESS, "REQUEST_SUCCESS");
			ht.put(SSH_MSG_REQUEST_FAILURE, "REQUEST_FAILURE");
			ht.put(SSH_MSG_CHANNEL_OPEN, "CHANNEL_OPEN");
			ht.put(SSH_MSG_CHANNEL_OPEN_CONFIRMATION, "CHANNEL_OPEN_CONFIRMATION");
			ht.put(SSH_MSG_CHANNEL_OPEN_FAILURE, "CHANNEL_OPEN_FAILURE");
			ht.put(SSH_MSG_CHANNEL_WINDOW_ADJUST, "CHANNEL_WINDOW_ADJUST");
			ht.put(SSH_MSG_CHANNEL_DATA, "CHANNEL_DATA");
			ht.put(SSH_MSG_CHANNEL_EXTENDED_DATA, "CHANNEL_EXTENDED_DATA");
			ht.put(SSH_MSG_CHANNEL_REQUEST, "CHANNEL_REQUEST");
			ht.put(SSH_MSG_CHANNEL_CLOSE, "CHANNEL_CLOSE");
			ht.put(SSH_MSG_CHANNEL_EOF, "CHANNEL_EOF");
			ht.put(SSH_MSG_CHANNEL_FAILURE, "CHANNEL_FAILURE");

		}

	}

	/** Received when remote host disconnects us (or sent when we disconnect them) */
	public static final byte SSH_MSG_DISCONNECT = 1;
	/** Some implementations use this as a keepalive, but it has no official use. */
	public static final byte SSH_MSG_IGNORE = 2;
	/** Some implementations use this as a keepalive, but it has no official use. */
	public static final byte SSH_MSG_UNIMPLEMENTED = 3;
	/** Remote system can send debug strings (optionally for display) with this message. */
	public static final byte SSH_MSG_DEBUG = 4;
	/** Request a service to be initialized. */
	public static final byte SSH_MSG_SERVICE_REQUEST = 5;
	/** Indicates that a service request was accepted. */
	public static final byte SSH_MSG_SERVICE_ACCEPT = 6;
	/** Initialization of key exchange */
	public static final byte SSH_MSG_KEXINIT = 20;
	/** Indicates that new keys have been agreed upon and are now in effect */
	public static final byte SSH_MSG_NEWKEYS = 21;
	/** Diffie-Hellman key exchange initialization */
	public static final byte SSH_MSG_KEXDH_INIT = 30;
	/** Diffie-Hellman key exchange response message */
	public static final byte SSH_MSG_KEXDH_REPLY = 31;
	/** Client sends this when attempting to authenticate to server */
	public static final byte SSH_MSG_USERAUTH_REQUEST = 50;
	/** Server sends this reply if auth fails */
	public static final byte SSH_MSG_USERAUTH_FAILURE = 51;
	/** Server sends this reply if auth succeeds */
	public static final byte SSH_MSG_USERAUTH_SUCCESS = 52;
	/** If the server sends this, client SHOULD display this message at login. */
	public static final byte SSH_MSG_USERAUTH_BANNER = 53;

	public static final byte SSH_MSG_USERAUTH_INFO_REQUEST = 60;
	public static final byte SSH_MSG_USERAUTH_INFO_RESPONSE = 61;
	public static final byte SSH_MSG_GLOBAL_REQUEST = 80;
	/** a GLOBAL_REQUEST has failed */
	public static final byte SSH_MSG_REQUEST_SUCCESS = 81;
	/** a GLOBAL_REQUEST has succeeded */
	public static final byte SSH_MSG_REQUEST_FAILURE = 82;
	/** Request to open a channel, such as PTY login */
	public static final byte SSH_MSG_CHANNEL_OPEN = 90;
	/** Indicates that the requested channel has been successfully opened */
	public static final byte SSH_MSG_CHANNEL_OPEN_CONFIRMATION = 91;
	/** Indicaets that the requested channel has not been opened */
	public static final byte SSH_MSG_CHANNEL_OPEN_FAILURE = 92;
	/**
	 * Sent to tell us how many bytes we can accept before the next window adjustment. Not currently implemented.
	 */
	public static final byte SSH_MSG_CHANNEL_WINDOW_ADJUST = 93;
	/**
	 * Indicates data arriving (or being sent) on the specified channel. For example, data received on a PTY channel
	 * would be rendered to the associated terminal
	 */
	public static final byte SSH_MSG_CHANNEL_DATA = 94;
	public static final byte SSH_MSG_CHANNEL_EXTENDED_DATA = 95;
	public static final byte SSH_MSG_CHANNEL_EOF = 96;
	/** send a channel request, such as window-change, shell, pty */
	public static final byte SSH_MSG_CHANNEL_REQUEST = 98;
	/** Notification indicating that the specified channel has been closed. */
	public static final byte SSH_MSG_CHANNEL_CLOSE = 97;
	/** Notification indicating that the received channel request has failed. */
	public static final byte SSH_MSG_CHANNEL_FAILURE = 100;
	// @todo - more ssh messages to process Also:
	// SSH_MSG_USERAUTH_PK_OK 60 - public key
	// SSH_MSG_USERAUTH_PASSWD_CHANGEREQ - password
	// Noet: SSH_MSG_USERAUTH_REQUEST
	// Server replies with: SSH_MSG_USERAUTH_SUCCESS, SSH_MSG_USERAUTH_FAILURE, or
	// SSH_MSG_USERAUTH_PASSWD_CHANGEREQ.
	// Client MAY send old/new in userauth request for a password change!
	// @todo "hostbased" support for auth.

}
