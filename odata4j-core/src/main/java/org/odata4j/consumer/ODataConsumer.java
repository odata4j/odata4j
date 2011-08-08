package org.odata4j.consumer;

import java.util.HashMap;
import java.util.Map;

import org.core4j.Enumerable;
import org.core4j.Func1;
import org.odata4j.core.OClientBehavior;
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
import org.odata4j.format.xml.AtomFeedFormatParser.CollectionInfo;
import org.odata4j.internal.EdmDataServicesDecorator;
import org.odata4j.internal.FeedCustomizationMapping;

/**
 * <code>ODataConsumer</code> is the client-side interface to an OData service.
 * 
 * <p>Use {@link #create(String)} or one of the other static factory methods to connect to an existing OData service.</p>
 */
public class ODataConsumer {

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

  /**
   * Sends http request and/or response information to standard out.  Useful for debugging.
   */
  public static enum Dump {
    /**
     * enum-as-singleton pattern
     */
    INSTANCE;
    private boolean requestHeaders;
    private boolean requestBody;
    private boolean responseHeaders;
    private boolean responseBody;

    /**
     * Sets whether or not to dump all http request and response information.
     */
    public void all(boolean dump) {
      requestHeaders(dump);
      requestBody(dump);
      responseHeaders(dump);
      responseBody(dump);
    }

    /**
     * Are http request headers currently dumped?
     */
    public boolean requestHeaders() {
      return requestHeaders;
    }

    /**
     * Sets whether or not to dump http request headers.
     */
    public void requestHeaders(boolean dump) {
      this.requestHeaders = dump;
    }

    /**
     * Are http request bodies currently dumped?
     */
    public boolean requestBody() {
      return requestBody;
    }

    /**
     * Sets whether or not to dump http request bodies.
     */
    public void requestBody(boolean dump) {
      this.requestBody = dump;
    }

    /**
     * Are http response headers currently dumped?
     */
    public boolean responseHeaders() {
      return responseHeaders;
    }

    /**
     * Sets whether or not to dump http response headers.
     */
    public void responseHeaders(boolean dump) {
      this.responseHeaders = dump;
    }

    /**
     * Are http response bodies currently dumped?
     */
    public boolean responseBody() {
      return responseBody;
    }

    /**
     * Sets whether or not to dump http response bodies.
     */
    public void responseBody(boolean dump) {
      this.responseBody = dump;
    }
  }

  /**
   * Sends http request and/or response information to standard out.  Useful for debugging.
   */
  public static final Dump dump = Dump.INSTANCE;

  private final Map<String, FeedCustomizationMapping> cachedMappings = new HashMap<String, FeedCustomizationMapping>();
  private final String serviceRootUri;
  private final ODataClient client;

  private EdmDataServices cachedMetadata;

  private ODataConsumer(FormatType type, String serviceRootUri, OClientBehavior... behaviors) {
    if (!serviceRootUri.endsWith("/"))
      serviceRootUri = serviceRootUri + "/";

    this.serviceRootUri = serviceRootUri;
    this.client = new ODataClient(type, behaviors);
  }

  /**
   * Gets the OData service uri.  
   * <p>e.g. <code>http://services.odata.org/Northwind/Northwind.svc/</code></p>
   * 
   * @return the service uri
   */
  public String getServiceRootUri() {
    return serviceRootUri;
  }

  /**
   * Creates a new consumer for the given OData service uri.
   * 
   * @param serviceRootUri  the service uri <p>e.g. <code>http://services.odata.org/Northwind/Northwind.svc/</code></p>
   * @return a new OData consumer
   */
  public static ODataConsumer create(String serviceRootUri) {
    return new ODataConsumer(FormatType.ATOM, serviceRootUri);
  }

  /**
   * Creates a new consumer for the given OData service uri, adding one or more client behaviors.  
   * Client behaviors transform http requests to interact with services that require custom extensions.
   * 
   * @param serviceRootUri  the service uri <p>e.g. <code>http://services.odata.org/Northwind/Northwind.svc/</code></p>
   * @param behaviors  one or more client behaviors
   * @return a new OData consumer
   */
  public static ODataConsumer create(String serviceRootUri, OClientBehavior... behaviors) {
    return new ODataConsumer(FormatType.ATOM, serviceRootUri, behaviors);
  }

