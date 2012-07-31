package org.odata4j.exceptions;

import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;

public class MethodNotAllowedException extends ODataProducerException {

  private static final long serialVersionUID = 1L;

  // available in JAX-RS Version 2.0
  public static final StatusType METHOD_NOT_ALLOWED = new StatusType() {

    public int getStatusCode() {
      return 405;
    }

    public Family getFamily() {
      return Family.CLIENT_ERROR;
    }

    public String getReasonPhrase() {
      return "Method Not Allowed";
    }
  };

  public MethodNotAllowedException() {
    this(null, null);
  }

  public MethodNotAllowedException(String message) {
    this(message, null);
  }

  public MethodNotAllowedException(Throwable cause) {
    this(null, cause);
  }

  public MethodNotAllowedException(String message, Throwable cause) {
    super(message, cause, METHOD_NOT_ALLOWED);
  }
}
