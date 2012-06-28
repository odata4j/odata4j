package org.odata4j.producer.exceptions;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

public class ForbiddenException extends ODataException {

  private static final long serialVersionUID = 1L;

  public ForbiddenException() {}

  public ForbiddenException(String message) {
    super(message);
  }

  public ForbiddenException(Throwable cause) {
    super(cause);
  }

  public ForbiddenException(String message, Throwable cause) {
    super(message, cause);
  }

  @Override
  public StatusType getStatus() {
    return Status.FORBIDDEN;
  }
}
