package org.odata4j.producer.exceptions;

import javax.ws.rs.core.Response;

public class NotAuthorizedException extends ODataException {

  private static final long serialVersionUID = 1L;

  public NotAuthorizedException() {
    super(Response.status(401).build());
  }

  public NotAuthorizedException(String message) {
    super(Response.status(401).entity(message).build());
  }

}
