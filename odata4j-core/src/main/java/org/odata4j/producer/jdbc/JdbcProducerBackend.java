package org.odata4j.producer.jdbc;

import java.lang.reflect.Proxy;
import java.util.Map;

import org.odata4j.command.ChainCommand;
import org.odata4j.command.Command;
import org.odata4j.command.CommandContext;
import org.odata4j.command.CommandExecution;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityId;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.producer.QueryInfo;
import org.odata4j.producer.command.CallFunctionCommandContext;
import org.odata4j.producer.command.CloseCommandContext;
import org.odata4j.producer.command.CommandProducerBackend;
import org.odata4j.producer.command.CreateEntityAtPropertyCommandContext;
import org.odata4j.producer.command.CreateEntityCommandContext;
import org.odata4j.producer.command.CreateLinkCommandContext;
import org.odata4j.producer.command.DeleteEntityCommandContext;
import org.odata4j.producer.command.DeleteLinkCommandContext;
import org.odata4j.producer.command.GetEntitiesCommandContext;
import org.odata4j.producer.command.GetEntitiesCountCommandContext;
import org.odata4j.producer.command.GetEntityCommandContext;
import org.odata4j.producer.command.GetLinksCommandContext;
import org.odata4j.producer.command.GetMetadataCommandContext;
import org.odata4j.producer.command.GetMetadataProducerCommandContext;
import org.odata4j.producer.command.GetNavPropertyCommandContext;
import org.odata4j.producer.command.GetNavPropertyCountCommandContext;
import org.odata4j.producer.command.MergeEntityCommandContext;
import org.odata4j.producer.command.UpdateEntityCommandContext;
import org.odata4j.producer.command.UpdateLinkCommandContext;

public abstract class JdbcProducerBackend implements CommandProducerBackend {

  @Override
  abstract public CommandExecution getCommandExecution();

  abstract public Jdbc getJdbc();

  public JdbcMetadataMapping getMetadataMapping() {
    GetMetadataCommandContext context = newGetMetadataCommandContext();
    try {
      getCommand(GetMetadataCommandContext.class).execute(context);
      return (JdbcMetadataMapping) context.getResult();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public JdbcProducerCommandContext newJdbcCommandContext() {
    return new JdbcProducerCommandContext() {

      @Override
      public Jdbc getJdbc() {
        return JdbcProducerBackend.this.getJdbc();
      }

      @Override
      public JdbcProducerBackend getBackend() {
        return JdbcProducerBackend.this;
      }};
  }

  @Override
  public <TContext extends CommandContext> Command<TContext> getCommand(Class<TContext> contextType) {

    ChainCommand.Builder<TContext> chain = ChainCommand.newBuilder();
    chain.add(new LoggingCommand());

    if (CloseCommandContext.class.isAssignableFrom(contextType)) {
      return chain.build();
    }
    if (GetMetadataCommandContext.class.isAssignableFrom(contextType)) {
      chain.add(new JdbcGetMetadataCommand());
      return chain.build();
    }
    if (GetEntitiesCommandContext.class.isAssignableFrom(contextType)) {
      chain.add(new JdbcGetEntitiesCommand());
      return chain.build();
    }
    throw new UnsupportedOperationException("TODO implement: " + contextType.getSimpleName());
  }

  @SuppressWarnings("unchecked")
  private <T> T newContext(Class<?> contextType, Object... args) {
    return (T) Proxy.newProxyInstance(
        getClass().getClassLoader(),
        new Class<?>[]{ contextType, JdbcProducerCommandContext.class },
        new JdbcProducerBackendInvocationHandler(this, contextType, args));
  }

  @Override
  public GetMetadataCommandContext newGetMetadataCommandContext() {
    return newContext(GetMetadataCommandContext.class);
  }

  @Override
  public GetEntitiesCommandContext newGetEntitiesCommandContext(String entitySetName, QueryInfo queryInfo) {
    return newContext(GetEntitiesCommandContext.class, "entitySetName", entitySetName, "queryInfo", queryInfo);
  }

  @Override
  public CloseCommandContext newCloseCommandContext() {
    return newContext(CloseCommandContext.class);
  }

  @Override
  public GetMetadataProducerCommandContext newGetMetadataProducerCommandContext() {
    throw new UnsupportedOperationException();
  }

  @Override
  public GetEntitiesCountCommandContext newGetEntitiesCountCommandContext(String entitySetName, QueryInfo queryInfo) {
    throw new UnsupportedOperationException();
  }

  @Override
  public GetEntityCommandContext newGetEntityCommandContext(String entitySetName, OEntityKey entityKey, QueryInfo queryInfo) {
    throw new UnsupportedOperationException();
  }

  @Override
  public GetNavPropertyCommandContext newGetNavPropertyCommandContext(String entitySetName, OEntityKey entityKey, String navProp, QueryInfo queryInfo) {
    throw new UnsupportedOperationException();
  }

  @Override
  public GetNavPropertyCountCommandContext newGetNavPropertyCountCommandContext(String entitySetName, OEntityKey entityKey, String navProp, QueryInfo queryInfo) {
    throw new UnsupportedOperationException();
  }

  @Override
  public CreateEntityCommandContext newCreateEntityCommandContext(String entitySetName, OEntity entity) {
    throw new UnsupportedOperationException();
  }

  @Override
  public CreateEntityAtPropertyCommandContext newCreateEntityAtPropertyCommandContext(String entitySetName, OEntityKey entityKey, String navProp, OEntity entity) {
    throw new UnsupportedOperationException();
  }

  @Override
  public DeleteEntityCommandContext newDeleteEntityCommandContext(String entitySetName, OEntityKey entityKey) {
    throw new UnsupportedOperationException();
  }

  @Override
  public MergeEntityCommandContext newMergeEntityCommandContext(String entitySetName, OEntity entity) {
    throw new UnsupportedOperationException();
  }

  @Override
  public UpdateEntityCommandContext newUpdateEntityCommandContext(String entitySetName, OEntity entity) {
    throw new UnsupportedOperationException();
  }

  @Override
  public GetLinksCommandContext newGetLinksCommandContext(OEntityId sourceEntity, String targetNavProp) {
    throw new UnsupportedOperationException();
  }

  @Override
  public CreateLinkCommandContext newCreateLinkCommandContext(OEntityId sourceEntity, String targetNavProp, OEntityId targetEntity) {
    throw new UnsupportedOperationException();
  }

  @Override
  public UpdateLinkCommandContext newUpdateLinkCommandContext(OEntityId sourceEntity, String targetNavProp, OEntityKey oldTargetEntityKey, OEntityId newTargetEntity) {
    throw new UnsupportedOperationException();
  }

  @Override
  public DeleteLinkCommandContext newDeleteLinkCommandContext(OEntityId sourceEntity, String targetNavProp, OEntityKey targetEntityKey) {
    throw new UnsupportedOperationException();
  }

  @Override
  public CallFunctionCommandContext newCallFunctionCommandContext(EdmFunctionImport name, Map<String, OFunctionParameter> params, QueryInfo queryInfo) {
    throw new UnsupportedOperationException();
  }

}