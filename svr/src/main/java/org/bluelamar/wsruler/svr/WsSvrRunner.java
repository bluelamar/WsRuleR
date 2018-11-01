/**
 * 
 */
package org.bluelamar.wsruler.svr;

/**
 * Run server with this class.
 *
 */
public class WsSvrRunner {

	/**
	 * 
	 */
	public WsSvrRunner() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		WsSvrHandler handler = new WsSvrImpl();
        WsSvrContainer container = new WsSvrContainer(handler);
        container.run();
	}

}
