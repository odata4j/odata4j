package org.odata4j.producer.jpa.oneoff07;

import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperties;
import org.odata4j.fit.support.ConsumerSupport;
import org.odata4j.producer.jpa.oneoff.AbstractOneoffTestBase;

public abstract class AbstractOneoff07GuidPrimaryKey extends AbstractOneoffTestBase implements ConsumerSupport {

  @Test
  public void guidPrimaryKey() {
    ODataConsumer c = this.create(endpointUri, null, null);
    Assert.assertEquals(0, c.getEntities("CommunicationCellCarrier").execute().count());
    String id = UUID.randomUUID().toString();
    c.createEntity("CommunicationCellCarrier")
        .properties(
            OProperties.guid("id", id),
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
