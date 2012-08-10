
package org.odata4j.producer;

import javax.ws.rs.core.HttpHeaders;

/**
 * An (probably the only ever) implementation of ODataHeadersContext 
 * 
 */
public class ODataHeadersImpl implements ODataHeadersContext {

  public ODataHeadersImpl(HttpHeaders headers) {
    this.headers = headers;
  }
  
  @Override
  public Iterable<String> getRequestHeaderFieldNames() {
    return this.headers.getRequestHeaders().keySet();
  }

  @Override
  public Iterable<String> getRequestHeaderValue(String fieldName) {
    return this.headers.getRequestHeader(fieldName);
  }

  private HttpHeaders headers;
}
