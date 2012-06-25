package org.odata4j.producer.exceptions;

import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;

public class NotImplementedException extends ODataException {

  private static final long serialVersionUID = 1L;

  // available in JAX-RS Version 2.0
  public static final StatusType NOT_IMPLEMENTED = new StatusType() {

    public int getStatusCode() {
      return 501;
    }

    public Family getFamily() {
      return Family.SERVER_ERROR;
    }

    public String getReasonPhrase() {
      return "Not Implemented";
    }
  };
  
  public NotImplementedException() {
    super(NOT_IMPLEMENTED);
  }

  public NotImplementedException(String message) {
    super(NOT_IMPLEMENTED, message);
  }

}
