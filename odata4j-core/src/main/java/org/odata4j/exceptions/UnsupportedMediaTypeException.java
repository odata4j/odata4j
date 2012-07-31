package org.odata4j.exceptions;

import javax.ws.rs.core.Response.Status;

public class UnsupportedMediaTypeException extends ODataProducerException {

  private static final long serialVersionUID = 1L;

  public UnsupportedMediaTypeException() {
    this(null, null);
  }

  public UnsupportedMediaTypeException(String message) {
    this(message, null);
  }

  public UnsupportedMediaTypeException(Throwable cause) {
    this(null, cause);
  }

  public UnsupportedMediaTypeException(String message, Throwable cause) {
    super(message, cause, Status.UNSUPPORTED_MEDIA_TYPE);
  }
}
