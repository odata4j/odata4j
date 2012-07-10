package org.odata4j.consumer;

/**
 * An OData client exception.
 */
public class ODataClientException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ODataClientException() {}

  public ODataClientException(String message) {
    super(message);
  }

  public ODataClientException(Throwable cause) {
    super(cause);
  }

  public ODataClientException(String message, Throwable cause) {
    super(message, cause);
  }

}
