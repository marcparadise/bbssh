package org.bbssh.crypto;

import java.io.IOException;

import net.rim.device.api.crypto.CryptoException;
import net.rim.device.api.crypto.CryptoInteger;
import net.rim.device.api.crypto.CryptoTokenException;
import net.rim.device.api.crypto.CryptoUnsupportedOperationException;
import net.rim.device.api.crypto.DSACryptoSystem;
import net.rim.device.api.crypto.DSAPrivateKey;
import net.rim.device.api.crypto.DSAPublicKey;
import net.rim.device.api.crypto.PKCS1SignatureVerifier;
import net.rim.device.api.crypto.RSACryptoSystem;
import net.rim.device.api.crypto.RSAPrivateKey;
import net.rim.device.api.crypto.RSAPublicKey;
import net.rim.device.api.crypto.SHA1Digest;
import net.rim.device.api.io.Base64OutputStream;

import org.bbssh.ssh.packets.SshPacket2;
import org.bbssh.util.Tools;

public class SignatureTools {

	public static byte[] encodePublicKey(RSAPrivateKey key) throws CryptoException {
		SshPacket2 p = new SshPacket2();
		p.putString("ssh-rsa");
		p.putMpInt(key.getE());
		p.putMpInt(key.getN());
		return p.getBytes();
	}

	// @todo unfortunately we don't know that we'll be able to use DSAPrivateKey,
	// because RIM only supports up to 1024 bit keys...

	public static byte[] encodePublicKey(DSAPrivateKey key) throws CryptoException {
		SshPacket2 p = new SshPacket2();
		DSACryptoSystem ds = key.getDSACryptoSystem();

		p.putString("ssh-dss");
		p.putMpInt(ds.getP());
		p.putMpInt(ds.getQ());
		p.putMpInt(ds.getG());
		p.putMpInt(key.getPublicKeyData());

		return p.getBytes();

	}

	public static String getExportedKey(DSAPrivateKey key) throws CryptoException, IOException {
		byte[] encoded = encodePublicKey(key);
		return "ssh-rsa " + Base64OutputStream.encodeAsString(encoded, 0, encoded.length, false, false);

	}

	public static String getExportedKey(RSAPrivateKey key) throws CryptoException, IOException {
		byte[] encoded = encodePublicKey(key);
		return "ssh-dss " + Base64OutputStream.encodeAsString(encoded, 0, encoded.length, false, false);

	}

	public static byte[] encodeSSHRSASignature(byte[] s) throws IOException {
		TypesWriter tw = new TypesWriter();

		tw.writeString("ssh-rsa");

		/* "The value for 'rsa_signature_blob' is encoded as a string
		 * containing s (which is an integer, without lengths or padding, unsigned and in
		 * network byte order)."
		 */

		/* Remove first zero sign byte, if present */
		int offset = ((s.length > 1) && (s[0] == 0x00)) ? 1 : 0;
		tw.writeString(s, offset, s.length - offset);
		return tw.getBytes();
	}

	public static byte[] encodeSSHDSASignature(byte[] r, byte[] s) {
		TypesWriter tw = new TypesWriter();

		tw.writeString("ssh-dss");

		byte[] a40 = new byte[40];

		/* Patch (unsigned) r and s into the target array. */

		// int roffset = (r.length > 1 && r[0] == 0x00) ? 1 : 0;
		int r_copylen = (r.length < 20) ? r.length : 20;
		// int soffset = (s.length > 1 && s[0] == 0x00) ? 1 : 0;
		int s_copylen = (s.length < 20) ? s.length : 20;

		System.arraycopy(r, r.length - r_copylen, a40, 20 - r_copylen, r_copylen);
		System.arraycopy(s, s.length - s_copylen, a40, 40 - s_copylen, s_copylen);

		tw.writeString(a40, 0, 40);

		return tw.getBytes();
	}

