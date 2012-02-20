package org.odata4j.producer.jdbc.commandproducer;

import java.util.Map;

import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityId;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.producer.QueryInfo;
import org.odata4j.producer.jdbc.command.Command;
import org.odata4j.producer.jdbc.command.CommandContext;
import org.odata4j.producer.jdbc.command.CommandExecution;

public interface CommandProducerBackend {

  CommandExecution getCommandExecution();

  <TContext extends CommandContext> Command<TContext> getCommand(Class<TContext> contextType);

  GetMetadataCommandContext newGetMetadataCommandContext();

  GetMetadataProducerCommandContext newGetMetadataProducerCommandContext();

  GetEntitiesCommandContext newGetEntitiesCommandContext(String entitySetName, QueryInfo queryInfo);

  GetEntitiesCountCommandContext newGetEntitiesCountCommandContext(String entitySetName, QueryInfo queryInfo);

  GetEntityCommandContext newGetEntityCommandContext(String entitySetName, OEntityKey entityKey, QueryInfo queryInfo);

  GetNavPropertyCommandContext newGetNavPropertyCommandContext(String entitySetName, OEntityKey entityKey, String navProp, QueryInfo queryInfo);

  GetNavPropertyCountCommandContext newGetNavPropertyCountCommandContext(String entitySetName, OEntityKey entityKey, String navProp, QueryInfo queryInfo);

  CloseCommandContext newCloseCommandContext();

  CreateEntityCommandContext newCreateEntityCommandContext(String entitySetName, OEntity entity);

  CreateEntityAtPropertyCommandContext newCreateEntityAtPropertyCommandContext(String entitySetName, OEntityKey entityKey, String navProp, OEntity entity);

  DeleteEntityCommandContext newDeleteEntityCommandContext(String entitySetName, OEntityKey entityKey);

  MergeEntityCommandContext newMergeEntityCommandContext(String entitySetName, OEntity entity);

  UpdateEntityCommandContext newUpdateEntityCommandContext(String entitySetName, OEntity entity);

  GetLinksCommandContext newGetLinksCommandContext(OEntityId sourceEntity, String targetNavProp);

  CreateLinkCommandContext newCreateLinkCommandContext(OEntityId sourceEntity, String targetNavProp, OEntityId targetEntity);

  UpdateLinkCommandContext newUpdateLinkCommandContext(OEntityId sourceEntity, String targetNavProp, OEntityKey oldTargetEntityKey, OEntityId newTargetEntity);

  DeleteLinkCommandContext newDeleteLinkCommandContext(OEntityId sourceEntity, String targetNavProp, OEntityKey targetEntityKey);

  CallFunctionCommandContext newCallFunctionCommandContext(EdmFunctionImport name, Map<String, OFunctionParameter> params, QueryInfo queryInfo);

}