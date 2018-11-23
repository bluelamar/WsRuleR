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

public class WsSvrImpl implements WsSvrHandler {

	private static final Logger LOG = LoggerFactory.getLogger(WsSvrImpl.class);
	
	static final String ID_FACTORY_PROP = "wsruler.idfactoryclass";
	static final String DEF_ID_FACTORY = "org.bluelamar.wsruler.ShortIdFactory";
	static final String CONN_POOL_FACT_PROP = "wsruler.connpoolfactoryclass";
	static final String DEF_CONN_POOL_FACT_CLASS = "org.bluelamar.wsruler.QueueConnPoolFactory";
	static final String DB_CONN_CLONER_PROP = "wsruler.dbconnclonerclass";
	static final String DEF_DB_CONN_CLONER_CLASS = "org.bluelamar.wsruler.CdbRestConnection";
	static final String DB_CONN_LOGIN_PROP = "wsruler.dbconnloginfactory";
	static final String DEF_DB_CONN_LOGIN_CLASS = "org.bluelamar.wsruler.CdbConnCredFactory";
	static final String OGRP_CONN_CLONER_PROP = "wsruler.ogrp.connclonerclass";
	static final String DEF_OGRP_CONN_CLONER_CLASS = "org.bluelamar.wsruler.DsRestConnection";
	static final String OGRP_URL_PROP = "wsruler.ogrp.url";
	static final String DEF_OGRP_URL = "http://localhost:5984/";
	static final String OGRP_CONN_LOGIN_PROP = "wsruler.ogrp.connloginfactory";
	static final String DEF_OGRP_CONN_LOGIN_CLASS = "org.bluelamar.wsruler.CdbConnCredFactory";
	static final String DB_URL_PROP = "wsruler.db.url";
	static final String DEF_DB_URL = "http://localhost:5984/";
	
	// names used for the remote services wsruler uses
	//
	static final String DB_SVC_NAME = "db";
	static final String OGRP_SVC_NAME = "dirsvc";
	
	// these are the names of the db's in the remote DB svc
	//
	static final String DBNAME_WS = "ws";
	static final String DBNAME_ENV = "wsenv";
	static final String DBNAME_REPO = "wsrepo";
	static final String DBNAME_DB = "wsdb";
	
	// fields returned by db in the response
	//
	static final String RESULT_FIELD_OK = "ok";
	static final String RESULT_FIELD_ROWS = "rows";
	
	// Should use unlikely string as separator between urls.
	// ';' is reserved char so could actually be used!
	// Need better string but good enough for the demo.
	static final String URL_SPLIT_STR = ";";

	final IdFactory idFactory; // used to create unique ID's for documents
	final ConnPool connPool; // used to get connections to remote svc's

