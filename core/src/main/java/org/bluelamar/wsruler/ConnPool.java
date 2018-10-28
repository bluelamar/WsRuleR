/**
 * 
 */
package org.bluelamar.wsruler;

/**
 * ConnPool manages pool of connections for various services.
 *
 */
public interface ConnPool {

	/*
	 * Obtains a Connection object per the request service type.
	 * @param svcName is name of type of service
	 * @return Connection object if the service type is supported
	 */
	Connection getConnection(String svcName) throws ConnException;
	
	/*
	 * Caller returns a Connection object obtained via @getConnection
	 */
	void returnConnection(Connection conn);
	
	/*
	 * Sets a Connection object for which clones will be created in calls
	 * to @getConnection.
	 */
	void setConnectionCloner(Connection connCloner, ConnLoginFactory creds);
	
	/*
	 * Shutdown all connections from the pool.
	 */
	void shutdown();
	
	/*
	 * Set a cloned connection object upper limit.
	 * @param numActiveConns is maximum cloned connections.
	 */
	void setConnLimit(int numActiveConns);
}
