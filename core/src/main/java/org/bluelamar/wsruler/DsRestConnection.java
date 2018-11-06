/**
 * 
 */
package org.bluelamar.wsruler;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Directory Service REST implementation for Group and Owners.
 *
 */
public class DsRestConnection extends RestConnection {
	
	private static final Logger LOG = LoggerFactory.getLogger(DsRestConnection.class);

	// database names
	static final String DBNAME_GRPS = "dirsvc_groups"; // contains the owner-group objects
	static final String DBNAME_LINKS = "dirsvc_links"; // links for owner to group
	static final String DBNAME_OWNERS = "dirsvc_owners"; // owner objects
	
	/**
	 * 
	 */
	public DsRestConnection() {

	}

	/**
	 * @param url
	 */
	public DsRestConnection(String url) {
		super(url);

	}

	/**
	 * @param svcName
	 * @param url
	 */
	public DsRestConnection(String svcName, String url) {
		super(svcName, url);
	}

	@Override
	public Map<String, Object> get(String path, Map<String, String> args) throws ConnException {
	
		LOG.debug("DirSvc:get: path=" + path);
		// split path to get id
		String[] splits = path.split("/");
		
		if (splits.length < 2) {
			throw new ConnException(400, "missing group name");
		}
		String grpName = splits[1];
		Map<String, Object> dsgrp = super.get(DBNAME_GRPS + "/" + grpName, args);
		// get the children of this group
		Object id = dsgrp.get("_id");
		List<Object> ownerLinks = searchEqual(DBNAME_LINKS, "parent", id.toString());
		// get owner object for each link
		List<Object> ownerObjs = new ArrayList<>();
		for (Object lobj: ownerLinks) {
			Map<String, Object> link = (Map<String, Object>)lobj;
			Object dlink = link.get("data_link");
			Object odata = super.get(DBNAME_OWNERS + "/" + dlink.toString(), args);
			if (odata != null) {
				ownerObjs.add(odata);
			}
		}
		dsgrp.put("owners", ownerObjs);
		return dsgrp;
	}
	
	@Override
	public Connection clone() {
		
		return new DsRestConnection(svcName, url);
	}
}
