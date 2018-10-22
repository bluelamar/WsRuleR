/**
 * 
 */
package org.bluelamar;

import java.io.IOException;
import java.util.Map;

import javax.ws.rs.client.*;
import javax.ws.rs.core.*;

/**
 * REST based service connection
 *
 */
public class RestConnection implements Connection {

	String svcName;
	WebTarget baseTarget;
	
	public RestConnection() {
	}
	
	/**
	 * Construct a RestConnection for the given base url
	 * @param url of target service
	 */
	public RestConnection(String url) {
        setUrl(url);
	}
	
	/**
	 * This constructor is used to clone this object.
	 * @param svcName name of target service to connect to
	 * @param baseTarget target base url to connect to
	 */
	public RestConnection(String svcName, WebTarget baseTarget) {
		this.svcName = svcName;
		this.baseTarget = baseTarget;
	}
	
	/*
	 * Setup for comm with target service
	 * Ex url: https://server-yourcompany.com:4443/
	 * @param url the base url of the target service
	 */
	public void setUrl(String url) {
		Client client = ClientBuilder.newClient();
        baseTarget = client.target(url);
	}

	/* (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.bluelamar.Connection#setSvcName(java.lang.String)
	 */
	@Override
	public void setSvcName(String svcName) {
		
		this.svcName = svcName;
	}

	/* (non-Javadoc)
	 * @see org.bluelamar.Connection#getSvcName()
	 */
	@Override
	public String getSvcName() {
		// TODO Auto-generated method stub
		return svcName;
	}

	/* (non-Javadoc)
	 * @see org.bluelamar.Connection#post(java.lang.String, java.lang.Object)
	 */
	@Override
	public int post(String path, Object obj) throws ConnException {
		
		WebTarget target = baseTarget.path(path);
        Invocation.Builder invocationBuilder = target.request("application/json");
        /* if (credsHeader != null) {
            invocationBuilder = credsHeader.startsWith("Cookie.") ? invocationBuilder.cookie(credsHeader.substring(7),
                credsToken) : invocationBuilder.header(credsHeader, credsToken);
        }
         */
        Response response = invocationBuilder.post(javax.ws.rs.client.Entity.entity(obj, "application/json"));
        int code = response.getStatus();
        switch (code) {
        case 200:
        	return code;
        	//Map<String, String> entity = response.readEntity(new GenericType<Map<String, String>>() {});
        	//return response.readEntity(obj.getClass());
        	//return response.readEntity(Class.forName(obj.getClass().getName()));
        default:
        	String msg = "Error code: " + code;
        	Object entObj = response.getEntity();
        	String extra = entObj == null ? "" : entObj.toString();
        	msg += " : " + extra;
            throw new ConnException(code, msg);
        }
	}

	/* (non-Javadoc)
	 * @see org.bluelamar.Connection#put(java.lang.String, java.lang.Object)
	 */
	@Override
	public int put(String path, Object obj) throws ConnException {
		WebTarget target = baseTarget.path(path);
        Invocation.Builder invocationBuilder = target.request("application/json");
        /* if (credsHeader != null) {
            invocationBuilder = credsHeader.startsWith("Cookie.") ? invocationBuilder.cookie(credsHeader.substring(7),
                credsToken) : invocationBuilder.header(credsHeader, credsToken);
        }
         */
        Response response = invocationBuilder.put(javax.ws.rs.client.Entity.entity(obj, "application/json"));
        int code = response.getStatus();
        switch (code) {
        case 200:
        	return code;
        default:
        	String msg = "Error code: " + code;
        	Object entObj = response.getEntity();
        	String extra = entObj == null ? "" : entObj.toString();
        	msg += " : " + extra;
            throw new ConnException(code, msg);
        }
		
	}

	/* (non-Javadoc)
	 * @see org.bluelamar.Connection#get(java.lang.String, java.util.Map)
	 */
	@Override
	public <T> T get(Class<T> retType, String path, Map<String, String> args) throws ConnException {
		WebTarget target = baseTarget.path(path);
        Invocation.Builder invocationBuilder = target.request("application/json");
        /* if (credsHeader != null) {
            invocationBuilder = credsHeader.startsWith("Cookie.") ? invocationBuilder.cookie(credsHeader.substring(7),
                credsToken) : invocationBuilder.header(credsHeader, credsToken);
        }
        if (auditRef != null) {
            invocationBuilder = invocationBuilder.header("Y-Audit-Ref", auditRef);
        } */
        Response response = invocationBuilder.get();
        int code = response.getStatus();
        switch (code) {
        case 200:
        	
        	//Map<String, String> entity = response.readEntity(new GenericType<Map<String, String>>() {});
        	return response.readEntity(retType);
        	//return response.readEntity(Class.forName(obj.getClass().getName()));
        default:
        	String msg = "Error code: " + code;
        	Object entObj = response.getEntity();
        	String extra = entObj == null ? "" : entObj.toString();
        	msg += " : " + extra;
            throw new ConnException(code, msg);
        }
	}
	
	/* (non-Javadoc)
	 * @see org.bluelamar.Connection#get(java.lang.String, java.util.Map)
	 */
	@Override
	public Map<String, String> get(String path, Map<String, String> args) throws ConnException {
		WebTarget target = baseTarget.path(path);
        Invocation.Builder invocationBuilder = target.request("application/json");
        /* if (credsHeader != null) {
            invocationBuilder = credsHeader.startsWith("Cookie.") ? invocationBuilder.cookie(credsHeader.substring(7),
                credsToken) : invocationBuilder.header(credsHeader, credsToken);
        }
         */
        Response response = invocationBuilder.get();
        int code = response.getStatus();
        switch (code) {
        case 200:
        	
        	Map<String, String> entity = response.readEntity(new GenericType<Map<String, String>>() {});
        	return entity;
        	//return response.readEntity(Class.forName(obj.getClass().getName()));
        default:
        	String msg = "Error code: " + code;
        	Object entObj = response.getEntity();
        	String extra = entObj == null ? "" : entObj.toString();
        	msg += " : " + extra;
            throw new ConnException(code, msg);
        }
	}

	/* (non-Javadoc)
	 * @see org.bluelamar.Connection#clone()
	 */
	@Override
	public Connection clone() {
		
		return new RestConnection(svcName, baseTarget);
	}

}
