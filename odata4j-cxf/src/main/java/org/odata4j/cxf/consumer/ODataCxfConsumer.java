package org.odata4j.cxf.consumer;

import java.util.HashMap;
import java.util.Map;

import org.core4j.Enumerable;
import org.odata4j.consumer.AbstractODataConsumer;
import org.odata4j.consumer.ODataClientRequest;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.consumer.behaviors.OClientBehavior;
import org.odata4j.core.EntitySetInfo;
import org.odata4j.core.OCreateRequest;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityGetRequest;
import org.odata4j.core.OEntityId;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OEntityRequest;
import org.odata4j.core.OFunctionRequest;
import org.odata4j.core.OModifyRequest;
import org.odata4j.core.OObject;
import org.odata4j.core.OQueryRequest;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmProperty;
import org.odata4j.format.FormatType;
import org.odata4j.internal.EdmDataServicesDecorator;
import org.odata4j.internal.FeedCustomizationMapping;

/**
 * <code>ODataConsumer</code> is the client-side interface to an OData service.
 *
 * <p>Use {@link #create(String)} or one of the other static factory methods to connect to an existing OData service.</p>
 */
public class ODataCxfConsumer extends AbstractODataConsumer {

  private final Map<String, FeedCustomizationMapping> cachedMappings = new HashMap<String, FeedCustomizationMapping>();
  private EdmDataServices cachedMetadata;
  private FormatType formatType;
  private OClientBehavior[] clientBehaviors = new OClientBehavior[] {};

  public static class Builder {

    private FormatType formatType;
    private String serviceRootUri;
    private OClientBehavior[] clientBehaviors;

    private Builder(String serviceRootUri) {
      this.serviceRootUri = serviceRootUri;
      this.formatType = FormatType.ATOM;
    }

    /**
     * Sets a preferred {@link FormatType}. Defaults to {@code FormatType.ATOM}.
     *
     * @param formatType  the format type
     * @return this builder
     */
    public Builder setFormatType(FormatType formatType) {
      this.formatType = formatType;
      return this;
    }

    /**
     * Sets one or more client behaviors.
     *
     * <p>Client behaviors transform http requests to interact with services that require custom extensions.
     *
     * @param clientBehaviors  the client behaviors
     * @return this builder
     */
    public Builder setClientBehaviors(OClientBehavior... clientBehaviors) {
      this.clientBehaviors = clientBehaviors;
      return this;
    }

    /**
     * Builds the {@link ODataCxfConsumer} object.
     *
     * @return a new OData consumer
     */
    public ODataCxfConsumer build() {
      ODataCxfConsumer consumer;

      if (this.clientBehaviors != null) {
        consumer = new ODataCxfConsumer(this.formatType, this.serviceRootUri, this.clientBehaviors);
      } else {
        consumer = new ODataCxfConsumer(this.formatType, this.serviceRootUri);
      }

      return consumer;
    }
  }

  public ODataCxfConsumer(FormatType formatType, String serviceRootUri) {
    super(serviceRootUri);
    this.formatType = formatType;
  }

  public ODataCxfConsumer(FormatType formatType, String serviceRootUri, OClientBehavior[] clientBehaviors) {
    this(formatType, serviceRootUri);
    this.clientBehaviors = clientBehaviors;

  }

  @Override
  public Enumerable<EntitySetInfo> getEntitySets() {
    ODataCxfClient client = new ODataCxfClient(this.formatType, this.clientBehaviors);
    ODataClientRequest request = ODataClientRequest.get(this.getServiceRootUri());
    Enumerable<EntitySetInfo> result = Enumerable.create(client.getCollections(request)).cast(EntitySetInfo.class);
    client.shuttdown();
    return result;
  }

  @Override
  public EdmDataServices getMetadata() {
    if (cachedMetadata == null)
      cachedMetadata = new CachedEdmDataServices();
    return cachedMetadata;
  }

  @Override
  public <T> OQueryRequest<T> getEntities(Class<T> entityType, String entitySetHref) {
    FeedCustomizationMapping mapping = this.getFeedCustomizationMapping(entitySetHref);
    CxFConsumerQueryEntitiesRequest<T> result = new CxFConsumerQueryEntitiesRequest<T>(this.formatType, entityType, this.getServiceRootUri(), getMetadata(), entitySetHref, mapping);
    return result;
  }

  @Override
  public <T> OEntityGetRequest<T> getEntity(Class<T> entityType, String entitySetName, OEntityKey key) {
    FeedCustomizationMapping mapping = getFeedCustomizationMapping(entitySetName);
    return new CxfConsumerGetEntityRequest<T>(this.formatType,
        entityType, this.getServiceRootUri(), getMetadata(),
        entitySetName, OEntityKey.create(key), mapping);
  }

  @Override
  public OQueryRequest<OEntityId> getLinks(OEntityId sourceEntity, String targetNavProp) {
    return new CxfConsumerQueryLinksRequest(this.formatType, this.getServiceRootUri(), getMetadata(), sourceEntity, targetNavProp);
  }

  @Override
  public OEntityRequest<Void> createLink(OEntityId sourceEntity, String targetNavProp, OEntityId targetEntity) {
    return new CxfConsumerCreateLinkRequest(this.formatType, this.getServiceRootUri(), getMetadata(), sourceEntity, targetNavProp, targetEntity);
  }

