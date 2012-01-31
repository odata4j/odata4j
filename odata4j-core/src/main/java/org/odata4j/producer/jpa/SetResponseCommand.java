package org.odata4j.producer.jpa;

import java.util.List;

import org.core4j.Enumerable;
import org.core4j.Func1;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.producer.Responses;

public class SetResponseCommand implements Command {

  private JPAContext.EntityAccessor accessor;

  public SetResponseCommand() {
    this(JPAContext.EntityAccessor.ENTITY);
  }

  public SetResponseCommand(JPAContext.EntityAccessor accessor) {
    this.accessor = accessor;
  }

  @Override
  public boolean execute(final JPAContext context) {

    if (context.getResult() instanceof EntityResult) {
      EntityResult result = (EntityResult) context.getResult();
      
      OEntity oentity = makeEntity(context, result.getEntity());
      context.setResponse(Responses.entity(oentity));

    } else if (context.getResult() instanceof EntitiesResult) {

      EntitiesResult result = (EntitiesResult) context.getResult();
      List<OEntity> entities = Enumerable.create(result.getEntities())
          .select(new Func1<Object, OEntity>() {
            public OEntity apply(final Object jpaEntity) {
              return makeEntity(context, jpaEntity);
            }
          }).toList();
      
      //  TODO create the skip token based on the jpaEntity and
      //  move this back to ExecuteJPQLQueryCommand
      String skipToken = null;    
      if (result.createSkipToken()) {
        skipToken = JPASkipToken.create(context.getQueryInfo() == null
          ? null
          : context.getQueryInfo().orderBy,
          Enumerable.create(entities).last());
      }
      
      context.setResponse(Responses.entities(entities, context.getEntity()
          .getEdmEntitySet(), result.getInlineCount(), skipToken));

    } else if (context.getResult() instanceof PropertyResult) {

      PropertyResult<?> result = (PropertyResult<?>) context.getResult();
      OProperty<?> op = OProperties.simple(result.getName(),
          result.getType(), result.getValue());
      context.setResponse(Responses.property(op));

    } else if (context.getResult() instanceof CountResult) {
      
      CountResult result = (CountResult) context.getResult();
      context.setResponse(Responses.count(result.getCount()));

    }

    return false;
  }

  private OEntity makeEntity(JPAContext context, Object jpaEntity) {
    return JPAProducer.jpaEntityToOEntity(
        context.getMetadata(),
        accessor.getEntity(context).getEdmEntitySet(),
        accessor.getEntity(context).getJPAEntityType(),
        jpaEntity,
        context.getQueryInfo() == null
            ? null
            : context.getQueryInfo().expand,
        context.getQueryInfo() == null
            ? null
            : context.getQueryInfo().select);
  }

}