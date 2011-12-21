package org.odata4j.jersey.consumer;

import java.util.HashMap;
import java.util.Map;

import org.core4j.Enumerable;
import org.odata4j.consumer.AbstractODataConsumer;
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
import org.odata4j.core.ORelatedEntitiesLink;
import org.odata4j.core.ORelatedEntityLink;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmProperty;
import org.odata4j.format.FormatType;
import org.odata4j.internal.EdmDataServicesDecorator;
import org.odata4j.internal.FeedCustomizationMapping;
import org.odata4j.jersey.consumer.behaviors.OClientBehavior;

/**
 * <code>ODataConsumer</code> is the client-side interface to an OData service.
 *
 * <p>Use {@link #create(String)} or one of the other static factory methods to connect to an existing OData service.</p>
 */
public class ODataJerseyConsumer extends AbstractODataConsumer {

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

  private final Map<String, FeedCustomizationMapping> cachedMappings = new HashMap<String, FeedCustomizationMapping>();
  private final String serviceRootUri;
  private final ODataClient client;

  private EdmDataServices cachedMetadata;

  private ODataJerseyConsumer(FormatType type, String serviceRootUri, ClientFactory clientFactory, OClientBehavior... behaviors) {
    if (!serviceRootUri.endsWith("/"))
      serviceRootUri = serviceRootUri + "/";

    this.serviceRootUri = serviceRootUri;
    this.client = new ODataClient(type, clientFactory, behaviors);
  }

  /* (non-Javadoc)
   * @see org.odata4j.jersey.consumer.ODataConsumer#getServiceRootUri()
   */
  @Override
  public String getServiceRootUri() {
    return serviceRootUri;
  }

  /**
   * Builder for {@link ODataJerseyConsumer} objects.
   */
  public static class Builder {

    private FormatType formatType;
    private String serviceRootUri;
    private ClientFactory clientFactory;
    private OClientBehavior[] clientBehaviors;

