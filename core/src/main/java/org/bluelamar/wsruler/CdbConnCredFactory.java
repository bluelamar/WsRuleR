/**
 * 
 */
package org.bluelamar.wsruler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the credentials and login uri used to build a login object
 * for CouchDB.
 *
 */
public class CdbConnCredFactory implements ConnLoginFactory {
	
	private static final Logger LOG = LoggerFactory.getLogger(CdbConnCredFactory.class);
	
	private String loginUri = "_session";
	private String connUser;
	private String connSecret; // @todo should be encrypted
	
	static final String TEST_USER = "wsruler.test.login.user";
	static final String TEST_SECRET = "wsruler.test.login.secret";
	
	public CdbConnCredFactory() {
		
		connUser = System.getProperty(TEST_USER);
		connSecret = System.getProperty(TEST_SECRET);
	}
	
	public CdbConnCredFactory(String loginUri, String connUser, String connSecret) {
		this.loginUri = loginUri;
		this.connUser = connUser;
		this.connSecret = connSecret;
	}
	
	/**
	 * This is a simple stub implementation that uses a completely
	 * unsafe way to get the creds for CouchDB.
	 */
	@Override
	public Object buildLogin(String connId, String connSecret) {
		// Return an Object that contains name and password for couchdb
		return new Object() {
			public String name = connId;
			public String password = connSecret;
		};
	}
	
	@Override
	public Object getAuthLogin(String url) {
		// The url points at particular host+service for which the login creds
		// must be obtained for.
		// Ex: key service based on host+service name,
		//     or a local config file that would be parsed to get the
		//     host+service credentials
		 
		LOG.debug("getAuthLogin: user=" + connUser + " sec=" + connSecret);
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
