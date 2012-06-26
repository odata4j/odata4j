package org.odata4j.producer.exceptions;

import javax.ws.rs.core.Response.Status;

public class BadRequestException extends ODataException {

  private static final long serialVersionUID = 1L;

  public BadRequestException() {
    super(Status.BAD_REQUEST);
  }

  public BadRequestException(String message) {
    super(Status.BAD_REQUEST, message);
  }

  public BadRequestException(String message, Throwable cause) {
    super(Status.BAD_REQUEST, message, cause);
  }
}
