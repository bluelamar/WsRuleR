package org.bluelamar.wsruler.svr;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.bluelamar.wsruler.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/v1")
public class WsSvrImpl implements WsSvrHandler {

	private static final Logger LOG = LoggerFactory.getLogger(WsSvrImpl.class);
	
	static final String ID_FACTORY_PROP = "wsruler.idfactoryclass";
	static final String DEF_ID_FACTORY = "org.bluelamar.wsruler.ShortIdFactory";
	static final String CONN_POOL_PROP = "wsruler.connpoolfactoryclass";
	static final String DEF_CONN_POOL_CLASS = "org.bluelamar.wsruler.QueueConnPool";
	static final String DB_CONN_CLONER_PROP = "wsruler.dbconnclonerclass";
	static final String DEF_DB_CONN_CLONER_CLASS = "org.bluelamar.wsruler.RestConnection";
	static final String DB_CONN_LOGIN_PROP = "wsruler.dbconnloginfactory";
	static final String DEF_DB_CONN_LOGIN_CLASS = "org.bluelamar.wsruler.CdbConnCredFactory";
	static final String OGRP_CONN_CLONER_PROP = "wsruler.ogrp.connclonerclass";
	static final String DEF_OGRP_CONN_CLONER_CLASS = "org.bluelamar.wsruler.RestConnection";
	static final String OGRP_CONN_LOGIN_PROP = "wsruler.ogrp.connloginfactory";
	static final String DEF_OGRP_CONN_LOGIN_CLASS = "org.bluelamar.wsruler.CdbConnCredFactory";
	static final String DB_URL_PROP = "wsruler.db.url";
	static final String DEF_DB_URL = "http://localhost:5984/";
	
	// names used for the remote services wsruler uses
	//
	static final String DB_SVC_NAME = "db";
	static final String OGRP_SVC_NAME = "ogrp";
	
	// these are the names of the db's in the remote DB svc
	//
	static final String DBNAME_WS = "ws";
	static final String DBNAME_ENV = "envlnk";
	static final String DBNAME_REPO = "repolnk";
	static final String DBNAME_DB = "dblnk";
	
	// fields returned by db in the response
	//
	static final String RESULT_FIELD_OK = "ok";
	
	final IdFactory idFactory; // used to create unique ID's for documents
	final ConnPool connPool; // used to get connections to remote svc's

	public WsSvrImpl() {
		
		// load instance of IdFactory
		Object obj = loadClass(ID_FACTORY_PROP, DEF_ID_FACTORY);
		idFactory = (IdFactory)obj;
		
		// load connection pool: instance of ConnPool
		obj = loadClass(CONN_POOL_PROP, DEF_CONN_POOL_CLASS);
		connPool = (ConnPool)obj;
		
		// load these per service class impls
		//
		// load Connection clone: instance of Connection
		obj = loadClass(DB_CONN_CLONER_PROP, DEF_DB_CONN_CLONER_CLASS);
		Object login = loadClass(DB_CONN_LOGIN_PROP, DEF_DB_CONN_LOGIN_CLASS);
		
		Connection conn = (Connection)obj;
		conn.setSvcName(DB_SVC_NAME);
		String url = System.getProperty(DB_URL_PROP, DEF_DB_URL);
		conn.setUrl(url);
		connPool.setConnectionCloner(conn, (ConnLoginFactory)login);
		
		if (initDBs() == false) {
			LOG.error("Severe: Init of DB failure: shutting down");
			throw new java.util.ServiceConfigurationError("Severe: Init of DB failure: shutting down");
		}
	}
	
	Object loadClass(String propName, String defaultClass) {
        
        String className = System.getProperty(propName, defaultClass);
        Object obj;
        try {
            obj = Class.forName(className).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Invalid class specified=").append(className).
            	append(" : for property=").append(propName).
            	append(" : error=").append(e.getMessage());
        	LOG.error(sb.toString());
            throw new java.util.MissingResourceException(sb.toString(), className, propName);
        }
        
        return obj;
	}
	
	boolean initDBs() {
		
		// build db's if dont exist
		Connection conn;
		try {
			conn = connPool.getConnection(DB_SVC_NAME);
		} catch (ConnException ex) {
			LOG.error("init DB failure: cannot make connection: " + ex);
			return false;
		}
		
		return (initDB(DBNAME_WS, conn) &&
				initDB(DBNAME_ENV, conn) &&
				initDB(DBNAME_REPO, conn) &&
				initDB(DBNAME_DB, conn));
	}
	
