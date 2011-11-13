package org.odata4j.core;

import org.odata4j.consumer.ODataClientRequest;

import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.filter.Filterable;

/**
 * Extension-point for modifying client http requests.
 * <p>The {@link OClientBehaviors} static factory class can be used to create built-in <code>OClientBehavior</code> instances.</p>
 */
public interface OClientBehavior {

  /**
   * Transforms the current http request.
   *
   * @param request  the current http request
   * @return the modified http request
   */
  ODataClientRequest transform(ODataClientRequest request);

  /**
   * Allows for modification of the jersey client api configuration.
   *
   * @param clientConfig  the jersey client api configuration
   */
  void modify(ClientConfig clientConfig);

  /**
   * Allows for modification of jersey filters at the client-level.
   *
   * @param client  the jersey {@link com.sun.jersey.api.client.Client}
   */
  void modifyClientFilters(Filterable client);

  /**
   * Allows for modification of jersey filters at the resource-level.
   *
   * @param webResource  the jersey {@link com.sun.jersey.api.client.WebResource}
   */
  void modifyWebResourceFilters(Filterable webResource);

}
