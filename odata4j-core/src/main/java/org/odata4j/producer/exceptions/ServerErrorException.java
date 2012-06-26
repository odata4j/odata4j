package org.odata4j.producer.exceptions;

import javax.ws.rs.core.Response.Status;

public class ServerErrorException extends ODataException {

  private static final long serialVersionUID = 1L;

  public ServerErrorException() {
    super(Status.INTERNAL_SERVER_ERROR);
  }

  public ServerErrorException(String message) {
    super(Status.INTERNAL_SERVER_ERROR, message);
  }

  public ServerErrorException(String message, Throwable cause) {
    super(Status.INTERNAL_SERVER_ERROR, message, cause);
  }
}
