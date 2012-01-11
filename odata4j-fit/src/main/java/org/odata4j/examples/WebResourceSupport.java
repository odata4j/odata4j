package org.odata4j.examples;

public interface WebResourceSupport {

  <T> String getWebResource(String uri, Class<T> c);
  
}
