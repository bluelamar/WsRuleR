package org.bluelamar.wsruler.svr;

import java.util.ArrayList;
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
	@Path("/db/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public WsEntity getDbEntity(@PathParam("id") String id) {
		return getEntity("db", id);
	}
	
	@GET
	@Path("/env/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public WsEntity getEnvEntity(@PathParam("id") String id) {
		return getEntity("env", id);
	}
	
	@GET
	@Path("/repo/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public WsEntity getRepoEntity(@PathParam("id") String id) {
		return getEntity("repo", id);
	}
	
	@GET
	@Path("/ws/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public WsOwnerEntity getWsOwners(@PathParam("id") String id) {
		return getOwners("ws", id);
	}
	
	@PUT
	@Path("/db/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void putDbEntity(@PathParam("id") String id, WsEntity entity) {
		putEntity("db", id, entity);
	}
	
	@PUT
	@Path("/env/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void putEnvEntity(@PathParam("id") String id, WsEntity entity) {
		putEntity("env", id, entity);
	}
	
	@PUT
	@Path("/repo/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void putRepoEntity(@PathParam("id") String id, WsEntity entity) {
		putEntity("repo", id, entity);
	}
	
	@PUT
	@Path("/ws/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void putWsGroups(@PathParam("id") String id, WsGroupEntity groups) {
		putGroups("ws", id, groups);
	}
	
    @DELETE
    @Path("/db/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteDbEntity(@PathParam("id") String id) {
		try {
			this.delegate.deleteEntity("db", id);
			// find db links that have data_link == id and delete them
			List<WsLink> links = getLinksForEntity("db", id);
			if (links != null) {
				for (WsLink link: links) {
					String lid = link.getId();
					deleteDbLink(lid);
				}
			}
		} catch (ConnException ex) {
			ex.printStackTrace();
			throw new WebApplicationException(ex, ex.getErrorCode());
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new WebApplicationException(ex, 500);
		}
    }
    
    @DELETE
    @Path("/env/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteEnvEntity(@PathParam("id") String id) {
		try {
			this.delegate.deleteEntity("env", id);
			// find env links that have data_link == id and delete them
			deleteEnvLink(id);
			
			// find db links that have parent == id and delete those links
			List<Object> res = this.delegate.getChildren("env", id);
			List<WsLink> links = processLinkList(res);
			if (res != null) {
				for (WsLink link: links) {
					String lid = link.getId();
					deleteDbLink(lid);
				}
			}
		} catch (ConnException ex) {
			ex.printStackTrace();
			throw new WebApplicationException(ex, ex.getErrorCode());
		}
    }
    
    @DELETE
    @Path("/repo/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteRepoEntity(@PathParam("id") String id) {
		try {
			this.delegate.deleteEntity("repo", id);
			// find repo links that have data_link == id and delete them
			List<WsLink> links = getLinksForEntity("repo", id);
			if (links != null) {
				for (WsLink link: links) {
					String lid = link.getId();
					deleteDbLink(lid);
				}
			}
		} catch (ConnException ex) {
			ex.printStackTrace();
			throw new WebApplicationException(ex, ex.getErrorCode());
		}
    }
    
    @DELETE
    @Path("/ws/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteWsGroups(@PathParam("id") String id) {
		try {
			this.delegate.deleteEntity("ws", id);
			
			// find env links that have parent == id and delete those links
			List<Object> res = this.delegate.getChildren("ws", id);
			List<WsLink> links = processLinkList(res);
			if (res != null) {
				for (WsLink link: links) {
					String lid = link.getId();
					this.delegate.deleteEntity("env", lid);
				}
			}
		} catch (ConnException ex) {
			ex.printStackTrace();
			throw new WebApplicationException(ex, ex.getErrorCode());
		}
    }
	
	@GET
	@Path("/link/db/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public WsLink getDbLink(@PathParam("id") String id) {
		return getLink("db", id);
	}
	
	@GET
	@Path("/link/env/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public WsLink getEnvLink(@PathParam("id") String id) {
		return getLink("env", id);
	}
	
	@GET
	@Path("/link/repo/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public WsLink getRepoLink(@PathParam("id") String id) {
		return getLink("repo", id);
	}
	
	@GET
	@Path("/env/children/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<WsChild> getEnvChildren(@PathParam("id") String id) {
		
		return getChildren("env", "db", id);
	}
	
	@GET
	@Path("/ws/children/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<WsChild> getWsChildren(@PathParam("id") String id) {
		
		List<WsChild> repoChildren = getChildren("ws", "repo", id);
		List<WsChild> envChildren = getChildren("ws", "env", id);

		// now get the db children of the env subcomponents
		for (WsEntity entity: envChildren) {
			String envId = entity.getId();
			List<WsChild> dbChildren = getEnvChildren(envId);
			if (!dbChildren.isEmpty()) {
				envChildren.addAll(dbChildren);
			}
		}
		envChildren.addAll(repoChildren);
		return envChildren;
	}
	
	@GET
	@Path("/all/{comp}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Object> getAllEntities(@PathParam("comp") String comp) {
		
		try {
			List<Object> res = delegate.getEntities(comp, null, null);
			return res;
		} catch (ConnException ex) {
			ex.printStackTrace();
			throw new WebApplicationException(ex, ex.getErrorCode());
		}
	}
	
	@GET
	@Path("/match/{comp}/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Object> getNameEntities(@PathParam("comp") String comp, @PathParam("name") String name) {
		
		try {
			List<Object> res = delegate.getEntities(comp, ENT_FIELD_NAME, name);
			return res;
		} catch (ConnException ex) {
			ex.printStackTrace();
			throw new WebApplicationException(ex, ex.getErrorCode());
		}
	}
	
	@GET
	@Path("/link/match/{comp}/parent/{parent}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Object> getParentLinks(@PathParam("comp") String comp, @PathParam("parent") String parent) {
		
		try {
			List<Object> res = delegate.getEntities(comp, ENT_FIELD_PAR, parent);
			return res;
		} catch (ConnException ex) {
			ex.printStackTrace();
			throw new WebApplicationException(ex, ex.getErrorCode());
		}
	}
	
	@GET
	@Path("/link/match/{comp}/data/{dlink}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Object> getDataLinks(@PathParam("comp") String comp, @PathParam("dlink") String dlink) {
		
		try {
			return delegate.getEntities(comp, ENT_FIELD_DLINK, dlink);
		} catch (ConnException ex) {
			ex.printStackTrace();
			throw new WebApplicationException(ex, ex.getErrorCode());
		}
	}
	
	@POST
	@Path("/db")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public WsEntity postDbEntity(WsEntity entity) {
		
		return postEntity("db", entity);
    }
	
	@POST
	@Path("/env")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public WsEntity postEnvEntity(WsEntity entity) {
		
		return postEntity("env", entity);
    }
	
	@POST
	@Path("/repo")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public WsEntity postRepoEntity(WsEntity entity) {
		
		return postEntity("repo", entity);
    }
	
	@POST
	@Path("/ws")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public WsGroupEntity postWsGroups(WsGroupEntity entity) {
		
		return postGroups("ws", entity);
    }

	@POST
	@Path("/link/db")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public WsLink postDbLink(WsLink link) {
		validateLinks("db", "env", link);
		return postLink("db", link);
    }
	
	@POST
	@Path("/link/env")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public WsLink postEnvLink(WsLink link) {
		validateLinks("env", "ws", link);
		return postLink("env", link);
    }
	
	@POST
	@Path("/link/repo")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public WsLink postRepoLink(WsLink link) {
		validateLinks("repo", "ws", link);
		return postLink("repo", link);
    }

    @DELETE
    @Path("/link/db/{id}")
    public void deleteDbLink(@PathParam("id") String id) {
		try {
			this.delegate.deleteEntity("db", id);
		} catch (ConnException ex) {
			ex.printStackTrace();
			throw new WebApplicationException(ex, ex.getErrorCode());
		}
    }
    
    @DELETE
    @Path("/link/repo/{id}")
    public void deleteRepoLink(@PathParam("id") String id) {
		try {
			this.delegate.deleteEntity("repo", id);
		} catch (ConnException ex) {
			ex.printStackTrace();
			throw new WebApplicationException(ex, ex.getErrorCode());
		}
    }
    
    @DELETE
    @Path("/link/env/{id}")
    public void deleteEnvLink(@PathParam("id") String id) {
		try {
			this.delegate.deleteEntity("env", id);
		} catch (ConnException ex) {
			ex.printStackTrace();
			throw new WebApplicationException(ex, ex.getErrorCode());
		}
    }
    
    WsEntity getEntity(String comp, String id) {
		try {
			Map<String,Object> entity = new HashMap<>();
			entity = this.delegate.getEntity(comp, id);
			WsEntity wsentity = new WsEntity();
			wsentity.setId(entity.get(ENT_FIELD_ID).toString());
			Object entFld = entity.get(ENT_FIELD_NAME);
			if (entFld != null) {
				wsentity.setName(entFld.toString());
			}
			return wsentity;
		} catch (ConnException ex) {
			ex.printStackTrace();
			throw new WebApplicationException(ex, ex.getErrorCode());
		}
    }
    
    WsEntity postEntity(String comp, WsEntity wsentity) {
 		try {
 			Map<String,Object> entity = new HashMap<>();
 			entity.put(ENT_FIELD_NAME, wsentity.getName());
 			entity = this.delegate.postEntity(comp, entity);
 			wsentity.setId(entity.get(ENT_FIELD_ID).toString());
 			return wsentity;
 		} catch (ConnException ex) {
 			ex.printStackTrace();
 			throw new WebApplicationException(ex, ex.getErrorCode());
 		}
    }
    
	void putEntity(String comp, String id, WsEntity wsentity) {
		try {
			Map<String,Object> entity = new HashMap<>();
			entity.put(ENT_FIELD_NAME, wsentity.getName());
			this.delegate.putEntity(comp, id, entity);
		} catch (ConnException ex) {
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
			return link;
		} catch (ConnException ex) {
			ex.printStackTrace();
			throw new WebApplicationException(ex, ex.getErrorCode());
		}
    }
    
    WsGroupEntity postGroups(String comp, WsGroupEntity groups) {
		try {
			Map<String,Object> entity = new HashMap<>();
			entity.put(ENT_FIELD_NAME, groups.getName());
			List<String> grpNames = groups.getGroups();
			if (grpNames != null) {
				entity.put(ENT_FIELD_GRPS, grpNames);
			}
			entity = this.delegate.postEntity(comp, entity);
			groups.setId(entity.get(ENT_FIELD_ID).toString());
			return groups;
		} catch (ConnException ex) {
			ex.printStackTrace();
			throw new WebApplicationException(ex, ex.getErrorCode());
		}
    }
    
    void putGroups(String comp, String id, WsGroupEntity groups) {
		try {
			Map<String,Object> entity = new HashMap<>();
			entity.put(ENT_FIELD_NAME, groups.getName());
			List<String> grpNames = groups.getGroups();
			if (grpNames != null) {
				entity.put(ENT_FIELD_GRPS, grpNames);
			}
			this.delegate.putEntity(comp, id, entity);
		} catch (ConnException ex) {
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
    
    WsOwnerEntity getOwners(String comp, String id) {
    	
    	try {
			Map<String,Object> entity = this.delegate.getEntity(comp, id);
			WsOwnerEntity owners = new WsOwnerEntity();
			owners.setId(entity.get(ENT_FIELD_ID).toString());
			Object entFld = entity.get(ENT_FIELD_NAME);
			if (entFld != null) {
				owners.setName(entFld.toString());
			}
			entFld = entity.get(ENT_FIELD_GRPS);
			if (entFld != null) {
				List<Object> glist = (List<Object>)entFld;
				System.out.println("FIX getowners: olist=" + glist);
				List<Object> dsObjList = new ArrayList<>();
				for (Object gnobj: glist) {
					// obj is groupname - get the owners for this groupname
					Map<String,Object> dsGroup = this.delegate.getEntity("dirsvc_groups", gnobj.toString());
					if (dsGroup == null || dsGroup.isEmpty()) {
						continue;
					}
					
					List<Object> dsOwners = (List<Object>)dsGroup.get("owners");
					if (dsOwners == null || dsOwners.isEmpty()) {
						continue;
					}
					dsObjList.addAll(dsOwners);
				}
				// convert all the dsOwnerList to List<WsOwner>
				List<WsOwner> dsOwnerList = processOwnerList(dsObjList);
				owners.setOwners(dsOwnerList);
			}
	    	return owners;
		} catch (ConnException ex) {
			ex.printStackTrace();
			throw new WebApplicationException(ex, ex.getErrorCode());
		}
    }
    
    List<WsOwner> processOwnerList(List<Object> dsObjList) {
    	
    	List<WsOwner> owners = new ArrayList<>();
    	for (Object obj: dsObjList) {
    		Map<String, Object> omap = (Map<String,Object>)obj;
    		WsOwner owner = new WsOwner();
    		owner.setId(omap.get("_id").toString());
    		owner.setName(omap.get(ENT_FIELD_NAME).toString());
    		owner.setEmail_address(omap.get(ENT_FIELD_EMAIL).toString());
    		owners.add(owner);
    	}
    	return owners;
    }
    
    void validateLinks(String dlComp, String plComp, WsLink link) {
    	try {
	    	Map<String,Object> entity = this.delegate.getEntity(dlComp, link.getData_link());
	    	// find the parent for this entity
	    	entity = this.delegate.getEntity(plComp, link.getParent());
	    	List<Object> links = null;
	    	try {
	    		links = getDataLinks(dlComp, link.getData_link());
	    	} catch (WebApplicationException ex) {
	    		if (ex.getResponse().getStatus() != 404) {
	    			throw ex;
	    		}
	    	}
	    	if (links != null && !links.isEmpty()) {
	    		throw new WebApplicationException("Link already exists", 400);
	    	}
    	} catch (ConnException ex) {
			ex.printStackTrace();
			throw new WebApplicationException(ex, ex.getErrorCode());
		}
    }
    
    List<WsLink> getLinksForEntity(String comp, String id) {
    	
    	try {
    		List<Object> res = this.delegate.getEntities(comp, ENT_FIELD_DLINK, id);
    		List<WsLink> links = processLinkList(res);
    		return links;
    	} catch (ConnException ex) {
			ex.printStackTrace();
			throw new WebApplicationException(ex, ex.getErrorCode());
		}
    }
    List<WsChild> getChildren(String pComp, String dComp, String id) {
    	
    	try {
	    	List<Object> res = this.delegate.getChildren(pComp, id);
	    	List<WsLink> links = processLinkList(res);
	    	// use the data_link for each returned link to get the entity
	    	List<WsChild> entities = new ArrayList<>();
	    	if (links != null) {
	    		for (WsLink link: links) {
	    			String entityId = link.getData_link();
	    			System.out.println("FIX rsrc: datalink=" + entityId + " par="+ link.getParent());
	    			WsEntity entity = getEntity(dComp, entityId);
	    			entities.add(new WsChild(dComp, entity));
	    		}
	    	}
    		return entities;
    	} catch (ConnException ex) {
			ex.printStackTrace();
			throw new WebApplicationException(ex, ex.getErrorCode());
		}
    }
    
    List<WsLink> processLinkList(List<Object> res) {
    	
		List<WsLink> linkList = new ArrayList<>();
		if (res != null) {
			for (Object obj: res) {
 				System.out.println("FIX rsrc: proclinklist: " + obj);
				if (obj instanceof Map) {
					// create a WsLink from the map
					Map<String,Object> mapObj = (Map<String,Object>)obj;
					WsLink link = new WsLink();
					link.setId(mapObj.get("_id").toString());
					Object resObj = mapObj.get(ENT_FIELD_DLINK);
					if (resObj != null) {
						link.setData_link(resObj.toString());
					}
					resObj = mapObj.get(ENT_FIELD_PAR);
					if (resObj != null) {
						link.setParent(resObj.toString());
					}
					linkList.add(link);
				}
			}
		}
		return linkList;
	}

	@Inject private WsSvrHandler delegate;
	@Context private HttpServletRequest request;
	@Context private HttpServletResponse response;
}
