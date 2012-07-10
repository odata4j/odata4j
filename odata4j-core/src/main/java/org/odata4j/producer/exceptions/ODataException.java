package org.odata4j.producer.exceptions;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.ws.rs.core.Response.StatusType;

import org.odata4j.core.OError;
import org.odata4j.core.OErrors;
import org.odata4j.producer.ErrorResponse;
import org.odata4j.producer.resources.ExceptionMappingProvider;

/**
 * An OData server exception with the information described in the OData documentation for
 * <a href="http://www.odata.org/documentation/operations#ErrorConditions">error conditions</a>.
 * <p>Correct formatting of every ODataException is ensured by routing it through the
 * {@link ExceptionMappingProvider}.</p>
 */
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
