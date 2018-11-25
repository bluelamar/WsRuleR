/**
 * 
 */
package org.bluelamar.wsruler;

/**
 * Builds ID for given object(s).
 *
 */
public interface IdGenerator {

	/**
	 * Make an ID for the given object.
	 * @param obj inwhich to create an ID for
	 * @return String representation of the ID
	 */
	String makeId(Object obj);
	
	/**
	 * Make an ID for the given objects.
	 * @param objs used inwhich to create an ID for
	 * @return String representation of the ID
	 */
	String makeId(Object[] objs);
}
