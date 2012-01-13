package org.odata4j.cxf.consumer;

import org.core4j.Enumerable;
import org.odata4j.consumer.AbstractODataConsumer;
import org.odata4j.consumer.ODataConsumer;
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
import org.odata4j.format.FormatType;

/**
 * <code>ODataConsumer</code> is the client-side interface to an OData service.
 *
 * <p>Use {@link #create(String)} or one of the other static factory methods to connect to an existing OData service.</p>
 */
public class ODataCxfConsumer extends AbstractODataConsumer {

  public static class Builder {

    private FormatType formatType;
    private String serviceRootUri;

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
     * Builds the {@link ODataCxfConsumer} object.
     *
     * @return a new OData consumer
     */
    public ODataCxfConsumer build() {
      return new ODataCxfConsumer(this.formatType, this.serviceRootUri);
    }
  }

  public ODataCxfConsumer(FormatType formatType, String serviceRootUri) {
    super(serviceRootUri);
    new ODataCxfClient(formatType);
  }

  @Override
  public Enumerable<EntitySetInfo> getEntitySets() {
    return null;
  }

  @Override
  public EdmDataServices getMetadata() {
    return null;
  }

  @Override
  public OQueryRequest<OEntity> getEntities(ORelatedEntitiesLink link) {
    return null;
  }

  @Override
  public OQueryRequest<OEntity> getEntities(String entitySetHref) {
    return null;
  }

  @Override
  public <T> OQueryRequest<T> getEntities(Class<T> entityType, String entitySetHref) {
    return null;
  }

  @Override
  public OEntityGetRequest<OEntity> getEntity(ORelatedEntityLink link) {
    return null;
  }

  @Override
  public OEntityGetRequest<OEntity> getEntity(String entitySetName, Object keyValue) {
    return null;
  }

  @Override
  public OEntityGetRequest<OEntity> getEntity(OEntity entity) {
    return null;
  }

  @Override
  public OEntityGetRequest<OEntity> getEntity(String entitySetName, OEntityKey key) {
    return null;
  }

  @Override
  public <T> OEntityGetRequest<T> getEntity(Class<T> entityType, String entitySetName, Object keyValue) {
    return null;
  }

  @Override
  public <T> OEntityGetRequest<T> getEntity(Class<T> entityType, String entitySetName, OEntityKey key) {
    return null;
  }

  @Override
  public OQueryRequest<OEntityId> getLinks(OEntityId sourceEntity, String targetNavProp) {
    return null;
  }

  @Override
  public OEntityRequest<Void> createLink(OEntityId sourceEntity, String targetNavProp, OEntityId targetEntity) {
    return null;
  }

  @Override
  public OEntityRequest<Void> deleteLink(OEntityId sourceEntity, String targetNavProp, Object... targetKeyValues) {
    return null;
  }

  @Override
  public OEntityRequest<Void> updateLink(OEntityId sourceEntity, OEntityId newTargetEntity, String targetNavProp, Object... oldTargetKeyValues) {
    return null;
  }

  @Override
  public OCreateRequest<OEntity> createEntity(String entitySetName) {
    return null;
  }

  @Override
  public OModifyRequest<OEntity> updateEntity(OEntity entity) {
    return null;
  }

  @Override
  public OModifyRequest<OEntity> mergeEntity(OEntity entity) {
    return null;
  }

  @Override
  public OModifyRequest<OEntity> mergeEntity(String entitySetName, Object keyValue) {
    return null;
  }

  @Override
  public OModifyRequest<OEntity> mergeEntity(String entitySetName, OEntityKey key) {
    return null;
  }

  @Override
  public OEntityRequest<Void> deleteEntity(OEntityId entity) {
    return null;
  }

  @Override
  public OEntityRequest<Void> deleteEntity(String entitySetName, Object keyValue) {
    return null;
  }

  @Override
  public OEntityRequest<Void> deleteEntity(String entitySetName, OEntityKey key) {
    return null;
  }

  @Override
  public OFunctionRequest<OObject> callFunction(String functionName) {
    return null;
  }

  public static ODataConsumer create(String serviceRootUri) {
    return ODataCxfConsumer.newBuilder(serviceRootUri).build();
  }

  private static Builder newBuilder(String serviceRootUri) {
    return new Builder(serviceRootUri);
  }

}
