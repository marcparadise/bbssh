package org.bbssh.ssh.kex;

import java.util.Hashtable;

import net.rim.device.api.crypto.NoSuchAlgorithmException;

public class CipherManager {
	private static CipherManager me;
	private String[] supportedCiphers;

	private Hashtable ciphers = new Hashtable(10);

	/**
	 * Constructor. INitializes table of supported ciphers.
	 */
	private CipherManager() {
		ciphers.put("aes256-cbc", new CipherAttributes(32, "AES_256_256", "AES_256", "AES/CBC"));
		ciphers.put("aes192-cbc", new CipherAttributes(24, "AES_192_192", "AES_192", "AES/CBC"));
		ciphers.put("aes128-cbc", new CipherAttributes(16, "AES_128_128", "AES_128", "AES/CBC"));
		ciphers.put("3des-cbc", new CipherAttributes(24, "TripleDES_192_64", "TripleDES_192", "TripleDES/CBC"));
		ciphers.put("cast128-cbc", new CipherAttributes(16, "CAST128", "CAST128", "CAST128/CBC"));
		// Because the order is important (most-preferred first) we can't just walk through the
		// hash keys to biuld the array..
		supportedCiphers = new String[] { "aes128-cbc", "3des-cbc", "cast128-cbc", "aes192-cbc", "aes256-cbc" };

		// @todo future: blowfish-cbc,arcfour128,arcfour256,arcfour
		// @todo future: stream-based: aes192-ctr,aes128-ctr,aes256-ctr

	}

	public CipherAttributes getCipherAttributes(String name) throws NoSuchAlgorithmException {
		CipherAttributes attributes = (CipherAttributes) ciphers.get(name);
		if (name == null) {
			throw new NoSuchAlgorithmException("Unsupported algorithm: " + name);
		}
		return attributes;

	}

	public synchronized static CipherManager getInstance() {
		if (me == null) {
			me = new CipherManager();
		}
		return me;
	}

	public String[] getSupportedCiphers() {
		return supportedCiphers;

	}

}
