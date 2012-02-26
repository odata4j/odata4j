package org.odata4j.producer.command;

import java.util.Map;

import org.odata4j.command.Command;
import org.odata4j.command.CommandExecution;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityId;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.core.Throwables;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmDataServicesProvider;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.producer.BaseResponse;
import org.odata4j.producer.CountResponse;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.EntityIdResponse;
import org.odata4j.producer.EntityQueryInfo;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.QueryInfo;
import org.odata4j.producer.edm.MetadataProducer;

public class CommandProducer implements ODataProducer {

  private final CommandProducerBackend backend;

  public CommandProducer(CommandProducerBackend backend) {
    this.backend = backend;
  }

  private <TResult, TContext extends ProducerCommandContext<TResult>>
      TResult executeCommand(Class<TContext> contextType, Class<TResult> resultType, TContext context) {
    Command<TContext> command = backend.getCommand(contextType);
    CommandExecution execution = backend.getCommandExecution();
    try {
      execution.execute(command, context);
      TResult result = context.getResult();
      if (result != null)
        return result;
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
    if (resultType.equals(Void.class)) {
      return null;  // ok for Void
    }
    throw new RuntimeException("Command " + contextType.getSimpleName() + " implementation did not return result, expected " + resultType.getSimpleName());
  }

  @Override
  public EdmDataServices getMetadata() {
    return executeCommand(GetMetadataCommandContext.class, EdmDataServicesProvider.class, backend.newGetMetadataCommandContext()).getMetadata();
  }

  @Override
  public MetadataProducer getMetadataProducer() {
    return executeCommand(GetMetadataProducerCommandContext.class, MetadataProducer.class, backend.newGetMetadataProducerCommandContext());
  }

  @Override
  public EntitiesResponse getEntities(String entitySetName, QueryInfo queryInfo) {
    return executeCommand(GetEntitiesCommandContext.class, EntitiesResponse.class, backend.newGetEntitiesCommandContext(entitySetName, queryInfo));
  }

  @Override
  public CountResponse getEntitiesCount(String entitySetName, QueryInfo queryInfo) {
    return executeCommand(GetEntitiesCountCommandContext.class, CountResponse.class, backend.newGetEntitiesCountCommandContext(entitySetName, queryInfo));
  }

  @Override
  public EntityResponse getEntity(String entitySetName, OEntityKey entityKey, EntityQueryInfo queryInfo) {
    return executeCommand(GetEntityCommandContext.class, EntityResponse.class, backend.newGetEntityCommandContext(entitySetName, entityKey, queryInfo));
  }

  @Override
  public BaseResponse getNavProperty(String entitySetName, OEntityKey entityKey, String navProp, QueryInfo queryInfo) {
    return executeCommand(GetNavPropertyCommandContext.class, BaseResponse.class, backend.newGetNavPropertyCommandContext(entitySetName, entityKey, navProp, queryInfo));
  }

  @Override
  public CountResponse getNavPropertyCount(String entitySetName, OEntityKey entityKey, String navProp, QueryInfo queryInfo) {
    return executeCommand(GetNavPropertyCountCommandContext.class, CountResponse.class, backend.newGetNavPropertyCountCommandContext(entitySetName, entityKey, navProp, queryInfo));
  }

  @Override
  public void close() {
    executeCommand(CloseCommandContext.class, Void.class, backend.newCloseCommandContext());
  }

  @Override
  public EntityResponse createEntity(String entitySetName, OEntity entity) {
    return executeCommand(CreateEntityCommandContext.class, EntityResponse.class, backend.newCreateEntityCommandContext(entitySetName, entity));
  }

  @Override
  public EntityResponse createEntity(String entitySetName, OEntityKey entityKey, String navProp, OEntity entity) {
    return executeCommand(CreateEntityAtPropertyCommandContext.class, EntityResponse.class, backend.newCreateEntityAtPropertyCommandContext(entitySetName, entityKey, navProp, entity));
  }

  @Override
  public void deleteEntity(String entitySetName, OEntityKey entityKey) {
    executeCommand(DeleteEntityCommandContext.class, Void.class, backend.newDeleteEntityCommandContext(entitySetName, entityKey));
  }

  @Override
  public void mergeEntity(String entitySetName, OEntity entity) {
    executeCommand(MergeEntityCommandContext.class, Void.class, backend.newMergeEntityCommandContext(entitySetName, entity));
  }

  @Override
  public void updateEntity(String entitySetName, OEntity entity) {
    executeCommand(UpdateEntityCommandContext.class, Void.class, backend.newUpdateEntityCommandContext(entitySetName, entity));
  }

  @Override
  public EntityIdResponse getLinks(OEntityId sourceEntity, String targetNavProp) {
    return executeCommand(GetLinksCommandContext.class, EntityIdResponse.class, backend.newGetLinksCommandContext(sourceEntity, targetNavProp));
  }

  @Override
  public void createLink(OEntityId sourceEntity, String targetNavProp, OEntityId targetEntity) {
    executeCommand(CreateLinkCommandContext.class, Void.class, backend.newCreateLinkCommandContext(sourceEntity, targetNavProp, targetEntity));
  }

  @Override
  public void updateLink(OEntityId sourceEntity, String targetNavProp, OEntityKey oldTargetEntityKey, OEntityId newTargetEntity) {
    executeCommand(UpdateLinkCommandContext.class, Void.class, backend.newUpdateLinkCommandContext(sourceEntity, targetNavProp, oldTargetEntityKey, newTargetEntity));
  }

  @Override
  public void deleteLink(OEntityId sourceEntity, String targetNavProp, OEntityKey targetEntityKey) {
    executeCommand(DeleteLinkCommandContext.class, Void.class, backend.newDeleteLinkCommandContext(sourceEntity, targetNavProp, targetEntityKey));
  }

  @Override
  public BaseResponse callFunction(EdmFunctionImport name, Map<String, OFunctionParameter> params, QueryInfo queryInfo) {
    return executeCommand(CallFunctionCommandContext.class, BaseResponse.class, backend.newCallFunctionCommandContext(name, params, queryInfo));
  }

}
