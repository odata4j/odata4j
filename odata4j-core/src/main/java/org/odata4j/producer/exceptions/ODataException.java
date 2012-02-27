package org.odata4j.producer.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class ODataException extends WebApplicationException {

  private static final long serialVersionUID = 1L;

  private String message;

  public ODataException() {
    super();
  }

  public ODataException(int status) {
    super(status);
  }

  public ODataException(Response response) {
    super(response);
  }

  public ODataException(Response response, String message) {
    super(response);
    this.message = message;
  }

  public ODataException(Status status) {
    super(status);
  }

  public ODataException(Throwable cause, int status) {
    super(cause, status);
  }

  public ODataException(Throwable cause, Response response) {
    super(cause, response);
  }

  public ODataException(Throwable cause, Status status) {
    super(cause, status);
  }

  public ODataException(Throwable cause) {
    super(cause);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(getClass().getName());
    if (message != null)
      sb.append(": " + message);
    return sb.toString();
  }

}
