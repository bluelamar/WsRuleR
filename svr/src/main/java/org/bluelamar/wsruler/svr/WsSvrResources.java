package org.bluelamar.wsruler.svr;

import java.util.List;

import org.bluelamar.wsruler.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.inject.Inject;

@Path("/v1")
public class WsSvrResources {

	@GET
	@Path("/envlink/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public WsLink getEnvLink(@PathParam("id") String id) {
		try {
			return this.delegate.getLink("env", id);
		} catch (ConnException ex) {
			throw new WebApplicationException(ex, ex.getErrorCode());
		}
	}
	
	@GET
	@Path("/envlink/children/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<WsLink> getEnvChildren(@PathParam("id") String id) {
		try {
			return this.delegate.getChildren("env", id);
		} catch (ConnException ex) {
			throw new WebApplicationException(ex, ex.getErrorCode());
		}
	}

	@POST
	@Path("/envlink")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public WsLink postEnvLink(WsLink link) {
		try {
			return this.delegate.postLink("env", link);
		} catch (ConnException ex) {
			throw new WebApplicationException(ex, ex.getErrorCode());
		}
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
			this.delegate.deleteLink("env", id);
		} catch (ConnException ex) {
			throw new WebApplicationException(ex, ex.getErrorCode());
		}
    }
	
	@GET
	@Path("/dblink/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public WsLink getDbLink(@PathParam("id") String id) {
		try {
			return this.delegate.getLink("db", id);
		} catch (ConnException ex) {
			throw new WebApplicationException(ex, ex.getErrorCode());
		}
	}

	@POST
	@Path("/dblink")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public WsLink postDbLink(WsLink link) {
		try {
			return this.delegate.postLink("db", link);
		} catch (ConnException ex) {
			throw new WebApplicationException(ex, ex.getErrorCode());
		}
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
			this.delegate.deleteLink("db", id);
		} catch (ConnException ex) {
			throw new WebApplicationException(ex, ex.getErrorCode());
		}
    }

	@Inject private WsSvrHandler delegate;
	@Context private HttpServletRequest request;
	@Context private HttpServletResponse response;
}
