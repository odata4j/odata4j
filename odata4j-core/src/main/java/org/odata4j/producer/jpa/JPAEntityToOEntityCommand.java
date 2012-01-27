package org.odata4j.producer.jpa;

import org.odata4j.core.OEntity;

public class JPAEntityToOEntityCommand implements Command {

  private JPAContext.EntityAccessor accessor;

  public JPAEntityToOEntityCommand() {
    this(JPAContext.EntityAccessor.ENTITY);
  }

  public JPAEntityToOEntityCommand(JPAContext.EntityAccessor accessor) {
    this.accessor = accessor;
  }

  @Override
  public boolean execute(JPAContext context) {

    OEntity oentity = JPAProducer.jpaEntityToOEntity(
        context.getMetadata(),
        accessor.getEntity(context).getEdmEntitySet(),
        accessor.getEntity(context).getJPAEntityType(),
        accessor.getEntity(context).getJpaEntity(),
        context.getQueryInfo() == null
            ? null
            : context.getQueryInfo().expand,
        context.getQueryInfo() == null
            ? null
            : context.getQueryInfo().select);

    accessor.getEntity(context).setOEntity(oentity);

    return false;
  }
}