	/**
	 * Perform standard RSA signature verification, using RIM's crypto API.
	 * 
	 * @param tr contains public key
	 * @param signature signature to use in verification
	 * @param message message to verify
	 * @return true if signature verifies message successfully.
	 * @throws IOException
	 */
	public static boolean verifyRSASignature(TypesReader tr, byte[] signature, byte[] message) throws IOException {

		try {
			byte[] e = tr.readByteString();
			byte[] n = tr.readByteString();
			n = Tools.removeBytePadding(n);
			RSACryptoSystem cs = new RSACryptoSystem(n.length * 8);
			TypesReader sig = new TypesReader(signature);
			sig.readString(); // "ssh-rsa"
			byte[] s = sig.readByteString();

			RSAPublicKey pk = new RSAPublicKey(cs, e, n);
			PKCS1SignatureVerifier v = new PKCS1SignatureVerifier(pk, new SHA1Digest(), s, 0);
			v.update(message);
			if (v.verify())
				return true;
			return false;

		} catch (Throwable e1) {
			throw new IOException(e1.toString() + " - " + e1.getMessage());
		}

	}

	/**
	 * This method implements a DSA signature verification (quick-reference:
	 * http://en.wikipedia.org/wiki/Digital_Signature_Algorithm#Verifying) using the RIM CryptoInteger class to do most
	 * of the heavy lifting.
	 * 
	 * Note that we're not able to use the RIM crypto library: it is compatible with DSA keys compliant with FIPS-186-2
	 * which allows for key of up to 1024 bits in length. However FIPS-186-3 (approved jun 2009) allows for up to 3072
	 * bit. Many more installations are using key lengths of > 1024 bits; but these will not pass RIM crypto validation.
	 * 
	 * Therefore we've implemented our own validation (which does not check bit length component at all) to work around
	 * this.
	 * 
	 * @param message message to sign
	 * @param sig signature
	 * @param p
	 * @param q
	 * @param g
	 * @param y
	 * @throws IOException
	 */
	public static void verifyDSASignature(byte[] message, byte[] sig, CryptoInteger p, CryptoInteger q,
			CryptoInteger g,
			CryptoInteger y) throws IOException {

		SHA1Digest md = new SHA1Digest();
		SshPacket2 buf = new SshPacket2();
		buf.putBytes(sig);
		buf.getByteString(); // algorithm wihch we know to be ssh-dss
		byte[] blob = buf.getByteString();
		md.update(message);
		// extract R and S from the input
		int rslen = blob.length / 2;
		CryptoInteger r = new CryptoInteger(blob, 0, rslen);
		CryptoInteger s = new CryptoInteger(blob, rslen, rslen);
		CryptoInteger m = new CryptoInteger(md.getDigest());
		CryptoInteger wc = s.invert(q);
		CryptoInteger u1 = m.multiply(wc, q);
		CryptoInteger u2 = r.multiply(wc, q);
		CryptoInteger zero = new CryptoInteger(0);
		if (zero.compareTo(r) >= 0 || q.compareTo(r) <= 0) {
			throw new IOException("DSA signature invalid:  r/q out of range.");
		}
		if (zero.compareTo(s) >= 0 || q.compareTo(s) <= 0) {
			throw new IOException("DSA signature invalid: s/q out of range.");
		}
		u1 = g.exponent(u1, p);
		u2 = y.exponent(u2, p);// biginteger.modpow
		CryptoInteger vc = u1.multiply(u2, p).mod(q);
		if (!vc.equals(r)) {
			throw new IOException("DSA signature verification failed.");
		}
	}

	public static Object exportPublicKey(RSAPublicKey pk) throws IOException, CryptoTokenException,
			CryptoUnsupportedOperationException {
		SshPacket2 p = new SshPacket2();
		p.putString("ssh-rsa");
		p.putMpInt(pk.getE());
		p.putMpInt(pk.getN());
		byte[] encoded = p.getBytes();
		return "ssh-rsa " + Base64OutputStream.encodeAsString(encoded, 0,
				encoded.length, false, false);
	}

	public static Object exportPublicKey(DSAPublicKey pk) throws CryptoTokenException,
			CryptoUnsupportedOperationException, IOException {
		// public byte[] encode() {
		SshPacket2 p = new SshPacket2();
		DSACryptoSystem cs = pk.getDSACryptoSystem();
		p.putString("ssh-dss");
		p.putMpInt(cs.getP());
		p.putMpInt(cs.getQ());
		p.putMpInt(cs.getG());

		p.putMpInt(pk.getPublicKeyData());
		byte[] encoded = p.getBytes();
		return "ssh-dss " + Base64OutputStream.encodeAsString(encoded, 0,
							encoded.length, false, false);
	}

}
