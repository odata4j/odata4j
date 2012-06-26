package org.odata4j.core;

/**
 * An OData error message, consisting of an error code, an error-message text,
 * and an optional inner error.
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

  /**
   * Get the inner error.
   *
   * @return the inner error
   */
  String getInnerError();
}
