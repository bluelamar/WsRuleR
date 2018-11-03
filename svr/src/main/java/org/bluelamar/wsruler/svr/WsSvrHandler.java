package org.bluelamar.wsruler.svr;

import java.util.List;
import java.util.Map;

import org.bluelamar.wsruler.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface WsSvrHandler {
	
	Map<String,Object> postEntity(String comp, Map<String,Object> entity) throws ConnException;
	Map<String,Object> getEntity(String comp, String id) throws ConnException;
	void deleteEntity(String comp, String id) throws ConnException;
	void putEntity(String comp, String id, Map<String,Object> entity) throws ConnException;
	List<Object> getChildren(String comp, String id) throws ConnException;
	List<Object> getEntities(String comp, String field, String id) throws ConnException;
	
}
