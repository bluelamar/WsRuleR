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
	static final String CONN_POOL_FACT_PROP = "wsruler.connpoolfactoryclass";
	static final String DEF_CONN_POOL_FACT_CLASS = "org.bluelamar.wsruler.QueueConnPoolFactory";
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
	static final String DBNAME_ENV = "wsenv";
	static final String DBNAME_REPO = "wsrepo";
	static final String DBNAME_DB = "wsdb";
	
	// fields returned by db in the response
	//
	static final String RESULT_FIELD_OK = "ok";
	
	// entity fields
	//
	static final String ENT_FIELD_ID = "id";
	static final String ENT_FIELD_NAME = "name";
	static final String ENT_FIELD_PAR = "parent";
	static final String ENT_FIELD_DLINK = "data_link";
	static final String ENT_FIELD_TYPE = "type";
	static final String ENT_FIELD_GRPS = "groups";
	static final String ENT_FIELD_OWNERS = "owners";
	static final String ENT_FIELD_EMAIL = "email_address";
	

	final IdFactory idFactory; // used to create unique ID's for documents
	final ConnPool connPool; // used to get connections to remote svc's

	public WsSvrImpl() {
		
		// load instance of IdFactory
		Object obj = loadClass(ID_FACTORY_PROP, DEF_ID_FACTORY);
		idFactory = (IdFactory)obj;
		
		// load connection pool: instance of ConnPool
		obj = loadClass(CONN_POOL_FACT_PROP, DEF_CONN_POOL_FACT_CLASS);
		connPool = ((ConnPoolFactory)obj).makeConnPool();
		
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
	public List<Object> getChildren(String comp, String id) throws ConnException {
		
		LOG.debug("getChildren: component=" + comp + " id=" + id);
		Connection conn = null;
		String dbname = DBNAME_ENV;
		if (comp.equals("ws")) {
			conn = getConnection(DB_SVC_NAME);
			try {
				List<Object> eres = getEntities(conn, dbname, "parent", id);
				List<Object> dbres = getChildren("env", id);
				List<Object> res = new ArrayList<>();
				if (eres != null) {
					res.addAll(eres);
				}
				if (dbres != null) {
					res.addAll(dbres);
				}
				return res;
			} finally {
				connPool.returnConnection(conn);
			}
		} else if (comp.equals("env")) {
			dbname = DBNAME_DB;
		} else if (comp.equals("db") || comp.equals("repo")) {
			return new ArrayList<Object>();
		}

		conn = getConnection(DB_SVC_NAME);
		try {
			return getEntities(conn, dbname, "parent", id); // find DB's whose parent==id
		} finally {
			connPool.returnConnection(conn);
		}
	}
	@Override
	public List<Object> getEntities(String comp, String field, String id) throws ConnException {
		
		LOG.debug("getEntities: component=" + comp + " id=" + id);
		String dbname = DBNAME_ENV;
		if (comp.equals("ws")) {
			dbname = DBNAME_WS;
		} else if (comp.equals("env")) {
			dbname = DBNAME_ENV;
		} else if (comp.equals("repo")) {
			dbname = DBNAME_REPO;
		} else if (comp.equals("db")) {
			dbname = DBNAME_DB;
		} else {
			throw new ConnException(406, "unsupported db name");
		}

		Connection conn = getConnection(DB_SVC_NAME);
		try {
			return getEntities(conn, dbname, field, id); // find DB's whose field==id
		} finally {
			connPool.returnConnection(conn);
		}
	}

	@Override
	public void putEntity(String comp, String id, Map<String,Object> entity) throws ConnException {
		
		LOG.debug("putEntity: comp=" + comp + " entity=" + entity + " with id=" + id);
		String dbname = DBNAME_ENV;
		if (comp.equals("db")) {
			dbname = DBNAME_DB;
		} else if (comp.equals("ws")) {
			dbname = DBNAME_WS;
		} else if (comp.equals("repo")) {
			dbname = DBNAME_REPO;
		}
		Connection conn = getConnection(DB_SVC_NAME);
		try {
			putEntity(conn, dbname, id, entity);
		} finally {
			connPool.returnConnection(conn);
		}
	}

    @Override
    public void deleteEntity(String comp, String id) throws ConnException {
    // FIX public void deleteLink(String comp, String id) throws ConnException {
    	
    	LOG.debug("deleteLink: comp=" + comp + " id=" + id);
    	String dbname = DBNAME_ENV;
		if (comp.equals("db")) {
			dbname = DBNAME_DB;
		}
		Connection conn = getConnection(DB_SVC_NAME);
		try {
			deleteEntity(conn, dbname, id);
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
		link.setData_link(entity.get("data_link").toString());
		link.setParent(entity.get("parent").toString());
		return link;
	}
	
	@Override
	public Map<String,Object> postEntity(String comp, Map<String,Object> entity) throws ConnException {
		
		String dbname = DBNAME_ENV;
		if (comp.equals("db")) {
			dbname = DBNAME_DB;
		}
		Connection conn = getConnection(DB_SVC_NAME);
		try {
			Map<String,Object> res = postEntity(conn, dbname, entity);
			String id = res.remove("_id").toString();
			res.put("id", id);
			return res;
		} finally {
			connPool.returnConnection(conn);
		}
	}
	Map<String,Object> postEntity(Connection conn, String dbName, Map<String,Object> entity) throws ConnException {
		
		// create the unique id for the new object
		String id = this.idFactory.makeId(entity);
		LOG.debug("postEntity: unique id=" + id);
		// convert link to a map
		// add _id set to the new "id" value to the map - dont add "id" to the map
		entity.put("_id", id);
				
		Map<String,Object> res = conn.post(dbName, entity, null);
		if (res != null) {
			// verify ok then return link
			Boolean ok = (Boolean)((Map<String,Object>)res).get(RESULT_FIELD_OK);
			if (ok != null && ok.booleanValue()) {
				return entity;
			}
		}
		throw new ConnException(500, "post entity failed");
	}
	
	@Override
    public Map<String,Object> getEntity(String comp, String id) throws ConnException {
    	
    	LOG.debug("getEntity: id=" + id);
    	String dbname = DBNAME_ENV;
		if (comp.equals("db")) {
			dbname = DBNAME_DB;
		}
    	Connection conn = getConnection(DB_SVC_NAME);
    	try {
    		Map<String,Object> res = conn.get(dbname + "/" + id, null); // getEntity(conn, dbname, id);
	    	if (res == null) {
	    		throw new ConnException(404, "get entity failed");
	    	}
    		id = res.remove("_id").toString();
			res.put("id", id);
			return res;
		} finally {
			connPool.returnConnection(conn);
		}
    }
	
	void putLink(Connection conn, String dbName, String id, WsLink link) throws ConnException {
		
		Map<String, Object> oldRes = conn.get(dbName + "/" + id, null);
		if (oldRes == null) {
			throw new ConnException(404, "DB link doesnt exist");
		}
		// now merge the values from link into the old object
		oldRes.put("data_link", link.getData_link());
		String parent = link.getParent();
		if (parent != null) {
			oldRes.put("parent", parent);
		}
			
		int ret = conn.put(dbName + "/" + id, oldRes, null);
		if (ret != 200 && ret != 201) {
			throw new RuntimeException(new ConnException(ret, "put link failed"));
		}
	}

	void putEntity(Connection conn, String dbName, String id, Map<String, Object> entity) throws ConnException {
		
		Map<String, Object> oldRes = conn.get(dbName + "/" + id, null);
		if (oldRes == null) {
			throw new ConnException(404, "DB entity doesnt exist");
		}
		// now merge the values from entity into the old object
		for (String key: entity.keySet()) {
			oldRes.put(key, oldRes.get(key));
		}
			
		int ret = conn.put(dbName + "/" + id, oldRes, null);
		if (ret != 200 && ret != 201) {
			throw new RuntimeException(new ConnException(ret, "put link failed"));
		}
	}

	// FIX void deleteLink(Connection conn, String dbName, String id) throws ConnException {
	void deleteEntity(Connection conn, String dbName, String id) throws ConnException {
			
		int ret = conn.delete(dbName + "/" + id, null);
		if (ret != 200 && ret != 201 && ret != 404) {
			throw new ConnException(ret, "delete link failed");
		}
    }
	
	List<Object> getEntities(Connection conn, String dbName, String field, String id) throws ConnException {
		
		if (field == null || id == null) {
			Map<String,Object> res = conn.get(dbName + "/_all_docs", null);
			if (res == null || res.get("rows") == null) {
				return new ArrayList<Object>();
			}
			return (List<Object>)res.get("rows");
		}
		// get the db entity objects whose field-name == id
		List<Object> res = conn.searchEqual(dbName, field, id);
		return res;
	}
	List<Object> getChildren(Connection conn, String dbName, String id) throws ConnException {
		
		// get the db link objects whose "parent" = id
		List<Object> res = conn.searchEqual(dbName, "parent", id);
		if (res != null) LOG.debug("FIX IMP: getchildren got=" +res.size() );
		else LOG.debug("FIX IMPL: getchildren got NOTHING");
		return res;
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
