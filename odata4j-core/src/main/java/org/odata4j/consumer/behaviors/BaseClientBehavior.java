package org.odata4j.consumer.behaviors;

import org.odata4j.consumer.ODataClientRequest;
import org.odata4j.core.OClientBehavior;

import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.filter.Filterable;

public abstract class BaseClientBehavior implements OClientBehavior {

  @Override
  public ODataClientRequest transform(ODataClientRequest request) {
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
