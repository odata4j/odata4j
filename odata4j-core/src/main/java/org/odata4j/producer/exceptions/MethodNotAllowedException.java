package org.odata4j.producer.exceptions;

public class MethodNotAllowedException extends ODataException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MethodNotAllowedException() {
		super();
	}

	public MethodNotAllowedException(String message, Throwable cause) {
		super(message, cause);
	}

	public MethodNotAllowedException(String message) {
		super(message);
	}

	public MethodNotAllowedException(Throwable cause) {
		super(cause);
	}

}
