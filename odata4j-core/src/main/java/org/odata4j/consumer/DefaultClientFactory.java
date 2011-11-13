package org.odata4j.consumer;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;

/**
 * The default factory for jersey clients.
 *
 * <p>Use {@link #INSTANCE} to obtain a reference to the singleton implementation of this factory.</p>
 */
public class DefaultClientFactory implements ClientFactory {

  public static final DefaultClientFactory INSTANCE = new DefaultClientFactory();

  private DefaultClientFactory() {}

  @Override
  public Client createClient(ClientConfig clientConfig) {
    return Client.create(clientConfig);
  }

}
