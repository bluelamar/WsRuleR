
package org.bluelamar.wsruler;

/**
 * Implementations return object to be used for login to target service.
 *
 */
public interface ConnLoginFactory {

	/**
	 * buildLogin takes the id and secret and returns appropriate
	 * login object.
	 * @param connId used for login to service
	 * @param connSecret used for login to service
	 * @return login object used to login to service
	 */
	Object buildLogin(String connId, String connSecret);
	
	/**
	 * Returns an appropriate login object used to login to a target service.
	 * @param url is the target service which can be used to determine the required creds
	 * @return login object for target service
	 */
	Object getAuthLogin(String url);
}

