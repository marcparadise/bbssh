package ch.ethz.ssh2.crypto;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import net.rim.device.api.crypto.AESKey;
import net.rim.device.api.crypto.BlockDecryptorEngine;
import net.rim.device.api.crypto.CBCDecryptorEngine;
import net.rim.device.api.crypto.CryptoException;
import net.rim.device.api.crypto.CryptoInteger;
import net.rim.device.api.crypto.CryptoTokenException;
import net.rim.device.api.crypto.CryptoUnsupportedOperationException;
import net.rim.device.api.crypto.DESKey;
import net.rim.device.api.crypto.DSACryptoSystem;
import net.rim.device.api.crypto.DSAKeyPair;
import net.rim.device.api.crypto.DSAPrivateKey;
import net.rim.device.api.crypto.DSAPublicKey;
import net.rim.device.api.crypto.DecryptorFactory;
import net.rim.device.api.crypto.InitializationVector;
import net.rim.device.api.crypto.InvalidKeyException;
import net.rim.device.api.crypto.KeyPair;
import net.rim.device.api.crypto.MD5Digest;
import net.rim.device.api.crypto.NoSuchAlgorithmException;
import net.rim.device.api.crypto.RSACryptoSystem;
import net.rim.device.api.crypto.RSAKeyPair;
import net.rim.device.api.crypto.RSAPrivateKey;
import net.rim.device.api.crypto.RSAPublicKey;
import net.rim.device.api.crypto.SymmetricKey;
import net.rim.device.api.crypto.TripleDESKey;
import net.rim.device.api.io.Base64InputStream;
import net.rim.device.api.io.LineReader;

import org.bbssh.util.Tools;

/**
 * PEM Support.
 * 
 * @author Christian Plattner, plattner@inf.ethz.ch
 * @version $Id: PEMDecoder.java,v 1.7 2006/02/02 09:11:03 cplattne Exp $
 */
public class PEMDecoder {
	private static final int PEM_RSA_PRIVATE_KEY = 1;
	private static final int PEM_DSA_PRIVATE_KEY = 2;

	private static byte[] generateKeyFromPasswordSaltWithMD5(byte[] password, byte[] salt,
			int keyLen)
			throws IOException {
		if (salt.length < 8) {
			throw new IllegalArgumentException(
					"Salt needs to be at least 8 bytes for key generation.");
		}
		MD5Digest md5 = new MD5Digest();

		byte[] key = new byte[keyLen];
		byte[] tmp = new byte[md5.getDigestLength()];

		while (true) {
			md5.update(password, 0, password.length);
			md5.update(salt, 0, 8); // ARGH we only use the first 8 bytes of the salt in this step.
			// This took me two hours until I got AES-xxx running.

			int copy = (keyLen < tmp.length) ? keyLen : tmp.length;

			tmp = md5.getDigest();

			System.arraycopy(tmp, 0, key, key.length - keyLen, copy);

			keyLen -= copy;

			if (keyLen == 0) {
				return key;
			}

			md5.update(tmp, 0, tmp.length);
		}
	}

	private static byte[] removePadding(byte[] buff, int blockSize) throws IOException {
		/* Removes RFC 1423/PKCS #7 padding */

		int rfc_1423_padding = buff[buff.length - 1] & 0xff;

		if ((rfc_1423_padding < 1) || (rfc_1423_padding > blockSize)) {
			throw new IOException(
					"Decrypted PEM has wrong padding, did you specify the correct password?");
		}

		for (int i = 2; i <= rfc_1423_padding; i++) {
			if (buff[buff.length - i] != rfc_1423_padding) {
				throw new IOException(
						"Decrypted PEM has wrong padding, did you specify the correct password?");
			}
		}

		byte[] tmp = new byte[buff.length - rfc_1423_padding];
		System.arraycopy(buff, 0, tmp, 0, buff.length - rfc_1423_padding);
		return tmp;
	}

