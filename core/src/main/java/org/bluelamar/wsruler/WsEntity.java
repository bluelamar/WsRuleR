/**
 * 
 */
package org.bluelamar.wsruler;

/**
 * Data model for WsRuler data entity object.
 *
 */
public class WsEntity {

	public String id;
	public String name;
	
	/**
	 * 
	 */
	public WsEntity() {
	}

	public String getId() {
		return id;
	}
	public WsEntity setId(String id) {
		this.id = id;
		return this;
	}
	public String getName() {
		return name;
	}
	public WsEntity setName(String name) {
		this.name = name;
		return this;
	}
}
