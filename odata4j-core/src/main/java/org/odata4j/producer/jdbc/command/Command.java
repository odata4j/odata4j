package org.odata4j.producer.jdbc.command;

public interface Command<TContext extends CommandContext> {

  CommandResult execute(TContext context) throws Exception;

}
