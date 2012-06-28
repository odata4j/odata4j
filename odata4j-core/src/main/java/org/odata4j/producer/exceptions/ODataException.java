package org.odata4j.producer.exceptions;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.ws.rs.core.Response.StatusType;

import org.odata4j.core.OError;
import org.odata4j.core.OErrors;
import org.odata4j.producer.ErrorResponse;

public abstract class ODataException extends RuntimeException implements ErrorResponse {

  private static final long serialVersionUID = 1L;

  protected ODataException() {}

  protected ODataException(String message) {
    super(message);
  }

  protected ODataException(Throwable cause) {
    super(cause);
  }

  protected ODataException(String message, Throwable cause) {
    super(message, cause);
  }

  public abstract StatusType getStatus();

  public OError getError() {
    final String message = ODataException.this.getMessage() != null ? ODataException.this.getMessage() : ODataException.this.getStatus().getReasonPhrase();
    StringWriter sw = new StringWriter();
    ODataException.this.printStackTrace(new PrintWriter(sw));
    final String innerError = sw.toString();
    return OErrors.error(ODataException.this.getClass().getSimpleName(), message, innerError);
  }

}
