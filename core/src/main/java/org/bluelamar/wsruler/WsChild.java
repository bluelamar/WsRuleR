/**
 * 
 */
package org.bluelamar.wsruler;

/**
 * Extends WsEntity to add the field "type" to show component type of entity.
 * For example: "ws", "env", "repo", or "db"
 *
 */
public class WsChild extends WsEntity {

	public String type;
	
	/**
	 * 
	 */
	public WsChild() {
	}
	
	public WsChild(String type, WsEntity entity) {
		this.type = type;
		setId(entity.getId());
		setName(entity.getName());
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String getType() {
		return type;
	}
}
