package org.odata4j.producer.jdbc;

import org.odata4j.producer.jdbc.command.Command;
import org.odata4j.producer.jdbc.command.CommandContext;
import org.odata4j.producer.jdbc.command.CommandResult;

public class NoopCommand implements Command<CommandContext> {

  @Override
  public CommandResult execute(CommandContext context) throws Exception {
    return CommandResult.CONTINUE;
  }

}