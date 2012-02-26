package org.odata4j.producer.jdbc;

import junit.framework.Assert;

import org.core4j.Func;
import org.junit.Test;
import org.odata4j.core.OEntityKey;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.expression.BoolCommonExpression;
import org.odata4j.expression.Expression;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.EntityQueryInfo;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.QueryInfo;
import org.odata4j.producer.command.ProducerCommandContext;
import org.odata4j.producer.exceptions.NotFoundException;
import org.odata4j.test.Asserts;

public class JdbcProducerTest {

  private static final String CUSTOMER = "customer";
  private static final String CUSTOMER_ID = "customer_id";
  private static final String CUSTOMER_NAME = "customer_name";

  @Test
  public void jdbcProducer() {

    JdbcTest.populateExample();

    JdbcModelToMetadata modelToMetadata = new JdbcModelToMetadata() {
      @Override
      public String rename(String dbName) {
        return dbName.toLowerCase();
      }
    };

    JdbcProducer producer = JdbcProducer.newBuilder()
        .jdbc(JdbcTest.HSQL_DB)
        .insert(ProducerCommandContext.class, new LoggingCommand())
        .register(JdbcModelToMetadata.class, modelToMetadata)
        .build();

    EdmDataServices metadata = producer.getMetadata();
    Assert.assertNotNull(metadata);
    JdbcTest.dump(metadata);



    // getEntity
    EntityResponse entityResponse = producer.getEntity(CUSTOMER, OEntityKey.create(1), null);
    Assert.assertNotNull(entityResponse);
    Assert.assertNotNull(entityResponse.getEntity());
    Assert.assertEquals("Customer One", entityResponse.getEntity().getProperty(CUSTOMER_NAME).getValue());

    Asserts.assertThrows(NotFoundException.class, getEntity(producer, CUSTOMER, OEntityKey.create(-1), null));

    BoolCommonExpression filter = Expression.boolean_(false);
    Asserts.assertThrows(NotFoundException.class, getEntity(producer, CUSTOMER, OEntityKey.create(1), EntityQueryInfo.newBuilder().setFilter(filter).build()));

    // getEntities
    EntitiesResponse entitiesResponse = producer.getEntities(CUSTOMER, null);
    Assert.assertNotNull(entitiesResponse);
    Assert.assertEquals(CUSTOMER, entitiesResponse.getEntitySet().getName());
    Assert.assertEquals(2, entitiesResponse.getEntities().size());

    Asserts.assertThrows(NotFoundException.class, getEntities(producer, "badEntitySet", null));

    filter = Expression.eq(Expression.simpleProperty(CUSTOMER_ID), Expression.literal(1));
    entitiesResponse = producer.getEntities(CUSTOMER, QueryInfo.newBuilder().setFilter(filter).build());
    Assert.assertNotNull(entitiesResponse);
    Assert.assertEquals(CUSTOMER, entitiesResponse.getEntitySet().getName());
    Assert.assertEquals(1, entitiesResponse.getEntities().size());
    Assert.assertEquals("Customer One", entitiesResponse.getEntities().get(0).getProperty(CUSTOMER_NAME).getValue());

    filter = Expression.eq(Expression.literal(1), Expression.simpleProperty(CUSTOMER_ID));
    entitiesResponse = producer.getEntities(CUSTOMER, QueryInfo.newBuilder().setFilter(filter).build());
    Assert.assertNotNull(entitiesResponse);
    Assert.assertEquals(CUSTOMER, entitiesResponse.getEntitySet().getName());
    Assert.assertEquals(1, entitiesResponse.getEntities().size());
    Assert.assertEquals("Customer One", entitiesResponse.getEntities().get(0).getProperty(CUSTOMER_NAME).getValue());

    filter = Expression.eq(Expression.simpleProperty(CUSTOMER_ID), Expression.literal(-1));
    entitiesResponse = producer.getEntities(CUSTOMER, QueryInfo.newBuilder().setFilter(filter).build());
    Assert.assertNotNull(entitiesResponse);
    Assert.assertEquals(CUSTOMER, entitiesResponse.getEntitySet().getName());
    Assert.assertEquals(0, entitiesResponse.getEntities().size());

    producer.close();
  }

  private static Func<EntitiesResponse> getEntities(final ODataProducer producer, final String entitySet, final QueryInfo queryInfo) {
    return new Func<EntitiesResponse>() {
      @Override
      public EntitiesResponse apply() {
        return producer.getEntities(entitySet, queryInfo);
      }};
  }

  private static Func<EntityResponse> getEntity(final ODataProducer producer, final String entitySet, final OEntityKey key, final EntityQueryInfo queryInfo) {
    return new Func<EntityResponse>() {
      @Override
      public EntityResponse apply() {
        return producer.getEntity(entitySet, key, queryInfo);
      }};
  }

}
