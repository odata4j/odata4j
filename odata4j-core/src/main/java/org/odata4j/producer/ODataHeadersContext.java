
package org.odata4j.producer;

import java.util.List;

/**
 * Carries the request headers into a producer
 */
public interface ODataHeadersContext {
  
  
  /**
   * Get an Iterable containing all request header field names.
   * @return the Iterable
   */
  public Iterable<String> getRequestHeaderFieldNames();
  
  /**
   * Get an Iterable of all values for the given header field name.
   * @param fieldName
   * @return the Iterable
   */
  public Iterable<String> getRequestHeaderValue(String fieldName);
}
