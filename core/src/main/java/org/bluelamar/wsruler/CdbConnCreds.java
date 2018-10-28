/**
 * 
 */
package org.bluelamar.wsruler;

/**
 * Contains the credentials and login uri used to build a login object
 * for CouchDB.
 *
 */
public class CdbConnCreds implements ConnLoginFactory {
	private String loginUri;
	private String connUser;
	private String connSecret; // @todo should be encrypted
	
	public CdbConnCreds() {
		
	}
	
	public CdbConnCreds(String loginUri, String connUser, String connSecret) {
		this.loginUri = loginUri;
		this.connUser = connUser;
		this.connSecret = connSecret;
	}
	
	@Override
	public Object buildLogin(String connId, String connSecret) {
		return new Object() {
			public String name = connId;
			public String password = connSecret;
		};
	}
	
	@Override
	public Object getAuthLogin() {
		// ex: return an Object that contains name and password for couchdb
		// 
		return buildLogin(connUser, connSecret);
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
