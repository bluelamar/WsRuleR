/**
 * 
 */
package org.bluelamar.wsruler;

/**
 * Data model for WsRuler link object.
 */
public class WsLink {

	public String id;
	public String data_link;
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
	public String getData_link() {
		return data_link;
	}
	public WsLink setData_link(String dlink) {
		this.data_link = dlink;
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
