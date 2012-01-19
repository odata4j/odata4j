package org.odata4j.producer;

import java.util.Map;

import org.odata4j.core.Delegate;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityId;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.producer.edm.MetadataProducer;

/** Abstract base {@link Delegate} for {@link ODataProducer}. */
public abstract class ODataProducerDelegate implements Delegate<ODataProducer>, ODataProducer {

  @Override
  public EdmDataServices getMetadata() {
    return getDelegate().getMetadata();
  }

  @Override
  public MetadataProducer getMetadataProducer() {
    return getDelegate().getMetadataProducer();
  }

  @Override
  public EntitiesResponse getEntities(String entitySetName, QueryInfo queryInfo) {
    return getDelegate().getEntities(entitySetName, queryInfo);
  }

  @Override
  public CountResponse getEntitiesCount(String entitySetName, QueryInfo queryInfo) {
    return getDelegate().getEntitiesCount(entitySetName, queryInfo);
  }

  @Override
  public EntityResponse getEntity(String entitySetName, OEntityKey entityKey, QueryInfo queryInfo) {
    return getDelegate().getEntity(entitySetName, entityKey, queryInfo);
  }

  @Override
  public BaseResponse getNavProperty(String entitySetName, OEntityKey entityKey, String navProp, QueryInfo queryInfo) {
    return getDelegate().getNavProperty(entitySetName, entityKey, navProp, queryInfo);
  }

  @Override
  public CountResponse getNavPropertyCount(String entitySetName, OEntityKey entityKey, String navProp, QueryInfo queryInfo) {
    return getDelegate().getNavPropertyCount(entitySetName, entityKey, navProp, queryInfo);
  }

  @Override
  public void close() {
    getDelegate().close();
  }

  @Override
  public EntityResponse createEntity(String entitySetName, OEntity entity) {
    return getDelegate().createEntity(entitySetName, entity);
  }

  @Override
  public EntityResponse createEntity(String entitySetName, OEntityKey entityKey, String navProp, OEntity entity) {
    return getDelegate().createEntity(entitySetName, entityKey, navProp, entity);
  }

  @Override
  public void deleteEntity(String entitySetName, OEntityKey entityKey) {
    getDelegate().deleteEntity(entitySetName, entityKey);
  }

  @Override
  public void mergeEntity(String entitySetName, OEntity entity) {
    getDelegate().mergeEntity(entitySetName, entity);
  }

  @Override
  public void updateEntity(String entitySetName, OEntity entity) {
    getDelegate().updateEntity(entitySetName, entity);
  }

  @Override
  public EntityIdResponse getLinks(OEntityId sourceEntity, String targetNavProp) {
    return getDelegate().getLinks(sourceEntity, targetNavProp);
  }

  @Override
  public void createLink(OEntityId sourceEntity, String targetNavProp, OEntityId targetEntity) {
    getDelegate().createLink(sourceEntity, targetNavProp, targetEntity);
  }

  @Override
  public void updateLink(OEntityId sourceEntity, String targetNavProp, OEntityKey oldTargetEntityKey, OEntityId newTargetEntity) {
    getDelegate().updateLink(sourceEntity, targetNavProp, oldTargetEntityKey, newTargetEntity);
  }

  @Override
  public void deleteLink(OEntityId sourceEntity, String targetNavProp, OEntityKey targetEntityKey) {
    getDelegate().deleteLink(sourceEntity, targetNavProp, targetEntityKey);
  }

  @Override
  public BaseResponse callFunction(EdmFunctionImport name, Map<String, OFunctionParameter> params, QueryInfo queryInfo) {
    return getDelegate().callFunction(name, params, queryInfo);
  }

}
