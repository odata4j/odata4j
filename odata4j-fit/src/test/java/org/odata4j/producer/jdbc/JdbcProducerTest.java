package org.odata4j.producer.jdbc;

import junit.framework.Assert;

import org.junit.Test;
import org.odata4j.core.OEntityKey;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.QueryInfo;
import org.odata4j.producer.command.ProducerCommandContext;

public class JdbcProducerTest {

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

    String customerEntityset = "customer";
    QueryInfo queryInfo = null;
    EntitiesResponse response = producer.getEntities(customerEntityset, queryInfo);
    Assert.assertNotNull(response);
    Assert.assertEquals(customerEntityset, response.getEntitySet().getName());
    Assert.assertEquals(1, response.getEntities().size());

    EntityResponse entityResponse = producer.getEntity(customerEntityset, OEntityKey.create(1), null);
    Assert.assertNotNull(entityResponse);
    Assert.assertNotNull(entityResponse.getEntity());
    Assert.assertEquals("Customer One", entityResponse.getEntity().getProperty("customer_name").getValue());

    producer.close();
  }

}
