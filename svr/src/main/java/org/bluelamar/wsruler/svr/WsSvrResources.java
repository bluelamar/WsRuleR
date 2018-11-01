package org.bluelamar.wsruler.svr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bluelamar.wsruler.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.inject.Inject;

@Path("/v1")
public class WsSvrResources {
	
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

	@GET
	@Path("/envlink/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public WsLink getEnvLink(@PathParam("id") String id) {
		return getLink("env", id);
	}
	
	@GET
	@Path("/envlink/children/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<WsLink> getEnvChildren(@PathParam("id") String id) {
		try {
			return this.delegate.getChildren("env", id);
		} catch (ConnException ex) {
			ex.printStackTrace();
			throw new WebApplicationException(ex, ex.getErrorCode());
		}
	}

	@POST
	@Path("/envlink")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public WsLink postEnvLink(WsLink link) {
		return postLink("env", link);
    }
	
	@PUT
	@Path("/envlink/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void putEnvLink(@PathParam("id") String id, WsLink link) {
		try {
			this.delegate.putLink("env", id, link);
		} catch (ConnException ex) {
			throw new WebApplicationException(ex, ex.getErrorCode());
		}
	}

    @DELETE
    @Path("/envlink/{id}")
    public void deleteEnvLink(@PathParam("id") String id) {
		try {
			this.delegate.deleteEntity("env", id);
		} catch (ConnException ex) {
			ex.printStackTrace();
			throw new WebApplicationException(ex, ex.getErrorCode());
		}
    }
	
	@GET
	@Path("/dblink/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public WsLink getDbLink(@PathParam("id") String id) {
		return getLink("db", id);
	}

	@POST
	@Path("/dblink")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public WsLink postDbLink(WsLink link) {
		return postLink("db", link);
    }
	
	@PUT
	@Path("/dblink/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void putDbLink(@PathParam("id") String id, WsLink link) {
		try {
			this.delegate.putLink("db", id, link);
		} catch (ConnException ex) {
			throw new WebApplicationException(ex, ex.getErrorCode());
		}
	}

    @DELETE
    @Path("/dblink/{id}")
    public void deleteDbLink(@PathParam("id") String id) {
		try {
			this.delegate.deleteEntity("db", id);
		} catch (ConnException ex) {
			ex.printStackTrace();
			throw new WebApplicationException(ex, ex.getErrorCode());
		}
    }
    
    WsLink postLink(String comp, WsLink link) {
		try {
			Map<String,Object> entity = new HashMap<>();
			entity.put(ENT_FIELD_DLINK, link.getData_link());
			entity.put(ENT_FIELD_PAR, link.getParent());
			entity = this.delegate.postEntity(comp, entity);
			link.setId(entity.get(ENT_FIELD_ID).toString());
			link.setData_link(entity.get(ENT_FIELD_DLINK).toString());
			link.setParent(entity.get(ENT_FIELD_PAR).toString());
			return link;
		} catch (ConnException ex) {
			ex.printStackTrace();
			throw new WebApplicationException(ex, ex.getErrorCode());
		}
    }
    
    WsLink getLink(String comp, String id) {
		try {
			Map<String,Object> entity = new HashMap<>();
			entity = this.delegate.getEntity(comp, id);
			WsLink link = new WsLink();
			link.setId(entity.get(ENT_FIELD_ID).toString());
			Object entFld = entity.get(ENT_FIELD_DLINK);
			if (entFld != null) {
				link.setData_link(entFld.toString());
			}
			entFld = entity.get(ENT_FIELD_PAR);
			if (entFld != null) {
				link.setParent(entFld.toString());
			}
			return link;
		} catch (ConnException ex) {
			ex.printStackTrace();
			throw new WebApplicationException(ex, ex.getErrorCode());
		}
    }

	@Inject private WsSvrHandler delegate;
	@Context private HttpServletRequest request;
	@Context private HttpServletResponse response;
}
