package org.odata4j.jersey.consumer.behaviors;

import org.odata4j.jersey.consumer.ODataJerseyClientRequest;

import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.filter.Filterable;

public abstract class BaseClientBehavior implements OClientBehavior {

  @Override
  public ODataJerseyClientRequest transform(ODataJerseyClientRequest request) {
    return request;
  }

  @Override
  public void modify(ClientConfig clientConfig) {
    // noop
  }

  @Override
  public void modifyClientFilters(Filterable client) {
    // noop
  }

  @Override
  public void modifyWebResourceFilters(Filterable webResource) {
    // noop
  }

}
