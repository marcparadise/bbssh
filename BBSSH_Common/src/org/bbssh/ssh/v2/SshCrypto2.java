/**
 * This file is part of "BBSSH" (c) 2010 Marc A. Paradise
 *
 * BBSSH is based upon MidpSSH by Karl von Randow. Portions
 * Copyright (C) 2004 Karl von Randow
 * MidpSSH was based upon Telnet Floyd and FloydSSH by Radek Polak.
 *
 * --LICENSE NOTICE-- This program is free software; you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 675 Mass
 * Ave, Cambridge, MA 02139, USA. --LICENSE NOTICE--
 *  
 */
package org.bbssh.ssh.v2;

import net.rim.device.api.crypto.BlockDecryptorEngine;
import net.rim.device.api.crypto.BlockEncryptorEngine;
import net.rim.device.api.crypto.CryptoTokenException;
import net.rim.device.api.crypto.DecryptorFactory;
import net.rim.device.api.crypto.EncryptorFactory;
import net.rim.device.api.crypto.HMAC;
import net.rim.device.api.crypto.HMACKey;
import net.rim.device.api.crypto.InitializationVector;
import net.rim.device.api.crypto.InitializationVectorFactory;
import net.rim.device.api.crypto.SHA1Digest;
import net.rim.device.api.crypto.SymmetricKey;
import net.rim.device.api.crypto.SymmetricKeyFactory;

import org.bbssh.ssh.kex.CipherAttributes;
import org.bbssh.util.Logger;

public class SshCrypto2 {
	private HMAC sndHmac;
	private HMAC rcvHmac;
	BlockEncryptorEngine sndCipher;
	BlockDecryptorEngine rcvCipher;

	public SshCrypto2(CipherAttributes sendAttr, CipherAttributes recvAttr,  byte[] sndIVData, byte[] rcvIVData,
			byte[] sndKeyData, byte[] rcvKeyData, byte[] sndMACData, byte[] rcvMACData) {
		try {
			InitializationVector ivSend = InitializationVectorFactory.getInstance(sendAttr.getIVAlgorithm(), sndIVData, 0);
			InitializationVector ivRecv = InitializationVectorFactory.getInstance(recvAttr.getIVAlgorithm(), rcvIVData, 0);
			SymmetricKey sndKey = SymmetricKeyFactory.getInstance(sendAttr.getKeyAlgorithm(), sndKeyData, 0);
			SymmetricKey rcvKey = SymmetricKeyFactory.getInstance(recvAttr.getKeyAlgorithm(), rcvKeyData, 0);
			sndCipher = EncryptorFactory.getBlockEncryptorEngine(sndKey, sendAttr.getCryptoAlgorithm(), ivSend);
			rcvCipher = DecryptorFactory.getBlockDecryptorEngine(rcvKey, recvAttr.getCryptoAlgorithm(), ivRecv);
			sndHmac = new HMAC(new HMACKey(sndMACData), new SHA1Digest());
			rcvHmac = new HMAC(new HMACKey(rcvMACData), new SHA1Digest());
			Logger.debug("  Crypto outbound block size: " + sndCipher.getBlockLength());
			Logger.debug("   Crypto inbound block size: " + rcvCipher.getBlockLength());
		} catch (Throwable e) {
			// @todo - crap-ass exception handlig and throwing practices. Clean this up.
			Logger.fatal("Exception in crypto init:" + e.toString() + " - " + e.toString());
			throw new RuntimeException("Crypto initialization failed: " + e.toString());
		}

	}

	public byte[] encrypt(byte[] src) {
		// @todo - do we need to throw an error if num bytes not a multiple of blocksize? Payload should be padded
		// to a multiple. 
		int blockSize = sndCipher.getBlockLength();
		int blocks = src.length / blockSize;
		byte[] dest = new byte[src.length];
		try {
			int blockOffset = 0;
			for (int x = 0; x < blocks; x++) {
				sndCipher.encrypt(src, blockOffset, dest, blockOffset);
				blockOffset += blockSize;
			}
		} catch (Throwable e) {
			throw new RuntimeException(e.toString());
		}
		return dest;
	}

	public byte[] decrypt(byte[] src, int count) {
		// @todo - do we need to throw an error if num bytes not a multiple of blocksize?
		byte[] dest = new byte[src.length];
		int blockSize = rcvCipher.getBlockLength();
		int blocks = count / blockSize;
		try {
			int blockOffset = 0;
			for (int x = 0; x < blocks; x++) {
				rcvCipher.decrypt(src, blockOffset, dest, blockOffset);
				blockOffset += blockSize;
			}
		} catch (Throwable e) {
			throw new RuntimeException(e.toString());
		}
		return dest;
	}

	public int getReceiveBlockSize() {
		return rcvCipher.getBlockLength();
	}

	private static final byte[] converter = new byte[4];

	byte[] updateMACForBlock(int blockSeq, byte[] block, int offset, int length) {
		if (sndHmac == null)
			return null;
		// This works around an apparent flaw in HMAC.updateInt wherein
		// it's not correctly calculating the hash when an int is used - perhaps
		// using reverse endian-ness?
		converter[0] = (byte) (blockSeq >>> 24);
		converter[1] = (byte) (blockSeq >>> 16);
		converter[2] = (byte) (blockSeq >>> 8);
		converter[3] = (byte) blockSeq;
		try {
			sndHmac.update(converter);
			sndHmac.update(block, 0, length);
			return sndHmac.getMAC();
		} catch (CryptoTokenException ex) {
			Logger.fatal("Failed to obtain mac: " + ex.getMessage());
		}
		return null;

	}

	public int getSendBlockSize() {
		return sndCipher.getBlockLength();
	}

	public int getSendHMACLength() {
		if (sndHmac == null)
			return 0;
		return sndHmac.getLength();
	}

	public int getReceiveHMACLength() {
		if (rcvHmac == null)
			return 0;
		return rcvHmac.getLength();
	}

	/**
	 * @return Returns the rcvHmac.
	 */
	public HMAC getRcvHmac() {
		return rcvHmac;
	}

	/**
	 * @return Returns the sndHmac.
	 */
	public HMAC getSndHmac() {
		return sndHmac;
	}

}