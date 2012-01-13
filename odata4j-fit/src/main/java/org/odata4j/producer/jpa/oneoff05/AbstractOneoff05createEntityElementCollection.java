package org.odata4j.producer.jpa.oneoff05;

import org.junit.Ignore;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OProperties;
import org.odata4j.fit.support.ConsumerSupport;
import org.odata4j.producer.jpa.oneoff.AbstractOneoffTestBase;

public abstract class AbstractOneoff05createEntityElementCollection extends AbstractOneoffTestBase implements ConsumerSupport {

  @Ignore
  @Test
  public void createEntityElementCollection() {
    final long now = System.currentTimeMillis();
    ODataConsumer consumer = this.create(endpointUri, null, null);

    consumer
        .createEntity("Student")
        .properties(OProperties.string("StudentName", "Student1" + now))
        //todo add courses as well to the student
        .execute();
  }
}
