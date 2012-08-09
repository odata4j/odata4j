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
 * <p>OData producer exceptions can be either created by using one of its sub-classes or by the
 * static factory {@link ODataProducerExceptions}.</p>
 */
public abstract class ODataProducerException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final OError error;

  /**
   * Constructor used by sub-classes to instantiate an exception that is thrown by an OData provider at runtime.
   * <p>Parameters are delegated to {@link RuntimeException#RuntimeException(String, Throwable)}.</p>
   */
  protected ODataProducerException(String message, Throwable cause) {
    super(message, cause);
    error = OErrors.error(code(), message(), innerError());
  }

  /**
   * Constructor used by sub-classes to instantiate an exception based on the given OError
   * that has been received and parsed by an OData consumer.
   */
  protected ODataProducerException(OError error) {
    super(error.getMessage());
    this.error = error;
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

  /**
   * Gets the HTTP status.
   *
   * @return the HTTP status
   * @see Status
   */
  public abstract StatusType getHttpStatus();

  /**
   * Gets the OData error message.
   *
   * @return the OData error message
   */
  public OError getOError() {
    return error;
  }
}
