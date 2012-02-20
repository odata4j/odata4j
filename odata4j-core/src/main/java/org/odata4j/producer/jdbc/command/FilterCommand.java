package org.odata4j.producer.jdbc.command;

public interface FilterCommand<TContext extends CommandContext> extends Command<TContext> {

  FilterResult postProcess(TContext context, Exception e);

}
