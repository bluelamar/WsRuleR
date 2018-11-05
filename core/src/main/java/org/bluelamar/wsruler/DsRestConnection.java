/**
 * 
 */
package org.bluelamar.wsruler;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * Directory Service REST implementation for Group and Owners.
 *
 */
public class DsRestConnection extends RestConnection {

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
	
		Map<String, Object> dsgrp = super.get(path + "_groups", args);
		// get the children of this group
		Object id = dsgrp.get("_id");
		List<Object> ownerLinks = searchEqual(path + "_links", "parent", id.toString());
		// get owner object for each link
		List<Object> ownerObjs = new ArrayList<>();
		for (Object lobj: ownerLinks) {
			Map<String, Object> link = (Map<String, Object>)lobj;
			Object dlink = link.get("data_link");
			Object odata = super.get(path + "_owners/" + dlink.toString(), args);
			if (odata != null) {
				ownerObjs.add(odata);
			}
		}
		dsgrp.put("owners", ownerObjs);
		return dsgrp;
	}
}
