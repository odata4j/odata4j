package org.odata4j.exceptions;

import javax.ws.rs.core.Response.Status;

public class ServerErrorException extends ODataProducerException {

  private static final long serialVersionUID = 1L;

  public ServerErrorException() {
    this(null, null);
  }

  public ServerErrorException(String message) {
    this(message, null);
  }

  public ServerErrorException(Throwable cause) {
    this(null, cause);
  }

  public ServerErrorException(String message, Throwable cause) {
    super(message, cause, Status.INTERNAL_SERVER_ERROR);
  }
}