	public static final PEMStructure parsePEM(byte[] pem) throws IOException {
		PEMStructure ps = new PEMStructure();

		String line = null;
		LineReader reader = new LineReader(new ByteArrayInputStream(pem));

		String endLine = null;

		while (true) {
			line = new String(reader.readLine());

//			if (line.length() == 0) {
//				throw new IOException("Invalid PEM structure, '-----BEGIN...' missing");
//			}

			line = line.trim();

			if (line.startsWith("-----BEGIN DSA PRIVATE KEY-----")) {
				endLine = "-----END DSA PRIVATE KEY-----";
				ps.pemType = PEM_DSA_PRIVATE_KEY;
				break;
			}

			if (line.startsWith("-----BEGIN RSA PRIVATE KEY-----")) {
				endLine = "-----END RSA PRIVATE KEY-----";
				ps.pemType = PEM_RSA_PRIVATE_KEY;
				break;
			}
		}

		while (true) {
			line = new String(reader.readLine());

//			if (line == null) {
//				throw new IOException("Invalid PEM structure, " + endLine + " missing");
//			}

			line = line.trim();

			int sem_idx = line.indexOf(':');

			if (sem_idx == -1) {
				break;
			}

			String name = line.substring(0, sem_idx + 1);
			String value = line.substring(sem_idx + 1);
			String values[] = Tools.splitString(value, ',');

			for (int i = 0; i < values.length; i++) {
				values[i] = values[i].trim();
			}

			// Proc-Type: 4,ENCRYPTED
			// DEK-Info: DES-EDE3-CBC,579B6BE3E5C60483

			if ("Proc-Type:".equals(name)) {
				ps.procType = values;
				continue;
			}

			if ("DEK-Info:".equals(name)) {
				ps.dekInfo = values;
				continue;
			}
			/* Ignore line */
		}

		StringBuffer keyData = new StringBuffer();

		while (true) {
			if (line == null) {
				throw new IOException("Invalid PEM structure, " + endLine + " missing");
			}

			line = line.trim();

			if (line.startsWith(endLine)) {
				break;
			}

			keyData.append(line);

			line = new String(reader.readLine());

		}

		char[] pem_chars = new char[keyData.length()];
		keyData.getChars(0, pem_chars.length, pem_chars, 0);

		ps.data = Base64InputStream.decode(keyData.toString()); // pem_chars);

		if (ps.data.length == 0) {
			throw new IOException("Invalid PEM structure, no data available");
		}

		return ps;
	}

	public static final void decryptPEM(PEMStructure ps, byte[] pw) throws
			IOException, CryptoTokenException, CryptoUnsupportedOperationException {
		try {
			if (!isPEMEncrypted(ps)) {
				return;
			}
			if (pw == null || pw.length == 0) {
				return;
			}
			if (ps.dekInfo == null) {
				throw new IOException("Broken PEM, no mode and salt given, but encryption enabled");
			}
			if (ps.dekInfo.length != 2) {
				throw new IOException("Broken PEM, DEK-Info is incomplete!");
			}
			BlockDecryptorEngine bc;
			BlockDecryptorEngine bcmain;
			String algo = ps.dekInfo[0];
			byte[] salt = Tools.getHexStringAsBytes(ps.dekInfo[1]);
			SymmetricKey key;
			// NOte that the key is generated here, based on password + salt (as provided in the key file)
			if (algo.equals("DES-EDE3-CBC")) {
				key = new TripleDESKey(generateKeyFromPasswordSaltWithMD5(pw, salt, 24));
			} else if (algo.equals("DES-CBC")) {
				key = new DESKey(generateKeyFromPasswordSaltWithMD5(pw, salt, 8));
			} else if (algo.equals("AES-128-CBC")) {
				key = new AESKey(generateKeyFromPasswordSaltWithMD5(pw, salt, 16));
			} else if (algo.equals("AES-192-CBC")) {
				key = new AESKey(generateKeyFromPasswordSaltWithMD5(pw, salt, 24));
			} else if (algo.equals("AES-256-CBC")) {
				key = new AESKey(generateKeyFromPasswordSaltWithMD5(pw, salt, 32));
			} else {
				throw new IOException("Cannot decrypt PEM structure, unknown cipher " + algo);
			}
			bcmain = DecryptorFactory.getBlockDecryptorEngine(key);
			bc =
					new CBCDecryptorEngine(bcmain,
							new InitializationVector(salt));
			int blockSize = bc.getBlockLength();
			if ((ps.data.length % blockSize) != 0) {
				throw new IOException("Invalid PEM structure, size of encrypted block is not a multiple of " + bc.
						getBlockLength());
			}
			byte[] dz = new byte[ps.data.length];
			for (int i = 0; i < ps.data.length / blockSize; i++) {
				bc.decrypt(ps.data, i * blockSize, dz, i * blockSize);
			}
			/* Now check and remove RFC 1423/PKCS #7 padding */
			dz = removePadding(dz, blockSize);
			ps.data = dz;
			ps.dekInfo = null;
			ps.procType = null;
		} catch (NoSuchAlgorithmException ex) {
			throw new IOException(ex.getMessage());
		} catch (CryptoException ex) {
			throw new IOException(ex.getMessage());
		}
	}

