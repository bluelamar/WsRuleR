/**
 * 
 */
package org.bluelamar.wsruler;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

/**
 * Connection object used to connect to a service with REST api.
 *
 */
public interface Connection extends Cloneable, Closeable {

	enum ConnStatus {
		Unconnected,
		Connected,
		BadConnection
	}
	/*
	 * Set the service type name for this connection.
	 * @param svcName is the service type name
	 */
	void setSvcName(String svcName);
	
	/*
	 * Get the service type name as set by @setSvcName
	 * @return service type name
	 */
	String getSvcName();
	
	/*
	 * Setup for comm with target service
	 * Ex url: https://server-yourcompany.com:4443/
	 * @param url the base url of the target service
	 */
	void setUrl(String url);
	
	/**
	 * Give access to the serverbase  url
	 * @return base url
	 */
	String getUrl();
	
	/**
	 * Get the current connection status
	 * @return current connection status
	 */
	ConnStatus getConnStatus();
	
	/**
	 * Set connection status
	 * @param status current status representation
	 */
	void setConnStatus(ConnStatus status);
	
	/*
	 * Perform initialization with the server for given creds.
	 * @param creds used to get a session with the server
	 */
	void doAuthInit(ConnLoginFactory creds) throws ConnException;
	
	/*
	 * Http method POST of specified object
	 * @param path is the uri to post the resource
	 * @param obj should be annotated for serialization
	 * @param inCookies are cookies to send to the server
	 * @param outHeaders headers returned from the server
	 * @return response object as a Map, else null
	 */
	Map<String,Object> post(String path, Object obj, Map<String, List<String>> outHeaders) throws ConnException;
	
	/*
	 * Http method PUT of specified object
	 * @param path is the uri to put the resource
	 * @param obj should be annotated for serialization
	 * @param args are query params (optional)
	 * @return response object if any, else null
	 */
	int put(String path, Object obj, Map<String, String> args) throws ConnException;
	
	/*
	 * Http method GET resource from specified path
	 * @param retType is the class type of the returned object
	 * @param path is the uri to get the resource
	 * @param args are query params (optional)
	 * @return response object of type retType
	 */
	<T> T get(Class<T> retType, String path, Map<String, String> args) throws ConnException;
	
	/*
	 * Http method GET resource from specified path
	 * @param path is the uri to get the resource
	 * @param args are query params (optional)
	 * @return response as a map
	 */
	Map<String, Object> get(String path, Map<String, String> args) throws ConnException;
	
	/*
	 * Delete specified resource path.
	 * @param path is the uri to get the resource
	 * @param args are query params (optional)
	 * @return http response code
	 */
	int delete(String path, Map<String, String> args) throws ConnException;
	
	/**
	 * Search path for field that contains the specified value.
	 * @param path is the uri to search for resources
	 * @param field name upon which to search for a match
	 * @param value to match against in the field
	 * @return list of matched objects
	 * @throws ConnException
	 */
	List<Object> searchEqual(String path, String field, String value) throws ConnException;
	
	/*
	 * For when the connection object is used to clone new Connection objects.
	 * Meant to get all relevant fields of implementation passed to its clone.
	 */
	Connection clone();
}
