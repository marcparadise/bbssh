package org.bbssh.ssh.packets;

import java.io.IOException;

import net.rim.device.api.crypto.CryptoException;
import net.rim.device.api.crypto.DSAPrivateKey;
import net.rim.device.api.crypto.DSASignatureSigner;
import net.rim.device.api.crypto.PKCS1SignatureSigner;
import net.rim.device.api.crypto.RSAPrivateKey;
import net.rim.device.api.crypto.SHA1Digest;

import org.bbssh.crypto.SignatureTools;
import org.bbssh.crypto.TypesReader;
import org.bbssh.crypto.TypesWriter;
import org.bbssh.ssh.SSHMessages;

public class PacketUserauthRequestPublicKey {
	byte[] payload;
	String userName;
	String serviceName;
	String password;
	String pkAlgoName;
	byte[] pkEnc;
	// byte[] sig = new byte[20];
	private byte[] sig;

	;

	public PacketUserauthRequestPublicKey(String serviceName, String user, String pkAlgorithmName, byte[] pk) {
		this.serviceName = serviceName;
		this.userName = user;
		this.pkAlgoName = pkAlgorithmName;
		this.pkEnc = pk;
	}

	public PacketUserauthRequestPublicKey(byte payload[], int off, int len) throws IOException {
		this.payload = new byte[len];
		System.arraycopy(payload, off, this.payload, 0, len);

		TypesReader tr = new TypesReader(payload, off, len);

		int packet_type = tr.readByte();

		if (packet_type != SSHMessages.SSH_MSG_USERAUTH_REQUEST) {
			throw new IOException("This is not a SSH_MSG_USERAUTH_REQUEST! (" + packet_type + ")");
		}

	}

	// @todo can this also use the PrivateKey interface?
	public PacketUserauthRequestPublicKey(RSAPrivateKey key, byte[] sessionId, String serviceName, String user,
			String pkAlgorithmName) throws IOException, CryptoException {
		this(serviceName, user, pkAlgorithmName, SignatureTools.encodePublicKey(key));

		// Now properly intiialize our signature.
		TypesWriter tw = new TypesWriter();
		tw.writeString(sessionId, 0, sessionId.length);
		addSharedFields(tw);

		PKCS1SignatureSigner signer = new PKCS1SignatureSigner(key, new SHA1Digest());
		signer.update(tw.getBytes());
		byte[] s = new byte[signer.getLength()];
		try {
			signer.sign(s, 0);
		} catch (IllegalArgumentException e) {

		}
		this.sig = SignatureTools.encodeSSHRSASignature(s);

	}

	public PacketUserauthRequestPublicKey(DSAPrivateKey key, byte[] sessionId, String serviceName, String user,
			String pkAlgorithmName) throws IOException, CryptoException {
		this(serviceName, user, pkAlgorithmName, SignatureTools.encodePublicKey(key));

		// Now properly intiialize our signature.
		TypesWriter tw = new TypesWriter();
		// In addition to the auth fields, signature also requires
		// the session ID.
		tw.writeString(sessionId, 0, sessionId.length);
		addSharedFields(tw);

		DSASignatureSigner signer = new DSASignatureSigner(key, new SHA1Digest());
		signer.update(tw.getBytes());
		byte[] r = new byte[signer.getRLength()];
		byte[] s = new byte[signer.getSLength()];
		signer.sign(r, 0, s, 0);
		this.sig = SignatureTools.encodeSSHDSASignature(r, s);

		// Generate signature.
	}

	public byte[] getPayload() {
		if (payload == null) {
			TypesWriter tw = new TypesWriter();
			addSharedFields(tw);
			tw.writeString(sig, 0, sig.length);
			payload = tw.getBytes();
		}
		return payload;
	}

	private void addSharedFields(TypesWriter tw) {
		tw.writeByte(SSHMessages.SSH_MSG_USERAUTH_REQUEST);
		tw.writeString(userName);
		tw.writeString(serviceName);
		tw.writeString("publickey");
		tw.writeBoolean(true); // yes, we have a key.
		tw.writeString(pkAlgoName);
		tw.writeString(pkEnc, 0, pkEnc.length);

	}
}
