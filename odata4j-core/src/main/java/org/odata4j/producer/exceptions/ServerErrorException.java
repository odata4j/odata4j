package org.odata4j.producer.exceptions;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

public class ServerErrorException extends ODataException {

  private static final long serialVersionUID = 1L;

  public ServerErrorException() {}

  public ServerErrorException(String message) {
    super(message);
  }

  public ServerErrorException(Throwable cause) {
    super(cause);
  }

  public ServerErrorException(String message, Throwable cause) {
    super(message, cause);
  }

  @Override
  public StatusType getStatus() {
    return Status.INTERNAL_SERVER_ERROR;
  }
}
