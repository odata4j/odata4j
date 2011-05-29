package org.odata4j.consumer;

import org.core4j.Enumerable;
import org.odata4j.core.OEntityId;
import org.odata4j.core.OEntityIds;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.format.SingleLinks;

class ConsumerCreateLinkRequest extends ConsumerEntityRequestBase<Void> {

  private final String targetNavProp;
  private final OEntityId targetEntity;
  
  public ConsumerCreateLinkRequest(ODataClient client, String serviceRootUri,
      EdmDataServices metadata, OEntityId sourceEntity, String targetNavProp, OEntityId targetEntity) {
    super(client, serviceRootUri, metadata, sourceEntity.getEntitySet().name, sourceEntity.getEntityKey());
    this.targetNavProp = targetNavProp;
    this.targetEntity = targetEntity;
  }

  @Override
  public Void execute() {
    String path = Enumerable.create(getSegments()).join("/");
    path = ConsumerQueryLinksRequest.linksPath(targetNavProp, null).apply(path);
    
    String uri = getServiceRootUri();
    if (!uri.endsWith("/"))
      uri += "/";
    uri += OEntityIds.toKeyString(targetEntity);
    
    ODataClientRequest request = ODataClientRequest.post(getServiceRootUri() + path, SingleLinks.create(uri));
    getClient().createLink(request);
    return null;
  }

}
