/**
 * 
 */
package org.bluelamar.wsruler;

import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Data Model sent via POST or PUT to create or update a workspace.
 */
public class WsGroupEntity extends WsEntity {
	
	@JsonDeserialize(as=ArrayList.class, contentAs=String.class)
	public List<String> groups;
	
	/**
	 * 
	 */
	public WsGroupEntity() {
	}
	
	public void setGroups(List<String> groups) {
		this.groups = groups;
	}
	
	public List<String> getGroups() {
		return groups;
	}
}
