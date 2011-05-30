package org.odata4j.producer.exceptions;

import javax.ws.rs.core.Response;

public class NotAcceptableException extends ODataException {

  private static final long serialVersionUID = 1L;

  public NotAcceptableException() {
    super(Response.status(406).build());
  }

  public NotAcceptableException(String message) {
    super(Response.status(406).entity(message).build());
  }

}
