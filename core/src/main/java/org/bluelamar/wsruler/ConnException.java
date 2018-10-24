/**
 * 
 */
package org.bluelamar.wsruler;

/**
 * Thrown by Connection implementations.
 *
 */
public class ConnException extends Exception {

	private static final long serialVersionUID = 3048354693386664365L;
	
	private int errorCode;
	
	/**
	 * 
	 */
	public ConnException() {
	}

	/**
	 * @param message
	 */
	public ConnException(int httpCode, String message) {
		super(message);
		errorCode = httpCode;
	}
	
	/**
	 * @param message
	 */
	public ConnException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public ConnException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ConnException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public ConnException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * Get the http error code associated with this exception.
	 * @return http error code
	 */
	public int getErrorCode() {
		return errorCode;
	}
}
