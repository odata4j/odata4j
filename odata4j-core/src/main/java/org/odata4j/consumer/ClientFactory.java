package org.odata4j.consumer;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;

public interface ClientFactory {

  /**
   * Creates a jersey client.
   *
   * @param clientConfig  the jersey client api configuration
   */
  Client createClient(ClientConfig clientConfig);

}
