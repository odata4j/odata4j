package org.odata4j.consumer;

import org.core4j.Enumerable;
import org.core4j.Func1;
import org.odata4j.core.OEntityId;
import org.odata4j.core.OEntityIds;
import org.odata4j.edm.EdmDataServices;

class ConsumerQueryLinksRequest extends ConsumerQueryRequestBase<OEntityId> {

  protected ConsumerQueryLinksRequest(ODataClient client, String serviceRootUri, EdmDataServices metadata, String lastSegment) {
    super(client, serviceRootUri, metadata, lastSegment);
  }

  @Override
  public Enumerable<OEntityId> execute() {
    ODataClientRequest request = buildRequest();
    return Enumerable.create(getClient().getLinkUris(request)).select(new Func1<String, OEntityId>() {
      @Override
      public OEntityId apply(String uri) {
        return OEntityIds.parse(getMetadata(), getServiceRootUri(), uri);
      }
    });
  }
}
