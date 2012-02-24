package org.odata4j.cxf.consumer;

import org.core4j.Enumerable;
import org.odata4j.consumer.ODataClientRequest;
import org.odata4j.core.OEntityId;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.format.FormatType;

class CxfConsumerUpdateLinkRequest extends CxfConsumerEntityRequestBase<Void> {

  private final String targetNavProp;
  private final Object[] oldTargetKeyValues;
  private final OEntityId newTargetEntity;

  CxfConsumerUpdateLinkRequest(FormatType formatType, String serviceRootUri,
      EdmDataServices metadata, OEntityId sourceEntity, OEntityId newTargetEntity, String targetNavProp, Object... oldTargetKeyValues) {
    super(formatType, serviceRootUri, metadata, sourceEntity.getEntitySetName(), sourceEntity.getEntityKey());
    this.targetNavProp = targetNavProp;
    this.oldTargetKeyValues = oldTargetKeyValues;
    this.newTargetEntity = newTargetEntity;
  }

  @Override
  public Void execute() {
    ODataCxfClient client = new ODataCxfClient(this.getFormatType());
    try {
      String path = Enumerable.create(getSegments()).join("/");
      path = CxfConsumerQueryLinksRequest.linksPath(targetNavProp, oldTargetKeyValues).apply(path);

      ODataClientRequest request = ODataClientRequest.put(getServiceRootUri() + path, toSingleLink(newTargetEntity));
      client.updateLink(request);
      return null;
    } finally {
      client.shuttdown();
    }
  }

}