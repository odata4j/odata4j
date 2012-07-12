package org.odata4j.consumer;

import javax.ws.rs.core.MultivaluedMap;

public interface Response {

  MultivaluedMap<String, String> getHeaders();

  void close();
}
