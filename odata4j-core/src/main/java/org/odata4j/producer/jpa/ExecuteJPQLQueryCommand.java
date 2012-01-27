package org.odata4j.producer.jpa;

import java.util.Collections;
import java.util.List;

import javax.persistence.Query;

import org.core4j.Enumerable;
import org.core4j.Func1;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmMultiplicity;
import org.odata4j.edm.EdmNavigationProperty;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.producer.BaseResponse;
import org.odata4j.producer.InlineCount;
import org.odata4j.producer.Responses;

public class ExecuteJPQLQueryCommand implements Command {

  private int maxResults;

  public ExecuteJPQLQueryCommand(int maxResults) {
    this.maxResults = maxResults;
  }

  @Override
  public boolean execute(JPAContext context) {
    context.setResponse(getEntitiesResponse(context));

    return false;
  }

  private BaseResponse getEntitiesResponse(final JPAContext context) {

    // get the jpql
    String jpql = context.getJPQLQuery();

    // jpql -> jpa query
    Query tq = context.getEntityManager().createQuery(jpql);

    Integer inlineCount = context.getQueryInfo() != null
        && context.getQueryInfo().inlineCount == InlineCount.ALLPAGES
        ? tq.getResultList().size()
        : null;

    int queryMaxResults = maxResults;
    if (context.getQueryInfo() != null
        && context.getQueryInfo().top != null) {

      // top=0: don't even hit jpa, return a response with zero
      // entities
      if (context.getQueryInfo().top.equals(0)) {
        // returning null from this function would cause the
        // FormatWriters to throw
        // a null reference exception as the entities collection is
        // expected to be empty and
        // not null. This prevents us from being able to
        // successfully
        // respond to $top=0 contexts.
        List<OEntity> emptyList = Collections.emptyList();
        return Responses.entities(
            emptyList,
            context.getEntity().getEdmEntitySet(),
            inlineCount,
            null);
      }

      if (context.getQueryInfo().top < maxResults)
        queryMaxResults = context.getQueryInfo().top;
    }

    // jpa query for one more than specified to determine whether or not
    // to
    // return a skip token
    tq = tq.setMaxResults(queryMaxResults + 1);

    if (context.getQueryInfo() != null
        && context.getQueryInfo().skip != null)
      tq = tq.setFirstResult(context.getQueryInfo().skip);

    // execute jpa query
    @SuppressWarnings("unchecked")
    List<Object> results = tq.getResultList();

    // property response
    if (context.getEdmPropertyBase() instanceof EdmProperty) {
      EdmProperty propInfo = (EdmProperty) context
          .getEdmPropertyBase();

      if (results.size() != 1)
        throw new RuntimeException(
            "Expected one and only one result for property, found "
                + results.size());

      Object value = results.get(0);
      OProperty<?> op = OProperties.simple(
          ((EdmProperty) propInfo).getName(),
          (EdmSimpleType<?>) ((EdmProperty) propInfo).getType(),
          value);
      return Responses.property(op);
    }

    // entities response
    List<OEntity> entities = Enumerable.create(results)
        .take(queryMaxResults)
        .select(new Func1<Object, OEntity>() {
          public OEntity apply(final Object jpaEntity) {
            return makeEntity(context, jpaEntity);
          }
        }).toList();

    // compute skip token if necessary
    String skipToken = null;
    boolean hasMoreResults = context.getQueryInfo() != null
        && context.getQueryInfo().top != null
        ? context.getQueryInfo().top > maxResults
            && results.size() > queryMaxResults
        : results.size() > queryMaxResults;

    if (hasMoreResults)
      skipToken = JPASkipToken.create(context.getQueryInfo() == null
          ? null
          : context.getQueryInfo().orderBy,
          Enumerable.create(entities)
              .last());

    if (context.getEdmPropertyBase() instanceof EdmNavigationProperty) {
      EdmNavigationProperty edmNavProp = (EdmNavigationProperty) context
          .getEdmPropertyBase();
      if (edmNavProp.getToRole().getMultiplicity() == EdmMultiplicity.ONE
          || edmNavProp.getToRole().getMultiplicity() == EdmMultiplicity.ZERO_TO_ONE) {
        if (entities.size() != 1)
          throw new RuntimeException(
              "Expected only one entity, found "
                  + entities.size());
        return Responses.entity(entities.get(0));
      }
    }

    return Responses.entities(entities, context.getEntity()
        .getEdmEntitySet(),
        inlineCount, skipToken);
  }

  private OEntity makeEntity(JPAContext context, Object jpaEntity) {

    return JPAProducer.jpaEntityToOEntity(
        context.getMetadata(),
        context.getEntity().getEdmEntitySet(),
        context.getEntity().getJPAEntityType(),
        jpaEntity,
        context.getQueryInfo() == null ? null : context
            .getQueryInfo().expand,
        context.getQueryInfo() == null ? null : context
            .getQueryInfo().select);
  }
}