  public static ODataConsumer create(FormatType preferredType, String serviceRootUri) {
    return new ODataConsumer(preferredType, serviceRootUri);
  }

  public static ODataConsumer create(FormatType preferredType, String serviceRootUri, OClientBehavior... behaviors) {
    return new ODataConsumer(preferredType, serviceRootUri, behaviors);
  }

  /**
   * Lists the names of all top-level entity-sets for the OData service.
   * 
   * @return the entity-set names
   */
  public Enumerable<String> getEntitySets() {
    ODataClientRequest request = ODataClientRequest.get(serviceRootUri);
    return Enumerable.create(client.getCollections(request)).select(new Func1<CollectionInfo, String>() {
      public String apply(CollectionInfo input) {
        return input.title;
      }
    });
  }

  /**
   * Gets the OData service metadata.
   * 
   * @return the service metadata
   * @see <a href="http://msdn.microsoft.com/en-us/library/dd541087(v=prot.10).aspx">[msdn] 2.2 &lt;edmx:DataServices&gt;</a>
   */
  public EdmDataServices getMetadata() {
    if (cachedMetadata == null)
      cachedMetadata = new CachedEdmDataServices();
    return cachedMetadata;
  }

  /**
   * Gets entities referred to by the given related-entities link.
   * <p>The query-request builder returned can be used for further server-side filtering.  Call {@link OQueryRequest#execute()} or simply iterate to issue request.</p>
   * 
   * @param link  the link
   * @return a new query-request builder
   */
  public OQueryRequest<OEntity> getEntities(ORelatedEntitiesLink link) {
    ParsedHref parsed = ParsedHref.parse(link.getHref());
    return getEntities(parsed.entitySetName).nav(parsed.entityKey, parsed.navProperty);
  }

  /**
   * Gets entities from the given entity-set.
   * <p>The query-request builder returned can be used for further server-side filtering.  Call {@link OQueryRequest#execute()} or simply iterate to issue request.</p>
   * 
   * @param entitySetName  the entity-set name
   * @return a new query-request builder
   */
  public OQueryRequest<OEntity> getEntities(String entitySetName) {
    return getEntities(OEntity.class, entitySetName);
  }

  /**
   * Gets entities from the given entity-set.  The entities will be represented as the given java-type.
   * <p>The query-request builder returned can be used for further server-side filtering.  Call {@link OQueryRequest#execute()} or simply iterate to issue request.</p>
   *  
   * @param <T>  the entity representation as a java type
   * @param entityType  the entity representation as a java type
   * @param entitySetName  the entity-set name
   * @return  a new query-request builder
   */
  public <T> OQueryRequest<T> getEntities(Class<T> entityType, String entitySetName) {
    FeedCustomizationMapping mapping = getFeedCustomizationMapping(entitySetName);
    return new ConsumerQueryEntitiesRequest<T>(client, entityType, serviceRootUri, getMetadata(), entitySetName, mapping);
  }

  /**
   * Gets the entity referred to by the given related entity link.
   * <p>The entity-request builder returned can be used for further navigation.  Call {@link OEntityRequest#execute()} to issue request.</p>
   * 
   * @param link  the link
   * @return a new entity-request builder
   */
  public OEntityGetRequest<OEntity> getEntity(ORelatedEntityLink link) {
    ParsedHref parsed = ParsedHref.parse(link.getHref());
    return (OEntityGetRequest<OEntity>)getEntity(parsed.entitySetName, parsed.entityKey).nav(parsed.navProperty);
  }

  /**
   * Gets the entity by entity-set name and entity-key value.
   * <p>The entity-request builder returned can be used for further navigation.  Call {@link OEntityRequest#execute()} to issue request.</p>
   * 
   * @param entitySetName  the name of the entity-set
   * @param keyValue  the entity-key value
   * @return a new entity-request builder
   */
  public OEntityGetRequest<OEntity> getEntity(String entitySetName, Object keyValue) {
    return getEntity(entitySetName, OEntityKey.create(keyValue));
  }

