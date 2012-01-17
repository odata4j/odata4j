package org.odata4j.fit.support;

import javax.ws.rs.core.MediaType;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.format.FormatType;
import org.odata4j.producer.server.ODataServer;

public interface RuntimeFacade {

  public void hostODataServer(String baseUri);

  public ODataServer startODataServer(String baseUri);

  public ODataConsumer create(String endpointUri, FormatType format, String methodToTunnel);

  public String getWebResource(String uri);

  public String acceptAndReturn(String uri, MediaType mediaType);

  public void accept(String uri, MediaType mediaType);

  public String getWebResource(String uri, String accept);

}
