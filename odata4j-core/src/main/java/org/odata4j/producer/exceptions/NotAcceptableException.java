package org.odata4j.producer.exceptions;

import javax.ws.rs.core.Response.Status;

public class NotAcceptableException extends ODataException {

  private static final long serialVersionUID = 1L;

  public NotAcceptableException() {
    super(Status.NOT_ACCEPTABLE);
  }

  public NotAcceptableException(String message) {
    super(Status.NOT_ACCEPTABLE, message);
  }

  public NotAcceptableException(String message, Throwable cause) {
    super(Status.NOT_ACCEPTABLE, message, cause);
  }
}
