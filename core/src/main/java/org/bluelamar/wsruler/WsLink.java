/**
 * 
 */
package org.bluelamar.wsruler;

/**
 * Data model for WsRuler link object.
 */
public class WsLink {

	public String id;
	public String name;
	public String parent;
	
	/**
	 * 
	 */
	public WsLink() {
	}

	public String getId() {
		return id;
	}
	public WsLink setId(String id) {
		this.id = id;
		return this;
	}
	public String getName() {
		return name;
	}
	public WsLink setName(String name) {
		this.name = name;
		return this;
	}
	public String getParent() {
		return parent;
	}
	public WsLink setParent(String parent) {
		this.parent = parent;
		return this;
	}
}
