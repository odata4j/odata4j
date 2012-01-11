package org.odata4j.examples;

import javax.ws.rs.core.MediaType;

public interface WebResourceSupport {

  <T> String getWebResource(String uri, Class<T> c);

  void accept(String uri, MediaType mediaType);

}
