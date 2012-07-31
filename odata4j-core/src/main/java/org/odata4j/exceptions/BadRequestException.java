package org.odata4j.exceptions;

import javax.ws.rs.core.Response.Status;

public class BadRequestException extends ODataProducerException {

  private static final long serialVersionUID = 1L;

  public BadRequestException() {
    this(null, null);
  }

  public BadRequestException(String message) {
    this(message, null);
  }

  public BadRequestException(Throwable cause) {
    this(null, cause);
  }

  public BadRequestException(String message, Throwable cause) {
    super(message, cause, Status.BAD_REQUEST);
  }
}