	boolean initDB(String dbName, Connection conn) {
		
		try {
			int ret = conn.put(dbName, "", null);
			if (ret != 200 && ret != 201) {
				LOG.error("init DB failure: cannot init DB=" + dbName + ": error-code=" + ret);
				return false;
			}
		} catch (ConnException ex) {
			if (ex.getErrorCode() == 412) {
				LOG.info("DB exists: " + dbName);
			} else {
				LOG.error("init DB failure: cannot init DB=" + dbName + ": " + ex);
				return false;
			}
		}
		return true;
	}
/* FIX
	@Override
	public WsLink getDbLink(String id) {
		
		LOG.debug("FIX getDbLink: id=" + id);
		Connection conn = getConnection(DB_SVC_NAME);
		WsLink link = null;
		try {
			link = getLink(conn, DBNAME_DB, id);
		} finally {
			connPool.returnConnection(conn);
		}
		return link;
	}

	@Override
	public WsLink postDbLink(WsLink link) {
		
		LOG.debug("FIX postDbLink: link=" + link);
		Connection conn = getConnection(DB_SVC_NAME);
		try {
			link = postLink(conn, DBNAME_DB, link);
		} finally {
			connPool.returnConnection(conn);
		}
		return link;
    } 
	
	@Override
	public void putDbLink(String id, WsLink link) {
		
		LOG.debug("FIX putDbLink: link=" + link + " with id=" + id);
		Connection conn = getConnection(DB_SVC_NAME);
		try {
			putLink(conn, DBNAME_DB, id, link);
		} finally {
			connPool.returnConnection(conn);
		}
	}

    @Override
    public void deleteDbLink(String id) {
    	
    	LOG.debug("FIX deleteDbLink: id=" + id);
		Connection conn = getConnection(DB_SVC_NAME);
		try {
			deleteLink(conn, DBNAME_DB, id);
		} finally {
			connPool.returnConnection(conn);
		}
    } */
    /* FIX
    @Override
    public WsLink getEnvLink(String id) {
    	
    	LOG.debug("FIX getEnvLink: id=" + id);
    	Connection conn = getConnection(DB_SVC_NAME);
    	WsLink link = getLink(conn, DBNAME_ENV, id);
    	return link;
    }
    */
	@Override
	public List<WsLink> getChildren(String comp, String id) throws ConnException {
		
		LOG.debug("getChildren: component=" + comp + " id=" + id);
		String dbname = DBNAME_ENV;
		if (comp.equals("ws")) {
			dbname = DBNAME_ENV; // FIX @todo handle ws
			// get all the env's - then search for db for each env
			// dbname = DBNAME_REPO;
		} else if (comp.equals("env")) {
			dbname = DBNAME_DB;
		} else if (comp.equals("db") || comp.equals("repo")) {
			return new ArrayList<WsLink>();
		}

		Connection conn = getConnection(DB_SVC_NAME);
		try {
			return getChildren(conn, dbname, id); // find DB's whose parent==id
		} finally {
			connPool.returnConnection(conn);
		}
	}
	/* FIX
	@Override
	public WsLink postEnvLink(WsLink link) {
		
		LOG.debug("FIX postEnvLink: link=" + link);
		Connection conn = getConnection(DB_SVC_NAME);
		link = postLink(conn, DBNAME_ENV, link);
		return link;
	} */
	@Override
	public WsLink postLink(String comp, WsLink link) throws ConnException {
		
		String dbname = DBNAME_ENV;
		if (comp.equals("db")) {
			dbname = DBNAME_DB;
		}
		Connection conn = getConnection(DB_SVC_NAME);
		try {
			link = postLink(conn, dbname, link);
			return link;
		} finally {
			connPool.returnConnection(conn);
		}
	}
	
	@Override
    public WsLink getLink(String comp, String id) throws ConnException {
    	
    	LOG.debug("getLink: id=" + id);
    	String dbname = DBNAME_ENV;
		if (comp.equals("db")) {
			dbname = DBNAME_DB;
		}
    	Connection conn = getConnection(DB_SVC_NAME);
    	try {
	    	WsLink link = getLink(conn, dbname, id);
	    	return link;
		} finally {
			connPool.returnConnection(conn);
		}
    }
	