  /**
   * Gets the latest version of an entity using the given entity as a template.
   * <p>The entity-request builder returned can be used for further navigation.  Call {@link OEntityRequest#execute()} to issue request.</p>
   *  
   * @param entity  an existing entity to use as a template, using its entity-set and entity-key
   * @return a new entity-request builder
   */
  public OEntityGetRequest<OEntity> getEntity(OEntity entity) {
    return getEntity(entity.getEntitySet().name, entity.getEntityKey());
  }

  /**
   * Gets the entity by entity-set name and entity-key.
   * <p>The entity-request builder returned can be used for further navigation.  Call {@link OEntityRequest#execute()} to issue request.</p>
   * 
   * @param entitySetName  the name of the entity-set
   * @param key  the entity-key
   * @return a new entity-request builder
   */
  public OEntityGetRequest<OEntity> getEntity(String entitySetName, OEntityKey key) {
    return getEntity(OEntity.class, entitySetName, key);
  }

  /**
   * Gets the entity by entity-set name and entity-key value.  The entity will be represented as the given java-type.
   * <p>The entity-request builder returned can be used for further navigation.  Call {@link OEntityRequest#execute()} to issue request.</p>
   * 
   * @param <T>  the entity representation as a java type
   * @param entityType  the entity representation as a java type
   * @param entitySetName  the name of the entity-set
   * @param keyValue  the entity-key value
   * @return a new entity-request builder
   */
  public <T> OEntityGetRequest<T> getEntity(Class<T> entityType, String entitySetName, Object keyValue) {
    return getEntity(entityType, entitySetName, OEntityKey.create(keyValue));
  }

  /**
   * Gets the entity by entity-set name and entity-key.  The entity will be represented as the given java-type.
   * <p>The entity-request builder returned can be used for further navigation.  Call {@link OEntityRequest#execute()} to issue request.</p>
   * 
   * @param <T>  the entity representation as a java type
   * @param entityType   the entity representation as a java type
   * @param entitySetName  the name of the entity-set
   * @param key  the entity-key
   * @return a new entity-request builder
   */
  public <T> OEntityGetRequest<T> getEntity(Class<T> entityType, String entitySetName, OEntityKey key) {
    FeedCustomizationMapping mapping = getFeedCustomizationMapping(entitySetName);
    return new ConsumerGetEntityRequest<T>(client,
        entityType, serviceRootUri, getMetadata(),
        entitySetName, OEntityKey.create(key), mapping);
  }

  /**
   * Creates a new entity in the given entity-set.
   * <p>The create-request builder returned can be used to construct the new entity.  Call {@link OCreateRequest#execute()} to issue request.</p>
   * 
   * @param entitySetName  the name of the entity-set
   * @return a new create-request builder
   */
  public OCreateRequest<OEntity> createEntity(String entitySetName) {
    FeedCustomizationMapping mapping = getFeedCustomizationMapping(entitySetName);
    return new ConsumerCreateEntityRequest<OEntity>(client, serviceRootUri, getMetadata(),
        entitySetName, mapping);
  }

  /**
   * Modifies an existing entity using update semantics.
   * <p>The modification-request builder returned can be used to redefine the new entity.  Call {@link OModifyRequest#execute()} to issue request.</p>
   * 
   * @param entity  the entity identity
   * @return a new modification-request builder
   */
  public OModifyRequest<OEntity> updateEntity(OEntity entity) {
    return new ConsumerEntityModificationRequest<OEntity>(entity, client, serviceRootUri, getMetadata(),
        entity.getEntitySet().name, entity.getEntityKey());
  }

  /**
   * Modifies an existing entity using merge semantics.
   * <p>The modification-request builder returned can be used to modify the new entity.  Call {@link OModifyRequest#execute()} to issue request.</p>
   * 
   * @param entity  the entity identity
   * @return a new modification-request builder
   */
  public OModifyRequest<OEntity> mergeEntity(OEntity entity) {
    return mergeEntity(entity.getEntitySet().name, entity.getEntityKey());
  }

