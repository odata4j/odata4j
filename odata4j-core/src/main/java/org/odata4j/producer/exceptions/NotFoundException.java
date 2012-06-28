package org.odata4j.producer.exceptions;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

public class NotFoundException extends ODataException {

  private static final long serialVersionUID = 1L;

  public NotFoundException() {}

  public NotFoundException(String message) {
    super(message);
  }

  public NotFoundException(Throwable cause) {
    super(cause);
  }

  public NotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  @Override
  public StatusType getStatus() {
    return Status.NOT_FOUND;
  }
}
