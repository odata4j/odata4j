package org.odata4j.producer.exceptions;

public class ServerErrorException extends ODataException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ServerErrorException() {
		super();
	}

	public ServerErrorException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServerErrorException(String message) {
		super(message);
	}

	public ServerErrorException(Throwable cause) {
		super(cause);
	}

}