    private Builder(String serviceRootUri) {
      this.serviceRootUri = serviceRootUri;
      this.formatType = FormatType.ATOM;
      this.clientFactory = DefaultClientFactory.INSTANCE;
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
     * Sets a specific {@link ClientFactory}.
     *
     * @param clientFactory  the jersey client factory
     * @return this builder
     */
    public Builder setClientFactory(ClientFactory clientFactory) {
      this.clientFactory = clientFactory;
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
     * Builds the {@link ODataJerseyConsumer} object.
     *
     * @return a new OData consumer
     */
    public ODataJerseyConsumer build() {
      if (this.clientBehaviors != null)
        return new ODataJerseyConsumer(this.formatType, this.serviceRootUri, this.clientFactory, this.clientBehaviors);
      else
        return new ODataJerseyConsumer(this.formatType, this.serviceRootUri, this.clientFactory);
    }
  }

  /**
   * Constructs a new builder for an {@link ODataJerseyConsumer} object.
   *
   * @param serviceRootUri  the OData service root uri
   */
  public static Builder newBuilder(String serviceRootUri) {
    return new Builder(serviceRootUri);
  }

  /**
   * Creates a new consumer for the given OData service uri.
   *
   * <p>Wrapper for {@code ODataConsumer.newBuilder(serviceRootUri).build()}.
   *
   * @param serviceRootUri  the service uri <p>e.g. <code>http://services.odata.org/Northwind/Northwind.svc/</code></p>
   * @return a new OData consumer
   */
  public static ODataJerseyConsumer create(String serviceRootUri) {
    return ODataJerseyConsumer.newBuilder(serviceRootUri).build();
  }

  /* (non-Javadoc)
   * @see org.odata4j.jersey.consumer.ODataConsumer#getEntitySets()
   */
  @Override
  public Enumerable<EntitySetInfo> getEntitySets() {
    ODataClientRequest request = ODataClientRequest.get(serviceRootUri);
    return Enumerable.create(client.getCollections(request)).cast(EntitySetInfo.class);
  }

  /* (non-Javadoc)
   * @see org.odata4j.jersey.consumer.ODataConsumer#getMetadata()
   */
  @Override
  public EdmDataServices getMetadata() {
    if (cachedMetadata == null)
      cachedMetadata = new CachedEdmDataServices();
    return cachedMetadata;
  }

  /* (non-Javadoc)
   * @see org.odata4j.jersey.consumer.ODataConsumer#getEntities(org.odata4j.core.ORelatedEntitiesLink)
   */
  @Override
  public OQueryRequest<OEntity> getEntities(ORelatedEntitiesLink link) {
    ParsedHref parsed = ParsedHref.parse(link.getHref());
    return getEntities(parsed.entitySetName).nav(parsed.entityKey, parsed.navProperty);
  }

  /* (non-Javadoc)
   * @see org.odata4j.jersey.consumer.ODataConsumer#getEntities(java.lang.String)
   */
  @Override
  public OQueryRequest<OEntity> getEntities(String entitySetHref) {
    return getEntities(OEntity.class, entitySetHref);
  }

  /* (non-Javadoc)
   * @see org.odata4j.jersey.consumer.ODataConsumer#getEntities(java.lang.Class, java.lang.String)
   */
  @Override
  public <T> OQueryRequest<T> getEntities(Class<T> entityType, String entitySetHref) {
    FeedCustomizationMapping mapping = getFeedCustomizationMapping(entitySetHref);
    return new ConsumerQueryEntitiesRequest<T>(client, entityType, serviceRootUri, getMetadata(), entitySetHref, mapping);
  }

  /* (non-Javadoc)
   * @see org.odata4j.jersey.consumer.ODataConsumer#getEntity(org.odata4j.core.ORelatedEntityLink)
   */
  @Override
  public OEntityGetRequest<OEntity> getEntity(ORelatedEntityLink link) {
    ParsedHref parsed = ParsedHref.parse(link.getHref());
    return (OEntityGetRequest<OEntity>) getEntity(parsed.entitySetName, parsed.entityKey).nav(parsed.navProperty);
  }

  /* (non-Javadoc)
   * @see org.odata4j.jersey.consumer.ODataConsumer#getEntity(java.lang.String, java.lang.Object)
   */
  @Override
  public OEntityGetRequest<OEntity> getEntity(String entitySetName, Object keyValue) {
    return getEntity(entitySetName, OEntityKey.create(keyValue));
  }

  /* (non-Javadoc)
   * @see org.odata4j.jersey.consumer.ODataConsumer#getEntity(org.odata4j.core.OEntity)
   */
  @Override
  public OEntityGetRequest<OEntity> getEntity(OEntity entity) {
    return getEntity(entity.getEntitySet().getName(), entity.getEntityKey());
  }

  /* (non-Javadoc)
   * @see org.odata4j.jersey.consumer.ODataConsumer#getEntity(java.lang.String, org.odata4j.core.OEntityKey)
   */
  @Override
  public OEntityGetRequest<OEntity> getEntity(String entitySetName, OEntityKey key) {
    return getEntity(OEntity.class, entitySetName, key);
  }

  /* (non-Javadoc)
   * @see org.odata4j.jersey.consumer.ODataConsumer#getEntity(java.lang.Class, java.lang.String, java.lang.Object)
   */
  @Override
  public <T> OEntityGetRequest<T> getEntity(Class<T> entityType, String entitySetName, Object keyValue) {
    return getEntity(entityType, entitySetName, OEntityKey.create(keyValue));
  }

  /* (non-Javadoc)
   * @see org.odata4j.jersey.consumer.ODataConsumer#getEntity(java.lang.Class, java.lang.String, org.odata4j.core.OEntityKey)
   */
  @Override
  public <T> OEntityGetRequest<T> getEntity(Class<T> entityType, String entitySetName, OEntityKey key) {
    FeedCustomizationMapping mapping = getFeedCustomizationMapping(entitySetName);
    return new ConsumerGetEntityRequest<T>(client,
        entityType, serviceRootUri, getMetadata(),
        entitySetName, OEntityKey.create(key), mapping);
  }

  /* (non-Javadoc)
   * @see org.odata4j.jersey.consumer.ODataConsumer#getLinks(org.odata4j.core.OEntityId, java.lang.String)
   */
  @Override
  public OQueryRequest<OEntityId> getLinks(OEntityId sourceEntity, String targetNavProp) {
    return new ConsumerQueryLinksRequest(client, serviceRootUri, getMetadata(), sourceEntity, targetNavProp);
  }

  /* (non-Javadoc)
   * @see org.odata4j.jersey.consumer.ODataConsumer#createLink(org.odata4j.core.OEntityId, java.lang.String, org.odata4j.core.OEntityId)
   */
  @Override
  public OEntityRequest<Void> createLink(OEntityId sourceEntity, String targetNavProp, OEntityId targetEntity) {
    return new ConsumerCreateLinkRequest(client, serviceRootUri, getMetadata(), sourceEntity, targetNavProp, targetEntity);
  }

  /* (non-Javadoc)
   * @see org.odata4j.jersey.consumer.ODataConsumer#deleteLink(org.odata4j.core.OEntityId, java.lang.String, java.lang.Object)
   */
  @Override
  public OEntityRequest<Void> deleteLink(OEntityId sourceEntity, String targetNavProp, Object... targetKeyValues) {
    return new ConsumerDeleteLinkRequest(client, serviceRootUri, getMetadata(), sourceEntity, targetNavProp, targetKeyValues);
  }

  /* (non-Javadoc)
   * @see org.odata4j.jersey.consumer.ODataConsumer#updateLink(org.odata4j.core.OEntityId, org.odata4j.core.OEntityId, java.lang.String, java.lang.Object)
   */
  @Override
  public OEntityRequest<Void> updateLink(OEntityId sourceEntity, OEntityId newTargetEntity, String targetNavProp, Object... oldTargetKeyValues) {
    return new ConsumerUpdateLinkRequest(client, serviceRootUri, getMetadata(), sourceEntity, newTargetEntity, targetNavProp, oldTargetKeyValues);
  }

  /* (non-Javadoc)
   * @see org.odata4j.jersey.consumer.ODataConsumer#createEntity(java.lang.String)
   */
  @Override
  public OCreateRequest<OEntity> createEntity(String entitySetName) {
    FeedCustomizationMapping mapping = getFeedCustomizationMapping(entitySetName);
    return new ConsumerCreateEntityRequest<OEntity>(client, serviceRootUri, getMetadata(),
        entitySetName, mapping);
  }

  /* (non-Javadoc)
   * @see org.odata4j.jersey.consumer.ODataConsumer#updateEntity(org.odata4j.core.OEntity)
   */
  @Override
  public OModifyRequest<OEntity> updateEntity(OEntity entity) {
    return new ConsumerEntityModificationRequest<OEntity>(entity, client, serviceRootUri, getMetadata(),
        entity.getEntitySet().getName(), entity.getEntityKey());
  }

  /* (non-Javadoc)
   * @see org.odata4j.jersey.consumer.ODataConsumer#mergeEntity(org.odata4j.core.OEntity)
   */
  @Override
  public OModifyRequest<OEntity> mergeEntity(OEntity entity) {
    return mergeEntity(entity.getEntitySet().getName(), entity.getEntityKey());
  }

  /* (non-Javadoc)
   * @see org.odata4j.jersey.consumer.ODataConsumer#mergeEntity(java.lang.String, java.lang.Object)
   */
  @Override
  public OModifyRequest<OEntity> mergeEntity(String entitySetName, Object keyValue) {
    return mergeEntity(entitySetName, OEntityKey.create(keyValue));
  }

  /* (non-Javadoc)
   * @see org.odata4j.jersey.consumer.ODataConsumer#mergeEntity(java.lang.String, org.odata4j.core.OEntityKey)
   */
  @Override
  public OModifyRequest<OEntity> mergeEntity(String entitySetName, OEntityKey key) {
    return new ConsumerEntityModificationRequest<OEntity>(null, client, serviceRootUri,
        getMetadata(), entitySetName, key);
  }

  /* (non-Javadoc)
   * @see org.odata4j.jersey.consumer.ODataConsumer#deleteEntity(org.odata4j.core.OEntityId)
   */
  @Override
  public OEntityRequest<Void> deleteEntity(OEntityId entity) {
    return deleteEntity(entity.getEntitySetName(), entity.getEntityKey());
  }

  /* (non-Javadoc)
   * @see org.odata4j.jersey.consumer.ODataConsumer#deleteEntity(java.lang.String, java.lang.Object)
   */
  @Override
  public OEntityRequest<Void> deleteEntity(String entitySetName, Object keyValue) {
    return deleteEntity(entitySetName, OEntityKey.create(keyValue));
  }

  /* (non-Javadoc)
   * @see org.odata4j.jersey.consumer.ODataConsumer#deleteEntity(java.lang.String, org.odata4j.core.OEntityKey)
   */
  @Override
  public OEntityRequest<Void> deleteEntity(String entitySetName, OEntityKey key) {
    return new ConsumerDeleteEntityRequest(client, serviceRootUri, getMetadata(), entitySetName, key);
  }

  /* (non-Javadoc)
   * @see org.odata4j.jersey.consumer.ODataConsumer#callFunction(java.lang.String)
   */
  @Override
  public OFunctionRequest<OObject> callFunction(String functionName) {
    return new ConsumerFunctionCallRequest<OObject>(client, serviceRootUri, getMetadata(), functionName);
  }

  private FeedCustomizationMapping getFeedCustomizationMapping(String entitySetName) {
    if (!cachedMappings.containsKey(entitySetName)) {
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
      cachedMappings.put(entitySetName, rt);
    }
    return cachedMappings.get(entitySetName);
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
      ODataClientRequest request = ODataClientRequest.get(serviceRootUri + "$metadata");
      EdmDataServices metadata = client.getMetadata(request);
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
