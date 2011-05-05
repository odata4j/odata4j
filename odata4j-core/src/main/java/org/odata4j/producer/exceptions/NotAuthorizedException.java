package org.odata4j.producer.exceptions;

public class NotAuthorizedException extends ODataException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NotAuthorizedException() {
		super();
	}

	public NotAuthorizedException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotAuthorizedException(String message) {
		super(message);
	}

	public NotAuthorizedException(Throwable cause) {
		super(cause);
	}

}
