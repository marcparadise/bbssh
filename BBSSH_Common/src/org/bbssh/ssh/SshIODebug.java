package org.bbssh.ssh;

import java.io.IOException;

import org.bbssh.net.session.Session;
import org.bbssh.net.session.SshSession;
import org.bbssh.ssh.packets.SshPacket2;
import org.bbssh.util.Logger;

/**
 * This class adds debug logging to SshIO. Note that this will be a potentially very heavy amount of logging.
 * 
 * @author marc
 * 
 */
public class SshIODebug extends SshIO {

	public SshIODebug(SshSession sshSession) {
		super(sshSession);
		Logger.debug("SshIODebug instance created.");

	}

	public void write(byte[] b) throws IOException {
		Logger.debug("SshIO.write begin: " + b.length);
		super.write(b);
		Logger.debug("SshIO.write end");

	}

	protected String handlePacket2(SshPacket2 p, Session session) throws IOException {
		Object type = SSHMessages.DebugAide.ht.get(p.getType());
		if (type == null) {
			type = "" + p.getType();
		}
		Logger.debug("SshIO.handlePacket2 begin - length " + p.getLength() + " type: " + type);
		String result = super.handlePacket2(p, session);
		
		Logger.debug("SshIO.handlePacket2 end");
		return result;
	}

	public void sendPacket2(SshPacket2 packet) throws IOException {
		Logger.debug("SshIO.sendPacket2 begin: " + SSHMessages.DebugAide.ht.get(packet.getType()) + " length "
				+ packet.getLength() + " seq " + outgoingseq);

		super.sendPacket2(packet);
		Logger.debug("SshIO.sendPacket2 end");
	}

	protected void sendOutboundChannelData() throws IOException {
		Logger.debug("SshIO.sendOutboundChannelData: " + dataToSend.toString());
		super.sendOutboundChannelData();
	}

	protected String handleChannelData(SshPacket2 p, Session session) throws IOException {
		String result = super.handleChannelData(p, session);
		Logger.debug("SshIO.handleChannelData: " + result);
		return result;

	}
}
