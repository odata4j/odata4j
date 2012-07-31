package org.odata4j.exceptions;

import javax.ws.rs.core.Response.Status;

public class NotFoundException extends ODataProducerException {

  private static final long serialVersionUID = 1L;

  public NotFoundException() {
    this(null, null);
  }

  public NotFoundException(String message) {
    this(message, null);
  }

  public NotFoundException(Throwable cause) {
    this(null, cause);
  }

  public NotFoundException(String message, Throwable cause) {
    super(message, cause, Status.NOT_FOUND);
  }
}