	public WsSvrImpl() {
		
		// load instance of IdFactory
		Object obj = loadClass(ID_FACTORY_PROP, DEF_ID_FACTORY);
		idFactory = (IdFactory)obj;
		
		// load connection pool: instance of ConnPool
		obj = loadClass(CONN_POOL_FACT_PROP, DEF_CONN_POOL_FACT_CLASS);
		connPool = ((ConnPoolFactory)obj).makeConnPool();
		
		// load the per service class impls
		//
		
		// load DS Owners Group service
		//
		obj = loadClass(OGRP_CONN_CLONER_PROP, DEF_OGRP_CONN_CLONER_CLASS);
		Object login = loadClass(OGRP_CONN_LOGIN_PROP, DEF_OGRP_CONN_LOGIN_CLASS);
		String dsUrl = System.getProperty(OGRP_URL_PROP, DEF_OGRP_URL);
		RestConnection conn = (RestConnection)obj;
		conn.setSvcName(OGRP_SVC_NAME);
		
		String[] urlSplits = dsUrl.split(URL_SPLIT_STR);
		for (String url: urlSplits) {
			LOG.debug("Server init: add DS service for url=" + url);
			conn = conn.clone();
			conn.setUrl(dsUrl);
			connPool.setConnectionCloner(conn, (ConnLoginFactory)login);
		}
		
		// load DB Connection clone: instance of Connection
		//
		obj = loadClass(DB_CONN_CLONER_PROP, DEF_DB_CONN_CLONER_CLASS);
		login = loadClass(DB_CONN_LOGIN_PROP, DEF_DB_CONN_LOGIN_CLASS);
		
		conn = (RestConnection)obj;
		conn.setSvcName(DB_SVC_NAME);
		
		String urlVals = System.getProperty(DB_URL_PROP, DEF_DB_URL);
		// if there is more than 1 url, create a clone for each one to add to
		// the pool. Could be primary and backup connections.
		urlSplits = urlVals.split(";");
		for (String url: urlSplits) {
			LOG.debug("Server init: add db service for url=" + url);
			conn = conn.clone();
			conn.setUrl(url);
			connPool.setConnectionCloner(conn, (ConnLoginFactory)login);
		}
		
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
	
	/**
	 * This is actually a convenience implementation for the demo.
	 * It will actually create the db's. In production this would not be done
	 * here as a db should already be created for use by the service.
	 * @return true upon success
	 */
	boolean initDBs() {
		
		// build db's if dont exist
		RestConnection conn;
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
	
	boolean initDB(String dbName, RestConnection conn) {
		
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

	@Override
	public List<Object> getChildren(String comp, String id) throws ConnException {
		
		LOG.debug("getChildren: component=" + comp + " id=" + id);
		RestConnection conn = null;
		String dbname = DBNAME_ENV;
		if (comp.equals("ws")) {
			try {
				conn = getConnection(DB_SVC_NAME);
				List<Object> repos = null;
				try {
					repos = getEntities(conn, DBNAME_REPO, "parent", id);
				} catch (ConnException ex) {
					if (ex.getErrorCode() != 404) {
						throw ex;
					}
				}
			
				List<Object> eres = null;
				try {
					eres = getEntities(conn, DBNAME_ENV, "parent", id);
				} catch (ConnException ex) {
					if (ex.getErrorCode() != 404) {
						throw ex;
					}
				}
				
				List<Object> dbres = new ArrayList<>();
				if (eres != null) {
					// for each env - get its id so we can find its children
					for (Object eobj: eres) {
						Object eid = ((Map<String,Object>)eobj).get("data_link");
						try {
							List<Object> dres = getEntities(conn, DBNAME_DB, "parent", eid.toString());
							if (dres != null) {
								dbres.addAll(dres);
							}
						} catch (ConnException ex) {
							if (ex.getErrorCode() != 404) {
								throw ex;
							}
						}
					}
				}

				List<Object> res = new ArrayList<>();
				if (repos != null) {
					for (Object obj: repos) {
						Map<String, Object> mobj = (Map<String,Object>)obj;
						mobj.put("type", "repo");
					}
					res.addAll(repos);
				}
				if (eres != null) {
					for (Object obj: eres) {
						Map<String, Object> mobj = (Map<String,Object>)obj;
						mobj.put("type", "env");
					}
					res.addAll(eres);
				}
				if (dbres != null) {
					for (Object obj: dbres) {
						Map<String, Object> mobj = (Map<String,Object>)obj;
						mobj.put("type", "db");
					}
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
			List<Object> dbres = getEntities(conn, dbname, "parent", id); // find DB's whose parent==id
			if (dbres != null) {
				for (Object obj: dbres) {
					Map<String, Object> mobj = (Map<String,Object>)obj;
					mobj.put("type", "db");
				}
				return dbres;
			}
			return new ArrayList<Object>();
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

		RestConnection conn = getConnection(DB_SVC_NAME);
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
		RestConnection conn = getConnection(DB_SVC_NAME);
		try {
			putEntity(conn, dbname, id, entity);
		} finally {
			connPool.returnConnection(conn);
		}
	}

    @Override
    public void deleteEntity(String comp, String id) throws ConnException {
	
    	LOG.debug("deleteEntity: comp=" + comp + " id=" + id);
		String dbname = DBNAME_ENV;
		if (comp.equals("db")) {
			dbname = DBNAME_DB;
		} else if (comp.equals("repo")) {
			dbname = DBNAME_REPO;
		} else if (comp.equals("ws")) {
			dbname = DBNAME_WS;
		}
		RestConnection conn = getConnection(DB_SVC_NAME);
		try {
			deleteEntity(conn, dbname, id);
		} finally {
			connPool.returnConnection(conn);
		}
    }
    
	@Override
    public Map<String,Object> getEntity(String comp, String id) throws ConnException {
    	
    	LOG.debug("getEntity: component=" + comp + " id=" + id);
    	
    	RestConnection conn = null;
		String dbname = DBNAME_ENV;
		if (comp.equals("db")) {
			dbname = DBNAME_DB;
		} else if (comp.equals("repo")) {
			dbname = DBNAME_REPO;
		} else if (comp.equals("ws")) {
			dbname = DBNAME_WS;
		} else if (comp.startsWith(OGRP_SVC_NAME)) {
			dbname = OGRP_SVC_NAME;
			conn = getConnection(OGRP_SVC_NAME);
			LOG.debug("getEntity: calling Dir Svc for id=" + id);
		} 
		
		if (conn == null) {
			conn = getConnection(DB_SVC_NAME);
		}
    	try {
    		Map<String,Object> res = conn.get(dbname + "/" + id, null);
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
	
	@Override
	public Map<String,Object> postEntity(String comp, Map<String,Object> entity) throws ConnException {
		
		String dbname = DBNAME_ENV;
		if (comp.equals("db")) {
			dbname = DBNAME_DB;
		} else if (comp.equals("repo")) {
			dbname = DBNAME_REPO;
		} else if (comp.equals("ws")) {
			dbname = DBNAME_WS;
		}
		RestConnection conn = getConnection(DB_SVC_NAME);
		try {
			Map<String,Object> res = postEntity(conn, dbname, entity);
			String id = res.remove("_id").toString();
			res.put("id", id);
			return res;
		} finally {
			connPool.returnConnection(conn);
		}
	}
	Map<String,Object> postEntity(RestConnection conn, String dbName, Map<String,Object> entity) throws ConnException {
		
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

	void putEntity(RestConnection conn, String dbName, String id, Map<String, Object> entity) throws ConnException {
		
		Map<String, Object> oldRes = conn.get(dbName + "/" + id, null);
		if (oldRes == null) {
			throw new ConnException(404, "DB entity doesnt exist");
		}
		// now merge the values from entity into the old object
		for (String key: entity.keySet()) {
			oldRes.put(key, entity.get(key));
		}
			
		int ret = conn.put(dbName + "/" + id, oldRes, null);
		if (ret != 200 && ret != 201) {
			throw new RuntimeException(new ConnException(ret, "put link failed"));
		}
	}

	void deleteEntity(RestConnection conn, String dbName, String id) throws ConnException {
			
		int ret = conn.delete(dbName + "/" + id, null);
		if (ret != 200 && ret != 201 && ret != 404) {
			throw new ConnException(ret, "delete link failed");
		}
    }
	
	/**
	 * Search for entities where field equals the value of id.
	 * If no field or value search criteria, then return all docs from the
	 * specified db.
	 * @param conn
	 * @param dbName is name of db to search against
	 * @param field in the JSON doc to compare value for
	 * @param val is the value to match
	 * @return matched entities
	 * @throws ConnException
	 */
	List<Object> getEntities(RestConnection conn, String dbName, String field, String val) throws ConnException {
		
		if (field == null || val == null) {
			Map<String,Object> res = conn.get(dbName + "/_all_docs", null);
			if (res == null || res.get(RESULT_FIELD_ROWS) == null) {
				return new ArrayList<Object>();
			}
			return (List<Object>)res.get(RESULT_FIELD_ROWS);
		}
		// get the db entity objects whose field-name == id
		return conn.searchEqual(dbName, field, val);
	}
	
	List<Object> getChildren(RestConnection conn, String dbName, String id) throws ConnException {
		
		// get the db link objects whose "parent" = id
		List<Object> res = conn.searchEqual(dbName, "parent", id);
		return res;
	}

    RestConnection getConnection(String dbName) {
    	
    	RestConnection conn;
		try {
			conn = connPool.getConnection(dbName);
		} catch (ConnException ex) {
			LOG.error("getConnection: DB failure: " + dbName + " : cannot make connection: " + ex);
			throw new RuntimeException(ex);
		}
		return conn;
    }
}
