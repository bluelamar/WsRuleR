/**
 * 
 */
package org.bluelamar.wsruler;

/**
 * Creates a simple Queue type connection pool implementation
 *
 */
public class QueueConnPoolFactory implements ConnPoolFactory {

	/**
	 * 
	 */
	public QueueConnPoolFactory() {
	}

	public ConnPool makeConnPool() {
		return new QueueConnPool();
	}
}
