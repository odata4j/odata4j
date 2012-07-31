package org.odata4j.exceptions;

import javax.ws.rs.core.Response.Status;

public class NotAcceptableException extends ODataProducerException {

  private static final long serialVersionUID = 1L;

  public NotAcceptableException() {
    this(null, null);
  }

  public NotAcceptableException(String message) {
    this(message, null);
  }

  public NotAcceptableException(Throwable cause) {
    this(null, cause);
  }

  public NotAcceptableException(String message, Throwable cause) {
    super(message, cause, Status.NOT_ACCEPTABLE);
  }
}
