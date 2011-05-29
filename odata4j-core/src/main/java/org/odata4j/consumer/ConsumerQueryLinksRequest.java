package org.odata4j.consumer;

import org.core4j.Enumerable;
import org.core4j.Func1;
import org.odata4j.core.OEntityId;
import org.odata4j.core.OEntityIds;
import org.odata4j.core.OEntityKey;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.format.SingleLink;

class ConsumerQueryLinksRequest extends ConsumerQueryRequestBase<OEntityId> {

  private final String targetNavProp;
  
  protected ConsumerQueryLinksRequest(ODataClient client, String serviceRootUri, EdmDataServices metadata, OEntityId sourceEntity, String targetNavProp) {
    super(client, serviceRootUri, metadata, OEntityIds.toKeyString(sourceEntity));
    this.targetNavProp = targetNavProp;
  }

  public static Func1<String, String> linksPath(final String targetNavProp, final Object[] targetKeyValues) {
    return new Func1<String, String>(){
      public String apply(String input) {
        String keyString = targetKeyValues == null ? "" : OEntityKey.create(targetKeyValues).toKeyString();
        return input + "/$links/" + targetNavProp + keyString;
      }};
  }
  
  @Override
  public Enumerable<OEntityId> execute() {
    ODataClientRequest request = buildRequest(linksPath(targetNavProp, null));
    return Enumerable.create(getClient().getLinks(request)).select(new Func1<SingleLink, OEntityId>() {
      @Override
      public OEntityId apply(SingleLink link) {
        return OEntityIds.parse(getMetadata(), getServiceRootUri(), link.getUri());
      }
    });
  }
}
