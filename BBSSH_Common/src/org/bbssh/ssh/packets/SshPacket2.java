/*
 * This file is part of "The Java Telnet Application".
 * 
 * (c) Matthias L. Jugel, Marcus Mei�ner 1996-2002. All Rights Reserved. The
 * file was changed by Radek Polak to work as midlet in MIDP 1.0
 * 
 * This file has been modified by Karl von Randow for MidpSSH.
 * 
 * Please visit http://javatelnet.org/ for updates and contact.
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
 */
package org.bbssh.ssh.packets;

import java.io.IOException;

import net.rim.device.api.crypto.CryptoTokenException;
import net.rim.device.api.crypto.HMAC;

import org.bbssh.ssh.SshIO;
import org.bbssh.ssh.v2.SshCrypto2;
import org.bbssh.util.Logger;
import org.bbssh.util.Tools;

public class SshPacket2 extends SshPacket {
	private static final int PHASE_PACKET_LENGTH = 0;
	private static final int PHASE_BLOCK = 1;
	private byte[] packetLengthArray = new byte[32]; // allows block sizes up to 32. If any larger are used, this size
	// must increase.
	private int packetLength; // 32-bit sign int
	private int padlen; // packet length 1 byte unsigned
	private int position;
	private int phase = PHASE_PACKET_LENGTH;
	private SshCrypto2 crypto;

	public SshPacket2() {
	}

	public SshPacket2(SshCrypto2 _crypto) {
		/* receiving packet */
		crypto = _crypto;
	}

	public SshPacket2(byte newType) {
		packetType = newType;
	}

	/**
	 * Return the mp-int at the position offset in the data First 4 bytes are the number of bytes in the integer, msb
	 * first (for example, the value 0x00012345 would have 17 bits). The value zero has zero bits. It is permissible
	 * that the number of bits be larger than the real number of bits. The number of bits is followed by (bits + 7) / 8
	 * bytes of binary data, msb first, giving the value of the integer.
	 */
	public byte[] getMpInt() {
		return getBytes(getInt32());
	}

	public void putMpInt(byte[] foo) {
		int i = foo.length;
		if ((foo[0] & 0x80) != 0) {
			i++;
			putInt32(i);
			putByte((byte) 0);
		} else {
			putInt32(i);
		}
		putBytes(foo);
	}

	public byte[] getPayLoad(SshCrypto2 xcrypt, long seqnr) throws IOException {
		byte[] data = getData();

		int blocksize = xcrypt == null ? 8 : xcrypt.getSendBlockSize();

		// crypted data is:
		// packet length [ payloadlen + padlen + type + data ]
		packetLength = 4 + 1 + 1;
		if (data != null) {
			packetLength += data.length;
		}

		// pad it up to full blocksize.
		// If not crypto, zeroes, otherwise random.
		// (zeros because we do not want to tell the attacker the state of our
		// random generator)
		int padlen = blocksize - (packetLength % blocksize);
		if (padlen < 4) {
			padlen += blocksize;
		}

		byte[] padding = new byte[padlen];
		if (xcrypt == null) {
			for (int i = 0; i < padlen; i++) {
				padding[i] = 0;
			}
		} else {
			for (int i = 0; i < padlen; i++) {
				padding[i] = SshIO.getNotZeroRandomByte();
			}
		}

		// [ packetlength, padlength, padding, packet type, data ]
		byte[] block = new byte[packetLength + padlen];

		int xlen = padlen + packetLength - 4;
		block[3] = (byte) (xlen & 0xff);
		block[2] = (byte) ((xlen >> 8) & 0xff);
		block[1] = (byte) ((xlen >> 16) & 0xff);
		block[0] = (byte) ((xlen >> 24) & 0xff);

		block[4] = (byte) padlen;
		block[5] = getType();
		System.arraycopy(data, 0, block, 6, data.length);
		System.arraycopy(padding, 0, block, 6 + data.length, padlen);

		byte[] mac = null;
		if (xcrypt != null) {
			HMAC c2smac = xcrypt.getSndHmac();
			if (c2smac != null) {
				try {
					Tools.updateMACForInt((int) seqnr, c2smac);
					c2smac.update(block, 0, block.length);
					mac = c2smac.getMAC();
				} catch (CryptoTokenException ex) {
					Logger.fatal("Failed to obtain mac: " + ex.getMessage());
				}
			}
		}

		if (xcrypt != null) {
			block = xcrypt.encrypt(block);
		}

		byte[] sendblock = new byte[block.length + (mac != null ? mac.length : 0)];
		System.arraycopy(block, 0, sendblock, 0, block.length);
		if (mac != null) {
			System.arraycopy(mac, 0, sendblock, block.length, mac.length);
		}
		return sendblock;
	}

