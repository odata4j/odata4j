package org.odata4j.exceptions;

import javax.ws.rs.core.Response.Status;

public class NotAuthorizedException extends ODataProducerException {

  private static final long serialVersionUID = 1L;

  public NotAuthorizedException() {
    this(null, null);
  }

  public NotAuthorizedException(String message) {
    this(message, null);
  }

  public NotAuthorizedException(Throwable cause) {
    this(null, cause);
  }

  public NotAuthorizedException(String message, Throwable cause) {
    super(message, cause, Status.UNAUTHORIZED);
  }
}
