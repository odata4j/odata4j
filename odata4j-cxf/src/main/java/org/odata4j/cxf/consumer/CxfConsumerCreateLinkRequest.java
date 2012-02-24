package org.odata4j.cxf.consumer;

import org.core4j.Enumerable;
import org.odata4j.consumer.ODataClientRequest;
import org.odata4j.core.OEntityId;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.format.FormatType;

class CxfConsumerCreateLinkRequest extends CxfConsumerEntityRequestBase<Void> {

  private final String targetNavProp;
  private final OEntityId targetEntity;

  CxfConsumerCreateLinkRequest(FormatType formatType, String serviceRootUri,
      EdmDataServices metadata, OEntityId sourceEntity, String targetNavProp, OEntityId targetEntity) {
    super(formatType, serviceRootUri, metadata, sourceEntity.getEntitySetName(), sourceEntity.getEntityKey());
    this.targetNavProp = targetNavProp;
    this.targetEntity = targetEntity;
  }

  @Override
  public Void execute() {
    ODataCxfClient client = new ODataCxfClient(this.getFormatType());
    try {
      String path = Enumerable.create(getSegments()).join("/");
      path = CxfConsumerQueryLinksRequest.linksPath(targetNavProp, null).apply(path);

      ODataClientRequest request = ODataClientRequest.post(getServiceRootUri() + path, toSingleLink(targetEntity));
      client.createLink(request);
      return null;
    } finally {
      client.shuttdown();
    }
  }

}
