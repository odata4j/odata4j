package org.odata4j.jersey.consumer;

import org.core4j.Enumerable;
import org.odata4j.core.OEntityKey;
import org.odata4j.edm.EdmDataServices;

class ConsumerDeleteEntityRequest extends ConsumerEntityRequestBase<Void> {

  ConsumerDeleteEntityRequest(ODataJerseyClient client, String serviceRootUri,
      EdmDataServices metadata, String entitySetName, OEntityKey key) {
    super(client, serviceRootUri, metadata, entitySetName, key);
  }

  @Override
  public Void execute() {
    String path = Enumerable.create(getSegments()).join("/");
    ODataJerseyClientRequest request = ODataJerseyClientRequest.delete(getServiceRootUri() + path);
    getClient().deleteEntity(request);
    return null;
  }

}
