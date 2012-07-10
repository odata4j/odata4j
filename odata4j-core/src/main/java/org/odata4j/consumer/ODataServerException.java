package org.odata4j.consumer;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import org.odata4j.core.OError;

/**
 * An OData server exception with the information provided by the server as described in the
 * OData documentation for <a href="http://www.odata.org/documentation/operations#ErrorConditions">error conditions</a>.
 */
public class ODataServerException extends RuntimeException implements OError {

  private static final long serialVersionUID = 1L;

  private final StatusType status;
  private final OError error;

  public ODataServerException(StatusType status, OError error) {
    this.status = status;
    this.error = error;
  }

  /**
   * @return the HTTP status
   * @see Status
   */
  public StatusType getStatus() {
    return status;
  }

  public String getCode() {
    return error.getCode();
  }

  @Override
  public String getMessage() {
    return error.getMessage();
  }

  public String getInnerError() {
    return error.getInnerError();
  }
}
