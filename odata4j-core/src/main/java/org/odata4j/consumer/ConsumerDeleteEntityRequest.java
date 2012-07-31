package org.odata4j.consumer;

import org.core4j.Enumerable;
import org.odata4j.core.OEntityKey;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.exceptions.ODataProducerException;

public class ConsumerDeleteEntityRequest extends ConsumerEntityRequestBase<Void> {

  public ConsumerDeleteEntityRequest(ODataClient client, String serviceRootUri,
      EdmDataServices metadata, String entitySetName, OEntityKey key) {
    super(client, serviceRootUri, metadata, entitySetName, key);
  }

  @Override
  public Void execute() throws ODataProducerException {
    String path = Enumerable.create(getSegments()).join("/");
    ODataClientRequest request = ODataClientRequest.delete(getServiceRootUri() + path);
    getClient().deleteEntity(request);
    return null;
  }

}
