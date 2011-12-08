package org.bbssh.net.session;

public interface SessionDataListener {
	/** 
	 * invoked when data is received. 
     * @TODO this should be invoked AFTER data is translated by the io handler!
	 */
	public void onDataReceived(int sessionId, String data); 
}
