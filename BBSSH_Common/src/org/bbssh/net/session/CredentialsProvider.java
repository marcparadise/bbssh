package org.bbssh.net.session;

import org.bbssh.model.ConnectionProperties;
import org.bbssh.model.Key;

public interface CredentialsProvider {
	public class Credentials { 
		private String username; 
		private String password;
		/**
		 * @return the username
		 */
		public String getUsername() {
			return this.username;
		}
		/**
		 * @param username
		 */
		public void setUsername(String username) {
			this.username = username;
		}
		/**
		 * @return the password
		 */
		public String getPassword() {
			return this.password;
		}
		/**
		 * @param password the password to set
		 */
		public void setPassword(String password) {
			this.password = password;
		}
		
	}
	/**
	 * If a key is not successfully decrypted, or if is encrytped but no password provided,
	 * this callback will be invoked to obtain the password. 
	 * @param key
	 * @return the password supplied for the given key. 
	 */
	public String getKeyPassword(Key key);
	
	public Credentials getSessionCredentials(ConnectionProperties prop);
	
	

}
