package org.odata4j.exceptions;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import org.odata4j.core.OError;
import org.odata4j.core.OErrors;

/**
 * An OData producer exception with the information described in the OData documentation for
 * <a href="http://www.odata.org/documentation/operations#ErrorConditions">error conditions</a>.
 *
 * <p>OData producer exceptions can be either created by using one of its sub-classes or by the
 * static factory {@link ODataProducerExceptions}.</p>
 */
public class ODataProducerException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final StatusType status;
  private final OError error;

  protected ODataProducerException(String message, Throwable cause, StatusType status) {
    super(message, cause);
    this.status = status;
    this.error = OErrors.error(code(), message(), innerError());
  }

  private String code() {
    return getClass().getSimpleName();
  }

  private String message() {
    if (getMessage() != null)
      return getMessage();
    if (getHttpStatus() != null)
      return getHttpStatus().getReasonPhrase();
    return null;
  }

  private String innerError() {
    StringWriter sw = new StringWriter();
    printStackTrace(new PrintWriter(sw));
    return sw.toString();
  }

  protected ODataProducerException(StatusType status, OError error) {
    this.status = status;
    this.error = error;
  }

  /**
   * Gets the HTTP status.
   *
   * @return the HTTP status
   * @see Status
   */
  public StatusType getHttpStatus() {
    return status;
  }

  /**
   * Gets the OData error message.
   *
   * @return the OData error message or {@code null} in case the exceptions has not (yet) been 
   */
  public OError getOError() {
    return error;
  }
}
