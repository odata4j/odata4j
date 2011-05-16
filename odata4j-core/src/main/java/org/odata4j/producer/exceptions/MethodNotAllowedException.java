package org.odata4j.producer.exceptions;

import javax.ws.rs.core.Response;

public class MethodNotAllowedException extends ODataException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MethodNotAllowedException() {
		super(Response.status(405).build());
	}

	public MethodNotAllowedException(String message) {
		super(Response.status(405).entity(message).build());
	}


}
