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
package org.bbssh.ssh.kex;



import net.rim.device.api.crypto.DHCryptoSystem;
import net.rim.device.api.crypto.DHKeyPair;
import net.rim.device.api.crypto.DHPublicKey;

/**
 * State data for a given key exchange between client and server.
 */
public class KexStateData {
	public KexInitData serverKEX;
	public KexInitData clientKEX;
	public DHCryptoSystem dhCryptoSystem;
	
	/** the Hash (Ref RFC 4253 Sec. 8.3 for this and below fields. ) */
	public byte[] H;
	/** Server host public key. */
	public byte[] K_S;
	DHKeyPair localKey;
	/** Payload of server's SSH_MSG_KEXINIT */
	public byte[] I_S;
	/** Payload of client's SSH_MSG_KEXINIT */
	public byte[] I_C;
	/** The shared secret between client and server */
	public byte[] K; 
	/** Agreed upon crypto properties for the current session at the current point in time. . */
	public KexAgreement agreement;
	/** Client-side keypair */
	public DHKeyPair keyPair;
	/** Server's signature of "H", the hash */
	public byte[] SigOfH;
	/** "F" - server public key */
	public DHPublicKey serverPubKey;
	/** Key algorithm used by the server, eg ssh-dss or ssh-rsa */
	public String keyAlgorithm;
	
}