	;

	private byte encryptedBlock[];

	public int addPayload(byte buff[], int offset, int length) {
		int hmaclen;
		int end = offset + length;
		int blockSize;
		if (crypto == null) {
			blockSize = 8;
			hmaclen = 0;
		} else {
			hmaclen = crypto.getRcvHmac().getLength();
			blockSize = crypto.getReceiveBlockSize();
		}
		// Length of data decrypted after the packet length + padding length, up to block size.
		int decryptedLen = blockSize - 5;
		while (offset < end) {
			// @todo - this logic must be in a PacketFactory, which generates specific Packet instances
			// after doing initial decryption etc.
			switch (phase) {
				// Packet length: 32 bit unsigned integer
				// gives the length of the packet, not including the length field and padding.
				// @todo - check packet length against the maximum we said we'd accept (if we're after channel open -
				// what's the default prior to channel open? )
				case PHASE_PACKET_LENGTH:
					packetLengthArray[position++] = buff[offset++];
					// In order to decrypt, we must fill up our decrypt block size - even though we only want the first 
					// four bytes to get the packet length.  
					if (position == blockSize) {
						byte[] tempLengthArray;
						if (crypto == null) {
							tempLengthArray = this.packetLengthArray;
						} else {
							tempLengthArray = crypto.decrypt(this.packetLengthArray, blockSize);
						};
						// @todo - make a conversion-helper for these byte->ints , as they're showing up all over
						packetLength = (tempLengthArray[3] & 0xff) + ((tempLengthArray[2] & 0xff) << 8)
								+ ((tempLengthArray[1] & 0xff) << 16) + ((tempLengthArray[0] & 0xff) << 24);
						padlen = tempLengthArray[4];
						position = decryptedLen;
						packetLength += hmaclen;
						encryptedBlock = new byte[packetLength - 1];
						System.arraycopy(tempLengthArray, 5, encryptedBlock, 0, decryptedLen);
						phase++;
					}
					break; 

				case PHASE_BLOCK:
					if (encryptedBlock.length > position) {
						if (offset < end) {
							int amount = end - offset;
							if (amount > encryptedBlock.length - position) {
								amount = encryptedBlock.length - position;
							}
							System.arraycopy(buff, offset, encryptedBlock, position, amount);
							offset += amount;
							position += amount;
						}
					}

					if (position == encryptedBlock.length) { // the block fully loaded, now we're going to decrypt it
						packetLength -= hmaclen; // now we're going to load the hmac.
						// (incl type) + padding
						byte[] decryptedBlock = new byte[encryptedBlock.length - hmaclen - decryptedLen];
						byte[] data;
						System.arraycopy(encryptedBlock, decryptedLen, decryptedBlock, 0, decryptedBlock.length);

						if (crypto != null) {
							decryptedBlock = crypto.decrypt(decryptedBlock, decryptedBlock.length);
						}

						// Add back in first bytes
						byte[] dd = new byte[decryptedBlock.length + decryptedLen];
						System.arraycopy(encryptedBlock, 0, dd, 0, decryptedLen);
						System.arraycopy(decryptedBlock, 0, dd, decryptedLen, decryptedBlock.length);
						decryptedBlock = dd;

						packetType = decryptedBlock[0];
						// data
						if (packetLength > padlen + 1 + 1) { // 1 for padding
							// length, 1 for type
							data = new byte[packetLength - 2 - padlen];
							System.arraycopy(decryptedBlock, 1, data, 0, data.length);
							putData(data);
						} else {
							putData(null);
						}
						/* MAC! */
						return offset;
					}
					break;
			}
		}
		return offset;
	}

	public int getLength() {
		if (byteArray == null)
			return 0; 
		return byteArray.length;
	}
}
