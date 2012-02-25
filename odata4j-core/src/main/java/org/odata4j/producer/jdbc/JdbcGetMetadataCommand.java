package org.odata4j.producer.jdbc;

import org.odata4j.command.Command;
import org.odata4j.command.CommandResult;
import org.odata4j.producer.command.GetMetadataCommandContext;

public class JdbcGetMetadataCommand implements Command<GetMetadataCommandContext> {

  @Override
  public CommandResult execute(GetMetadataCommandContext context) throws Exception {
    JdbcProducerCommandContext jdbcContext = (JdbcProducerCommandContext) context;

    // 1. get jdbc model
    JdbcModel model = jdbcContext.getJdbc().execute(new CreateJdbcModel());

    // 2. apply model cleanup
    new LimitJdbcModelToDefaultSchema().apply(model);

    // 3. project jdbc model into edm metadata
    JdbcMetadataMapping mapping = new JdbcModelToMetadata().apply(model);

    context.setResult(mapping);
    return CommandResult.CONTINUE;
  }

}