package org.bbssh.ssh.kex;
public class CipherAttributes {
	private int keySize;
	private String keyAlgorithm;
	private String ivAlgorithm;
	private String cryptoAlgorithm;

	public CipherAttributes(int keySize, String keyAlgorithm, String ivAlgorithm, String cryptoAlgorithm) {
		this.keySize = keySize;
		this.keyAlgorithm = keyAlgorithm;
		this.ivAlgorithm = ivAlgorithm;
		this.cryptoAlgorithm = cryptoAlgorithm;
	}

	/**
	 * @return the key size in bytes
	 */
	public int getKeySize() {
		return this.keySize;
	}
	/**
	 * @return the key algorithm name
	 */
	public String getKeyAlgorithm() {
		return this.keyAlgorithm;
	}
	/**
	 * @return the initialization vector algorithm name
	 */
	public String getIVAlgorithm() {
		return this.ivAlgorithm;
	}
	/**
	 * @return crypto algorithm name
	 */
	public String getCryptoAlgorithm() {
		return this.cryptoAlgorithm;
	}


}

