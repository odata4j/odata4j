package org.odata4j.jersey.consumer;

import org.core4j.Enumerable;
import org.odata4j.core.OEntityId;
import org.odata4j.edm.EdmDataServices;

class ConsumerUpdateLinkRequest extends ConsumerEntityRequestBase<Void> {

  private final String targetNavProp;
  private final Object[] oldTargetKeyValues;
  private final OEntityId newTargetEntity;

  ConsumerUpdateLinkRequest(ODataJerseyClient client, String serviceRootUri,
      EdmDataServices metadata, OEntityId sourceEntity, OEntityId newTargetEntity, String targetNavProp, Object... oldTargetKeyValues) {
    super(client, serviceRootUri, metadata, sourceEntity.getEntitySetName(), sourceEntity.getEntityKey());
    this.targetNavProp = targetNavProp;
    this.oldTargetKeyValues = oldTargetKeyValues;
    this.newTargetEntity = newTargetEntity;
  }

  @Override
  public Void execute() {
    String path = Enumerable.create(getSegments()).join("/");
    path = ConsumerQueryLinksRequest.linksPath(targetNavProp, oldTargetKeyValues).apply(path);

    ODataClientRequest request = ODataClientRequest.put(getServiceRootUri() + path, toSingleLink(newTargetEntity));
    getClient().updateLink(request);
    return null;
  }

}