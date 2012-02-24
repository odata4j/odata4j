package org.odata4j.cxf.consumer;

import org.core4j.Enumerable;
import org.odata4j.consumer.ODataClientRequest;
import org.odata4j.core.OEntityKey;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.format.FormatType;

class CxfConsumerDeleteEntityRequest extends CxfConsumerEntityRequestBase<Void> {

  CxfConsumerDeleteEntityRequest(FormatType formatType, String serviceRootUri,
      EdmDataServices metadata, String entitySetName, OEntityKey key) {
    super(formatType, serviceRootUri, metadata, entitySetName, key);
  }

  @Override
  public Void execute() {
    ODataCxfClient client = new ODataCxfClient(this.getFormatType());
    try {
      String path = Enumerable.create(getSegments()).join("/");
      ODataClientRequest request = ODataClientRequest.delete(getServiceRootUri() + path);
      client.deleteEntity(request);
      return null;
    } finally {
      client.shuttdown();
    }
  }

}