	@Override
	public void putLink(String comp, String id, WsLink link) throws ConnException {
		
		LOG.debug("FIX putLink: comp=" + comp + " link=" + link + " with id=" + id);
		String dbname = DBNAME_ENV;
		if (comp.equals("db")) {
			dbname = DBNAME_DB;
		}
		Connection conn = getConnection(DB_SVC_NAME);
		try {
			putLink(conn, dbname, id, link);
		} finally {
			connPool.returnConnection(conn);
		}
	}

    @Override
    public void deleteLink(String comp, String id) throws ConnException {
    	
    	LOG.debug("deleteLink: comp=" + comp + " id=" + id);
    	String dbname = DBNAME_ENV;
		if (comp.equals("db")) {
			dbname = DBNAME_DB;
		}
		Connection conn = getConnection(DB_SVC_NAME);
		try {
			deleteLink(conn, dbname, id);
		} finally {
			connPool.returnConnection(conn);
		}
    }
	/* FIX
	@Override
	public void putEnvLink(String id, WsLink link) {
		
		LOG.debug("FIX putEnvLink: id=" + id);
		Connection conn = getConnection(DB_SVC_NAME);
		putLink(conn, DBNAME_ENV, id, link);
	}
	
	@Override
    public void deleteEnvLink(String id) {
		
		LOG.debug("FIX deleteEnvLink: id=" + id);
		Connection conn = getConnection(DB_SVC_NAME);
		deleteLink(conn, DBNAME_ENV, id);
	} */
	
	WsLink getLink(Connection conn, String dbName, String id) throws ConnException {
		
		Map<String,Object> entity = conn.get(dbName + "/" + id, null);
		WsLink link = new WsLink();
		link.setId(entity.get("_id").toString());
		link.setName(entity.get("name").toString());
		link.setParent(entity.get("parent").toString());
		return link;
	}
	
	WsLink postLink(Connection conn, String dbName, WsLink link) throws ConnException {
		
		// create the unique id for the new object
		String id = this.idFactory.makeId(link);
		LOG.debug("postLink: link=" + link + " with id=" + id);
		link.setId(id);
		// convert link to a map
		// add _id set to the new "id" value to the map - dont add "id" to the map
		Map<String,Object> entity = new HashMap<>();
		entity.put("_id", id);
		entity.put("name", link.getName());
		entity.put("parent", link.getParent());
		
		Object res = conn.post(dbName, entity, null);
		if (res != null && res instanceof Map) {
			// verify ok then return link
			Boolean ok = (Boolean)((Map<String,Object>)res).get(RESULT_FIELD_OK);
			if (ok != null && ok.booleanValue()) {
				return link;
			}
		}
		throw new ConnException(500, "post link failed");
    }
	
	void putLink(Connection conn, String dbName, String id, WsLink link) throws ConnException {
		
		Map<String, Object> oldRes = conn.get(dbName + "/" + id, null);
		if (oldRes == null) {
			throw new ConnException(404, "DB link doesnt exist");
		}
		// now merge the values from link into the old object
		oldRes.put("name", link.getName());
		oldRes.put("parent", link.getParent());
			
		int ret = conn.put(dbName + "/" + id, oldRes, null);
		if (ret != 200 && ret != 201) {
			throw new RuntimeException(new ConnException(ret, "put link failed"));
		}
	}

	void deleteLink(Connection conn, String dbName, String id) throws ConnException {
		
		int ret = conn.delete(dbName + "/" + id, null);
		if (ret != 200 && ret != 201) {
			throw new ConnException(ret, "delete link failed");
		}
    }
	
	List<WsLink> getChildren(Connection conn, String dbName, String id) throws ConnException {
			
		// get the db link objects whose "parent" = id
		List<Object> res = conn.searchEqual(dbName, "parent", id);
		List<WsLink> linkList = new ArrayList<>();
		if (res != null) {
			for (Object obj: res) {
				if (obj instanceof Map) {
					// create a WsLink from the map
					Map<String,Object> mapObj = (Map<String,Object>)obj;
					WsLink link = new WsLink();
					link.setId(mapObj.get("_id").toString());
					link.setName(mapObj.get("name").toString());
					link.setParent(mapObj.get("parent").toString());
					linkList.add(link);
				}
			}
		}
		return linkList;
	}

    Connection getConnection(String dbName) {
    	
    	Connection conn;
		try {
			conn = connPool.getConnection(dbName);
		} catch (ConnException ex) {
			LOG.error("getConnection: DB failure: " + dbName + " : cannot make connection: " + ex);
			throw new RuntimeException(ex);
		}
		return conn;
    }
}
