package org.odata4j.producer.exceptions;

import javax.ws.rs.core.Response;

public class ForbiddenException extends ODataException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ForbiddenException() {
		super(Response.status(403).build());
	}

	public ForbiddenException(String message) {
		super(Response.status(403).entity(message).build());
	}

}
