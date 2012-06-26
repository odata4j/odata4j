package org.odata4j.producer.exceptions;

import javax.ws.rs.core.Response.Status;

public class NotAuthorizedException extends ODataException {

  private static final long serialVersionUID = 1L;

  public NotAuthorizedException() {
    super(Status.UNAUTHORIZED);
  }

  public NotAuthorizedException(String message) {
    super(Status.UNAUTHORIZED, message);
  }

  public NotAuthorizedException(String message, Throwable cause) {
    super(Status.UNAUTHORIZED, message, cause);
  }
}
