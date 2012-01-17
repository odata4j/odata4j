package org.odata4j.test;

import javax.ws.rs.core.MediaType;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.format.FormatType;
import org.odata4j.producer.server.ODataServer;

public class CxfRuntimeFacade implements RuntimeFacade {

  @Override
  public void hostODataServer(String baseUri) {
    throw new RuntimeException("not implemented");
  }

  @Override
  public ODataServer startODataServer(String baseUri) {
    throw new RuntimeException("not implemented");
  }

  @Override
  public ODataConsumer create(String endpointUri, FormatType format, String methodToTunnel) {
    throw new RuntimeException("not implemented");
  }

  @Override
  public String getWebResource(String uri) {
    throw new RuntimeException("not implemented");
  }

  @Override
  public String acceptAndReturn(String uri, MediaType mediaType) {
    throw new RuntimeException("not implemented");
  }

  @Override
  public String getWebResource(String uri, String accept) {
    throw new RuntimeException("not implemented");
  }

  @Override
  public void accept(String uri, MediaType mediaType) {
    throw new RuntimeException("not implemented");
  }
}
