package org.odata4j.jersey.format.xml;

import org.odata4j.edm.EdmDecorator;
import org.odata4j.format.xml.AbstractEdmxFormatWriterTest;
import org.odata4j.jersey.examples.producer.JerseyProducerUtil;
import org.odata4j.producer.server.ODataServer;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

/**
 * a simple test for writing annotations and documentation in the edmx.
 *
 * This test also demonstrates the use of {@link EdmDecorator}.
 */
public class EdmxFormatWriterTest extends AbstractEdmxFormatWriterTest {

  @Override
  public void hostODataServer(String baseUri) {
    JerseyProducerUtil.hostODataServer(baseUri);
  }

  @Override
  public ODataServer startODataServer(String baseUri) {
    return JerseyProducerUtil.startODataServer(baseUri);
  }

  @Override
  public <T> String getWebResource(String uri, Class<T> c) {
    WebResource webResource = new Client().resource(endpointUri + "$metadata");

    String metadata = webResource.get(
        String.class);
    return metadata;
  }

}