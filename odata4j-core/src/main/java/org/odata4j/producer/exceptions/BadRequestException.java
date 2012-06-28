package org.odata4j.producer.exceptions;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

public class BadRequestException extends ODataException {

  private static final long serialVersionUID = 1L;

  public BadRequestException() {}

  public BadRequestException(String message) {
    super(message);
  }

  public BadRequestException(Throwable cause) {
    super(cause);
  }

  public BadRequestException(String message, Throwable cause) {
    super(message, cause);
  }

  @Override
  public StatusType getStatus() {
    return Status.BAD_REQUEST;
  }
}
