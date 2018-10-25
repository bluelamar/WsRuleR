/**
 * 
 */
package org.bluelamar.wsruler;

/**
 * Contains the credentials and login uri for a service.
 *
 */
public class ConnCreds {
	private String loginUri;
	private String connUser;
	private String connSecret; // @todo should be encrypted
	private ConnLoginFactory loginFactory;
	
	public ConnCreds() {
		
	}
	
	public ConnCreds(String loginUri, String connUser, String connSecret) {
		this.loginUri = loginUri;
		this.connUser = connUser;
		this.connSecret = connSecret;
	}
	
	public void setLoginFactory(ConnLoginFactory loginFactory) {
		this.loginFactory = loginFactory;
	}
	
	public Object getAuthLogin() {
		// ex: return an Object that contains name and password for couchdb
		// 
		if (loginFactory != null) {
			return loginFactory.buildLogin(connUser, connSecret);
		}
		return null;
	}
	
	public void setLoginUri(String loginUri) {
		this.loginUri = loginUri;
	}
	public void setConnUser(String connUser) {
		this.connUser = connUser;
	}
	public void setConnSecret(String connSecret) {
		this.connSecret = connSecret;
	}
	
	public String getLoginUri() {
		return loginUri;
	}
	public String getConnUser() {
		return connUser;
	}
	public String getConnSecret() {
		return connSecret;
	}
}
