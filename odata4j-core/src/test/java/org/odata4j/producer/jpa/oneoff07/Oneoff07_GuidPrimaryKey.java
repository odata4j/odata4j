package org.odata4j.producer.jpa.oneoff07;

import java.util.UUID;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperties;
import org.odata4j.producer.jpa.oneoff.OneoffTestBase;

public class Oneoff07_GuidPrimaryKey extends OneoffTestBase {
  @BeforeClass
  public static void setUpClass() throws Exception {
    setUpClass(Oneoff07_GuidPrimaryKey.class, 20);
  }

  @Test
  public void guidPrimaryKey() {
    ODataConsumer c = ODataConsumer.create(endpointUri);
    Assert.assertEquals(0, c.getEntities("CommunicationCellCarrier").execute().count());
    String id = UUID.randomUUID().toString();
    c.createEntity("CommunicationCellCarrier")
        .properties(
            OProperties.guid("id", id ),
            OProperties.string("name", "TMobile"))
        .execute();
    Assert.assertEquals(1, c.getEntities("CommunicationCellCarrier").execute().count());
    OEntity firstEntity = c.getEntity("CommunicationCellCarrier", id).execute();
    Assert.assertEquals(id, firstEntity.getProperty("id").getValue().toString());
    Assert.assertEquals("TMobile", firstEntity.getProperty("name").getValue());
    c.deleteEntity(firstEntity).execute();
    Assert.assertEquals(0, c.getEntities("CommunicationCellCarrier").execute().count());
  }

}
