package org.odata4j.producer.exceptions;

import javax.ws.rs.core.Response.Status;

public class ForbiddenException extends ODataException {

  private static final long serialVersionUID = 1L;

  public ForbiddenException() {
    super(Status.FORBIDDEN);
  }

  public ForbiddenException(String message) {
    super(Status.FORBIDDEN, message);
  }

}
