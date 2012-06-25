package org.odata4j.core;

/**
 * An OData error message, consisting of an error code and an error-message text.
 */
public interface OError {

  /**
   * Get the error code.
   *
   * @return the error code
   */
  String getCode();

  /**
   * Get the error-message text.
   *
   * @return the error-message text
   */
  String getMessage();
}