  @Override
  public OEntityRequest<Void> deleteLink(OEntityId sourceEntity, String targetNavProp, Object... targetKeyValues) {
    return new CxfConsumerDeleteLinkRequest(this.formatType, this.getServiceRootUri(), getMetadata(), sourceEntity, targetNavProp, targetKeyValues);
  }

  @Override
  public OEntityRequest<Void> updateLink(OEntityId sourceEntity, OEntityId newTargetEntity, String targetNavProp, Object... oldTargetKeyValues) {
    return new CxfConsumerUpdateLinkRequest(this.formatType, this.getServiceRootUri(), getMetadata(), sourceEntity, newTargetEntity, targetNavProp, oldTargetKeyValues);
  }

  @Override
  public OCreateRequest<OEntity> createEntity(String entitySetName) {
    FeedCustomizationMapping mapping = getFeedCustomizationMapping(entitySetName);
    return new CxfConsumerCreateEntityRequest<OEntity>(this.formatType, this.getServiceRootUri(), getMetadata(),
        entitySetName, mapping);
  }

  @Override
  public OModifyRequest<OEntity> updateEntity(OEntity entity) {
    return new CxfConsumerEntityModificationRequest<OEntity>(entity, this.formatType, this.getServiceRootUri(), getMetadata(),
        entity.getEntitySet().getName(), entity.getEntityKey());
  }

  @Override
  public OModifyRequest<OEntity> mergeEntity(OEntity entity) {
    return mergeEntity(entity.getEntitySet().getName(), entity.getEntityKey());
  }

  @Override
  public OModifyRequest<OEntity> mergeEntity(String entitySetName, Object keyValue) {
    return mergeEntity(entitySetName, OEntityKey.create(keyValue));
  }

  @Override
  public OModifyRequest<OEntity> mergeEntity(String entitySetName, OEntityKey key) {
    return new CxfConsumerEntityModificationRequest<OEntity>(null, this.formatType, this.getServiceRootUri(),
        getMetadata(), entitySetName, key);
  }

  @Override
  public OEntityRequest<Void> deleteEntity(OEntityId entity) {
    return deleteEntity(entity.getEntitySetName(), entity.getEntityKey());
  }

  @Override
  public OEntityRequest<Void> deleteEntity(String entitySetName, Object keyValue) {
    return deleteEntity(entitySetName, OEntityKey.create(keyValue));
  }

  @Override
  public OEntityRequest<Void> deleteEntity(String entitySetName, OEntityKey key) {
    return new CxfConsumerDeleteEntityRequest(this.formatType, this.getServiceRootUri(), getMetadata(), entitySetName, key);
  }

  @Override
  public OFunctionRequest<OObject> callFunction(String functionName) {
    return new CxfConsumerFunctionCallRequest<OObject>(this.formatType, this.getServiceRootUri(), getMetadata(), functionName);
  }

  public static ODataConsumer create(String serviceRootUri) {
    return ODataCxfConsumer.newBuilder(serviceRootUri).build();
  }

  public static Builder newBuilder(String serviceRootUri) {
    return new Builder(serviceRootUri);
  }

  private FeedCustomizationMapping getFeedCustomizationMapping(String entitySetName) {
    if (!this.cachedMappings.containsKey(entitySetName)) {
      FeedCustomizationMapping rt = new FeedCustomizationMapping();
      EdmDataServices metadata = getMetadata();
      if (metadata != null) {
        EdmEntitySet ees = metadata.findEdmEntitySet(entitySetName);
        if (ees == null) {
          rt = null;
        } else {
          EdmEntityType eet = ees.getType();
          for (EdmProperty ep : eet.getProperties()) {
            if ("SyndicationTitle".equals(ep.getFcTargetPath()) && "false".equals(ep.getFcKeepInContent()))
              rt.titlePropName = ep.getName();
            if ("SyndicationSummary".equals(ep.getFcTargetPath()) && "false".equals(ep.getFcKeepInContent()))
              rt.summaryPropName = ep.getName();
          }
        }
      }
      this.cachedMappings.put(entitySetName, rt);
    }
    return this.cachedMappings.get(entitySetName);
  }

  private class CachedEdmDataServices extends EdmDataServicesDecorator {

    private EdmDataServices delegate;

    public CachedEdmDataServices() {}

    @Override
    protected EdmDataServices getDelegate() {
      if (delegate == null)
        refreshDelegate();
      return delegate;
    }

    private void refreshDelegate() {
      ODataClientRequest request = ODataClientRequest.get(ODataCxfConsumer.this.getServiceRootUri() + "$metadata");
      ODataCxfClient client = new ODataCxfClient(ODataCxfConsumer.this.formatType, ODataCxfConsumer.this.clientBehaviors);
      EdmDataServices metadata = client.getMetadata(request);
      client.shuttdown();
      delegate = metadata == null ? EdmDataServices.EMPTY : metadata;
    }

    @Override
    public EdmEntitySet findEdmEntitySet(String entitySetName) {
      EdmEntitySet rt = super.findEdmEntitySet(entitySetName);
      if (rt == null) {
        refreshDelegate();
        rt = super.findEdmEntitySet(entitySetName);
      }
      return rt;
    }
  }

}
