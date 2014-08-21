package org.odata4j.producer.jpa;

import javax.persistence.metamodel.EntityType;

import org.odata4j.exceptions.NotFoundException;

public class ExpectSingleEntityCommand implements Command {

  @Override
  public boolean execute(JPAContext context)
  {
    EntitiesResult entitiesResult = (EntitiesResult)context.getResult();
    
    if (entitiesResult.getEntities().size() == 0) {
      EntityType<?> jpaEntityType = context.getEntity()
        .getJPAEntityType();
      
      Object typeSafeEntityKey = context.getEntity()
        .getTypeSafeEntityKey();
      
      throw new NotFoundException(jpaEntityType
          .getJavaType()
          + " not found with key "
          + typeSafeEntityKey);
    }
    
    context.setResult(JPAResults.entity(entitiesResult.getEntities().get(0)));
    
    return false;
  }

}
