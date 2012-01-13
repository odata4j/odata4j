package org.odata4j.fit.support;

import javax.ws.rs.core.MediaType;

public interface WebResourceSupport {

  String getWebResource(String uri, String accept, Class<String> c);

  String getWebResource(String uri, Class<String> c);

  void accept(String uri, MediaType mediaType);

}
