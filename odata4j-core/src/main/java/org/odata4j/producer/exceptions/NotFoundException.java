package org.odata4j.producer.exceptions;

import javax.ws.rs.core.Response;

public class NotFoundException extends ODataException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NotFoundException() {
		super(Response.status(404).build());
	}


	public NotFoundException(String message) {
		super(Response.status(404).entity(message).build());
	}

}
