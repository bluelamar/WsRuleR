/**
 * 
 */
package org.bluelamar.wsruler;

import java.util.List;

/**
 * Data Model for workspace object returned from get children.
 */
public class WsOwnerEntity extends WsEntity {

	List<WsOwner> owners;
	
	/**
	 * 
	 */
	public WsOwnerEntity() {
	}

	public void setOwners(List<WsOwner> owners) {
		this.owners = owners;
	}
	
	public List<WsOwner> getOwners() {
		return owners;
	}
}
