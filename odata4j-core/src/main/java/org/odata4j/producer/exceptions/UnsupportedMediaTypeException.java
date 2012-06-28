package org.odata4j.producer.exceptions;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

public class UnsupportedMediaTypeException extends ODataException {

  private static final long serialVersionUID = 1L;

  public UnsupportedMediaTypeException() {}

  public UnsupportedMediaTypeException(String message) {
    super(message);
  }

  public UnsupportedMediaTypeException(Throwable cause) {
    super(cause);
  }

  public UnsupportedMediaTypeException(String message, Throwable cause) {
    super(message, cause);
  }

  @Override
  public StatusType getStatus() {
    return Status.UNSUPPORTED_MEDIA_TYPE;
  }
}
