
package org.odata4j.core;

/**
 * A namespace uri and local prefix
 */
public class Namespace {

  private final String uri;
  private final String prefix;

  public Namespace(String uri, String prefix) {
    this.uri = uri;
    this.prefix = prefix;
  }

  public String getUri() { return uri; }
  public String getPrefix() { return prefix; }

}
