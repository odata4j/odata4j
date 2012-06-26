package org.odata4j.producer.exceptions;

import javax.ws.rs.core.Response.Status;

public class NotFoundException extends ODataException { 

  private static final long serialVersionUID = 1L;

  public NotFoundException() {
    super(Status.NOT_FOUND);
  }

  public NotFoundException(String message) {
    super(Status.NOT_FOUND, message);
  }

  public NotFoundException(String message, Throwable cause) {
    super(Status.NOT_FOUND, message, cause);
  }
}
