package org.odata4j.exceptions;

import javax.ws.rs.core.Response.StatusType;

import org.odata4j.core.OError;

/**
 * A static factory to create {@link ODataProducerException} instances.
 */
public class ODataProducerExceptions {

  /**
   * Creates a new {@link ODataProducerException}.
   *
   * @param status  the HTTP status
   * @param error  the OData error message
   * @return an instance of {@link ODataProducerException}
   */
  public static ODataProducerException create(final StatusType status, final OError error) {
    return new ODataProducerException(status, error);
  }
}
