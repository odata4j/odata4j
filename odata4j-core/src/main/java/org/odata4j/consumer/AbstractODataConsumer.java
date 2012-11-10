package org.odata4j.consumer;

import org.core4j.Enumerable;
import org.odata4j.core.EntitySetInfo;
import org.odata4j.core.OCountRequest;
import org.odata4j.core.OCreateRequest;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityDeleteRequest;
import org.odata4j.core.OEntityGetRequest;
import org.odata4j.core.OEntityId;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OEntityRequest;
import org.odata4j.core.OFunctionRequest;
import org.odata4j.core.OModifyRequest;
import org.odata4j.core.OObject;
import org.odata4j.core.OQueryRequest;
import org.odata4j.core.ORelatedEntitiesLink;
import org.odata4j.core.ORelatedEntityLink;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.exceptions.ODataProducerException;
import org.odata4j.internal.EdmDataServicesDecorator;

/**
 * Useful base class for {@link ODataConsumer} implementations with common functionality.
 */
public abstract class AbstractODataConsumer implements ODataConsumer {

  private final String serviceRootUri;
  private EdmDataServices cachedMetadata;

  protected AbstractODataConsumer(String serviceRootUri) {
    if (!serviceRootUri.endsWith("/"))
      serviceRootUri = serviceRootUri + "/";

    this.serviceRootUri = serviceRootUri;
  }

  public String getServiceRootUri() {
    return serviceRootUri;
  }

  public Enumerable<EntitySetInfo> getEntitySets() throws ODataProducerException {
    ODataClientRequest request = ODataClientRequest.get(getServiceRootUri());
    return Enumerable.create(getClient().getCollections(request)).cast(EntitySetInfo.class);
  }

  public EdmDataServices getMetadata() {
    if (cachedMetadata == null)
      cachedMetadata = new CachedEdmDataServices();
    return cachedMetadata;
  }

  public OQueryRequest<OEntity> getEntities(ORelatedEntitiesLink link) {
    ParsedHref parsed = ParsedHref.parse(link.getHref());
    return getEntities(parsed.entitySetName).nav(parsed.entityKey, parsed.navProperty);
  }

  public OQueryRequest<OEntity> getEntities(String entitySetHref) {
    return getEntities(OEntity.class, entitySetHref);
  }

  public <T> OQueryRequest<T> getEntities(Class<T> entityType, String entitySetHref) {
    return new ConsumerQueryEntitiesRequest<T>(getClient(), entityType, getServiceRootUri(), getMetadata(), entitySetHref);
  }

  public OEntityGetRequest<OEntity> getEntity(ORelatedEntityLink link) {
    ParsedHref parsed = ParsedHref.parse(link.getHref());
    return (OEntityGetRequest<OEntity>) getEntity(parsed.entitySetName, parsed.entityKey).nav(parsed.navProperty);
  }

  public OEntityGetRequest<OEntity> getEntity(String entitySetName, Object keyValue) {
    return getEntity(entitySetName, OEntityKey.create(keyValue));
  }

  public OEntityGetRequest<OEntity> getEntity(OEntity entity) {
    return getEntity(entity.getEntitySet().getName(), entity.getEntityKey());
  }

  public OEntityGetRequest<OEntity> getEntity(String entitySetName, OEntityKey key) {
    return getEntity(OEntity.class, entitySetName, key);
  }

  public <T> OEntityGetRequest<T> getEntity(Class<T> entityType, String entitySetName, Object keyValue) {
    return getEntity(entityType, entitySetName, OEntityKey.create(keyValue));
  }

  public <T> OEntityGetRequest<T> getEntity(Class<T> entityType, String entitySetName, OEntityKey key) {
    return new ConsumerGetEntityRequest<T>(getClient(), entityType, getServiceRootUri(), getMetadata(), entitySetName, OEntityKey.create(key));
  }

  public OQueryRequest<OEntityId> getLinks(OEntityId sourceEntity, String targetNavProp) {
    return new ConsumerQueryLinksRequest(getClient(), getServiceRootUri(), getMetadata(), sourceEntity, targetNavProp);
  }

  public OEntityRequest<Void> createLink(OEntityId sourceEntity, String targetNavProp, OEntityId targetEntity) {
    return new ConsumerCreateLinkRequest(getClient(), getServiceRootUri(), getMetadata(), sourceEntity, targetNavProp, targetEntity);
  }

  public OEntityRequest<Void> deleteLink(OEntityId sourceEntity, String targetNavProp, Object... targetKeyValues) {
    return new ConsumerDeleteLinkRequest(getClient(), getServiceRootUri(), getMetadata(), sourceEntity, targetNavProp, targetKeyValues);
  }