	public static final boolean isPEMEncrypted(PEMStructure ps) throws IOException {
		if (ps.procType == null) {
			return false;
		}

		if (ps.procType.length != 2) {
			throw new IOException("Unknown Proc-Type field.");
		}

		if ("4".equals(ps.procType[0]) == false) {
			throw new IOException("Unknown Proc-Type field (" + ps.procType[0] + ")");
		}

		if ("ENCRYPTED".equals(ps.procType[1])) {
			return true;
		}

		return false;
	}

	public static RSAKeyPair decodeRSA(SimpleDERReader dr) throws IOException, InvalidKeyException {
		CryptoInteger version = dr.readCryptoInt();
		if (version.compareTo(0) != 0 && version.compareTo(1) != 0) {
			throw new IOException("Wrong version (" + version.toString() + ") in RSA PRIVATE KEY DER stream.");
		}

		CryptoInteger n = dr.readCryptoInt();
		CryptoInteger e = dr.readCryptoInt();
		CryptoInteger d = dr.readCryptoInt();
		try {
			byte[] nb = n.toByteArray();
			RSACryptoSystem cs = new RSACryptoSystem(nb.length * 8);
			RSAPrivateKey prk = new RSAPrivateKey(cs, e.toByteArray(), d.toByteArray(), nb);
			RSAPublicKey pk = new RSAPublicKey(cs, e.toByteArray(), n.toByteArray());
			return new RSAKeyPair(pk, prk);
		} catch (InvalidKeyException ex) {
			throw ex;
		} catch (Throwable ex) {
			throw new IOException("Unable to create key-pair"); // " + ex.toString()); ;

		}

	}

	public static DSAKeyPair decodeDSA(SimpleDERReader dr) throws IOException, InvalidKeyException {

		CryptoInteger version = dr.readCryptoInt();

		if (version.compareTo(0) != 0) {
			throw new IOException("Wrong version (" + version + ") in DSA PRIVATE KEY DER stream.");
		}

		CryptoInteger p = dr.readCryptoInt();
		CryptoInteger q = dr.readCryptoInt();
		CryptoInteger g = dr.readCryptoInt();
		CryptoInteger y = dr.readCryptoInt();
		CryptoInteger x = dr.readCryptoInt();

		if (dr.available() != 0) {
			throw new IOException("Padding in DSA PRIVATE KEY DER stream.");
		}
		DSACryptoSystem cs;
		try {
			cs = new DSACryptoSystem(p.toByteArray(), q.toByteArray(), g.toByteArray());
			byte[] privk = x.toByteArray();
			byte[] pubk = y.toByteArray();

			DSAPublicKey pubKey = new DSAPublicKey(cs, pubk);
			DSAPrivateKey privKey = new DSAPrivateKey(cs, privk);

			return new DSAKeyPair(pubKey, privKey);
		} catch (InvalidKeyException ex) {
			throw ex;
		} catch (Throwable e) {
			throw new IOException("Unable to create key-pair: " + e.toString());
		}

	}

	public static KeyPair decode(byte[] pem, String password) throws IOException, InvalidKeyException {
		// @todo this must b e converted to parse into BB Crypto version of RSAPrivateKey.
		PEMStructure ps = parsePEM(pem);

		if (isPEMEncrypted(ps)) {
			try {
				if (password == null || password.length() == 0) {
					throw new IOException("PEM is encrypted, but no password was specified");
				}
				decryptPEM(ps, password.getBytes());
			} catch (CryptoTokenException ex) {
				throw new IOException(ex.getMessage());
			} catch (CryptoUnsupportedOperationException ex) {
				throw new IOException(ex.getMessage());
			}
		}
		SimpleDERReader dr = new SimpleDERReader(ps.data);
		byte[] seq = dr.readSequenceAsByteArray();

		if (dr.available() != 0) {
			throw new IOException("Padding in PRIVATE KEY DER stream.");
		}

		dr.resetInput(seq);

		if (ps.pemType == PEM_DSA_PRIVATE_KEY) {
			return decodeDSA(dr);
		}

		if (ps.pemType == PEM_RSA_PRIVATE_KEY) {
			return decodeRSA(dr);
		}
		throw new IOException("Unrecognized key type.");
	}
}
