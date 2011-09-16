
package org.odata4j.core;

/**
 * A namespace
 * @author Tony Rozga
 */
public class Namespace {
  
  public Namespace(String uri, String prefix) {
    this.uri = uri;
    this.prefix = prefix;
  }
  
  public String getURI() { return uri; }
  public String getPrefix() { return prefix; }
  
  private final String uri;
  private final String prefix;
}
