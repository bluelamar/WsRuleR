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
	
	/*
	 * Perform initialization with the server for given creds.
	 * @param creds used to get a session with the server
	 */
	void doAuthInit(ConnCreds creds) throws ConnException;
	
	/*
	 * Http method POST of specified object
	 * @param path is the uri to post the resource
	 * @param obj should be annotated for serialization
	 * @param inCookies are cookies to send to the server
	 * @param outHeaders headers returned from the server
	 * @return response object if any, else null
	 */
	int post(String path, Object obj, Map<String, List<String>> outHeaders) throws ConnException;
	
	/*
	 * Http method PUT of specified object
	 * @param path is the uri to put the resource
	 * @param obj should be annotated for serialization
	 * @param inCookies are cookies to send to the server
	 * @return response object if any, else null
	 */
	int put(String path, Object obj) throws ConnException;
	
	/*
	 * Http method GET resource from specified path
	 * @param retType is the class type of the returned object
	 * @param path is the uri to get the resource
	 * @param args are query params if any
	 * @param inCookies are cookies to send to the server
	 * @return response object of type retType
	 */
	<T> T get(Class<T> retType, String path, Map<String, String> args) throws ConnException;
	
	/*
	 * Http method GET resource from specified path
	 * @param path is the uri to get the resource
	 * @param args are query params if any
	 * @param inCookies are cookies to send to the server
	 * @return response as a map
	 */
	Map<String, String> get(String path, Map<String, String> args) throws ConnException;
	
	/*
	 * For when the connection object is used to clone new Connection objects. 
	 */
	Connection clone();
}
