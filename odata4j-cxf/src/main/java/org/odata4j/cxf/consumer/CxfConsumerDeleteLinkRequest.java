package org.odata4j.cxf.consumer;

import org.core4j.Enumerable;
import org.odata4j.consumer.ODataClientRequest;
import org.odata4j.core.OEntityId;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.format.FormatType;

class CxfConsumerDeleteLinkRequest extends CxfConsumerEntityRequestBase<Void> {

  private final String targetNavProp;
  private final Object[] targetKeyValues;

  CxfConsumerDeleteLinkRequest(FormatType formatType, String serviceRootUri,
      EdmDataServices metadata, OEntityId sourceEntity, String targetNavProp, Object... targetKeyValues) {
    super(formatType, serviceRootUri, metadata, sourceEntity.getEntitySetName(), sourceEntity.getEntityKey());
    this.targetNavProp = targetNavProp;
    this.targetKeyValues = targetKeyValues;
  }

  @Override
  public Void execute() {
    ODataCxfClient client = new ODataCxfClient(this.getFormatType());
    try {
      String path = Enumerable.create(getSegments()).join("/");
      path = CxfConsumerQueryLinksRequest.linksPath(targetNavProp, targetKeyValues).apply(path);
      ODataClientRequest request = ODataClientRequest.delete(getServiceRootUri() + path);
      client.deleteLink(request);
      return null;
    } finally {
      client.shuttdown();
    }
  }

}
