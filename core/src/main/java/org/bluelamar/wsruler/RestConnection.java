/**
 * 
 */
package org.bluelamar.wsruler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.*;
import javax.ws.rs.core.*;

/**
 * REST based service connection
 *
 */
public class RestConnection implements Connection {

	private String svcName;
	private Client client;
	private WebTarget baseTarget;
	private String url;
	private Map<String, NewCookie> cookies; // set automatically from server response
	
	public static class Login {
		public String name;
		public String password;
	}
	
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
	public RestConnection(String svcName, String url) {
		this.svcName = svcName;
		setUrl(url);
	}
	
	/*
	 * Setup for comm with target service
	 * Ex url: https://server-yourcompany.com:4443/
	 * @param url the base url of the target service
	 */
	@Override
	public void setUrl(String url) {
		this.url = url;
		client = ClientBuilder.newClient();
        baseTarget = client.target(url);
	}
	
	/**
	 * Give access to the serverbase  url
	 * @return base url
	 */
	public String getUrl() {
		return url;
	}
	
	/*
	 * Perform initialization with the server for given creds.
	 * @param creds to get a session with the server
	 */
	public void doAuthInit(ConnCreds creds) throws ConnException {
		
		// @todo perform login
		//String cred = "{\"name\":\"wsruler\",\"password\",\"oneringtorule\"}";
		RestConnection.Login login = new RestConnection.Login();
		login.name = creds.getConnUser();
		login.password = creds.getConnSecret();
		int ret = post("_session", login, null);
		System.err.println("RR-conn:doauth ret=" + ret);
	}

	/* (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		client.close();
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
	public int post(String path, Object obj, Map<String, List<String>> outHeaders) throws ConnException {
		
		WebTarget target = baseTarget.path(path);
        Invocation.Builder invocationBuilder = target.request("application/json");
        setCookies(invocationBuilder);
        Response response = invocationBuilder.post(javax.ws.rs.client.Entity.entity(obj, "application/json"));
        int code = response.getStatus();
        switch (code) {
        case 200:
        	// FIX @todo get any response headers - look for Set-Cookie
        	// ex: Set-Cookie: AuthSession=d3NydWxlcjo1QkNFQjkyNTrEWInzBiC_9qSQx1rPl4Tu7LywLQ; Version=1; Path=/; HttpOnly
        	//  Map<String,NewCookie> getCookies() @todo just auto keep cookies
        	// response.getHeaders()
        	cookies = response.getCookies();
        	for (String key: cookies.keySet()) {
        		System.err.println("RestConn:post: cookie=" + cookies.get(key));
        	}
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
        setCookies(invocationBuilder);
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
        setCookies(invocationBuilder);
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
        setCookies(invocationBuilder);
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
		
		return new RestConnection(svcName, url);
	}
	
	void setCookies(Invocation.Builder invocationBuilder) {
		// FIX @todo set the cookies if any
		// invocationBuilder.cookie(credsHeader.substring(7), credsToken);
	}


}
