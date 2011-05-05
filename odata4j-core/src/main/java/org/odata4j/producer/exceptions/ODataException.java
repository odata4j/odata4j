package org.odata4j.producer.exceptions;

public class ODataException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ODataException() {
		super();
	}

	public ODataException(String message, Throwable cause) {
		super(message, cause);
	}

	public ODataException(String message) {
		super(message);
	}

	public ODataException(Throwable cause) {
		super(cause);
	}

}
