/**
 * 
 */
package org.bluelamar.wsruler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.ws.rs.client.*;
import javax.ws.rs.core.*;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * REST based service connection
 *
 */
public class RestConnection implements Connection {

	private String svcName;
	private Client client;
	private WebTarget baseTarget;
	private String url;
	private Map<String, String> cookieMap = new HashMap<>(); // set automatically from server responses
	
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
		
		Object login = creds.getAuthLogin();
		Object ret = post("_session", login, null);
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
		
		return svcName;
	}

	/* (non-Javadoc)
	 * @see org.bluelamar.Connection#post(java.lang.String, java.lang.Object)
	 */
	@Override
	public Object post(String path, Object obj, Map<String, List<String>> outHeaders) throws ConnException {
		
		WebTarget target = baseTarget.path(path);
        Invocation.Builder invocationBuilder = target.request("application/json");
        setCookies(invocationBuilder);
        Response response = invocationBuilder.post(javax.ws.rs.client.Entity.entity(obj, "application/json"));
        int code = response.getStatus();
        switch (code) {
        case 200:
	    case 201:
        	// FIX @todo get any response headers to add to outHeaders
        	// ex: Set-Cookie: AuthSession=d3NydWxlcjo1QkNFQjkyNTrEWInzBiC_9qSQx1rPl4Tu7LywLQ; Version=1; Path=/; HttpOnly
        	Map<String,NewCookie> cookies = response.getCookies();
        	for (String key: cookies.keySet()) {
        		System.err.println("RestConn:post: key=" + key + " cookie=" + cookies.get(key));
        		// split cookie by '=' to get cookie header and cookie value
        		NewCookie val = cookies.get(key);
        		if (val == null) {
        			continue;
        		}
        		String[] ck = val.toString().split("=");
        		if (ck.length < 2) {
        			continue;
        		}
        		System.err.println("RestConn:post: got ck-name=" + ck[0] + " ck-val=" + ck[1]); 
        		cookieMap.put(ck[0], ck[1]);
        		// invocationBuilder.cookie(credsHeader, credsToken)
        	}
        	
        	try {
	        	String ret = response.readEntity(String.class);
	        	ObjectMapper objectMapper = new ObjectMapper();
	        	Map<String,Object> entity = objectMapper.readValue(ret, HashMap.class);
	        	
	        	for (String key: entity.keySet()) {
	        		Object val = entity.get(key);
	        		System.err.println("RestConn:post:resp: key=" + key + " val-type=" + val.getClass().getName() + " obj=" + val);
	        	}
	        	
	        	Object val = entity.get("docs");
	        	return val;
        	} catch(Exception ex) {
        		System.out.println("FIX post got exc reading resp: " + ex);
        	}
        	// FIX return code;
        	//Map<String, String> entity = response.readEntity(new GenericType<Map<String, String>>() {});
        	//return response.readEntity(obj.getClass());
        	//return response.readEntity(Class.forName(obj.getClass().getName()))
        	
        default:
        	String msg = "Error code: " + code;
        	String extra = response.readEntity(String.class);
        	msg += " : " + extra;
            throw new ConnException(code, msg);
        }
	}

	/* (non-Javadoc)
	 * @see org.bluelamar.Connection#put(java.lang.String, java.lang.Object)
	 */
	@Override
	public int put(String path, Object obj, Map<String, String> args) throws ConnException {
		WebTarget target = baseTarget.path(path);
		target = setQueryParams(target, args);
		
        Invocation.Builder invocationBuilder = target.request("application/json");
        setCookies(invocationBuilder);
        Response response = null;
        if (obj == null) {
        	response = invocationBuilder.put(null);
        } else {
        	response = invocationBuilder.put(javax.ws.rs.client.Entity.entity(obj, "application/json"));
        }
        int code = response.getStatus();
        switch (code) {
        case 200:
        case 201:
        	return code;
        default:
        	String msg = "Error code: " + code;
        	// entObj could be a org.glassfish.jersey.client.internal.HttpUrlConnector
        	// @todo if it is what should we do with it?
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
		target = setQueryParams(target, args);
		
        Invocation.Builder invocationBuilder = target.request("application/json");
        setCookies(invocationBuilder);

        Response response = invocationBuilder.get();
        int code = response.getStatus();
        switch (code) {
        case 200:
        	
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
	public Map<String, Object> get(String path, Map<String, String> args) throws ConnException {
		WebTarget target = baseTarget.path(path);
		target = setQueryParams(target, args);
		
        Invocation.Builder invocationBuilder = target.request("application/json");
        setCookies(invocationBuilder);
        
        Response response = invocationBuilder.get();
        int code = response.getStatus();
        switch (code) {
        case 200:
        	try {
	        	String ret = response.readEntity(String.class);
	        	ObjectMapper objectMapper = new ObjectMapper();
	        	Map<String,Object> entity = objectMapper.readValue(ret, HashMap.class);
	        	return entity;
        	}
        	catch (JsonParseException e) {
        		e.printStackTrace(); // FIX @todo throw ConnException
        	}
            catch (JsonMappingException e) {
            	e.printStackTrace();  // FIX @todo throw ConnException
            }
            catch (IOException e) {
            	e.printStackTrace(); // FIX @todo throw ConnException
            }
            return null; // FIX should throw exc's above so shouldnt get here
        	
        	//return response.readEntity(Class.forName(obj.getClass().getName()));
        default:
        	String msg = "Error code: " + code;
        	String extra = response.readEntity(String.class);
        	msg += " : " + extra;
            throw new ConnException(code, msg);
        }
	}
	
	public int delete(String path, Map<String, String> args) throws ConnException {
		
		WebTarget target = baseTarget.path(path);
		target = setQueryParams(target, args);
        
		Invocation.Builder invocationBuilder = target.request("application/json");
        setCookies(invocationBuilder);
        
        Response response = invocationBuilder.delete();
        int code = response.getStatus();
        switch (code) {
        case 200:
        	return code;
        default:
        	String msg = "Error code: " + code;
        	String extra = response.readEntity(String.class);
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
		
		for (String key: cookieMap.keySet()) {
			invocationBuilder.cookie(key, cookieMap.get(key));
		}
	}

	WebTarget setQueryParams(WebTarget target, Map<String, String> args) {
		
		if (args == null) {
			return target;
		}
		for (String key: args.keySet()) {
			target = target.queryParam(key, args.get(key));
		}
		return target;
	}

}
