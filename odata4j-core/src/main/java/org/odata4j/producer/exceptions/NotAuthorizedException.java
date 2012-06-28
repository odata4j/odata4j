package org.odata4j.producer.exceptions;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

public class NotAuthorizedException extends ODataException {

  private static final long serialVersionUID = 1L;

  public NotAuthorizedException() {}

  public NotAuthorizedException(String message) {
    super(message);
  }

  public NotAuthorizedException(Throwable cause) {
    super(cause);
  }

  public NotAuthorizedException(String message, Throwable cause) {
    super(message, cause);
  }

  @Override
  public StatusType getStatus() {
    return Status.UNAUTHORIZED;
  }
}