  public OEntityRequest<Void> updateLink(OEntityId sourceEntity, OEntityId newTargetEntity, String targetNavProp, Object... oldTargetKeyValues) {
    return new ConsumerUpdateLinkRequest(getClient(), getServiceRootUri(), getMetadata(), sourceEntity, newTargetEntity, targetNavProp, oldTargetKeyValues);
  }

  public OCreateRequest<OEntity> createEntity(String entitySetName) {
    return new ConsumerCreateEntityRequest<OEntity>(getClient(), getServiceRootUri(), getMetadata(), entitySetName);
  }

  public OModifyRequest<OEntity> updateEntity(OEntity entity) {
    return new ConsumerEntityModificationRequest<OEntity>(entity, getClient(), getServiceRootUri(), getMetadata(),
        entity.getEntitySet().getName(), entity.getEntityKey(), entity.getEntityTag());
  }

  public OModifyRequest<OEntity> mergeEntity(OEntity entity) {
    return mergeEntity(entity.getEntitySet().getName(), entity.getEntityKey(), entity.getEntityTag());
  }

  public OModifyRequest<OEntity> mergeEntity(String entitySetName, Object keyValue) {
    return mergeEntity(entitySetName, OEntityKey.create(keyValue));
  }

  public OModifyRequest<OEntity> mergeEntity(String entitySetName, OEntityKey key) {
    return mergeEntity(entitySetName, key, null);
  }

  public OModifyRequest<OEntity> mergeEntity(String entitySetName, OEntityKey key, String entityTag) {
    return new ConsumerEntityModificationRequest<OEntity>(null, getClient(), getServiceRootUri(), getMetadata(), entitySetName, key, entityTag);
  }

  public OEntityDeleteRequest deleteEntity(OEntity entity) {
    return new ConsumerDeleteEntityRequest(getClient(), getServiceRootUri(), getMetadata(),
        entity.getEntitySetName(), entity.getEntityKey(), entity.getEntityTag());
  }

  public OEntityDeleteRequest deleteEntity(String entitySetName, Object keyValue) {
    return deleteEntity(entitySetName, OEntityKey.create(keyValue));
  }

  public OEntityDeleteRequest deleteEntity(String entitySetName, OEntityKey key) {
    return new ConsumerDeleteEntityRequest(getClient(), getServiceRootUri(), getMetadata(), entitySetName, key, null);
  }

  public OFunctionRequest<OObject> callFunction(String functionName) {
    return new ConsumerFunctionCallRequest<OObject>(getClient(), getServiceRootUri(), getMetadata(), functionName);
  }

  public OCountRequest getEntitiesCount(String entitySetName) {
    return new ConsumerCountRequest(getClient(), getServiceRootUri()).entitySetName(entitySetName);
  }

  protected abstract ODataClient getClient();

  private static class ParsedHref {
    public String entitySetName;
    public OEntityKey entityKey;
    public String navProperty;

    private ParsedHref() {}

    public static ParsedHref parse(String href) {
      // href: entityset(keyvalue[,keyvalue])/navprop[/navprop]
      // keyvalue: <literal> for one key value -or- <name=literal> for multiple key values

      int slashIndex = href.indexOf('/');
      String head = href.substring(0, slashIndex);
      String navProperty = href.substring(slashIndex + 1);

      int pIndex = head.indexOf('(');
      String entitySetName = head.substring(0, pIndex);

      String keyString = head.substring(pIndex + 1, head.length() - 1); // keyvalue[,keyvalue]

      ParsedHref rt = new ParsedHref();
      rt.entitySetName = entitySetName;
      rt.entityKey = OEntityKey.parse(keyString);
      rt.navProperty = navProperty;
      return rt;
    }
  }

  private class CachedEdmDataServices extends EdmDataServicesDecorator {

    private EdmDataServices delegate;

    private CachedEdmDataServices() {}

    @Override
    protected EdmDataServices getDelegate() {
      if (delegate == null)
        refreshDelegate();
      return delegate;
    }

    private void refreshDelegate() {
      ODataClientRequest request = ODataClientRequest.get(AbstractODataConsumer.this.getServiceRootUri() + "$metadata");
      try {
        delegate = AbstractODataConsumer.this.getClient().getMetadata(request);
      } catch (ODataProducerException e) {
        // to support services that do not expose metadata information
        delegate = EdmDataServices.EMPTY;
      }
    }

    @Override
    public EdmEntitySet findEdmEntitySet(String entitySetName) {
      EdmEntitySet rt = super.findEdmEntitySet(entitySetName);
      if (rt == null && delegate != EdmDataServices.EMPTY) {
        refreshDelegate();
        rt = super.findEdmEntitySet(entitySetName);
      }
      return rt;
    }
  }

}
