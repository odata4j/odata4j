package org.odata4j.producer.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class ODataException extends WebApplicationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ODataException() {
		super();
	}

	public ODataException(int status) {
		super(status);
	}

	public ODataException(Response response) {
		super(response);
	}

	public ODataException(Status status) {
		super(status);
	}

	public ODataException(Throwable cause, int status) {
		super(cause, status);
	}

	public ODataException(Throwable cause, Response response) {
		super(cause, response);
	}

	public ODataException(Throwable cause, Status status) {
		super(cause, status);
	}

	public ODataException(Throwable cause) {
		super(cause);
	}
	
	
}
