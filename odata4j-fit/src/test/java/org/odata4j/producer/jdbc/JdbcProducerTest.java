package org.odata4j.producer.jdbc;

import junit.framework.Assert;

import org.junit.Test;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.QueryInfo;

public class JdbcProducerTest {

  @Test
  public void jdbcProducer() {

    JdbcTest.populateExample();

    JdbcProducer producer = JdbcProducer.newBuilder()
        .jdbc(JdbcTest.HSQL_DB)
        .build();

    EdmDataServices metadata = producer.getMetadata();
    Assert.assertNotNull(metadata);
    JdbcTest.dump(metadata);

    String entitySetName = "CUSTOMER";
    QueryInfo queryInfo = null;
    EntitiesResponse response = producer.getEntities(entitySetName, queryInfo);
    Assert.assertNotNull(response);
    Assert.assertEquals(entitySetName, response.getEntitySet().getName());
    Assert.assertEquals(1, response.getEntities().size());

    producer.close();
  }

}
