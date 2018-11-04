/**
 * 
 */
package org.bluelamar.wsruler;

/**
 * Factory for creation of a Connection Pool
 *
 */
public interface ConnPoolFactory {
	ConnPool makeConnPool();
}
