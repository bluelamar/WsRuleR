/**
 * 
 */
package org.bluelamar;

import java.io.Closeable;
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
	 * Http method POST of specified object
	 * @param path is the uri to post the resource
	 * @param obj should be annotated for serialization
	 * @return response object if any, else null
	 */
	int post(String path, Object obj) throws ConnException;
	
	/*
	 * Http method PUT of specified object
	 * @param path is the uri to put the resource
	 * @param obj should be annotated for serialization
	 * @return response object if any, else null
	 */
	int put(String path, Object obj) throws ConnException;
	
	/*
	 * Http method GET resource from specified path
	 * @param retType is the class type of the returned object
	 * @param path is the uri to get the resource
	 * @param args are query params if any
	 * @return response object of type retType
	 */
	<T> T get(Class<T> retType, String path, Map<String, String> args) throws ConnException;
	
	/*
	 * Http method GET resource from specified path
	 * @param path is the uri to get the resource
	 * @param args are query params if any
	 * @return response as a map
	 */
	Map<String, String> get(String path, Map<String, String> args) throws ConnException;
	
	/*
	 * For when the connection object is used to clone new Connection objects. 
	 */
	Connection clone();
}
