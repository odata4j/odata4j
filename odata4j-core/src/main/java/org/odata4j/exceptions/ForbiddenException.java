package org.odata4j.exceptions;

import javax.ws.rs.core.Response.Status;

public class ForbiddenException extends ODataProducerException {

  private static final long serialVersionUID = 1L;

  public ForbiddenException() {
    this(null, null);
  }

  public ForbiddenException(String message) {
    this(message, null);
  }

  public ForbiddenException(Throwable cause) {
    this(null, cause);
  }

  public ForbiddenException(String message, Throwable cause) {
    super(message, cause, Status.FORBIDDEN);
  }
}
