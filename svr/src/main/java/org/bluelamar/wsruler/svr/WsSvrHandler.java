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
	
    public void putLink(String comp, String id, WsLink link) throws ConnException;

    public List<WsLink> getChildren(String comp, String id) throws ConnException;
}