  /**
   * Modifies an existing entity using merge semantics.
   * <p>The modification-request builder returned can be used to modify the new entity.  Call {@link OModifyRequest#execute()} to issue request.</p>
   *  
   * @param entitySetName  the entity identity entity-set name
   * @param keyValue  the entity identity key value
   * @return a new modification-request builder
   */
  public OModifyRequest<OEntity> mergeEntity(String entitySetName, Object keyValue) {
    return mergeEntity(entitySetName, OEntityKey.create(keyValue));
  }

  /**
   * Modifies an existing entity using merge semantics.
   * <p>The modification-request builder returned can be used to modify the new entity.  Call {@link OModifyRequest#execute()} to issue request.</p>
   * 
   * @param entitySetName  the entity identity entity-set name
   * @param key  the entity identity key
   * @return a new modification-request builder
   */
  public OModifyRequest<OEntity> mergeEntity(String entitySetName, OEntityKey key) {
    return new ConsumerEntityModificationRequest<OEntity>(null, client, serviceRootUri,
        getMetadata(), entitySetName, key);
  }

  /**
   * Deletes an existing entity.
   * <p>The entity-request builder returned can be used for further navigation.  Call {@link OEntityRequest#execute()} to issue request.</p>
   * 
   * @param entity  the entity identity
   * @return a new entity-request builder
   */
  public OEntityRequest<Void> deleteEntity(OEntityId entity) {
    return deleteEntity(entity.getEntitySetName(), entity.getEntityKey());
  }

  /**
   * Deletes an existing entity.
   * <p>The entity-request builder returned can be used for further navigation.  Call {@link OEntityRequest#execute()} to issue request.</p>
   * 
   * @param entitySetName  the entity identity entity-set name
   * @param keyValue  the entity identity key value
   * @return a new entity-request builder
   */
  public OEntityRequest<Void> deleteEntity(String entitySetName, Object keyValue) {
    return deleteEntity(entitySetName, OEntityKey.create(keyValue));
  }

  /**
   * Deletes an existing entity.
   * <p>The entity-request builder returned can be used for further navigation.  Call {@link OEntityRequest#execute()} to issue request.</p>
   * 
   * @param entitySetName  the entity identity entity-set name
   * @param key  the entity identity key
   * @return a new entity-request builder
   */
  public OEntityRequest<Void> deleteEntity(String entitySetName, OEntityKey key) {
    return new ConsumerDeleteEntityRequest(client, serviceRootUri, getMetadata(), entitySetName, key);
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
          EdmEntityType eet = ees.type;
          for (EdmProperty ep : eet.getProperties()) {
            if ("SyndicationTitle".equals(ep.fcTargetPath) && "false".equals(ep.fcKeepInContent))
              rt.titlePropName = ep.name;
            if ("SyndicationSummary".equals(ep.fcTargetPath) && "false".equals(ep.fcKeepInContent))
              rt.summaryPropName = ep.name;
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
  
  //TODO(0.5) javadoc
  public OQueryRequest<OEntityId> getLinks(OEntityId sourceEntity, String targetNavProp) {
    return new ConsumerQueryLinksRequest(client, serviceRootUri, getMetadata(), sourceEntity, targetNavProp);
  }
  
  public OEntityRequest<Void> createLink(OEntityId sourceEntity, String targetNavProp, OEntityId targetEntity) {
    return new ConsumerCreateLinkRequest(client, serviceRootUri, getMetadata(), sourceEntity, targetNavProp, targetEntity);
  }

  public OEntityRequest<Void> deleteLink(OEntityId sourceEntity, String targetNavProp, Object... targetKeyValues) {
    return new ConsumerDeleteLinkRequest(client, serviceRootUri, getMetadata(), sourceEntity, targetNavProp, targetKeyValues);
  }

  public OEntityRequest<Void> updateLink(OEntityId sourceEntity, OEntityId newTargetEntity, String targetNavProp, Object... oldTargetKeyValues) {
    return new ConsumerUpdateLinkRequest(client, serviceRootUri, getMetadata(), sourceEntity, newTargetEntity, targetNavProp, oldTargetKeyValues);
  }

  public OFunctionRequest<OObject> callFunction(String functionName) {
    return new ConsumerFunctionCallRequest(client, serviceRootUri, getMetadata(), functionName);
  }

}
