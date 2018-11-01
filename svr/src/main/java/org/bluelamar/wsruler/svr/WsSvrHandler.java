package org.bluelamar.wsruler.svr;

import java.util.List;

import org.bluelamar.wsruler.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface WsSvrHandler {
	
	// FIX public WsLink getEnvLink(String id);
	//public List<WsLink> getChildren(String comp, String id) throws ConnException;
	// FIX public WsLink postEnvLink(WsLink link);
	//public void putEnvLink(String id, WsLink link);
    //public void deleteEnvLink(String id);
    
    // FIX public WsLink getDbLink(String id);
	// FIX public WsLink postDbLink(WsLink link);
	//public void putDbLink(String id, WsLink link);
    //public void deleteDbLink(String id);
    
    public WsLink getLink(String comp, String id) throws ConnException;
    public WsLink postLink(String comp, WsLink link) throws ConnException;
    public void putLink(String comp, String id, WsLink link) throws ConnException;
    public void deleteLink(String comp, String id) throws ConnException;
    public List<WsLink> getChildren(String comp, String id) throws ConnException;
}
