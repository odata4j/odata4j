package org.odata4j.producer.exceptions;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

public class NotAcceptableException extends ODataException {

  private static final long serialVersionUID = 1L;

  public NotAcceptableException() {}

  public NotAcceptableException(String message) {
    super(message);
  }

  public NotAcceptableException(Throwable cause) {
    super(cause);
  }

  public NotAcceptableException(String message, Throwable cause) {
    super(message, cause);
  }

  @Override
  public StatusType getStatus() {
    return Status.NOT_ACCEPTABLE;
  }
}
