
package org.bluelamar.wsruler;

public interface ConnLoginFactory {

	Object buildLogin(String connId, String connSecret);
}

