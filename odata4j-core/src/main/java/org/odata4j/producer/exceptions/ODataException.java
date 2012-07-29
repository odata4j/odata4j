package org.odata4j.producer.exceptions;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.ws.rs.core.Response.StatusType;

import org.odata4j.core.OError;
import org.odata4j.core.OErrors;
import org.odata4j.producer.resources.ExceptionMappingProvider;

/**
 * An OData server exception with the information described in the OData documentation for
 * <a href="http://www.odata.org/documentation/operations#ErrorConditions">error conditions</a>.
 *
 * <p>Correct formatting of every ODataException is ensured by routing it through the
 * {@link ExceptionMappingProvider}.</p>
 */
public abstract class ODataException extends RuntimeException {

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

  public OError toOError(boolean includeInnerError) {
    String code = getClass().getSimpleName();
    String message = getMessage() != null ? getMessage() : getStatus().getReasonPhrase();
    String innerError = null;

    if (includeInnerError) {
      StringWriter sw = new StringWriter();
      printStackTrace(new PrintWriter(sw));
      innerError = sw.toString();
    }

    return OErrors.error(code, message, innerError);
  }
}
