package org.odata4j.producer.jpa;

import org.odata4j.core.OEntity;
import org.odata4j.producer.Responses;

public class SetOEntityResponseCommand implements Command {

  private JPAContext.EntityAccessor accessor;

  public SetOEntityResponseCommand() {
    this(JPAContext.EntityAccessor.ENTITY);
  }

  public SetOEntityResponseCommand(JPAContext.EntityAccessor accessor) {
    this.accessor = accessor;
  }

  @Override
  public boolean execute(JPAContext context) {

    OEntity oentity = accessor.getEntity(context).getOEntity();
    context.setResponse(Responses.entity(oentity));

    return false;
  }
}