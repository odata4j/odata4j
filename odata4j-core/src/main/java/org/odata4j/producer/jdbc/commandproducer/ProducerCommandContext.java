package org.odata4j.producer.jdbc.commandproducer;

import org.odata4j.producer.jdbc.command.CommandContext;

public interface ProducerCommandContext<TResult> extends CommandContext {

  TResult getResult();

  void setResult(TResult result);

}
