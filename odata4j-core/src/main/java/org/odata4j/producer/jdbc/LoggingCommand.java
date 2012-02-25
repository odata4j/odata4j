package org.odata4j.producer.jdbc;

import java.util.logging.Logger;

import org.odata4j.command.Command;
import org.odata4j.command.CommandResult;
import org.odata4j.producer.command.CloseCommandContext;
import org.odata4j.producer.command.GetEntitiesCommandContext;
import org.odata4j.producer.command.GetMetadataCommandContext;
import org.odata4j.producer.command.ProducerCommandContext;

public class LoggingCommand implements Command<ProducerCommandContext<?>> {
  private static final Logger log = Logger.getLogger(LoggingCommand.class.getName());

  @Override
  public CommandResult execute(ProducerCommandContext<?> context) throws Exception {
    if (context instanceof CloseCommandContext)
      log("close");
    else if (context instanceof GetMetadataCommandContext)
      log("getMetadata");
    else if (context instanceof GetEntitiesCommandContext) {
      GetEntitiesCommandContext c = (GetEntitiesCommandContext) context;
      log("getEntities", "entitySetName", c.getEntitySetName(), "queryInfo", c.getQueryInfo());
    } else
      throw new UnsupportedOperationException("TODO implement logging for : " + context);
    return CommandResult.CONTINUE;
  }

  private void log(String methodName, Object... args) {
    StringBuilder sb = new StringBuilder(methodName);
    for (int i = 0; i < args.length; i += 2) {
      sb.append(' ');
      sb.append(args[i]);
      sb.append('=');
      sb.append(args[i + 1]);
    }
    log.info(sb.toString());
  }

}