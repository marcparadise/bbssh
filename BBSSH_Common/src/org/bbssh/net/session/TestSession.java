package org.bbssh.net.session;

import java.io.IOException;

import net.rim.device.api.system.Alert;

import org.bbssh.model.ConnectionProperties;
import org.bbssh.terminal.VT320;

/**
 * A null session implementation, for use in testing terminal emulation (the
 * overall framework requires a live sesssion).
 * 
 * @author marc
 */
public class TestSession extends Session implements SessionIOHandler {

	public TestSession(ConnectionProperties prop, int sessId, SessionListener listenr) {
		super(prop, sessId, listenr, new VT320() {
			public void sendData(byte[] b, int offset, int length) throws IOException {

			}

			public void resize() {
			}

			public void beep() {
				Alert.startVibrate(100);

			}
		});
	}

	public void handleSendData(byte[] data, int offset, int length) throws IOException {
		// we don't actually send data

	}

	public void handleReceiveData(byte[] data, int offset, int length) throws IOException {

	}

	public void handleResize() {

	}

	public void handleConnection() throws IOException {

	}

	protected int getDefaultPort() {
		return 0;
	}

	public void connect() {
		setConnectionState(CONNSTATE_CONNECTED);
	}

}
