package org.odata4j.consumer;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;

/**
 * The default factory implementation for Jersey clients.
 *
 * <p>Use {@link #INSTANCE} to obtain a reference to the singleton instance of this factory.</p>
 */
public class DefaultClientFactory implements ClientFactory {

  public static final DefaultClientFactory INSTANCE = new DefaultClientFactory();

  private DefaultClientFactory() {}

  /**
   * Creates a new default {@link Client} by calling: <code>Client.create(clientConfig)</code>
   */
  @Override
  public Client createClient(ClientConfig clientConfig) {
    return Client.create(clientConfig);
  }

}
